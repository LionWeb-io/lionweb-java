package io.lionweb.gradleplugin;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
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

        TypeSpec languageClass = TypeSpec.classBuilder(language.getName() + "Language")
                .addModifiers(Modifier.PUBLIC)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, languageClass)
                .build();

        javaFile.writeTo(destinationDir.toPath());
    }
}
