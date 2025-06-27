package com.example;

import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.utils.NodeTreeValidator;
import io.lionweb.utils.ValidationResult;
import java.util.List;

public class HomogeneousAPIExample {

  private static Concept taskListConcept;
  private static Concept taskConcept;
  private static Property nameProperty;
  private static Containment tasksContainment;
  private static Language taskLanguage;

  {
    defineLanguage();
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

  public void useDynamicNode() {
    // Create the model
    DynamicNode errands = new DynamicNode("errands", taskListConcept);

    DynamicNode task1 = new DynamicNode("task1-id", taskConcept);
    task1.setPropertyValue(nameProperty, "My Task #1");
    errands.addChild(tasksContainment, task1);

    DynamicNode task2 = new DynamicNode("task2-id", taskConcept);
    task2.setPropertyValue(nameProperty, "My Task #2");
    errands.addChild(tasksContainment, task2);

    ValidationResult res = new NodeTreeValidator().validate(errands);
    if (!res.isSuccessful()) {
      throw new IllegalStateException("The tree is invalid: " + res);
    }

    // Access the model
    List<Node> tasks = errands.getChildren(tasksContainment);
    System.out.println("Tasks found: " + tasks.size());
    for (Node task : tasks) {
      System.out.println(" - " + task.getPropertyValue(nameProperty));
    }

    // Access the model, using ClassifierInstanceUtils
    List<? extends Node> tasksAgain =
        ClassifierInstanceUtils.getChildrenByContainmentName(errands, "tasks");
    System.out.println("Tasks found: " + tasksAgain.size());
    for (Node task : tasksAgain) {
      System.out.println(" - " + ClassifierInstanceUtils.getPropertyValueByName(task, "name"));
    }
  }

  public static void main(String[] args) {
    new HomogeneousAPIExample().useDynamicNode();
  }
}
