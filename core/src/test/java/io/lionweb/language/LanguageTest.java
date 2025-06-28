package io.lionweb.language;

import static org.junit.Assert.*;

import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class LanguageTest {

  @Test
  public void addDependency() {
    Language m1 = new Language("m1");
    Language m2 = new Language("m2");
    Language m3 = new Language("m3");

    assertEquals(Collections.emptyList(), m1.dependsOn());
    m1.addDependency(m2);
    assertEquals(Arrays.asList(m2), m1.dependsOn());
    m1.addDependency(m3);
    assertEquals(Arrays.asList(m2, m3), m1.dependsOn());
  }

  @Test
  public void languageCreation() {
    // Define the 'TaskList' concept
    Concept taskListConcept = new Concept("TaskList").makePartition();

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
