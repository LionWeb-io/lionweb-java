package io.lionweb.gradleplugin;

import com.palantir.javapoet.*;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;

public class LanguageJavaCodeGenerator {
    private final @Nonnull File destinationDir;

    public LanguageJavaCodeGenerator(@Nonnull File destinationDir) {
        Objects.requireNonNull(destinationDir, "destinationDir should not be null");
        this.destinationDir = destinationDir;
    }

    public void generate(@Nonnull Language language, @Nonnull String packageName) throws IOException {
        Objects.requireNonNull(language, "language should not be null");
        Objects.requireNonNull(packageName, "packageName should not be null");
        String className = capitalize(language.getName()) + "Language";

        ClassName lwLanguageClass = ClassName.get(Language.class);

        // private static LibraryLanguage INSTANCE;
        FieldSpec instanceField = FieldSpec.builder(
                        ClassName.get(packageName, className),
                        "INSTANCE",
                        Modifier.PRIVATE, Modifier.STATIC)
                .build();

        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($T.$L)", lionWebVersion, language.getLionWebVersion().name())
                .addStatement("this.setName($S)", language.getName())
                .addStatement("this.setVersion($S)", language.getVersion())
                .addStatement("this.setID($S)", language.getID())
                .addStatement("this.setKey($S)", language.getKey());

        language.dependsOn().forEach(dependency -> {
            throw new UnsupportedOperationException("Not yet implemented");
        });

        MethodSpec getInstance = MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(packageName, className))
                .beginControlFlow("if ($N == null)", instanceField)
                .addStatement("$N = new $T()", instanceField, ClassName.get(packageName, className))
                .endControlFlow()
                .addStatement("return $N", instanceField)
                .build();

        MethodSpec.Builder createElements = MethodSpec.methodBuilder("createElements")
                .addModifiers(Modifier.PRIVATE);
        constructor.addStatement("createElements()");

        TypeSpec.Builder languageClass = TypeSpec.classBuilder(className)
                .superclass(lwLanguageClass)
                .addField(instanceField)
                .addMethod(getInstance)
                .addModifiers(Modifier.PUBLIC);

        language.getConcepts().forEach(concept -> {
            //    public Concept getLibrary() {
            //        return this.requireConceptByName("Library");
            //    }
            MethodSpec conceptAccessor = MethodSpec.methodBuilder("get" + capitalize(concept.getName()))
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
            MethodSpec.Builder initMethod = MethodSpec.methodBuilder("init" + capitalize(concept.getName()))
                    .addModifiers(Modifier.PRIVATE)
                    .returns(void.class)
                    .addStatement("$T concept = this.requireConceptByName($S)", conceptClass, concept.getName())
                    .addStatement("concept.setAbstract($L)", concept.isAbstract())
                    .addStatement("concept.setPartition($L)", concept.isPartition());
            if (concept.getExtendedConcept() != null) {
                initMethod.addStatement("concept.setExtendedConcept($L)", toConceptExpr(language, concept.getExtendedConcept()));
            }
            concept.getImplemented().forEach(implemented -> {
                initMethod.addStatement("concept.addImplementedInterface($L)", toClassifierExpr(language, implemented));
            });
            concept.getFeatures().forEach(feature -> {
                initFeature(initMethod, language, feature);
            });
            languageClass.addMethod(initMethod.build());

            constructor.addStatement("init$L()", capitalize(concept.getName()));

            createElements.addStatement("new $T(this, $S, $S, $S);", conceptClass, concept.getName(), concept.getID(), concept.getKey());
        });

        language.getInterfaces().forEach(interf -> {
            MethodSpec getter = MethodSpec.methodBuilder("get" + capitalize(interf.getName()))
                    .returns(interfaceClass)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this.requireInterfaceByName($S)", interf.getName())
                    .build();
            languageClass.addMethod(getter);

            MethodSpec.Builder initMethod = MethodSpec.methodBuilder("init" + capitalize(interf.getName()))
                    .addModifiers(Modifier.PRIVATE)
                    .returns(void.class)
                    .addStatement("$T interf = this.requireInterfaceByName($S)", interfaceClass, interf.getName());
            interf.getExtendedInterfaces().forEach(implemented -> {
                initMethod.addStatement("interf.addImplementedInterface($L)", toClassifierExpr(language, implemented));
            });
            interf.getFeatures().forEach(feature -> {
                initFeature(initMethod, language, feature);
            });
            languageClass.addMethod(initMethod.build());

            constructor.addStatement("init$L()", capitalize(interf.getName()));

            createElements.addStatement("new $T(this, $S, $S, $S);", interfaceClass, interf.getName(), interf.getID(), interf.getKey());
        });

        language.getStructuredDataTypes().forEach(dataType -> {
            throw new UnsupportedOperationException("Not yet implemented");
        });

        language.getElements().forEach(element -> {
            if (element instanceof Enumeration<?>) {
                throw new UnsupportedOperationException("Not yet implemented");
            } else if (element instanceof Annotation) {
                throw new UnsupportedOperationException("Not yet implemented");
            } else if (element instanceof PrimitiveType) {
                throw new UnsupportedOperationException("Not yet implemented");
            }
        });

        languageClass.addMethod(constructor.build());
        languageClass.addMethod(createElements.build());
        JavaFile javaFile = JavaFile.builder(packageName, languageClass.build())
                .build();

        javaFile.writeTo(destinationDir.toPath());
    }

    private void initFeature(MethodSpec.Builder initMethod, Language language, Feature<?> feature) {
        if (feature instanceof Property) {
            initMethod.addStatement("$T $L = new Property($S, concept, $S)", ClassName.get(Property.class), feature.getName(), feature.getName(), feature.getID());
            initMethod.addStatement("$L.setKey($S)", feature.getName(), feature.getKey());
            initMethod.addStatement("$L.setType($L)", feature.getName(), toDataTypeExpr(((Property) feature).getType()));
            initMethod.addStatement("$L.setOptional($L)", feature.getName(), feature.isOptional());
        } else if (feature instanceof Containment) {
            initMethod.addStatement("$T $L = new Containment($S, concept, $S)", ClassName.get(Containment.class), feature.getName(), feature.getName(), feature.getID());
            initMethod.addStatement("$L.setKey($S)", feature.getName(), feature.getKey());
            initMethod.addStatement("$L.setType($L)", feature.getName(), toClassifierExpr(language, ((Containment) feature).getType()));
            initMethod.addStatement("$L.setOptional($L)", feature.getName(), feature.isOptional());
            initMethod.addStatement("$L.setMultiple($L)", feature.getName(), ((Containment) feature).isMultiple());
        } else if (feature instanceof Reference) {
            initMethod.addStatement("$T $L = new Reference($S, concept, $S)", ClassName.get(Reference.class), feature.getName(), feature.getName(), feature.getID());
            initMethod.addStatement("$L.setKey($S)", feature.getName(), feature.getKey());
            initMethod.addStatement("$L.setType($L)", feature.getName(), toClassifierExpr(language, ((Reference) feature).getType()));
            initMethod.addStatement("$L.setOptional($L)", feature.getName(), feature.isOptional());
            initMethod.addStatement("$L.setMultiple($L)", feature.getName(), ((Reference) feature).isMultiple());
        } else {
            throw new UnsupportedOperationException("Unknown feature type: " + feature.getClass());
        }
    }

    private static ClassName lionCore = ClassName.get(LionCore.class);
    private static ClassName lionCoreBuiltins = ClassName.get(LionCoreBuiltins.class);
    private static ClassName lionWebVersion  = ClassName.get(LionWebVersion.class);
    private static ClassName conceptClass = ClassName.get(Concept.class);
    private static ClassName interfaceClass = ClassName.get(Interface.class);

    private CodeBlock toDataTypeExpr(DataType<?> dataType) {
        if (dataType.equals(LionCoreBuiltins.getString(LionWebVersion.v2023_1))) {
            return CodeBlock.of(
                    "$T.getString($T.v2023_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else if (dataType.equals(LionCoreBuiltins.getInteger(LionWebVersion.v2023_1))) {
            return CodeBlock.of(
                    "$T.getInteger($T.v2023_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else if (dataType.equals(LionCoreBuiltins.getBoolean(LionWebVersion.v2023_1))) {
            return CodeBlock.of(
                    "$T.getBoolean($T.v2023_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else if (dataType.equals(LionCoreBuiltins.getString(LionWebVersion.v2024_1))) {
            return CodeBlock.of(
                    "$T.getString($T.v2024_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else if (dataType.equals(LionCoreBuiltins.getInteger(LionWebVersion.v2024_1))) {
            return CodeBlock.of(
                    "$T.getInteger($T.v2024_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else if (dataType.equals(LionCoreBuiltins.getBoolean(LionWebVersion.v2024_1))) {
            return CodeBlock.of(
                    "$T.getBoolean($T.v2024_1)",
                    lionCoreBuiltins,
                    lionWebVersion
            );
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private CodeBlock toClassifierExpr(Language language, Classifier<?> classifierType) {
        if (language.equals(classifierType.getLanguage())) {
            return CodeBlock.of("this.requireClassifierByName($S)", classifierType.getName());
        } else if (language.equals(LionCoreBuiltins.getInstance(LionWebVersion.v2023_1))) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (language.equals(LionCoreBuiltins.getInstance(LionWebVersion.v2024_1))) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (language.equals(LionCore.getInstance(LionWebVersion.v2023_1))) {
            return CodeBlock.of("$T.getInstance(LionWebVersion.v2023_1).requireClassifierByName($S)", lionCore, classifierType.getName());
        } else if (language.equals(LionCore.getInstance(LionWebVersion.v2024_1))) {
            return CodeBlock.of("$T.getInstance(LionWebVersion.v2024_1).requireClassifierByName($S)", lionCore, classifierType.getName());
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private CodeBlock toConceptExpr(Language language, Classifier<?> classifierType) {
        if (language.equals(classifierType.getLanguage())) {
            return CodeBlock.of("this.requireConceptByName($S)", classifierType.getName());
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
