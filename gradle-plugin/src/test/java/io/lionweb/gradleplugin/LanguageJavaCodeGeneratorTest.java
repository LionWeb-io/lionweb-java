package io.lionweb.gradleplugin;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.serialization.SerializationProvider;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LanguageJavaCodeGeneratorTest {

    @Test
    public void testLibraryGeneration() throws IOException {
        Language library = SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1)
                .loadLanguage(this.getClass().getResourceAsStream("/library-language.json"));
        File destination = Files.createTempDirectory("gen").toFile();
        LanguageJavaCodeGenerator generator = new LanguageJavaCodeGenerator(destination);
        generator.generate(library, "my.pack");

        Path javaFile = Files.walk(destination.toPath()).filter(f -> "my/pack/LibraryLanguage.java".equals(destination.toPath().relativize(f).toString())).findFirst().get();
        String javaCode = new String(Files.readAllBytes(javaFile), StandardCharsets.UTF_8);
        System.out.println(javaCode);
    }

}
