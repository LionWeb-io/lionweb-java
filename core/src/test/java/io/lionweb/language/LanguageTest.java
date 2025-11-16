package io.lionweb.language;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

import io.lionweb.LionWebVersion;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.lioncore.LionCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class LanguageTest {

  @Test
  public void addDependency() {
    Language l1 = new Language("l1");
    Language l2 = new Language("l2");
    Language l3 = new Language("l3");

    assertEquals(Collections.emptyList(), l1.dependsOn());
    l1.addDependency(l2);
    assertEquals(Arrays.asList(l2), l1.dependsOn());
    l1.addDependency(l3);
    assertEquals(Arrays.asList(l2, l3), l1.dependsOn());
  }

  @Test
  public void getAnnotationByName() {
    Language l1 = new Language("l1");

    Language l2 = new Language("l2");
    Annotation a1inM2 = new Annotation(l2, "A1", "my-id1");

    Language l3 = new Language("l3");
    Annotation a2inM3 = new Annotation(l3, "A2", "my-id2");

    assertNull(l1.getAnnotationByName("A1"));
    assertNull(l1.getAnnotationByName("A2"));

    assertEquals(a1inM2, l2.getAnnotationByName("A1"));
    assertNull(l2.getAnnotationByName("A2"));

    assertNull(l3.getAnnotationByName("A1"));
    assertEquals(a2inM3, l3.getAnnotationByName("A2"));
  }

  @Test
  public void languageCreation() {
    // Define the 'TaskList' concept
    Concept taskListConcept = new Concept("TaskList").setPartition();

    // Define the 'Task' concept
    Concept taskConcept = new Concept("Task");

    // Add a 'tasks' containment
    Containment tasksContainment =
        new Containment().setName("tasks").makeOneToMany().setType(taskConcept);
    taskListConcept.addFeature(tasksContainment);

    // Add a 'name' property
    Property nameProperty = new Property().setName("name").setType(LionCoreBuiltins.getString());
    taskConcept.addFeature(nameProperty);

    // Define the language container
    Language taskLanguage = new Language().setName("Task Language").setVersion("1.0");
    taskLanguage.addElement(taskListConcept);
    taskLanguage.addElement(taskConcept);

    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(taskLanguage);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(taskLanguage);

    assertEquals("Task-Language-TaskList", taskListConcept.getID());
    assertEquals("TaskList", taskListConcept.getKey());
    assertEquals("TaskList", taskListConcept.getName());
    assertFalse(taskListConcept.isAbstract());
    assertTrue(taskListConcept.isPartition());

    assertEquals("Task", taskConcept.getName());
    assertEquals("Task-Language-Task", taskConcept.getID());
    assertEquals("Task", taskConcept.getName());
    assertFalse(taskConcept.isAbstract());

    assertEquals("tasks", tasksContainment.getName());
    assertEquals("Task-Language-TaskList-tasks", tasksContainment.getID());
    assertEquals("TaskList-tasks", tasksContainment.getKey());
    assertFalse(tasksContainment.isOptional());
    assertTrue(tasksContainment.isMultiple());

    assertEquals("name", nameProperty.getName());
    assertEquals("Task-Language-Task-name", nameProperty.getID());
    assertEquals("Task-name", nameProperty.getKey());
    assertEquals(LionCoreBuiltins.getString(), nameProperty.getType());
    assertFalse(nameProperty.isOptional());

    assertEquals("Task Language", taskLanguage.getName());
    assertEquals("Task-Language", taskLanguage.getID());
    assertEquals("Task-Language", taskLanguage.getKey());
    assertEquals("1.0", taskLanguage.getVersion());
  }

  @Test
  public void getClassifierByNamePositive() {
    Language language = new Language("TestLanguage");
    Concept concept = new Concept(language, "MyConcept", "c-id");
    language.addElement(concept);

    Classifier<?> result = language.getClassifierByName("MyConcept");

    assertNotNull(result);
    assertEquals(concept, result);
  }

  @Test
  public void getClassifierByNameUnknownReturnsNull() {
    Language language = new Language("TestLanguage");

    assertNull(language.getClassifierByName("UnknownConcept"));
  }

  @Test
  public void getClassifierByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.getClassifierByName(null));
  }

  @Test
  public void requireClassifierByNamePositive() {
    Language language = new Language("TestLanguage");
    Concept concept = new Concept(language, "MyConcept", "c-id");
    language.addElement(concept);

    Classifier<?> result = language.requireClassifierByName("MyConcept");

    assertEquals(concept, result);
  }

  @Test
  public void requireClassifierByNameUnknownThrows() {
    Language language = new Language("TestLanguage");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> language.requireClassifierByName("UnknownConcept"));
    assertEquals("Classifier named UnknownConcept was not found", ex.getMessage());
  }

  @Test
  public void requireClassifierByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.requireClassifierByName(null));
  }

  @Test
  public void getEnumerationByNamePositive() {
    Language language = new Language("TestLanguage");
    Enumeration enumeration = new Enumeration(language, "MyEnum", "e-id");
    language.addElement(enumeration);

    Enumeration result = language.getEnumerationByName("MyEnum");

    assertNotNull(result);
    assertEquals(enumeration, result);
  }

  @Test
  public void getEnumerationByNameUnknownReturnsNull() {
    Language language = new Language("TestLanguage");

    assertNull(language.getEnumerationByName("UnknownEnum"));
  }

  @Test
  public void requireConceptByNamePositive() {
    Language language = new Language("TestLanguage");
    Concept concept = new Concept(language, "MyConcept", "c-id");
    language.addElement(concept);

    Concept result = language.requireConceptByName("MyConcept");

    assertEquals(concept, result);
  }

  @Test
  public void requireConceptByNameUnknownThrows() {
    Language language = new Language("TestLanguage");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> language.requireConceptByName("UnknownConcept"));
    assertEquals("Concept named UnknownConcept was not found", ex.getMessage());
  }

  @Test
  public void getInterfaceByNameAndRequireInterfaceByNamePositive() {
    Language language = new Language("TestLanguage");
    Interface iface = new Interface(language, "MyInterface", "i-id");
    language.addElement(iface);

    Interface byName = language.getInterfaceByName("MyInterface");
    Interface required = language.requireInterfaceByName("MyInterface");

    assertNotNull(byName);
    assertEquals(iface, byName);
    assertEquals(iface, required);
  }

  @Test
  public void requireInterfaceByNameUnknownThrows() {
    Language language = new Language("TestLanguage");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> language.requireInterfaceByName("UnknownInterface"));
    assertEquals("Interface named UnknownInterface was not found", ex.getMessage());
  }

  @Test
  public void requireInterfaceByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.requireInterfaceByName(null));
  }

  @Test
  public void getAnnotationByNameAndRequireAnnotationByNamePositive() {
    Language language = new Language("TestLanguage");
    Annotation annotation = new Annotation(language, "MyAnnotation", "a-id");
    language.addElement(annotation);

    Annotation byName = language.getAnnotationByName("MyAnnotation");
    Annotation required = language.requireAnnotationByName("MyAnnotation");

    assertNotNull(byName);
    assertEquals(annotation, byName);
    assertEquals(annotation, required);
  }

  @Test
  public void requireAnnotationByNameUnknownThrows() {
    Language language = new Language("TestLanguage");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> language.requireAnnotationByName("UnknownAnnotation"));
    assertEquals("Annotation named UnknownAnnotation was not found", ex.getMessage());
  }

  @Test
  public void requireAnnotationByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.requireAnnotationByName(null));
  }

  @Test
  public void getPrimitiveTypeByNamePositive() {
    Language language = new Language("TestLanguage");
    PrimitiveType intType = new PrimitiveType(language, "IntType", "p-id");
    language.addElement(intType);

    PrimitiveType result = language.getPrimitiveTypeByName("IntType");

    assertNotNull(result);
    assertEquals(intType, result);
  }

  @Test
  public void getPrimitiveTypeByNameUnknownReturnsNull() {
    Language language = new Language("TestLanguage");

    assertNull(language.getPrimitiveTypeByName("UnknownPrimitive"));
  }

  @Test
  public void getPrimitiveTypeByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.getPrimitiveTypeByName(null));
  }

  @Test
  public void getPrimitiveTypeByNameNonPrimitiveSameNameThrowsRuntimeException() {
    Language language = new Language("TestLanguage");
    // Add a Concept with the same name; this is not a PrimitiveType and should trigger the error
    Concept concept = new Concept(language, "SomeName", "c-id");
    language.addElement(concept);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> language.getPrimitiveTypeByName("SomeName"));
    assertEquals("Element SomeName is not a PrimitiveType", ex.getMessage());
  }

  @Test
  public void requirePrimitiveTypeByNamePositive() {
    Language language = new Language("TestLanguage");
    PrimitiveType stringType = new PrimitiveType(language, "StringType", "p-id");
    language.addElement(stringType);

    PrimitiveType result = language.requirePrimitiveTypeByName("StringType");

    assertEquals(stringType, result);
  }

  @Test
  public void requirePrimitiveTypeByNameUnknownThrows() {
    Language language = new Language("TestLanguage");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> language.requirePrimitiveTypeByName("UnknownPrimitive"));
    assertEquals("PrimitiveType named UnknownPrimitive was not found", ex.getMessage());
  }

  @Test
  public void requirePrimitiveTypeByNameNullNameThrows() {
    Language language = new Language("TestLanguage");

    assertThrows(NullPointerException.class, () -> language.requirePrimitiveTypeByName(null));
  }

  @Test
  public void getPrimitiveTypesReturnsOnlyPrimitiveTypes() {
    Language language = new Language("TestLanguage");
    PrimitiveType intType = new PrimitiveType(language, "IntType", "p1");
    PrimitiveType stringType = new PrimitiveType(language, "StringType", "p2");
    Concept concept = new Concept(language, "SomeConcept", "c-id");
    Enumeration enumeration = new Enumeration(language, "SomeEnum", "e-id");

    language.addElement(intType);
    language.addElement(stringType);
    language.addElement(concept);
    language.addElement(enumeration);

    List<PrimitiveType> primitiveTypes = language.getPrimitiveTypes();

    assertEquals(2, primitiveTypes.size());
    assertTrue(primitiveTypes.contains(intType));
    assertTrue(primitiveTypes.contains(stringType));
  }

  @Test
  public void isValidReturnsTrueForBuiltInLanguage() {
    // The built-in LionCore language is expected to be valid
    Language lionCoreLanguage = LionCore.getInstance(LionWebVersion.currentVersion);

    assertTrue(lionCoreLanguage.isValid());
  }
}
