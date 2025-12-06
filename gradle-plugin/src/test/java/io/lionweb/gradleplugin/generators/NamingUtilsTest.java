package io.lionweb.gradleplugin.generators;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NamingUtilsTest {

  // ========== capitalize() Tests ==========

  @Test
  void testCapitalize_normalWord() {
    assertEquals("Hello", NamingUtils.capitalize("hello"));
  }

  @Test
  void testCapitalize_alreadyCapitalized() {
    assertEquals("Hello", NamingUtils.capitalize("Hello"));
  }

  @Test
  void testCapitalize_singleCharacter() {
    assertEquals("A", NamingUtils.capitalize("a"));
  }

  @Test
  void testCapitalize_upperCaseSingleCharacter() {
    assertEquals("A", NamingUtils.capitalize("A"));
  }

  @Test
  void testCapitalize_allUpperCase() {
    assertEquals("HELLO", NamingUtils.capitalize("HELLO"));
  }

  @Test
  void testCapitalize_mixedCase() {
    assertEquals("HeLLo", NamingUtils.capitalize("heLLo"));
  }

  @Test
  void testCapitalize_withNumbers() {
    assertEquals("123hello", NamingUtils.capitalize("123hello"));
  }

  @Test
  void testCapitalize_throwsExceptionOnEmptyString() {
    assertThrows(StringIndexOutOfBoundsException.class, () -> NamingUtils.capitalize(""));
  }

  @Test
  void testCapitalize_throwsExceptionOnNull() {
    assertThrows(NullPointerException.class, () -> NamingUtils.capitalize(null));
  }

  // ========== camelCase() Tests ==========

  @Test
  void testCamelCase_simpleWords() {
    assertEquals("helloWorld", NamingUtils.camelCase("hello world"));
  }

  @Test
  void testCamelCase_withHyphens() {
    assertEquals("helloWorld", NamingUtils.camelCase("hello-world"));
  }

  @Test
  void testCamelCase_withUnderscores() {
    assertEquals("helloWorld", NamingUtils.camelCase("hello_world"));
  }

  @Test
  void testCamelCase_multipleWords() {
    assertEquals("helloWorldFromJava", NamingUtils.camelCase("hello world from java"));
  }

  @Test
  void testCamelCase_pascalCaseInput() {
    assertEquals("helloWorld", NamingUtils.camelCase("HelloWorld"));
  }

  @Test
  void testCamelCase_allCapsInput() {
    assertEquals("helloWorld", NamingUtils.camelCase("HELLO WORLD"));
  }

  @Test
  void testCamelCase_mixedDelimiters() {
    assertEquals("helloWorldFromJava", NamingUtils.camelCase("hello-world_from java"));
  }

  @Test
  void testCamelCase_withNumbers() {
    assertEquals("hello123World", NamingUtils.camelCase("hello123 world"));
  }

  @Test
  void testCamelCase_numbersAtStart() {
    assertEquals("123hello", NamingUtils.camelCase("123hello"));
  }

  @Test
  void testCamelCase_specialCharacters() {
    assertEquals("helloWorld", NamingUtils.camelCase("hello@world"));
  }

  @Test
  void testCamelCase_multipleSpaces() {
    assertEquals("helloWorld", NamingUtils.camelCase("hello   world"));
  }

  @Test
  void testCamelCase_leadingAndTrailingSpaces() {
    assertEquals("helloWorld", NamingUtils.camelCase("  hello world  "));
  }

  @Test
  void testCamelCase_onlySpecialCharacters() {
    assertEquals("", NamingUtils.camelCase("@#$%"));
  }

  @Test
  void testCamelCase_emptyString() {
    assertEquals("", NamingUtils.camelCase(""));
  }

  @Test
  void testCamelCase_null() {
    assertNull(NamingUtils.camelCase(null));
  }

  @Test
  void testCamelCase_singleWord() {
    assertEquals("hello", NamingUtils.camelCase("hello"));
  }

  @Test
  void testCamelCase_singleWordUpperCase() {
    assertEquals("hello", NamingUtils.camelCase("HELLO"));
  }

  @Test
  void testCamelCase_camelCaseAlready() {
    assertEquals("helloWorld", NamingUtils.camelCase("helloWorld"));
  }

  @Test
  void testCamelCase_consecutiveUpperCase() {
    assertEquals("hTTPResponse", NamingUtils.camelCase("HTTPResponse"));
  }

  @Test
  void testCamelCase_upperCaseBoundaries() {
    assertEquals("getHTTPResponse", NamingUtils.camelCase("GetHTTPResponse"));
  }

  @Test
  void testCamelCase_dotSeparated() {
    assertEquals("comExampleProject", NamingUtils.camelCase("com.example.project"));
  }

  @Test
  void testCamelCase_onlySpaces() {
    assertEquals("", NamingUtils.camelCase("   "));
  }

  @ParameterizedTest
  @CsvSource({
    "my-variable-name, myVariableName",
    "my_variable_name, myVariableName",
    "MyVariableName, myVariableName",
    "my variable name, myVariableName",
    "MY_CONSTANT, myConstant",
    "get-user-by-id, getUserById",
    "XMLHttpRequest, xMLHttpRequest"
  })
  void testCamelCase_parameterized(String input, String expected) {
    assertEquals(expected, NamingUtils.camelCase(input));
  }

  // ========== pascalCase() Tests ==========

  @Test
  void testPascalCase_simpleWords() {
    assertEquals("HelloWorld", NamingUtils.pascalCase("hello world"));
  }

  @Test
  void testPascalCase_withHyphens() {
    assertEquals("HelloWorld", NamingUtils.pascalCase("hello-world"));
  }

  @Test
  void testPascalCase_withUnderscores() {
    assertEquals("HelloWorld", NamingUtils.pascalCase("hello_world"));
  }

  @Test
  void testPascalCase_alreadyPascalCase() {
    assertEquals("HelloWorld", NamingUtils.pascalCase("HelloWorld"));
  }

  @Test
  void testPascalCase_allCaps() {
    assertEquals("HelloWorld", NamingUtils.pascalCase("HELLO WORLD"));
  }

  @Test
  void testPascalCase_emptyString() {
    assertThrows(StringIndexOutOfBoundsException.class, () -> NamingUtils.pascalCase(""));
  }

  @Test
  void testPascalCase_null() {
    assertThrows(NullPointerException.class, () -> NamingUtils.pascalCase(null));
  }

  @Test
  void testPascalCase_singleWord() {
    assertEquals("Hello", NamingUtils.pascalCase("hello"));
  }

  @Test
  void testPascalCase_withNumbers() {
    assertEquals("Version2Api", NamingUtils.pascalCase("version 2 api"));
  }

  @Test
  void testPascalCase_consecutiveUpperCase() {
    assertEquals("HTTPResponse", NamingUtils.pascalCase("HTTPResponse"));
  }

  @ParameterizedTest
  @CsvSource({
    "my-class-name, MyClassName",
    "my_class_name, MyClassName",
    "myClassName, MyClassName",
    "MY_CONSTANT, MyConstant",
    "user-service, UserService"
  })
  void testPascalCase_parameterized(String input, String expected) {
    assertEquals(expected, NamingUtils.pascalCase(input));
  }

  // ========== Integration Tests ==========

  @Test
  void testCamelCaseToPascalCase_roundTrip() {
    String input = "hello-world-test";
    String camel = NamingUtils.camelCase(input);
    String pascal = NamingUtils.pascalCase(input);

    assertEquals("helloWorldTest", camel);
    assertEquals("HelloWorldTest", pascal);
    assertEquals(pascal, NamingUtils.capitalize(camel));
  }

  @Test
  void testEdgeCase_onlySeparators() {
    assertEquals("", NamingUtils.camelCase("---___   "));
    assertThrows(StringIndexOutOfBoundsException.class, () -> NamingUtils.pascalCase("---___   "));
  }

  @Test
  void testEdgeCase_unicodeCharacters() {
    // While the implementation may not fully support Unicode,
    // we test that it doesn't crash
    assertDoesNotThrow(() -> NamingUtils.camelCase("hello-wörld"));
    assertDoesNotThrow(() -> NamingUtils.pascalCase("hello-wörld"));
  }
}
