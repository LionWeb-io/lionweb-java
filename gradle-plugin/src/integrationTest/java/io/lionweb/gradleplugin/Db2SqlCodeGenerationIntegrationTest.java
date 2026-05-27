package io.lionweb.gradleplugin;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.*;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests that exercise the full Gradle plugin pipeline:
 *
 * <p>language JSON → generateLWLanguages + generateNodeClasses → .java files → compile →
 * URLClassLoader → reflective AST manipulation
 *
 * <p>Language used: db2sql (from src/integrationTest/resources/db2sql/). Mapped types:
 * com.strumenta.ast.ASTNode / Statement (stubs in this source set). Language stubs: ASTLanguageV1,
 * CommentsLanguage (load from JSON at first use).
 */
public class Db2SqlCodeGenerationIntegrationTest {

  private static final String PACKAGE = "com.strumenta.db2sql";

  @TempDir File testProjectDir;

  private URLClassLoader generatedClassLoader;

  // ---------------------------------------------------------------------------
  // Setup – run the plugin once, compile the output, share across all tests
  // ---------------------------------------------------------------------------

  @BeforeEach
  public void runPluginAndCompile() throws Exception {
    writeFile(
        new File(testProjectDir, "settings.gradle.kts"), "rootProject.name = \"db2sql-test\"");
    writeFile(new File(testProjectDir, "build.gradle.kts"), buildFileContent());

    File lionwebDir = new File(testProjectDir, "src/main/lionweb");
    lionwebDir.mkdirs();
    copyResource("/db2sql/language.json", new File(lionwebDir, "language.json"));
    copyResource("/db2sql/ast.language.v1.json", new File(lionwebDir, "ast.language.v1.json"));
    copyResource(
        "/db2sql/comments.language.v1.json", new File(lionwebDir, "comments.language.v1.json"));

    BuildResult result;
    try {
      result =
          GradleRunner.create()
              .withProjectDir(testProjectDir)
              .withPluginClasspath()
              .withArguments(
                  "generateLWLanguages", "generateLWNodeClasses", "--info", "--stacktrace")
              .build();
    } catch (Exception e) {
      fail("Inner Gradle build failed:\n" + e.getMessage());
      return;
    }

    assertEquals(SUCCESS, result.task(":generateLWLanguages").getOutcome());
    assertEquals(SUCCESS, result.task(":generateLWNodeClasses").getOutcome());

    File generatedDir = new File(testProjectDir, "build/generated-lionweb");
    assertTrue(generatedDir.exists(), "build/generated-lionweb should exist");

    File compiledDir = Files.createTempDirectory("db2sql-compiled").toFile();
    compileGeneratedFiles(generatedDir, compiledDir);

    generatedClassLoader =
        new URLClassLoader(new URL[] {compiledDir.toURI().toURL()}, getClass().getClassLoader());
  }

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  /** Adds a SELECT statement to a script and verifies the list grows. */
  @Test
  public void testScriptContainsAddedStatement() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> selectClass = cls("DB2SQLSelectStatement");

    Object script = newNode(scriptClass, "script-1");
    Object select = newNode(selectClass, "select-1");

    invoke(scriptClass, "addToStatements", script, select);

    List<?> statements = getStatements(script, scriptClass);
    assertEquals(1, statements.size());
    assertSame(select, statements.get(0));
  }

  /** Adds multiple statements and verifies order. */
  @Test
  public void testMultipleStatementsPreserveOrder() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> insertClass = cls("DB2SQLInsertStatement");
    Class<?> deleteClass = cls("DB2SQLDeleteStatement");

    Object script = newNode(scriptClass, "script-1");
    Object insert = newNode(insertClass, "insert-1");
    Object delete = newNode(deleteClass, "delete-1");

    invoke(scriptClass, "addToStatements", script, insert);
    invoke(scriptClass, "addToStatements", script, delete);

    List<?> stmts = getStatements(script, scriptClass);
    assertEquals(2, stmts.size());
    assertSame(insert, stmts.get(0));
    assertSame(delete, stmts.get(1));
  }

  /** Verifies that parent is wired when a child is added to a containment. */
  @Test
  public void testParentIsSetWhenAddingToContainment() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> selectClass = cls("DB2SQLSelectStatement");

    Object script = newNode(scriptClass, "script-1");
    Object select = newNode(selectClass, "select-1");

    assertNull(getParent(select), "Parent should be null before being added");

    invoke(scriptClass, "addToStatements", script, select);

    assertSame(script, getParent(select), "Parent should be the script after being added");
  }

  /** Reading a Boolean property returns the value that was set. */
  @Test
  public void testSetAndGetBooleanProperty() throws Exception {
    Class<?> selectClass = cls("DB2SQLSelectStatement");
    Object select = newNode(selectClass, "select-1");

    selectClass.getMethod("setDistinct", boolean.class).invoke(select, Boolean.TRUE);
    Boolean distinct = (Boolean) selectClass.getMethod("getDistinct").invoke(select);
    assertTrue(distinct, "distinct should be true");
  }

  /** Reading a String property returns the value that was set. */
  @Test
  public void testSetAndGetStringProperty() throws Exception {
    Class<?> strLitClass = cls("DB2SQLStringLiteral");
    Object lit = newNode(strLitClass, "lit-1");

    strLitClass.getMethod("setValue", String.class).invoke(lit, "hello world");
    String val = (String) strLitClass.getMethod("getValue").invoke(lit);
    assertEquals("hello world", val);
  }

  /** String property defaults to null before being set. */
  @Test
  public void testStringPropertyDefaultIsNull() throws Exception {
    Class<?> strLitClass = cls("DB2SQLStringLiteral");
    Object lit = newNode(strLitClass, "lit-1");
    assertNull(strLitClass.getMethod("getValue").invoke(lit));
  }

  /** Builds a richer AST: script → SELECT(distinct) → FROM(table ref) + SELECT item. */
  @Test
  public void testBuildSelectWithFromAndItems() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> selectClass = cls("DB2SQLSelectStatement");
    Class<?> fromClass = cls("DB2SQLFromClause");
    Class<?> strLitClass = cls("DB2SQLStringLiteral");
    Class<?> explicitItemClass = cls("DB2SQLExplicitSelectItem");

    Object script = newNode(scriptClass, "script-1");
    Object select = newNode(selectClass, "select-1");
    Object from = newNode(fromClass, "from-1");
    Object colLit = newNode(strLitClass, "lit-1");
    Object item = newNode(explicitItemClass, "item-1");

    // SET distinct = true on the SELECT
    selectClass.getMethod("setDistinct", boolean.class).invoke(select, Boolean.TRUE);

    // Wire FROM clause onto SELECT
    invoke(selectClass, "setFromClause", select, from);

    // Wire string literal into explicit select item
    invoke(explicitItemClass, "setExpression", item, colLit);
    // set alias on item
    explicitItemClass.getMethod("setAlias", String.class).invoke(item, "myCol");
    // add item to SELECT
    invoke(selectClass, "addToSelectItems", select, item);

    // Add SELECT to script
    invoke(scriptClass, "addToStatements", script, select);

    // Assertions
    List<?> stmts = getStatements(script, scriptClass);
    assertEquals(1, stmts.size());
    assertSame(select, stmts.get(0));

    List<?> items = (List<?>) selectClass.getMethod("getSelectItems").invoke(select);
    assertEquals(1, items.size());
    assertSame(item, items.get(0));

    String alias = (String) explicitItemClass.getMethod("getAlias").invoke(item);
    assertEquals("myCol", alias);

    // Parent wiring
    assertSame(script, getParent(select));
    assertSame(select, getParent(item));
    assertSame(item, getParent(colLit));
  }

  /** Clear removes all items from a multiple containment. */
  @Test
  public void testClearStatementsEmptiesList() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> commitClass = cls("DB2SQLCommitStatement");

    Object script = newNode(scriptClass, "script-1");
    invoke(scriptClass, "addToStatements", script, newNode(commitClass, "c1"));
    invoke(scriptClass, "addToStatements", script, newNode(commitClass, "c2"));

    assertEquals(2, getStatements(script, scriptClass).size());

    scriptClass.getMethod("clearStatements").invoke(script);

    assertEquals(0, getStatements(script, scriptClass).size());
  }

  /** Removing by index leaves remaining elements intact. */
  @Test
  public void testRemoveFromStatementsByIndex() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Class<?> commitClass = cls("DB2SQLCommitStatement");

    Object script = newNode(scriptClass, "s");
    Object c0 = newNode(commitClass, "c0");
    Object c1 = newNode(commitClass, "c1");
    Object c2 = newNode(commitClass, "c2");

    invoke(scriptClass, "addToStatements", script, c0);
    invoke(scriptClass, "addToStatements", script, c1);
    invoke(scriptClass, "addToStatements", script, c2);

    // remove middle element
    scriptClass.getMethod("removeFromStatements", int.class).invoke(script, 1);

    List<?> stmts = getStatements(script, scriptClass);
    assertEquals(2, stmts.size());
    assertSame(c0, stmts.get(0));
    assertSame(c2, stmts.get(1));
  }

  /** getID returns the id passed to the constructor. */
  @Test
  public void testNodeIdIsRetained() throws Exception {
    Class<?> scriptClass = cls("DB2SQLScript");
    Object script = newNode(scriptClass, "my-id-42");
    String id = (String) scriptClass.getMethod("getID").invoke(script);
    assertEquals("my-id-42", id);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private Class<?> cls(String simpleName) throws ClassNotFoundException {
    return generatedClassLoader.loadClass(PACKAGE + "." + simpleName);
  }

  private Object newNode(Class<?> clazz, String id) throws Exception {
    return clazz.getConstructor(String.class).newInstance(id);
  }

  private Object getParent(Object node) throws Exception {
    return node.getClass().getMethod("getParent").invoke(node);
  }

  @SuppressWarnings("unchecked")
  private List<?> getStatements(Object script, Class<?> scriptClass) throws Exception {
    return (List<?>) scriptClass.getMethod("getStatements").invoke(script);
  }

  /** Finds the first method with the given name and a single parameter, then invokes it. */
  private Object invoke(Class<?> clazz, String methodName, Object receiver, Object arg)
      throws Exception {
    Method m =
        Arrays.stream(clazz.getMethods())
            .filter(x -> x.getName().equals(methodName) && x.getParameterCount() == 1)
            .findFirst()
            .orElseThrow(
                () -> new AssertionError("No single-arg method '" + methodName + "' on " + clazz));
    return m.invoke(receiver, arg);
  }

  private boolean compileGeneratedFiles(File sourceDir, File outputDir) throws IOException {
    List<File> files;
    try (Stream<Path> stream = Files.walk(sourceDir.toPath())) {
      files =
          stream
              .filter(p -> p.toString().endsWith(".java"))
              .map(Path::toFile)
              .collect(Collectors.toList());
    }
    if (files.isEmpty()) {
      throw new IllegalStateException("No .java files found under " + sourceDir);
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("No Java compiler available (run on a JDK, not a JRE)");
    }
    outputDir.mkdirs();

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(files);

    List<String> options =
        Arrays.asList(
            "-classpath", System.getProperty("java.class.path"),
            "-d", outputDir.getAbsolutePath());

    boolean ok = compiler.getTask(null, fm, diagnostics, options, null, units).call();
    fm.close();

    if (!ok) {
      String errors =
          diagnostics.getDiagnostics().stream()
              .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
              .map(Object::toString)
              .collect(Collectors.joining("\n"));
      fail("Generated code did not compile:\n" + errors);
    }
    return true;
  }

  private void writeFile(File dest, String content) throws IOException {
    dest.getParentFile().mkdirs();
    try (BufferedWriter w = new BufferedWriter(new FileWriter(dest))) {
      w.write(content);
    }
  }

  private void copyResource(String resourcePath, File dest) throws IOException {
    try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new IllegalArgumentException("Resource not found: " + resourcePath);
      }
      dest.getParentFile().mkdirs();
      Files.copy(in, dest.toPath());
    }
  }

  private static String buildFileContent() {
    return "plugins {\n"
        + "    id(\"io.lionweb\")\n"
        + "}\n"
        + "lionweb {\n"
        + "    defaultPackageName.set(\"com.strumenta.db2sql\")\n"
        + "    languagesToGenerate.set(setOf(\"language-db2sql-id\"))\n"
        + "    languagesClassNames.set(mapOf(\n"
        + "        \"com.strumenta.StarLasu\" to"
        + " \"com.strumenta.starlasu.base.v1.ASTLanguageV1\",\n"
        + "        \"com.strumenta.starlasu.comments\" to"
        + " \"com.strumenta.starlasu.base.v1.CommentsLanguage\"\n"
        + "    ))\n"
        + "    mappings.set(mapOf(\n"
        + "        \"com.strumenta.StarLasu.ASTNode\" to \"com.strumenta.ast.ASTNode\",\n"
        + "        \"com.strumenta.StarLasu.Statement\" to \"com.strumenta.ast.Statement\"\n"
        + "    ))\n"
        + "}\n";
  }
}
