package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

@Ignore // Ignoring the test as Annotation is still experimental and so is not yet reflected in
// LionCore
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
  public void getPropertyValuePlatformSpecific() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        null,
        annotation.getPropertyValue(
            LionCore.getAnnotation().getPropertyByName("platformSpecific")));

    annotation.setPlatformSpecific("java");
    assertEquals(
        "java",
        annotation.getPropertyValue(
            LionCore.getAnnotation().getPropertyByName("platformSpecific")));
  }

  @Test
  public void setPropertyValuePlatformSpecific() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    annotation.setPropertyValue(
        LionCore.getAnnotation().getPropertyByName("platformSpecific"), "java");
    assertEquals("java", annotation.getPlatformSpecific());
  }

  @Test
  public void getReferenceValueTarget() {
    Language language = new Language("mymm");
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        Arrays.asList(),
        annotation.getReferredNodes(LionCore.getAnnotation().getReferenceByName("target")));

    Concept myConcept = new Concept(language, "myc");
    annotation.setTarget(myConcept);
    assertEquals(
        Arrays.asList(myConcept),
        annotation.getReferredNodes(LionCore.getAnnotation().getReferenceByName("target")));
  }

  @Test
  public void setReferenceValueTarget() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");

    Concept myConcept = new Concept();
    annotation.addReferenceValue(
        LionCore.getAnnotation().getReferenceByName("target"), new ReferenceValue(myConcept, null));
    assertEquals(myConcept, annotation.getTarget());
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

  @Test
  public void getPropertyValueQualifiedName() {
    Language language = new Language("my.amazing.language");
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        "my.amazing.language.MyAnnotation",
        annotation.getPropertyValue(LionCore.getAnnotation().getPropertyByName("qualifiedName")));
  }

  @Test
  public void getPropertyValueNamespaceQualifier() {
    Language language = new Language("my.amazing.language");
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        "my.amazing.language.MyAnnotation",
        annotation.getPropertyValue(
            LionCore.getAnnotation().getPropertyByName("namespaceQualifier")));
  }
}
