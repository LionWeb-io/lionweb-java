package io.lionweb.gradleplugin;

import com.palantir.javapoet.*;
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

        ClassName lwLanguageClass = ClassName.get("io.lionweb.language", "Language");

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
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("this.setName($S)", language.getName())
                .addStatement("this.setVersion($S)", language.getVersion())
                .addStatement("this.setID($S)", language.getID())
                .addStatement("this.setKey($S)", language.getKey())
                .build();

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

        TypeSpec languageClass = TypeSpec.classBuilder(className)
                .superclass(lwLanguageClass)
                .addField(instanceField)
                .addMethod(constructor)
                .addMethod(getInstance)
                .addModifiers(Modifier.PUBLIC)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, languageClass)
                .build();

        javaFile.writeTo(destinationDir.toPath());
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
