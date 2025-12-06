package io.lionweb.gradleplugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

public abstract class AbstractGeneratorTest {

  protected boolean compileAllJavaFiles(File root) throws IOException {
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

  protected static String read(InputStream in) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      char[] buf = new char[2048];
      int n;
      while ((n = r.read(buf)) != -1) {
        sb.append(buf, 0, n);
      }
    }
    return sb.toString();
  }
}
