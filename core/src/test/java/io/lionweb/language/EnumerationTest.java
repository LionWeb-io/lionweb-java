package io.lionweb.language;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import org.junit.jupiter.api.Test;

public class EnumerationTest {
  @Test
  public void literalParentIsEnum_Constructor() {
    Enumeration enm = new Enumeration();
    enm.setName("MyEnum");

    EnumerationLiteral lit = new EnumerationLiteral(enm, "Lit1", "my-id");

    assertSame(enm, lit.getParent());
    assertSame(enm, lit.getEnumeration());
  }

  @Test
  public void literalParentIsEnum_setParent() {
    Enumeration enm = new Enumeration();
    enm.setName("MyEnum");

    EnumerationLiteral lit = new EnumerationLiteral();
    lit.setParent(enm);

    assertSame(enm, lit.getParent());
    assertSame(enm, lit.getEnumeration());
  }

  @Test
  public void literalParentIsEnum_setEnumeration() {
    Enumeration enm = new Enumeration();
    enm.setName("MyEnum");

    EnumerationLiteral lit = new EnumerationLiteral();
    lit.setEnumeration(enm);

    assertSame(enm, lit.getParent());
    assertSame(enm, lit.getEnumeration());
  }

  @Test
  public void testConstructorWithLionWebVersionAndName() {
    Enumeration enumeration = new Enumeration(LionWebVersion.v2024_1, "Status");

    assertEquals("Status", enumeration.getName());
    assertEquals(LionWebVersion.v2024_1, enumeration.getLionWebVersion());
  }

  @Test
  public void testConstructorWithLionWebVersion() {
    Enumeration enumeration = new Enumeration(LionWebVersion.v2023_1);

    assertNull(enumeration.getName());
    assertEquals(LionWebVersion.v2023_1, enumeration.getLionWebVersion());
  }

  @Test
  public void testNamespaceQualifierThrowsUnsupportedOperation() {
    Enumeration enumeration = new Enumeration("TestEnum");

    assertThrows(UnsupportedOperationException.class, () -> enumeration.namespaceQualifier());
  }
}
