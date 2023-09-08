package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;

import org.junit.Test;

public class AnnotationTest {

  @Test
  public void getPropertyValuename() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        "MyAnnotation",
        annotation.getPropertyValue(LionCore.getAnnotation().getPropertyByName("name")));
  }

  @Test
  public void setPropertyValuename() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    annotation.setPropertyValue(
        LionCore.getAnnotation().getPropertyByName("name"), "MyAmazingAnnotation");
    assertEquals("MyAmazingAnnotation", annotation.getName());
  }

  @Test
  public void getReferenceValueTarget() {
    Language language = new Language("mymm");
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        Arrays.asList(),
        annotation.getReferredNodes(LionCore.getAnnotation().getReferenceByName("annotates")));

    Concept myConcept = new Concept(language, "myc");
    annotation.setAnnotates(myConcept);
    assertEquals(
        Arrays.asList(myConcept),
        annotation.getReferredNodes(LionCore.getAnnotation().getReferenceByName("annotates")));
  }

  @Test
  public void setReferenceValueTarget() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");

    Concept myConcept = new Concept();
    annotation.addReferenceValue(
        LionCore.getAnnotation().getReferenceByName("annotates"), new ReferenceValue(myConcept, null));
    assertEquals(myConcept, annotation.getAnnotates());
  }

  @Test
  public void getPropertyValueFeatures() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        Arrays.asList(),
        annotation.getChildren(LionCore.getAnnotation().getContainmentByName("features")));

    Property property = new Property();
    annotation.addFeature(property);
    assertEquals(
        Arrays.asList(property),
        annotation.getChildren(LionCore.getAnnotation().getContainmentByName("features")));
  }

}
