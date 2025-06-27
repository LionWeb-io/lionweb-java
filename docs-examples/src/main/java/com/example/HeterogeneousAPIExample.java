package com.example;

import io.lionweb.language.*;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.utils.NodeTreeValidator;
import io.lionweb.utils.ValidationResult;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HeterogeneousAPIExample {

  private static Concept taskListConcept;
  private static Concept taskConcept;
  private static Property nameProperty;
  private static Containment tasksContainment;
  private static Language taskLanguage;

  {
    defineLanguage();
  }

  private class TaskList extends DynamicNode {
    TaskList() {
      super(UUID.randomUUID().toString(), taskListConcept);
    }

    void addTask(Task task) {
      addChild(tasksContainment, task);
    }

    List<Task> getTasks() {
      return getChildren(tasksContainment).stream().map(n -> (Task) n).collect(Collectors.toList());
    }
  }

  private class Task extends DynamicNode {
    Task(String name) {
      super(UUID.randomUUID().toString(), taskConcept);
      setName(name);
    }

    void setName(String name) {
      setPropertyValue(nameProperty, name);
    }

    String getName() {
      return (String) getPropertyValue(nameProperty);
    }
  }

  public static void defineLanguage() {
    // Define the 'TaskList' concept
    taskListConcept = new Concept("TaskList");
    taskListConcept.setID("TaskList-id");
    taskListConcept.setName("TaskList");
    taskListConcept.setKey("TaskList");
    taskListConcept.setAbstract(false);
    taskListConcept.setPartition(true);

    // Define the 'Task' concept
    taskConcept = new Concept("Task");
    taskConcept.setID("Task-id");
    taskConcept.setName("Task");
    taskConcept.setKey("Task");
    taskConcept.setAbstract(false);

    // Add a 'tasks' containment
    tasksContainment =
        new Containment()
            .setID("TasksList-tasks-id")
            .setName("tasks")
            .setKey("TasksList-tasks")
            .setMultiple(true)
            .setOptional(false)
            .setType(taskConcept);
    taskListConcept.addFeature(tasksContainment);

    // Add a 'name' property
    nameProperty = new Property();
    nameProperty.setID("task-name-id");
    nameProperty.setName("name");
    nameProperty.setKey("task-name");
    nameProperty.setType(LionCoreBuiltins.getString());
    taskConcept.addFeature(nameProperty);

    // Define the language container
    taskLanguage = new Language();
    taskLanguage.setID("task-id");
    taskLanguage.setKey("task");
    taskLanguage.setName("Task Language");
    taskLanguage.setVersion("1.0");
    taskLanguage.addElement(taskListConcept);
    taskLanguage.addElement(taskConcept);
  }

  public void useSpecificClasses() {
    // Create the model
    TaskList errands = new TaskList();

    Task task1 = new Task("My Task #1");
    errands.addTask(task1);

    Task task2 = new Task("My Task #2");
    errands.addTask(task2);

    ValidationResult res = new NodeTreeValidator().validate(errands);
    if (!res.isSuccessful()) {
      throw new IllegalStateException("The tree is invalid: " + res);
    }

    // Access the model
    List<Task> tasks = errands.getTasks();
    System.out.println("Tasks found: " + tasks.size());
    for (Task task : tasks) {
      System.out.println(" - " + task.getName());
    }
  }

  public static void main(String[] args) {
    new HeterogeneousAPIExample().useSpecificClasses();
  }
}
