package io.lionweb.gradleplugin;

import com.palantir.javapoet.*;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;

import javax.lang.model.element.Modifier;
import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;

public class LanguageJavaCodeGenerator {
    private File destinationDir;

    public LanguageJavaCodeGenerator(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    public void generate(Language language, String packageName) throws IOException {
        String className = capitalize(language.getName()) + "Language";

        ClassName lwLanguageClass = ClassName.get(Language.class);

        // private static LibraryLanguage INSTANCE;
        FieldSpec instanceField = FieldSpec.builder(
                        ClassName.get(packageName, className),
                        "INSTANCE",
                        Modifier.PRIVATE, Modifier.STATIC)
                .build();

        // private LibraryLanguage() {
        //     this.setName("Library");
        //     this.setVersion("1.0.0");
        //     this.setID("library-id");
        //     this.setKey("library");
        // }
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("this.setName($S)", language.getName())
                .addStatement("this.setVersion($S)", language.getVersion())
                .addStatement("this.setID($S)", language.getID())
                .addStatement("this.setKey($S)", language.getKey());
        // TODO consider dependsOn
        // TODO consider LionWeb Version

        // public static LibraryLanguage getInstance() {
        //     if (INSTANCE == null) {
        //         INSTANCE = new LibraryLanguage();
        //     }
        //     return INSTANCE;
        // }
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
            ClassName conceptClass = ClassName.get(Concept.class);

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
            });
            languageClass.addMethod(initMethod.build());

            constructor.addStatement("init$L()", capitalize(concept.getName()));

            createElements.addStatement("new Concept(this, $S, $S, $S);", concept.getName(), concept.getID(), concept.getKey());
        });

        // TODO interfaces
        // TODO data types
        // TODO annotations

        languageClass.addMethod(constructor.build());
        languageClass.addMethod(createElements.build());
        JavaFile javaFile = JavaFile.builder(packageName, languageClass.build())
                .build();

        javaFile.writeTo(destinationDir.toPath());
    }

    private static ClassName lionCoreBuiltins = ClassName.get(LionCoreBuiltins.class);
    private static ClassName lionWebVersion  = ClassName.get(LionWebVersion.class);

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
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private CodeBlock toClassifierExpr(Language language, Classifier<?> classifierType) {
        if (language.equals(classifierType.getLanguage())) {
            return CodeBlock.of("this.requireClassifierByName($S)", classifierType.getName());
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
