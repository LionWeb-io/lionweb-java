package io.lionweb.gradleplugin;

import com.palantir.javapoet.*;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;

import javax.lang.model.element.Modifier;
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
                    .addStatement("$T concept = new $T()", conceptClass, conceptClass)
                    .addStatement("concept.setID($S)", concept.getID())
                    .addStatement("concept.setName($S)", concept.getName())
                    .addStatement("concept.setKey($S)", concept.getKey())
                    .addStatement("concept.setAbstract($L)", concept.isAbstract())
                    .addStatement("concept.setPartition($L)", concept.isPartition())
                    .addStatement("this.addElement(concept)");
            // TODO set extended
            // TODO set implemented
            // TODO set feature
            // TODO split into declaration and population of concepts, so that we have references across them
            languageClass.addMethod(initMethod.build());

            constructor.addStatement("init$L()", capitalize(concept.getName()));
        });

        languageClass.addMethod(constructor.build());
        JavaFile javaFile = JavaFile.builder(packageName, languageClass.build())
                .build();

        javaFile.writeTo(destinationDir.toPath());
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
