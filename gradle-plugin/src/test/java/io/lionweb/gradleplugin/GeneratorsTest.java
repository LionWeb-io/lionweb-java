package io.lionweb.gradleplugin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.gradleplugin.generators.LanguageJavaCodeGenerator;
import io.lionweb.gradleplugin.generators.NodeClassesJavaCodeGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

public class GeneratorsTest {

  @Test
  public void testGenerateSimpleLanguage() throws IOException {
    File generationDir = Files.createTempDirectory("lionweb-test").toFile();
    LanguageJavaCodeGenerator languageGen = new LanguageJavaCodeGenerator(generationDir);
    NodeClassesJavaCodeGenerator classesGen = new NodeClassesJavaCodeGenerator(generationDir);
    String packageName = "com.foo";
    languageGen.generate(CompanyLanguage.getLanguage(), packageName);
    classesGen.generate(CompanyLanguage.getLanguage(), packageName);
    // TODO verify they compile

    assertTrue(compileAllJavaFiles(generationDir));
  }

  private boolean compileAllJavaFiles(File root) throws IOException {
    List<File> files;
    try (Stream<Path> stream = Files.walk(root.toPath())) {
      files =
          stream
              .filter(p -> p.toString().endsWith(".java"))
              .map(Path::toFile)
              .collect(Collectors.toList());
    }

    if (files.isEmpty()) {
      throw new IllegalStateException("No .java files generated");
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException(
          "No Java compiler available (are you running on a JRE instead of a JDK?)");
    }

    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(files);

    List<String> options = Arrays.asList("-classpath", System.getProperty("java.class.path"));

    JavaCompiler.CompilationTask task =
        compiler.getTask(null, fileManager, null, options, null, units);
    boolean result = task.call();

    fileManager.close();
    return result;
  }
}
