package io.lionweb.language;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.lioncore.LionCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

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

  // ========== getDataTypeByName() Tests ==========

  @Test
  public void getDataTypeByNamePrimitiveType() {
    Language language = new Language("TestLanguage");
    PrimitiveType stringType = new PrimitiveType(language, "String", "string-id");
    language.addElement(stringType);

    DataType<?> result = language.getDataTypeByName("String");

    assertNotNull(result);
    assertEquals(stringType, result);
    assertEquals("String", result.getName());
  }

  @Test
  public void getDataTypeByNameEnumeration() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    language.addElement(statusEnum);

    DataType<?> result = language.getDataTypeByName("Status");

    assertNotNull(result);
    assertEquals(statusEnum, result);
    assertTrue(result instanceof Enumeration);
  }

  @Test
  public void getDataTypeByNameStructuredDataType() {
    Language language = new Language("TestLanguage");
    StructuredDataType addressType = new StructuredDataType(language, "Address", "address-id");
    language.addElement(addressType);

    DataType<?> result = language.getDataTypeByName("Address");

    assertNotNull(result);
    assertEquals(addressType, result);
    assertTrue(result instanceof StructuredDataType);
  }

  @Test
  public void getDataTypeByNameNotFound() {
    Language language = new Language("TestLanguage");

    DataType<?> result = language.getDataTypeByName("NonExistent");

    assertNull(result);
  }

  @Test
  public void getDataTypeByNameElementIsNotDataType() {
    Language language = new Language("TestLanguage");
    Concept concept = new Concept(language, "Person", "person-id");
    language.addElement(concept);

    try {
      language.getDataTypeByName("Person");
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("is not a DataType"));
      assertTrue(e.getMessage().contains("Person"));
    }
  }

  @Test
  public void getDataTypeByNameElementIsInterface() {
    Language language = new Language("TestLanguage");
    Interface namedInterface = new Interface(language, "Named", "named-id");
    language.addElement(namedInterface);

    try {
      language.getDataTypeByName("Named");
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("is not a DataType"));
    }
  }

  @Test
  public void getDataTypeByNameNullName() {
    Language language = new Language("TestLanguage");
    assertThrows(NullPointerException.class, () -> language.getDataTypeByName(null));
  }

  @Test
  public void getDataTypeByNameEmptyName() {
    Language language = new Language("TestLanguage");

    DataType<?> result = language.getDataTypeByName("");

    assertNull(result);
  }

  @Test
  public void getDataTypeByNameMultiplePrimitiveTypes() {
    Language language = new Language("TestLanguage");
    PrimitiveType stringType = new PrimitiveType(language, "String", "string-id");
    PrimitiveType intType = new PrimitiveType(language, "Integer", "int-id");
    language.addElement(stringType);
    language.addElement(intType);

    assertEquals(stringType, language.getDataTypeByName("String"));
    assertEquals(intType, language.getDataTypeByName("Integer"));
  }

  @Test
  public void getDataTypeByNameCaseSensitive() {
    Language language = new Language("TestLanguage");
    PrimitiveType stringType = new PrimitiveType(language, "String", "string-id");
    language.addElement(stringType);

    assertNotNull(language.getDataTypeByName("String"));
    assertNull(language.getDataTypeByName("string"));
    assertNull(language.getDataTypeByName("STRING"));
  }

  // ========== requireDataTypeByName() Tests ==========

  @Test
  public void requireDataTypeByNamePrimitiveType() {
    Language language = new Language("TestLanguage");
    PrimitiveType intType = new PrimitiveType(language, "Integer", "int-id");
    language.addElement(intType);

    DataType<?> result = language.requireDataTypeByName("Integer");

    assertNotNull(result);
    assertEquals(intType, result);
    assertEquals("Integer", result.getName());
  }

  @Test
  public void requireDataTypeByNameEnumeration() {
    Language language = new Language("TestLanguage");
    Enumeration colorEnum = new Enumeration(language, "Color", "color-id");
    language.addElement(colorEnum);

    DataType<?> result = language.requireDataTypeByName("Color");

    assertNotNull(result);
    assertEquals(colorEnum, result);
    assertTrue(result instanceof Enumeration);
  }

  @Test
  public void requireDataTypeByNameStructuredDataType() {
    Language language = new Language("TestLanguage");
    StructuredDataType positionType = new StructuredDataType(language, "Position", "position-id");
    language.addElement(positionType);

    DataType<?> result = language.requireDataTypeByName("Position");

    assertNotNull(result);
    assertEquals(positionType, result);
  }

  @Test
  public void requireDataTypeByNameNotFound() {
    Language language = new Language("TestLanguage");

    try {
      language.requireDataTypeByName("NonExistent");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("DataType named NonExistent was not found", e.getMessage());
    }
  }

  @Test
  public void requireDataTypeByNameNullName() {
    Language language = new Language("TestLanguage");
    assertThrows(NullPointerException.class, () -> language.requireDataTypeByName(null));
  }

  @Test
  public void requireDataTypeByNameNullNameMessage() {
    Language language = new Language("TestLanguage");

    try {
      language.requireDataTypeByName(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("name should not be null", e.getMessage());
    }
  }

  @Test
  public void requireDataTypeByNameEmptyName() {
    Language language = new Language("TestLanguage");

    try {
      language.requireDataTypeByName("");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("DataType named  was not found"));
    }
  }

  @Test
  public void requireDataTypeByNameThrowsOnConcept() {
    Language language = new Language("TestLanguage");
    Concept concept = new Concept(language, "Company", "company-id");
    language.addElement(concept);

    try {
      language.requireDataTypeByName("Company");
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("is not a DataType"));
    }
  }

  @Test
  public void requireDataTypeByNameThrowsOnInterface() {
    Language language = new Language("TestLanguage");
    Interface entityInterface = new Interface(language, "Entity", "entity-id");
    language.addElement(entityInterface);

    try {
      language.requireDataTypeByName("Entity");
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("is not a DataType"));
    }
  }

  @Test
  public void requireDataTypeByNameVerifyDifferenceFromGet() {
    Language language = new Language("TestLanguage");

    // requireDataTypeByName should throw when not found
    try {
      language.requireDataTypeByName("Missing");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // getDataTypeByName should return null when not found
    assertNull(language.getDataTypeByName("Missing"));
  }

  // ========== getEnumerations() Tests ==========

  @Test
  public void getEnumerationsEmptyLanguage() {
    Language language = new Language("TestLanguage");

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
  }

  @Test
  public void getEnumerationsSingleEnumeration() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    language.addElement(statusEnum);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(statusEnum, result.get(0));
    assertEquals("Status", result.get(0).getName());
  }

  @Test
  public void getEnumerationsMultipleEnumerations() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    Enumeration colorEnum = new Enumeration(language, "Color", "color-id");
    Enumeration priorityEnum = new Enumeration(language, "Priority", "priority-id");
    language.addElement(statusEnum);
    language.addElement(colorEnum);
    language.addElement(priorityEnum);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(result.contains(statusEnum));
    assertTrue(result.contains(colorEnum));
    assertTrue(result.contains(priorityEnum));
  }

  @Test
  public void getEnumerationsMixedWithOtherElements() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    Concept personConcept = new Concept(language, "Person", "person-id");
    Interface namedInterface = new Interface(language, "Named", "named-id");
    PrimitiveType stringType = new PrimitiveType(language, "String", "string-id");
    language.addElement(statusEnum);
    language.addElement(personConcept);
    language.addElement(namedInterface);
    language.addElement(stringType);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(statusEnum, result.get(0));
  }

  @Test
  public void getEnumerationsNoConcepts() {
    Language language = new Language("TestLanguage");
    Concept personConcept = new Concept(language, "Person", "person-id");
    Concept companyConcept = new Concept(language, "Company", "company-id");
    Interface namedInterface = new Interface(language, "Named", "named-id");
    language.addElement(personConcept);
    language.addElement(companyConcept);
    language.addElement(namedInterface);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    assertEquals(0, result.size());
  }

  @Test
  public void getEnumerationsPreservesOrder() {
    Language language = new Language("TestLanguage");
    Enumeration firstEnum = new Enumeration(language, "First", "first-id");
    Enumeration secondEnum = new Enumeration(language, "Second", "second-id");
    Enumeration thirdEnum = new Enumeration(language, "Third", "third-id");
    language.addElement(firstEnum);
    language.addElement(secondEnum);
    language.addElement(thirdEnum);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(firstEnum, result.get(0));
    assertEquals(secondEnum, result.get(1));
    assertEquals(thirdEnum, result.get(2));
  }

  @Test
  public void getEnumerationsWithAllDataTypes() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    PrimitiveType intType = new PrimitiveType(language, "Integer", "int-id");
    StructuredDataType addressType = new StructuredDataType(language, "Address", "address-id");
    language.addElement(statusEnum);
    language.addElement(intType);
    language.addElement(addressType);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(statusEnum, result.get(0));
  }

  @Test
  public void getEnumerationsReturnsDifferentListInstance() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    language.addElement(statusEnum);

    List<Enumeration> result1 = language.getEnumerations();
    List<Enumeration> result2 = language.getEnumerations();

    assertNotSame(result1, result2);
    assertEquals(result1.size(), result2.size());
    assertEquals(result1.get(0), result2.get(0));
  }

  @Test
  public void getEnumerationsResultIsModifiable() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    language.addElement(statusEnum);

    List<Enumeration> result = language.getEnumerations();
    result.clear();
    assertEquals(0, result.size());

    // Original language should still have the enumeration
    assertEquals(1, language.getEnumerations().size());
  }

  @Test
  public void getEnumerationsWithLiterals() {
    Language language = new Language("TestLanguage");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");

    EnumerationLiteral activeLiteral = new EnumerationLiteral(language.getLionWebVersion());
    activeLiteral.setName("ACTIVE");
    activeLiteral.setID("active-id");
    statusEnum.addLiteral(activeLiteral);

    EnumerationLiteral inactiveLiteral = new EnumerationLiteral(language.getLionWebVersion());
    inactiveLiteral.setName("INACTIVE");
    inactiveLiteral.setID("inactive-id");
    statusEnum.addLiteral(inactiveLiteral);

    language.addElement(statusEnum);

    List<Enumeration> result = language.getEnumerations();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(2, result.get(0).getLiterals().size());
  }

  // ========== Integration Tests ==========

  @Test
  public void dataTypeMethodsIntegrationAllDataTypes() {
    Language language = new Language("TestLanguage");
    PrimitiveType boolType = new PrimitiveType(language, "Boolean", "bool-id");
    Enumeration statusEnum = new Enumeration(language, "Status", "status-id");
    StructuredDataType positionType = new StructuredDataType(language, "Position", "position-id");
    language.addElement(boolType);
    language.addElement(statusEnum);
    language.addElement(positionType);

    assertEquals(boolType, language.getDataTypeByName("Boolean"));
    assertEquals(statusEnum, language.getDataTypeByName("Status"));
    assertEquals(positionType, language.getDataTypeByName("Position"));

    assertEquals(boolType, language.requireDataTypeByName("Boolean"));
    assertEquals(statusEnum, language.requireDataTypeByName("Status"));
    assertEquals(positionType, language.requireDataTypeByName("Position"));

    List<Enumeration> enumerations = language.getEnumerations();
    assertEquals(1, enumerations.size());
    assertEquals(statusEnum, enumerations.get(0));
  }

  @Test
  public void dataTypeMethodsIntegrationMultipleEnumerations() {
    Language language = new Language("TestLanguage");
    Enumeration enum1 = new Enumeration(language, "Color", "color-id");
    Enumeration enum2 = new Enumeration(language, "Priority", "priority-id");
    language.addElement(enum1);
    language.addElement(enum2);

    assertEquals(enum1, language.getDataTypeByName("Color"));
    assertEquals(enum2, language.getDataTypeByName("Priority"));

    List<Enumeration> enumerations = language.getEnumerations();
    assertEquals(2, enumerations.size());
    assertTrue(enumerations.contains(enum1));
    assertTrue(enumerations.contains(enum2));
  }

  @Test
  public void dataTypeMethodsIntegrationNotFoundScenarios() {
    Language language = new Language("TestLanguage");
    PrimitiveType stringType = new PrimitiveType(language, "String", "string-id");
    language.addElement(stringType);

    assertNull(language.getDataTypeByName("NonExistent"));
    try {
      language.requireDataTypeByName("NonExistent");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    assertNotNull(language.getDataTypeByName("String"));
    assertNotNull(language.requireDataTypeByName("String"));
  }
}
