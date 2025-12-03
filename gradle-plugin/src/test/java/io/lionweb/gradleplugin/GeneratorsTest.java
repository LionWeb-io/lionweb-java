package io.lionweb.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.gradleplugin.generators.LanguageJavaCodeGenerator;
import io.lionweb.gradleplugin.generators.NodeClassesJavaCodeGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

public class GeneratorsTest extends AbstractGeneratorTest {

  @Test
  public void testGenerateSimpleLanguage() throws IOException {
    File generationDir = Files.createTempDirectory("lionweb-test").toFile();
    LanguageJavaCodeGenerator languageGen = new LanguageJavaCodeGenerator(generationDir);
    NodeClassesJavaCodeGenerator classesGen = new NodeClassesJavaCodeGenerator(generationDir);
    String packageName = "com.foo";
    languageGen.generate(CompanyLanguage.getLanguage(), packageName);
    classesGen.generate(CompanyLanguage.getLanguage(), packageName);

    assertTrue(compileAllJavaFiles(generationDir));
  }
}
