package io.lionweb.gradleplugin.generators;

import static io.lionweb.gradleplugin.generators.NamingUtils.*;

import com.palantir.javapoet.*;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.HasSettableParent;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.AbstractNode;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The NodeClassesJavaCodeGenerator class is responsible for generating Java code for node classes
 * based on provided models, languages, and configurations.
 */
public class NodeClassesJavaCodeGenerator extends AbstractJavaCodeGenerator {
  /**
   * Constructs a NodeClassesJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public NodeClassesJavaCodeGenerator(
      @NotNull File destinationDir, @NotNull Map<String, String> mappings) {
    super(destinationDir, mappings);
  }

  public void generate(@Nonnull Language language, @Nonnull String packageName) {
    generate(
        language,
        new GenerationContext(
            language, packageName, Collections.emptyMap(), Collections.emptyMap(), mappings));
  }

  public void generate(
      @Nonnull Collection<Language> languages, @Nullable String defaultPackageName) {
    generate(
        languages,
        defaultPackageName,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap());
  }

  public void generate(
      @Nonnull Collection<Language> languages,
      @Nullable String defaultPackageName,
      @Nonnull Map<String, String> specificPackages,
      @Nonnull Map<String, String> primitiveTypes,
      @Nonnull Map<String, String> languageClassNames) {
    Objects.requireNonNull(languages, "languages should not be null");
    Objects.requireNonNull(specificPackages, "specificPackages should not be null");
    if (languages.isEmpty()) {
      return;
    }
    Set<GenerationContext.LanguageGenerationConfiguration> languageConfs = new HashSet<>();
    for (Language language : languages) {
      String specificPackage = specificPackages.get(language.getID());
      if (specificPackage != null) {
        languageConfs.add(
            new GenerationContext.LanguageGenerationConfiguration(language, specificPackage));
      } else if (defaultPackageName != null) {
        languageConfs.add(
            new GenerationContext.LanguageGenerationConfiguration(language, defaultPackageName));
      } else {
        throw new IllegalArgumentException(
            "No default package name and no specific package name for language "
                + language.getID());
      }
    }
    GenerationContext languageContext =
        new GenerationContext(languageConfs, primitiveTypes, mappings);
    languages.forEach(
        language -> {
          generate(language, languageContext);
        });
  }

  private void generate(@Nonnull Language language, @Nonnull GenerationContext generationContext) {
    Objects.requireNonNull(language, "language should not be null");
    Objects.requireNonNull(generationContext, "languageContext should not be null");
    language.getConcepts().forEach(concept -> generateConcept(concept, generationContext));
    language.getInterfaces().forEach(interf -> generateInterface(interf, generationContext));
    language
        .getStructuredDataTypes()
        .forEach(
            sdt -> {
              throw new UnsupportedOperationException();
            });
    language
        .getEnumerations()
        .forEach(enumeration -> generateEnumeration(enumeration, generationContext));
  }

  private void generateEnumeration(
      @Nonnull Enumeration enumeration, @Nonnull GenerationContext generationContext) {
    String className = generationContext.getGeneratedName(enumeration);

    TypeSpec.Builder enumClass = TypeSpec.enumBuilder(className).addModifiers(Modifier.PUBLIC);

    enumeration.getLiterals().forEach(literal -> enumClass.addEnumConstant(literal.getName()));

    String packageName = generationContext.generationPackage(enumeration.getLanguage());
    JavaFile javaFile = JavaFile.builder(packageName, enumClass.build()).build();
    try {
      javaFile.writeTo(destinationDir.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void generateConcept(
      @Nonnull Concept concept, @Nonnull GenerationContext generationContext) {
    TypeSpec.Builder conceptClass;
    try {
      String className = generationContext.getGeneratedName(concept);

      TypeName classifierInstanceOfUnknown =
          ParameterizedTypeName.get(
              ClassName.get(ClassifierInstance.class),
              WildcardTypeName.subtypeOf(Object.class) // "?"
              );

      conceptClass = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);

      if (concept.getExtendedConcept() == null) {
        conceptClass
            .superclass(ClassName.get(AbstractNode.class))
            .addSuperinterface(ClassName.get(HasSettableParent.class));
      } else {
        throw new UnsupportedOperationException("Extended concepts are not yet implemented");
      }
      concept
          .getImplemented()
          .forEach(ii -> conceptClass.addSuperinterface(generationContext.getInterfaceType(ii)));
      if (concept.isAbstract()) {
        conceptClass.addModifiers(Modifier.ABSTRACT);
      }

      conceptClass.addField(
          FieldSpec.builder(ClassName.get(String.class), "id")
              .addAnnotation(NotNull.class)
              .addModifiers(Modifier.PRIVATE)
              .build());
      conceptClass.addField(
          FieldSpec.builder(
                  ParameterizedTypeName.get(
                      ClassName.get(ClassifierInstance.class),
                      WildcardTypeName.subtypeOf(Object.class)),
                  "parent")
              .addAnnotation(Nullable.class)
              .addModifiers(Modifier.PRIVATE)
              .build());
      conceptClass.addMethod(
          MethodSpec.constructorBuilder()
              .addParameter(
                  ParameterSpec.builder(ClassName.get(String.class), "id")
                      .addAnnotation(NotNull.class)
                      .build())
              .addStatement("$T.requireNonNull(id, $S)", Objects.class, "id must not be null")
              .addStatement("this.id = id")
              .addModifiers(Modifier.PUBLIC)
              .build());
      conceptClass.addMethod(
          MethodSpec.methodBuilder("getID")
              .returns(TypeName.get(String.class))
              .addAnnotation(NotNull.class)
              .addModifiers(Modifier.PUBLIC)
              .addStatement("return this.id")
              .build());
      conceptClass.addMethod(
          MethodSpec.methodBuilder("getParent")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(classifierInstanceOfUnknown)
              .addStatement("return this.parent")
              .build());
      conceptClass.addMethod(
          MethodSpec.methodBuilder("setParent")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(ClassName.get(ClassifierInstance.class))
              .addParameter(
                  ParameterSpec.builder(classifierInstanceOfUnknown, "parent")
                      .addAnnotation(ClassName.get(Nullable.class))
                      .build())
              .addStatement("this.parent = parent")
              .addStatement("return this")
              .build());
      conceptClass.addMethod(
          MethodSpec.methodBuilder("getClassifier")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(ClassName.get(Concept.class))
              .addStatement(
                  "return $L.$L()",
                  generationContext.resolveLanguage(concept.getLanguage(), null),
                  "get" + generationContext.getGeneratedName(concept, false))
              .build());
      MethodSpec.Builder getPropertyValue =
          MethodSpec.methodBuilder("getPropertyValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(Object.class)
              .addParameter(ClassName.get(Property.class), "property");

      MethodSpec.Builder setPropertyValue =
          MethodSpec.methodBuilder("setPropertyValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(ClassName.get(Property.class), "property")
              .addParameter(ClassName.get(Object.class), "value")
              .addStatement("Objects.requireNonNull(property, \"Property should not be null\");")
              .addStatement(
                  "Objects.requireNonNull(property.getKey(), \"Cannot assign a property with no Key specified\");");
      MethodSpec.Builder getChildren =
          MethodSpec.methodBuilder("getChildren")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(
                  ParameterizedTypeName.get(
                      ClassName.get(List.class), WildcardTypeName.subtypeOf(Node.class)))
              .addParameter(Containment.class, "containment");
      MethodSpec.Builder addChild1 =
          MethodSpec.methodBuilder("addChild")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(
                  ParameterSpec.builder(Containment.class, "containment")
                      .addAnnotation(NotNull.class)
                      .build())
              .addParameter(
                  ParameterSpec.builder(Node.class, "child").addAnnotation(NotNull.class).build())
              .addStatement(
                  "$T.requireNonNull(containment, $S)",
                  Objects.class,
                  "Containment should not be null")
              .addStatement(
                  "$T.requireNonNull(child, $S)", Objects.class, "Child should not be null");

      List<Feature<?>> features = new LinkedList<>();
      features.addAll(concept.getFeatures());
      concept.getImplemented().forEach(i -> i.getFeatures().forEach(features::add));

      features.forEach(
          feature -> {
            if (feature instanceof Property) {
              considerConceptProperty(
                  (Property) feature,
                  generationContext,
                  conceptClass,
                  getPropertyValue,
                  setPropertyValue);
            } else if (feature instanceof Containment) {
              considerConceptContainment(
                  (Containment) feature, generationContext, conceptClass, getChildren, addChild1);
            } else if (feature instanceof Reference) {
              considerConceptReference((Reference) feature, generationContext, conceptClass);
            } else {
              throw new IllegalStateException("Unknown feature type: " + feature.getClass());
            }
          });
      conceptClass.addMethod(
          getPropertyValue
              .addStatement(
                  "throw new $T($S + property + $S)",
                  IllegalStateException.class,
                  "Property ",
                  " not found.")
              .build());
      conceptClass.addMethod(
          setPropertyValue
              .addStatement(
                  "throw new $T($S + property + $S)",
                  IllegalStateException.class,
                  "Property ",
                  " not found.")
              .build());
      conceptClass.addMethod(
          getChildren
              .addStatement(
                  "throw new $T($S + containment + $S)",
                  IllegalStateException.class,
                  "Containment ",
                  " not found.")
              .build());
      conceptClass.addMethod(
          addChild1
              .addStatement(
                  "throw new $T($S + containment + $S)",
                  IllegalStateException.class,
                  "Containment ",
                  " not found.")
              .build());

      ClassName CONTAINMENT = ClassName.get(Containment.class);
      ClassName NODE = ClassName.get(Node.class);
      ClassName REFERENCE = ClassName.get(Reference.class);
      ClassName REFERENCE_VALUE = ClassName.get(ReferenceValue.class);

      // List<? extends ReferenceValue>
      TypeName LIST_OF_WILDCARD_REF_VALUE =
          ParameterizedTypeName.get(
              ClassName.get(List.class), WildcardTypeName.subtypeOf(REFERENCE_VALUE));

      // Common body for all methods
      CodeBlock unsupportedOpBody =
          CodeBlock.builder()
              .addStatement(
                  "throw new $T($S)", UnsupportedOperationException.class, "Not supported yet.")
              .build();

      // @Override
      // public void addChild(Containment containment, Node child, int index) { ... }
      MethodSpec addChild2 =
          MethodSpec.methodBuilder("addChild")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(CONTAINMENT, "containment")
              .addParameter(NODE, "child")
              .addParameter(int.class, "index")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(addChild2);

      // @Override
      // public List<ReferenceValue> getReferenceValues(Reference reference) { ... }
      MethodSpec getReferenceValues =
          MethodSpec.methodBuilder("getReferenceValues")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(ParameterizedTypeName.get(ClassName.get(List.class), REFERENCE_VALUE))
              .addParameter(REFERENCE, "reference")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(getReferenceValues);

      // @Override
      // public int addReferenceValue(Reference reference, ReferenceValue referredNode) { ... }
      MethodSpec addReferenceValue1 =
          MethodSpec.methodBuilder("addReferenceValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(int.class)
              .addParameter(REFERENCE, "reference")
              .addParameter(REFERENCE_VALUE, "referredNode")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(addReferenceValue1);

      // @Override
      // public int addReferenceValue(Reference reference, int index, ReferenceValue referredNode) {
      // ... }
      MethodSpec addReferenceValue2 =
          MethodSpec.methodBuilder("addReferenceValue")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(int.class)
              .addParameter(REFERENCE, "reference")
              .addParameter(int.class, "index")
              .addParameter(REFERENCE_VALUE, "referredNode")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(addReferenceValue2);

      // @Override
      // public void setReferenceValues(Reference reference, List<? extends ReferenceValue> values)
      // {
      // ... }
      MethodSpec setReferenceValues =
          MethodSpec.methodBuilder("setReferenceValues")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(REFERENCE, "reference")
              .addParameter(LIST_OF_WILDCARD_REF_VALUE, "values")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(setReferenceValues);

      // @Override
      // public void setReferred(Reference reference, int index, Node referredNode) { ... }
      MethodSpec setReferred =
          MethodSpec.methodBuilder("setReferred")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(REFERENCE, "reference")
              .addParameter(int.class, "index")
              .addParameter(NODE, "referredNode")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(setReferred);

      // @Override
      // public void setResolveInfo(Reference reference, int index, String resolveInfo) { ... }
      MethodSpec setResolveInfo =
          MethodSpec.methodBuilder("setResolveInfo")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(void.class)
              .addParameter(REFERENCE, "reference")
              .addParameter(int.class, "index")
              .addParameter(String.class, "resolveInfo")
              .addCode(unsupportedOpBody)
              .build();
      conceptClass.addMethod(setResolveInfo);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to generate class for concept " + concept.qualifiedName(), e);
    }

    String packageName = generationContext.generationPackage(concept.getLanguage());
    JavaFile javaFile = JavaFile.builder(packageName, conceptClass.build()).build();
    try {
      javaFile.writeTo(destinationDir.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void considerConceptProperty(
      @Nonnull Property property,
      @NotNull GenerationContext generationContext,
      TypeSpec.Builder conceptClass,
      MethodSpec.Builder getPropertyValue,
      MethodSpec.Builder setPropertyValue) {
    String fieldName = camelCase(property.getName());
    String getterName = "get" + pascalCase(property.getName());
    String setterName = "set" + pascalCase(property.getName());
    TypeName fieldType = generationContext.typeFor(property.getType());
    conceptClass.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build());
    getPropertyValue
        .beginControlFlow(
            "if ($T.equals(property.getKey(), $S))",
            ClassName.get(Objects.class),
            property.getKey())
        .addStatement("return $L", fieldName)
        .endControlFlow();
    setPropertyValue
        .beginControlFlow(
            "if ($T.equals(property.getKey(), $S))",
            ClassName.get(Objects.class),
            property.getKey())
        .addStatement("$L(($T) value)", setterName, fieldType)
        .addStatement("return")
        .endControlFlow();
    MethodSpec getter =
        MethodSpec.methodBuilder(getterName)
            .returns(generationContext.typeFor(property.getType()))
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return $L", camelCase(property.getName()))
            .build();
    conceptClass.addMethod(getter);
    MethodSpec setter =
        MethodSpec.methodBuilder(setterName)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                ParameterSpec.builder(generationContext.typeFor(property.getType()), "value")
                    .build())
            .addCode(
                "if (partitionObserverCache != null) {\n"
                    + "      partitionObserverCache.propertyChanged(\n"
                    + "          this, this.getClassifier().requirePropertyByName($S), $L(), value);\n"
                    + "    }\n",
                property.getName(),
                getterName)
            .addStatement("this.$L = value", fieldName)
            .build();
    conceptClass.addMethod(setter);
  }

  private static void considerConceptContainment(
      @Nonnull Containment containment,
      @NotNull GenerationContext generationContext,
      TypeSpec.Builder conceptClass,
      MethodSpec.Builder getChildren,
      MethodSpec.Builder addChild1) {
    String fieldName = camelCase(containment.getName());
    TypeName baseFieldType = generationContext.typeFor(containment.getType());
    TypeName fieldType = baseFieldType;
    if (containment.isMultiple()) {
      fieldType = ParameterizedTypeName.get(ClassName.get(List.class), baseFieldType);
    }
    conceptClass.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build());
    if (containment.isMultiple()) {
      getChildren
          .beginControlFlow(
              "if ($T.equals(containment.getKey(), $S))",
              ClassName.get(Objects.class),
              containment.getKey())
          .addStatement("return $L", fieldName)
          .endControlFlow();
    } else {
      getChildren
          .beginControlFlow(
              "if ($T.equals(containment.getKey(), $S))",
              ClassName.get(Objects.class),
              containment.getKey())
          .beginControlFlow("if ($L == null)", fieldName)
          .addStatement("return $T.emptyList()", ClassName.get(Collections.class))
          .nextControlFlow("else")
          .addStatement("return $T.singletonList($L)", ClassName.get(Collections.class), fieldName)
          .endControlFlow()
          .endControlFlow();
    }

    if (containment.isMultiple()) {
      addChild1.addCode(
          CodeBlock.builder()
              .beginControlFlow("if (containment.getKey().equals($S))", containment.getKey())
              .beginControlFlow("if ($N instanceof $T)", "child", HasSettableParent.class)
              .addStatement("(($T) $N).setParent(this)", HasSettableParent.class, "child")
              .endControlFlow()
              .addStatement("$L.add(($T)$N)", fieldName, baseFieldType, "child")
              .beginControlFlow("if ($N != null)", "partitionObserverCache")
              .addStatement(
                  "$N.childAdded(this, this.getClassifier().requireContainmentByName($S), $L.size() - 1, $N)",
                  "partitionObserverCache",
                  containment.getName(),
                  fieldName,
                  "child")
              .endControlFlow()
              .addStatement("return")
              .endControlFlow()
              .build());
    } else {
      addChild1
          .addStatement("$T removed = null", Node.class)
          .beginControlFlow("if ($N != null)", fieldName)
          .addStatement("this.removeChild($N)", fieldName)
          .addStatement(
              "partitionObserverCache.childRemoved(this, this.getClassifier().requireContainmentByName($S), 0, child)",
              containment.getName())
          .endControlFlow()
          .beginControlFlow("if ($N instanceof $T)", "child", HasSettableParent.class)
          .addStatement("(($T) $N).setParent(this)", HasSettableParent.class, "child")
          .endControlFlow()
          .addStatement("this.$N = ($T) $N", fieldName, fieldType, "child")
          .beginControlFlow("if (partitionObserverCache != null)")
          .addStatement(
              "partitionObserverCache.childAdded(this, this.getClassifier().requireContainmentByName($S), 0, $N)",
              containment.getName(),
              "child")
          .addStatement("return")
          .endControlFlow();
    }
  }

  private static void considerConceptReference(
      @Nonnull Reference reference,
      @NotNull GenerationContext generationContext,
      TypeSpec.Builder conceptClass) {
    String fieldName = camelCase(reference.getName());
    String getterName = "get" + pascalCase(reference.getName());
    String setterName = "set" + pascalCase(reference.getName());
    TypeName baseFieldType = ClassName.get(ReferenceValue.class);
    TypeName fieldType = baseFieldType;
    if (reference.isMultiple()) {
      fieldType = ParameterizedTypeName.get(ClassName.get(List.class), baseFieldType);
    }
    conceptClass.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build());
    if (reference.isMultiple()) {
      String adderName = "addTo" + pascalCase(reference.getName());
      MethodSpec.Builder adder =
          MethodSpec.methodBuilder(adderName)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(TypeName.get(ReferenceValue.class), "referenceValue")
              .addParameter(TypeName.INT, "index")
              .returns(TypeName.INT);
      adder.addCode(
          CodeBlock.builder()
              .beginControlFlow("if (index > $L.size())", fieldName)
              .addStatement(
                  "throw new IllegalArgumentException($S)",
                  "Index must be less than or equal to size")
              .endControlFlow()
              .beginControlFlow("if (partitionObserverCache != null)")
              .addStatement(
                  "partitionObserverCache.referenceValueAdded(this, this.getClassifier().requireReferenceByName($S), index, referenceValue)",
                  reference.getName())
              .endControlFlow()
              .addStatement("$L.add(index, referenceValue)", fieldName)
              .addStatement("return $L.size() - 1", fieldName)
              .build());
      conceptClass.addMethod(adder.build());
      MethodSpec getter =
          MethodSpec.methodBuilder(getterName)
              .returns(ParameterizedTypeName.get(ClassName.get(List.class), baseFieldType))
              .addModifiers(Modifier.PUBLIC)
              .addStatement("return $L", fieldName)
              .build();
      conceptClass.addMethod(getter);
    } else {
      MethodSpec.Builder setter =
          MethodSpec.methodBuilder(setterName)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(TypeName.get(ReferenceValue.class), "value")
              .returns(TypeName.VOID);
      setter.addCode(
          CodeBlock.builder()
              .beginControlFlow("if ($N == null)", "value")
              .beginControlFlow("if ($N != null)", "partitionObserverCache")
              .addStatement(
                  "$N.referenceValueRemoved(this, this.getClassifier().requireReferenceByName($S), $L, $N)",
                  "partitionObserverCache",
                  reference.getName(),
                  0,
                  fieldName)
              .endControlFlow()
              .addStatement("$N = null", fieldName)
              .nextControlFlow("else")
              .beginControlFlow("if ($N != null)", "partitionObserverCache")
              .beginControlFlow("if ($N != null)", fieldName)
              .addStatement("$T oldValue = $N", ReferenceValue.class, fieldName)
              .addStatement(
                  "$N.referenceValueChanged("
                      + "this, "
                      + "this.getClassifier().requireReferenceByName($S), 0, "
                      + "oldValue.getReferredID(), "
                      + "oldValue.getResolveInfo(), "
                      + "$N.getReferredID(), "
                      + "$N.getResolveInfo()"
                      + ")",
                  "partitionObserverCache",
                  reference.getName(),
                  "value",
                  "value")
              .nextControlFlow("else")
              .addStatement(
                  "$N.referenceValueAdded(this, this.getClassifier().requireReferenceByName($S), $L, $N)",
                  "partitionObserverCache",
                  reference.getName(),
                  0,
                  "value")
              .endControlFlow()
              .endControlFlow()
              .addStatement("this.$N = $N", fieldName, "value")
              .endControlFlow()
              .build());
      conceptClass.addMethod(setter.build());
      MethodSpec getter =
          MethodSpec.methodBuilder(getterName)
              .returns(baseFieldType)
              .addModifiers(Modifier.PUBLIC)
              .addStatement("return $L", fieldName)
              .build();
      conceptClass.addMethod(getter);
    }
  }

  private void generateInterface(
      @Nonnull Interface interf, @Nonnull GenerationContext generationContext) {
    String interfName = generationContext.getGeneratedName(interf);
    TypeSpec.Builder interfClass =
        TypeSpec.interfaceBuilder(interfName)
            .addSuperinterface(ClassName.get(Node.class))
            .addModifiers(Modifier.PUBLIC);
    interf
        .getExtendedInterfaces()
        .forEach(ii -> interfClass.addSuperinterface(generationContext.getInterfaceType(ii)));

    interf
        .getFeatures()
        .forEach(
            feature -> {
              if (feature instanceof Property) {
                // Getter
                interfClass.addMethod(
                    MethodSpec.methodBuilder("get" + capitalize(feature.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(generationContext.typeFor(((Property) feature).getType()))
                        .build());
                // Setter
                interfClass.addMethod(
                    MethodSpec.methodBuilder("set" + capitalize(feature.getName()))
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(
                            ParameterSpec.builder(
                                    generationContext.typeFor(((Property) feature).getType()),
                                    "value")
                                .build())
                        .build());
              } else if (feature instanceof Containment) {
                throw new UnsupportedOperationException("Containment not yet supported");
              } else if (feature instanceof Reference) {
                Reference reference = (Reference) feature;
                if (reference.isMultiple()) {
                  String adderName = "addTo" + pascalCase(reference.getName());
                  MethodSpec.Builder adder =
                      MethodSpec.methodBuilder(adderName)
                          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                          .addParameter(TypeName.get(ReferenceValue.class), "referenceValue")
                          .addParameter(TypeName.INT, "index")
                          .returns(TypeName.INT);
                  interfClass.addMethod(adder.build());
                } else {
                  MethodSpec.Builder setter =
                      MethodSpec.methodBuilder("set" + NamingUtils.capitalize(reference.getName()))
                          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                          .addParameter(TypeName.get(ReferenceValue.class), "value")
                          .returns(TypeName.VOID);
                  interfClass.addMethod(setter.build());
                  MethodSpec getter =
                      MethodSpec.methodBuilder("get" + NamingUtils.capitalize(reference.getName()))
                          .returns(ClassName.get(ReferenceValue.class))
                          .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                          .build();
                  interfClass.addMethod(getter);
                }
              } else {
                throw new IllegalStateException("Unknown feature type: " + feature.getClass());
              }
            });

    String packageName = generationContext.generationPackage(interf.getLanguage());
    JavaFile javaFile = JavaFile.builder(packageName, interfClass.build()).build();
    try {
      javaFile.writeTo(destinationDir.toPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
