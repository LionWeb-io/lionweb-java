package io.lionweb.gradleplugin.generators;

import com.palantir.javapoet.*;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;

import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.HasSettableParent;
import io.lionweb.model.Node;
import io.lionweb.model.impl.AbstractNode;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodeClassesJavaCodeGenerator extends AbstractJavaCodeGenerator {
  /**
   * Constructs a NodeClassesJavaCodeGenerator with a specified destination directory.
   *
   * @param destinationDir the directory where the generated code will be stored; must not be null
   * @throws NullPointerException if the destinationDir is null
   */
  public NodeClassesJavaCodeGenerator(@NotNull File destinationDir) {
    super(destinationDir);
  }

  public void generate(@Nonnull Language language, @Nonnull String packageName) throws IOException {
    generate(
        language,
        packageName,
        new LanguageContext(packageName, Collections.singletonList(language)));
  }

  private void generate(
      @Nonnull Language language,
      @Nonnull String packageName,
      @Nonnull LanguageContext languageContext)
      throws IOException {
      Objects.requireNonNull(language, "language should not be null");
      Objects.requireNonNull(packageName, "packageName should not be null");
      Objects.requireNonNull(languageContext, "languageContext should not be null");
      language.getConcepts().forEach(concept -> {
          generateConcept(concept, packageName, languageContext);
      });
      language.getInterfaces().forEach(interf -> {
          generateInterface(interf, packageName, languageContext);
      });
      language.getStructuredDataTypes().forEach(sdt -> {
          throw new UnsupportedOperationException();
      });
  }

  private void generateConcept(@Nonnull Concept concept, @Nonnull String packageName,
                                @Nonnull LanguageContext languageContext) {
      LionWebVersion lionWebVersion = concept.getLanguage().getLionWebVersion();
      String className = concept.getName();

      TypeName classifierInstanceOfUnknown = ParameterizedTypeName.get(
              ClassName.get(ClassifierInstance.class),
              WildcardTypeName.subtypeOf(Object.class)   // "?"
      );

      TypeSpec.Builder conceptClass =
              TypeSpec.classBuilder(className)
                      .superclass(ClassName.get(AbstractNode.class))
                      .addSuperinterface(ClassName.get(HasSettableParent.class))
                      .addModifiers(Modifier.PUBLIC);
      conceptClass.addField(
              FieldSpec.builder(ClassName.get(String.class), "id")
                      .addAnnotation(NotNull.class)
                      .addModifiers(Modifier.PRIVATE)
                      .build()
              );
      conceptClass.addField(
              FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(ClassifierInstance.class), WildcardTypeName.subtypeOf(Object.class)), "parent")
                      .addAnnotation(Nullable.class)
                      .addModifiers(Modifier.PRIVATE)
                      .build()
      );
      conceptClass.addMethod(
                      MethodSpec.constructorBuilder()
                              .addParameter(
                                      ParameterSpec.builder(ClassName.get(String.class), "id")
                                              .addAnnotation(NotNull.class)
                                              .build()
                              )
                              .addStatement("$T.requireNonNull(id, $S)", Objects.class,  "id must not be null")
                              .addStatement("this.id = id")
                              .addModifiers(Modifier.PUBLIC)
                              .build()
              );
        conceptClass.addMethod(
                      MethodSpec.methodBuilder("getID")
                              .returns(TypeName.get(String.class))
                              .addAnnotation(NotNull.class)
                              .addModifiers(Modifier.PUBLIC)
                              .addStatement("return this.id").build()
              );
        conceptClass.addMethod(
                MethodSpec.methodBuilder("getParent")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(
                                classifierInstanceOfUnknown
                        )
                        .addStatement("return this.parent")
                        .build()
        );
        conceptClass.addMethod(
                MethodSpec.methodBuilder("setParent")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(ClassifierInstance.class))
                        .addParameter(
                                ParameterSpec.builder(
                                                classifierInstanceOfUnknown,
                                                "parent"
                                        )
                                        .addAnnotation(ClassName.get(Nullable.class))
                                        .build()
                        )
                        .addStatement("this.parent = parent")
                        .addStatement("return this")
                        .build()
        );
        conceptClass.addMethod(
                MethodSpec.methodBuilder("getClassifier")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(Concept.class))
                        .addStatement("return $L.getCodebase()",
                                languageContext.resolveLanguage(concept.getLanguage()))
                        .build()
        );
        concept.getFeatures().forEach(feature -> {
            if (feature instanceof Property) {
                Property property = (Property) feature;
                TypeName fieldType;
                if (property.getType().equals(LionCoreBuiltins.getString(lionWebVersion))) {
                    fieldType = ClassName.get(String.class);
                } else if (property.getType().equals(LionCoreBuiltins.getInteger(lionWebVersion))) {
                    fieldType = ClassName.get(int.class);
                } else {
                    throw new UnsupportedOperationException("Unknown property type: " + property.getType());
                }
                conceptClass.addField(FieldSpec.builder(fieldType, feature.getName(), Modifier.PRIVATE)
                        .build());
            } else if (feature instanceof Containment) {
                throw new UnsupportedOperationException("Containments are not yet implemented");
            } else if (feature instanceof Reference) {
                throw new UnsupportedOperationException("References are not yet implemented");
            } else {
                throw new IllegalStateException("Unknown feature type: " + feature.getClass());
            }
        });
      JavaFile javaFile = JavaFile.builder(packageName, conceptClass.build()).build();
      try {
          javaFile.writeTo(destinationDir.toPath());
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
  }

    private void generateInterface(@Nonnull Interface interf, @Nonnull String packageName,
                                   @Nonnull LanguageContext languageContext) {
        String interfName = interf.getName();
        TypeSpec.Builder conceptClass =
                TypeSpec.interfaceBuilder(interfName)
                        .addSuperinterface(ClassName.get(Node.class))
                        .addModifiers(Modifier.PUBLIC);
        JavaFile javaFile = JavaFile.builder(packageName, conceptClass.build()).build();
        try {
            javaFile.writeTo(destinationDir.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

  public void generate(@Nonnull List<Language> languages, @Nonnull String packageName)
      throws IOException {
    Objects.requireNonNull(languages, "languages should not be null");
    Objects.requireNonNull(packageName, "packageName should not be null");
    if (languages.isEmpty()) {
      return;
    }
    LanguageContext languageContext = new LanguageContext(packageName, languages);
    languages.forEach(
        language -> {
          try {
            generate(language, packageName, languageContext);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
