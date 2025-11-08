package io.lionweb.language;

import static org.junit.Assert.*;

import io.lionweb.LionWebVersion;
import org.junit.Test;

public class ContainmentTest {

  @Test
  public void testMakeZeroToMany() {
    Containment containment = new Containment("children");

    Containment result = containment.makeZeroToMany();

    assertTrue(result.isOptional());
    assertTrue(result.isMultiple());
    assertSame(containment, result); // Should return self for fluent interface
  }

  @Test
  public void testMakeOneToMany() {
    Containment containment = new Containment("children");

    Containment result = containment.makeOneToMany();

    assertFalse(result.isOptional());
    assertTrue(result.isMultiple());
    assertSame(containment, result); // Should return self for fluent interface
  }

  @Test
  public void testMakeZeroToOne() {
    Containment containment = new Containment("child");

    Containment result = containment.makeZeroToOne();

    assertTrue(result.isOptional());
    assertFalse(result.isMultiple());
    assertTrue(result.isSingle());
    assertSame(containment, result); // Should return self for fluent interface
  }

  @Test
  public void testMakeExactlyOne() {
    Containment containment = new Containment("child");

    Containment result = containment.makeExactlyOne();

    assertFalse(result.isOptional());
    assertFalse(result.isMultiple());
    assertTrue(result.isSingle());
    assertSame(containment, result); // Should return self for fluent interface
  }

  @Test
  public void testCardinalityChanges() {
    Containment containment = new Containment("test");

    // Start with default settings
    assertFalse(containment.isOptional());
    assertFalse(containment.isMultiple());

    // Change to zero-to-many
    containment.makeZeroToMany();
    assertTrue(containment.isOptional());
    assertTrue(containment.isMultiple());

    // Change to one-to-many
    containment.makeOneToMany();
    assertFalse(containment.isOptional());
    assertTrue(containment.isMultiple());

    // Change to zero-to-one
    containment.makeZeroToOne();
    assertTrue(containment.isOptional());
    assertFalse(containment.isMultiple());

    // Change to exactly-one
    containment.makeExactlyOne();
    assertFalse(containment.isOptional());
    assertFalse(containment.isMultiple());
  }

  @Test
  public void testDefaultConstructor() {
    Containment containment = new Containment();

    assertNull(containment.getName());
    assertFalse(containment.isOptional());
    assertFalse(containment.isMultiple());
    assertTrue(containment.isSingle());
  }

  @Test
  public void testConstructorWithName() {
    Containment containment = new Containment("testContainment");

    assertEquals("testContainment", containment.getName());
    assertFalse(containment.isOptional());
    assertFalse(containment.isMultiple());
  }

  @Test
  public void testConstructorWithLionWebVersionAndName() {
    Containment containment = new Containment(LionWebVersion.v2024_1, "testContainment");

    assertEquals("testContainment", containment.getName());
    assertEquals(LionWebVersion.v2024_1, containment.getLionWebVersion());
    assertFalse(containment.isOptional());
    assertFalse(containment.isMultiple());
  }

  @Test
  public void testStaticFactoryMethods() {
    Concept targetType = new Concept("TargetConcept");

    // Test createOptional
    Containment optional = Containment.createOptional("optional", targetType);
    assertTrue(optional.isOptional());
    assertFalse(optional.isMultiple());
    assertEquals(targetType, optional.getType());

    // Test createRequired
    Containment required = Containment.createRequired("required", targetType);
    assertFalse(required.isOptional());
    assertFalse(required.isMultiple());
    assertEquals(targetType, required.getType());

    // Test createMultiple
    Containment multiple = Containment.createMultiple("multiple", targetType);
    assertTrue(multiple.isOptional());
    assertTrue(multiple.isMultiple());
    assertEquals(targetType, multiple.getType());

    // Test createMultipleAndRequired
    Containment multipleRequired =
        Containment.createMultipleAndRequired("multipleRequired", targetType);
    assertFalse(multipleRequired.isOptional());
    assertTrue(multipleRequired.isMultiple());
    assertEquals(targetType, multipleRequired.getType());
  }

  @Test
  public void testFluentInterfaceChaining() {
    Concept targetType = new Concept("Target");

    Containment containment = new Containment("test").setType(targetType).makeOneToMany();

    assertEquals("test", containment.getName());
    assertEquals(targetType, containment.getType());
    assertFalse(containment.isOptional());
    assertTrue(containment.isMultiple());
  }

  @Test
  public void testIsSingleMethod() {
    Containment containment = new Containment("test");

    // Initially single (not multiple)
    assertTrue(containment.isSingle());

    // Make it multiple
    containment.makeOneToMany();
    assertFalse(containment.isSingle());

    // Make it single again
    containment.makeExactlyOne();
    assertTrue(containment.isSingle());
  }
}
