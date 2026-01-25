package io.lionweb.language;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.lioncore.LionCore;
import org.junit.jupiter.api.Test;

public class EnumerationLiteralTest {

  @Test
  public void testDefaultConstructor() {
    EnumerationLiteral literal = new EnumerationLiteral();
    assertNull(literal.getName());
    assertNull(literal.getID());
    assertNull(literal.getEnumeration());
    assertEquals(LionWebVersion.currentVersion, literal.getLionWebVersion());
  }

  @Test
  public void testConstructorWithLionWebVersion() {
    EnumerationLiteral literal = new EnumerationLiteral(LionWebVersion.v2023_1);
    assertNull(literal.getName());
    assertNull(literal.getID());
    assertNull(literal.getEnumeration());
    assertEquals(LionWebVersion.v2023_1, literal.getLionWebVersion());
  }

  @Test
  public void testConstructorWithLionWebVersionAndName() {
    EnumerationLiteral literal = new EnumerationLiteral(LionWebVersion.v2023_1, "MyName");
    assertEquals("MyName", literal.getName());
    assertNull(literal.getID());
    assertNull(literal.getEnumeration());
    assertEquals(LionWebVersion.v2023_1, literal.getLionWebVersion());
  }

  @Test
  public void testConstructorWithName() {
    EnumerationLiteral literal = new EnumerationLiteral("TEST_LITERAL");
    assertEquals("TEST_LITERAL", literal.getName());
    assertNull(literal.getID());
    assertNull(literal.getEnumeration());
  }

  @Test
  public void testConstructorWithEnumerationNameAndId() {
    Enumeration enumeration = new Enumeration("TestEnum");
    enumeration.setID("enum-id");
    EnumerationLiteral literal = new EnumerationLiteral(enumeration, "LITERAL", "literal-id");

    assertEquals("LITERAL", literal.getName());
    assertEquals("literal-id", literal.getID());
    assertEquals(enumeration, literal.getEnumeration());
    assertTrue(enumeration.getLiterals().contains(literal));
  }

  @Test
  public void testSetAndGetName() {
    EnumerationLiteral literal = new EnumerationLiteral();
    literal.setName("NEW_NAME");
    assertEquals("NEW_NAME", literal.getName());
  }

  @Test
  public void testSetAndGetEnumeration() {
    EnumerationLiteral literal = new EnumerationLiteral();
    Enumeration enumeration = new Enumeration("TestEnum");

    literal.setEnumeration(enumeration);
    assertEquals(enumeration, literal.getEnumeration());
  }

  @Test
  public void testGetContainer() {
    EnumerationLiteral literal = new EnumerationLiteral();
    Enumeration enumeration = new Enumeration("TestEnum");

    literal.setEnumeration(enumeration);
    assertEquals(enumeration, literal.getContainer());
  }

  @Test
  public void testGetClassifier() {
    EnumerationLiteral literal = new EnumerationLiteral();
    assertEquals(
        LionCore.getEnumerationLiteral(literal.getLionWebVersion()), literal.getClassifier());
  }

  @Test
  public void testSetAndGetKey() {
    EnumerationLiteral literal = new EnumerationLiteral();
    literal.setKey("test-key");
    assertEquals("test-key", literal.getKey());
  }

  @Test
  public void testSetKeyReturnsSelf() {
    EnumerationLiteral literal = new EnumerationLiteral();
    EnumerationLiteral result = literal.setKey("test-key");
    assertEquals(literal, result);
  }

  @Test
  public void testGetEnumerationWithNonEnumerationParent() {
    EnumerationLiteral literal = new EnumerationLiteral();
    Language language = new Language(); // Not an Enumeration

    literal.setParent(language);
    assertThrows(IllegalStateException.class, () -> literal.getEnumeration());
  }

  @Test
  public void testToString() {
    EnumerationLiteral literal = new EnumerationLiteral();
    assertEquals("EnumerationLiteral[null]", literal.toString());

    literal.setID("test-id");
    assertEquals("EnumerationLiteral[test-id]", literal.toString());
  }

  @Test
  public void testAddLiteralToEnumeration() {
    Enumeration enumeration = new Enumeration("Colors");
    EnumerationLiteral red = new EnumerationLiteral("RED");
    EnumerationLiteral green = new EnumerationLiteral("GREEN");

    enumeration.addLiteral(red);
    enumeration.addLiteral(green);

    assertEquals(2, enumeration.getLiterals().size());
    assertTrue(enumeration.getLiterals().contains(red));
    assertTrue(enumeration.getLiterals().contains(green));
    assertEquals(enumeration, red.getEnumeration());
    assertEquals(enumeration, green.getEnumeration());
  }

  @Test
  public void testNamespaceQualifierThroughEnumeration() {
    Language language = new Language("TestLanguage");
    language.setID("lang-id");

    Enumeration enumeration = new Enumeration("Status");
    enumeration.setID("enum-id");
    language.addElement(enumeration);

    EnumerationLiteral literal = new EnumerationLiteral("ACTIVE");
    literal.setID("literal-id");
    enumeration.addLiteral(literal);

    // The enumeration should be properly connected to the language
    assertEquals(language, enumeration.getLanguage());
    assertEquals(enumeration, literal.getEnumeration());
  }
}
