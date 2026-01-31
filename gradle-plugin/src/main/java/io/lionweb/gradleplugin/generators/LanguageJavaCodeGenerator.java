package io.lionweb.gradleplugin.generators;

import static io.lionweb.gradleplugin.generators.CommonClassNames.*;
import static io.lionweb.gradleplugin.generators.CommonClassNames.enumerationLiteralClass;
import static io.lionweb.gradleplugin.generators.NamingUtils.capitalize;
import static io.lionweb.gradleplugin.generators.NamingUtils.toLanguageClassName;

import com.palantir.javapoet.*;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;

/**
 * The LanguageJavaCodeGenerator class is responsible for generating Java code representations of
 * language definitions and their associated components.
 */
public class LanguageJavaCodeGenerator extends AbstractJavaCodeGenerator {

  /**
   * Constructs a LanguageJavaCodeGenerator with the specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public LanguageJavaCodeGenerator(@Nonnull File destinationDir) {
    super(destinationDir, Collections.emptyMap());
  }

  /**
   * Constructs a LanguageJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public LanguageJavaCodeGenerator(
      @Nonnull File destinationDir, @Nonnull Map<String, String> mappings) {
    super(destinationDir, mappings);
  }

  /**
   * Generates code for the specified languages and package name.
   *
   * @param languages a collection of languages for which the code will be generated; must not be
   *     null
   * @param defaultPackageName the base package name under which the code will be generated; must
   *     not be null
   */
  public void generate(
      @Nonnull Collection<Language> languages,
      @Nullable String defaultPackageName,
      @Nonnull Map<String, String> specificPackages,
      @Nonnull Map<String, String> languageClassNames) {
    Objects.requireNonNull(languages, "languages should not be null");
    if (languages.isEmpty()) {
      return;
    }
    Set<GenerationContext.LanguageGenerationConfiguration> languageConfs = new HashSet<>();
    for (Language language : languages) {
      String packag = specificPackages.get(language.getID());
      if (packag == null) {
        if (defaultPackageName == null) {
          throw new IllegalArgumentException(
              "No default package name and no specific package name for language "
                  + language.getID());
        }

        packag = defaultPackageName;
      }
      String className = languageClassNames.get(language.getID());
      languageConfs.add(
          new GenerationContext.LanguageGenerationConfiguration(language, packag, className));
    }
    GenerationContext languageContext = new GenerationContext(languageConfs);
    languages.forEach(
        language -> {
          try {
            generate(language, languageContext);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  public void generate(@Nonnull Language language, @Nonnull String packageName) throws IOException {
    GenerationContext generationContext =
        new GenerationContext(
            new HashSet<>(
                Collections.singleton(
                    new GenerationContext.LanguageGenerationConfiguration(language, packageName))));
    generate(language, generationContext);
  }

  public void generate(@Nonnull Collection<Language> languages, @Nonnull String packageName) {
    Set<GenerationContext.LanguageGenerationConfiguration> languageConfs = new HashSet<>();
    languages.forEach(
        language ->
            languageConfs.add(
                new GenerationContext.LanguageGenerationConfiguration(language, packageName)));

    languages.forEach(
        language -> {
          try {
            generate(language, new GenerationContext(languageConfs));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Generates Java code files for a specified language and package name.
   *
   * @param language the language for which the code will be generated; must not be null
   * @throws IOException if an I/O error occurs during code generation
   */
  public void generate(@Nonnull Language language, @Nonnull GenerationContext generationContext)
      throws IOException {
    Objects.requireNonNull(language, "language should not be null");
    Objects.requireNonNull(generationContext, "generationContext should not be null");
    String className = toLanguageClassName(language, generationContext);

    ClassName lwLanguageClass = ClassName.get(Language.class);

    FieldSpec instanceField =
        FieldSpec.builder(
                ClassName.get(generationContext.generationPackage(language), className),
                "INSTANCE",
                Modifier.PRIVATE,
                Modifier.STATIC)
            .build();

    MethodSpec.Builder constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addStatement("super($T.$L)", lionWebVersion, language.getLionWebVersion().name())
            .addStatement("this.setName($S)", language.getName())
            .addStatement("this.setVersion($S)", language.getVersion())
            .addStatement("this.setID($S)", language.getID())
            .addStatement("this.setKey($S)", language.getKey());

    language
        .dependsOn()
        .forEach(
            dependency -> {
              constructor.addStatement(
                  "this.addDependency($L)",
                  generationContext.resolveLanguage(dependency, language));
            });

    MethodSpec getInstance =
        MethodSpec.methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(generationContext.generationPackage(language), className))
            .beginControlFlow("if ($N == null)", instanceField)
            .addStatement(
                "$N = new $T()",
                instanceField,
                ClassName.get(generationContext.generationPackage(language), className))
            .endControlFlow()
            .addStatement("return $N", instanceField)
            .build();

    MethodSpec.Builder createElements =
        MethodSpec.methodBuilder("createElements").addModifiers(Modifier.PRIVATE);
    constructor.addStatement("createElements()");

    TypeSpec.Builder languageClass =
        TypeSpec.classBuilder(className)
            .superclass(lwLanguageClass)
            .addField(instanceField)
            .addMethod(getInstance)
            .addModifiers(Modifier.PUBLIC);

    language
        .getConcepts()
        .forEach(
            concept -> {
              //    EXAMPLE:
              //    public Concept getLibrary() {
              //        return this.requireConceptByName("Library");
              //    }
              MethodSpec conceptAccessor =
                  MethodSpec.methodBuilder(getterName(concept.getName()))
                      .returns(ClassName.get(Concept.class))
                      .addModifiers(Modifier.PUBLIC)
                      .addStatement("return this.requireConceptByName($S)", concept.getName())
                      .build();
              languageClass.addMethod(conceptAccessor);

              //    EXAMPLE:
              // private void initLibrary() {
              //        Concept libraryConcept = new Concept("Library");
              //        libraryConcept.setID("Library-id");
              //        libraryConcept.setName("Library");
              //        libraryConcept.setKey("Library");
              //        libraryConcept.setAbstract(false);
              //        libraryConcept.setPartition(false);
              //        this.addElement(libraryConcept);
              //    }
              MethodSpec.Builder initMethod =
                  MethodSpec.methodBuilder("init" + capitalize(concept.getName()))
                      .addModifiers(Modifier.PRIVATE)
                      .returns(void.class)
                      .addStatement(
                          "$T concept = this.requireConceptByName($S)",
                          conceptClass,
                          concept.getName())
                      .addStatement("concept.setAbstract($L)", concept.isAbstract())
                      .addStatement("concept.setPartition($L)", concept.isPartition());
              if (concept.getExtendedConcept() != null) {
                initMethod.addStatement(
                    "concept.setExtendedConcept($L)",
                    toConceptExpr(concept.getExtendedConcept(), generationContext, language));
              }
              concept
                  .getImplemented()
                  .forEach(
                      implemented -> {
                        initMethod.addStatement(
                            "concept.addImplementedInterface($L)",
                            toInterfaceExpr(implemented, generationContext, language));
                      });
              concept
                  .getFeatures()
                  .forEach(
                      feature ->
                          initFeature(initMethod, feature, "concept", generationContext, language));
              languageClass.addMethod(initMethod.build());

              constructor.addStatement("init$L()", capitalize(concept.getName()));

              createElements.addStatement(
                  "new $T(this, $S, $S, $S);",
                  conceptClass,
                  concept.getName(),
                  concept.getID(),
                  concept.getKey());
            });

    language
        .getInterfaces()
        .forEach(
            interf -> {
              MethodSpec getter =
                  MethodSpec.methodBuilder(getterName(interf.getName()))
                      .returns(interfaceClass)
                      .addModifiers(Modifier.PUBLIC)
                      .addStatement("return this.requireInterfaceByName($S)", interf.getName())
                      .build();
              languageClass.addMethod(getter);

              MethodSpec.Builder initMethod =
                  MethodSpec.methodBuilder("init" + capitalize(interf.getName()))
                      .addModifiers(Modifier.PRIVATE)
                      .returns(void.class)
                      .addStatement(
                          "$T interf = this.requireInterfaceByName($S)",
                          interfaceClass,
                          interf.getName());
              interf
                  .getExtendedInterfaces()
                  .forEach(
                      implemented -> {
                        initMethod.addStatement(
                            "interf.addExtendedInterface($L)",
                            toInterfaceExpr(implemented, generationContext, language));
                      });
              interf
                  .getFeatures()
                  .forEach(
                      feature -> {
                        initFeature(initMethod, feature, "interf", generationContext, language);
                      });
              languageClass.addMethod(initMethod.build());

              constructor.addStatement("init$L()", capitalize(interf.getName()));

              createElements.addStatement(
                  "new $T(this, $S, $S, $S);",
                  interfaceClass,
                  interf.getName(),
                  interf.getID(),
                  interf.getKey());
            });

    language
        .getAnnotationDefinitions()
        .forEach(
            annotationDef -> {
              try {
                MethodSpec getter =
                    MethodSpec.methodBuilder(getterName(annotationDef.getName()))
                        .returns(annotationDefClass)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(
                            "return this.requireAnnotationByName($S)", annotationDef.getName())
                        .build();
                languageClass.addMethod(getter);

                MethodSpec.Builder initMethod =
                    MethodSpec.methodBuilder("init" + capitalize(annotationDef.getName()))
                        .addModifiers(Modifier.PRIVATE)
                        .returns(void.class)
                        .addStatement(
                            "$T annotationDef = this.requireAnnotationByName($S)",
                            annotationDefClass,
                            annotationDef.getName());
                if (annotationDef.getExtendedAnnotation() != null) {
                  initMethod.addStatement(
                      "annotationDef.setExtendedAnnotation($L)",
                      toAnnotationExpr(
                          annotationDef.getExtendedAnnotation(), generationContext, language));
                }
                annotationDef
                    .getImplemented()
                    .forEach(
                        interf ->
                            initMethod.addStatement(
                                "annotationDef.addImplementedInterface($L)",
                                toInterfaceExpr(interf, generationContext, language)));
                initMethod.addStatement(
                    "annotationDef.setAnnotates($L)",
                    toClassifierExpr(annotationDef.getAnnotates(), generationContext, language));
                annotationDef
                    .getFeatures()
                    .forEach(
                        feature ->
                            initFeature(
                                initMethod, feature, "annotationDef", generationContext, language));
                languageClass.addMethod(initMethod.build());

                constructor.addStatement("init$L()", capitalize(annotationDef.getName()));

                createElements.addStatement(
                    "new $T(this, $S, $S, $S);",
                    annotationDefClass,
                    annotationDef.getName(),
                    annotationDef.getID(),
                    annotationDef.getKey());
              } catch (RuntimeException e) {
                throw new RuntimeException(
                    "Issue generating annotation " + annotationDef.getName(), e);
              }
            });

    language
        .getStructuredDataTypes()
        .forEach(
            dataType -> {
              throw new UnsupportedOperationException("Not yet implemented");
            });

    language
        .getPrimitiveTypes()
        .forEach(
            primitiveType -> {
              MethodSpec getter =
                  MethodSpec.methodBuilder(getterName(primitiveType.getName()))
                      .returns(primitiveTypeClass)
                      .addModifiers(Modifier.PUBLIC)
                      .addStatement(
                          "return this.requirePrimitiveTypeByName($S)", primitiveType.getName())
                      .build();
              languageClass.addMethod(getter);
            });

    language
        .getEnumerations()
        .forEach(
            enumeration -> {
              MethodSpec getter =
                  MethodSpec.methodBuilder(getterName(enumeration.getName()))
                      .returns(enumerationClass)
                      .addModifiers(Modifier.PUBLIC)
                      .addStatement(
                          "return this.requireEnumerationByName($S)", enumeration.getName())
                      .build();
              languageClass.addMethod(getter);
            });

    language
        .getElements()
        .forEach(
            element -> {
              if (element instanceof Enumeration) {
                String varName = toVariableName(element.getName());
                createElements.addStatement(
                    "$T $L = new $T(this, $S, $S);",
                    enumerationClass,
                    varName,
                    enumerationClass,
                    element.getName(),
                    element.getID());
                createElements.addStatement("$L.setKey($S)", varName, element.getKey());
                Enumeration enumeration = (Enumeration) element;
                enumeration
                    .getLiterals()
                    .forEach(
                        literal ->
                            createElements.addStatement(
                                "$L.addLiteral(new $T(this.getLionWebVersion(), $S).setID($S).setKey($S))",
                                varName,
                                enumerationLiteralClass,
                                literal.getName(),
                                literal.getID(),
                                literal.getKey()));
              } else if (element instanceof PrimitiveType) {
                createElements.addStatement(
                    "$T $L = new $T(this, $S, $S);",
                    primitiveTypeClass,
                    toVariableName(element.getName()),
                    primitiveTypeClass,
                    element.getName(),
                    element.getID());
                createElements.addStatement(
                    "$L.setKey($S)", toVariableName(element.getName()), element.getKey());
              }
            });

    languageClass.addMethod(constructor.build());
    languageClass.addMethod(createElements.build());
    JavaFile javaFile =
        JavaFile.builder(generationContext.generationPackage(language), languageClass.build())
            .build();

    javaFile.writeTo(destinationDir.toPath());
  }

  private void initFeature(
      MethodSpec.Builder initMethod,
      Feature<?> feature,
      String container,
      GenerationContext generationContext,
      Language languageBeingGenerated) {
    String variableName = toVariableName(feature.getName());
    if (feature instanceof Property) {
      initMethod.addStatement(
          "$T $L = new Property($S, $L, $S)",
          ClassName.get(Property.class),
          variableName,
          feature.getName(),
          container,
          feature.getID());
      initMethod.addStatement("$L.setKey($S)", variableName, feature.getKey());
      initMethod.addStatement(
          "$L.setType($L)",
          variableName,
          toDataTypeExpr(
              ((Property) feature).getType(), generationContext, languageBeingGenerated));
      initMethod.addStatement("$L.setOptional($L)", variableName, feature.isOptional());
    } else if (feature instanceof Containment) {
      initMethod.addStatement(
          "$T $L = new Containment($S, $L, $S)",
          ClassName.get(Containment.class),
          variableName,
          feature.getName(),
          container,
          feature.getID());
      initMethod.addStatement("$L.setKey($S)", variableName, feature.getKey());
      initMethod.addStatement(
          "$L.setType($L)",
          variableName,
          toClassifierExpr(
              ((Containment) feature).getType(), generationContext, languageBeingGenerated));
      initMethod.addStatement("$L.setOptional($L)", variableName, feature.isOptional());
      initMethod.addStatement(
          "$L.setMultiple($L)", variableName, ((Containment) feature).isMultiple());
    } else if (feature instanceof Reference) {
      initMethod.addStatement(
          "$T $L = new Reference($S, $L, $S)",
          ClassName.get(Reference.class),
          variableName,
          feature.getName(),
          container,
          feature.getID());
      initMethod.addStatement("$L.setKey($S)", variableName, feature.getKey());
      initMethod.addStatement(
          "$L.setType($L)",
          variableName,
          toClassifierExpr(
              ((Reference) feature).getType(), generationContext, languageBeingGenerated));
      initMethod.addStatement("$L.setOptional($L)", variableName, feature.isOptional());
      initMethod.addStatement(
          "$L.setMultiple($L)", variableName, ((Reference) feature).isMultiple());
    } else {
      throw new UnsupportedOperationException("Unknown feature type: " + feature.getClass());
    }
  }

  private CodeBlock toDataTypeExpr(
      DataType<?> dataType, GenerationContext generationContext, Language languageBeingGenerated) {
    return CodeBlock.of(
        "$L.requireDataTypeByName($S)",
        generationContext.resolveLanguage(dataType.getLanguage(), languageBeingGenerated),
        dataType.getName());
  }

  private CodeBlock toClassifierExpr(
      Classifier<?> classifierType,
      GenerationContext generationContext,
      Language languageBeingGenerated) {
    return CodeBlock.of(
        "$L.requireClassifierByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage(), languageBeingGenerated),
        classifierType.getName());
  }

  private CodeBlock toConceptExpr(
      Classifier<?> classifierType,
      GenerationContext generationContext,
      Language languageBeingGenerated) {
    return CodeBlock.of(
        "$L.requireConceptByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage(), languageBeingGenerated),
        classifierType.getName());
  }

  private CodeBlock toAnnotationExpr(
      Annotation annotation, GenerationContext generationContext, Language languageBeingGenerated) {
    return CodeBlock.of(
        "$L.requireAnnotationByName($S)",
        generationContext.resolveLanguage(annotation.getLanguage(), languageBeingGenerated),
        annotation.getName());
  }

  private CodeBlock toInterfaceExpr(
      Classifier<?> classifierType,
      GenerationContext generationContext,
      Language languageBeingGenerated) {
    return CodeBlock.of(
        "$L.requireInterfaceByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage(), languageBeingGenerated),
        classifierType.getName());
  }
}
