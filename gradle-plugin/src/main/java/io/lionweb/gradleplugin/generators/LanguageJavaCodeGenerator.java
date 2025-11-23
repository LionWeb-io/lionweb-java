package io.lionweb.gradleplugin.generators;

import static io.lionweb.gradleplugin.generators.CommonClassNames.*;
import static io.lionweb.gradleplugin.generators.NamingUtils.capitalize;
import static io.lionweb.gradleplugin.generators.NamingUtils.toLanguageClassName;

import com.palantir.javapoet.*;
import io.lionweb.language.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;

/**
 * The LanguageJavaCodeGenerator class is responsible for generating Java code representations of
 * language definitions and their associated components.
 */
public class LanguageJavaCodeGenerator extends AbstractJavaCodeGenerator {

  /**
   * Constructs a LanguageJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public LanguageJavaCodeGenerator(@Nonnull File destinationDir) {
    super(destinationDir);
  }

  /**
   * Generates code for the specified languages and package name.
   *
   * @param languages a list of languages for which the code will be generated; must not be null
   * @param packageName the base package name under which the code will be generated; must not be
   *     null
   * @throws IOException if an I/O error occurs during code generation
   */
  public void generate(@Nonnull List<Language> languages, @Nonnull String packageName)
      throws IOException {
    Objects.requireNonNull(languages, "languages should not be null");
    Objects.requireNonNull(packageName, "packageName should not be null");
    if (languages.isEmpty()) {
      return;
    }
    Map<Language, String> languageSpecificPackages = new HashMap<>();
    //    specificPackages.entrySet().forEach(entry ->{
    //       Language language = languages.stream().filter(l ->
    // l.getID().equals(entry.getKey())).findFirst().get();
    //       languageSpecificPackages.put(language, entry.getValue());
    //    });
    //    LanguageContext languageContext = new LanguageContext(packageName, languages,
    // languageSpecificPackages);
    //    languages.forEach(
    //        language -> {
    //          try {
    //            generate(language, packageName, languageContext);
    //          } catch (IOException e) {
    //            throw new RuntimeException(e);
    //          }
    //        });
  }

  /**
   * Generates Java code files for a specified language and package name.
   *
   * @param language the language for which the code will be generated; must not be null
   * @param packageName the base package name under which the code will be generated; must not be
   *     null
   * @throws IOException if an I/O error occurs during code generation
   */
  public void generate(@Nonnull Language language, @Nonnull String packageName) throws IOException {
    generate(language, packageName, new GenerationContext(language, packageName));
  }

  private void generate(
      @Nonnull Language language,
      @Nonnull String packageName,
      @Nonnull GenerationContext generationContext)
      throws IOException {
    Objects.requireNonNull(language, "language should not be null");
    Objects.requireNonNull(packageName, "packageName should not be null");
    Objects.requireNonNull(generationContext, "languageContext should not be null");
    String className = toLanguageClassName(language, generationContext);

    ClassName lwLanguageClass = ClassName.get(Language.class);

    FieldSpec instanceField =
        FieldSpec.builder(
                ClassName.get(packageName, className),
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
                  "this.addDependency($L)", generationContext.resolveLanguage(dependency));
            });

    MethodSpec getInstance =
        MethodSpec.methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(ClassName.get(packageName, className))
            .beginControlFlow("if ($N == null)", instanceField)
            .addStatement("$N = new $T()", instanceField, ClassName.get(packageName, className))
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
              //    public Concept getLibrary() {
              //        return this.requireConceptByName("Library");
              //    }
              MethodSpec conceptAccessor =
                  MethodSpec.methodBuilder("get" + capitalize(concept.getName()))
                      .returns(ClassName.get(Concept.class))
                      .addModifiers(Modifier.PUBLIC)
                      .addStatement("return this.requireConceptByName($S)", concept.getName())
                      .build();
              languageClass.addMethod(conceptAccessor);

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
                    toConceptExpr(concept.getExtendedConcept(), generationContext));
              }
              concept
                  .getImplemented()
                  .forEach(
                      implemented -> {
                        initMethod.addStatement(
                            "concept.addImplementedInterface($L)",
                            toInterfaceExpr(implemented, generationContext));
                      });
              concept
                  .getFeatures()
                  .forEach(
                      feature -> {
                        initFeature(initMethod, language, feature, "concept", generationContext);
                      });
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
                  MethodSpec.methodBuilder("get" + capitalize(interf.getName()))
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
                            toInterfaceExpr(implemented, generationContext));
                      });
              interf
                  .getFeatures()
                  .forEach(
                      feature -> {
                        initFeature(initMethod, language, feature, "interf", generationContext);
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
              MethodSpec getter =
                  MethodSpec.methodBuilder("get" + capitalize(annotationDef.getName()))
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
                    toAnnotationExpr(annotationDef.getExtendedAnnotation(), generationContext));
              }
              annotationDef
                  .getImplemented()
                  .forEach(
                      interf -> {
                        initMethod.addStatement(
                            "annotationDef.addImplementedInterface($L)",
                            toInterfaceExpr(interf, generationContext));
                      });
              annotationDef
                  .getFeatures()
                  .forEach(
                      feature -> {
                        initFeature(
                            initMethod, language, feature, "annotationDef", generationContext);
                      });
              languageClass.addMethod(initMethod.build());

              constructor.addStatement("init$L()", capitalize(annotationDef.getName()));

              createElements.addStatement(
                  "new $T(this, $S, $S, $S);",
                  annotationDefClass,
                  annotationDef.getName(),
                  annotationDef.getID(),
                  annotationDef.getKey());
            });

    language
        .getStructuredDataTypes()
        .forEach(
            dataType -> {
              throw new UnsupportedOperationException("Not yet implemented");
            });

    language
        .getElements()
        .forEach(
            element -> {
              if (element instanceof Enumeration<?>) {
                throw new UnsupportedOperationException("Not yet implemented");
              } else if (element instanceof PrimitiveType) {
                createElements.addStatement(
                    "$T $L = new $T(this, $S, $S);",
                    primitiveType,
                    element.getName(),
                    primitiveType,
                    element.getName(),
                    element.getID());
                createElements.addStatement("$L.setKey($S)", element.getName(), element.getKey());
              }
            });

    languageClass.addMethod(constructor.build());
    languageClass.addMethod(createElements.build());
    JavaFile javaFile = JavaFile.builder(packageName, languageClass.build()).build();

    javaFile.writeTo(destinationDir.toPath());
  }

  private void initFeature(
      MethodSpec.Builder initMethod,
      Language language,
      Feature<?> feature,
      String container,
      GenerationContext generationContext) {
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
          toDataTypeExpr(((Property) feature).getType(), generationContext));
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
          toClassifierExpr(((Containment) feature).getType(), generationContext));
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
          toClassifierExpr(((Reference) feature).getType(), generationContext));
      initMethod.addStatement("$L.setOptional($L)", variableName, feature.isOptional());
      initMethod.addStatement(
          "$L.setMultiple($L)", variableName, ((Reference) feature).isMultiple());
    } else {
      throw new UnsupportedOperationException("Unknown feature type: " + feature.getClass());
    }
  }

  private CodeBlock toDataTypeExpr(DataType<?> dataType, GenerationContext generationContext) {
    return CodeBlock.of(
        "$L.requireDataTypeByName($S)",
        generationContext.resolveLanguage(dataType.getLanguage()),
        dataType.getName());
  }

  private CodeBlock toClassifierExpr(
      Classifier<?> classifierType, GenerationContext generationContext) {
    return CodeBlock.of(
        "$L.requireClassifierByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage()),
        classifierType.getName());
  }

  private CodeBlock toConceptExpr(
      Classifier<?> classifierType, GenerationContext generationContext) {
    return CodeBlock.of(
        "$L.requireConceptByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage()),
        classifierType.getName());
  }

  private CodeBlock toAnnotationExpr(Annotation annotation, GenerationContext generationContext) {
    return CodeBlock.of(
        "$L.requireAnnotationByName($S)",
        generationContext.resolveLanguage(annotation.getLanguage()),
        annotation.getName());
  }

  private CodeBlock toInterfaceExpr(
      Classifier<?> classifierType, GenerationContext generationContext) {
    return CodeBlock.of(
        "$L.requireInterfaceByName($S)",
        generationContext.resolveLanguage(classifierType.getLanguage()),
        classifierType.getName());
  }
}
