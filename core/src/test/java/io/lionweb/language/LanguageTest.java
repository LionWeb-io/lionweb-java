package io.lionweb.language;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import java.util.Arrays;
import java.util.Collections;
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
    Annotation a1inM2 = new Annotation(l2, "A1");

    Language l3 = new Language("l3");
    Annotation a2inM3 = new Annotation(l3, "A2");

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
}
