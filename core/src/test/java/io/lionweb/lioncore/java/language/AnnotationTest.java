package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import org.junit.Test;

public class AnnotationTest extends BaseTest {

  @Test
  public void getPropertyValueName() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(
        "MyAnnotation",
        annotation.getPropertyValue(LionCore.getAnnotation().getPropertyByName("name")));
  }

  @Test
  public void setPropertyValueName() {
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
        LionCore.getAnnotation().getReferenceByName("annotates"),
        new ReferenceValue(myConcept, null));
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

  @Test
  public void isMultiple() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation");
    assertEquals(false, annotation.isMultiple());
    annotation.setMultiple(true);
    assertEquals(true, annotation.isMultiple());
    annotation.setMultiple(false);
    assertEquals(false, annotation.isMultiple());
  }

  @Test
  public void annotates() {
    Language language = new Language("LangFoo", "lf", "lf");
    Concept myConcept = new Concept(language, "MyConcept", "c", "c");
    Annotation otherAnnotation = new Annotation(language, "OtherAnnotation", "oa", "oa");
    otherAnnotation.setAnnotates(myConcept);
    Annotation superAnnotation = new Annotation(language, "SuperAnnotation", "sa", "sa");
    superAnnotation.setAnnotates(myConcept);
    ConceptInterface myCI = new ConceptInterface(language, "MyCI", "ci", "ci");

    Annotation annotation = new Annotation(language, "MyAnnotation", "MyAnnotation-ID", "ma");
    assertEquals(null, annotation.getAnnotates());
    // From the node point of view the annotation is correct even if annotates is empty, because
    // it can be sometimes, if the annotation is inherited or inheriting and the parent or
    // sub-annotation
    // mark the annotation as valid
    assertNodeTreeIsValid(annotation);
    assertLanguageIsNotValid(language);

    annotation.setAnnotates(myConcept);
    assertEquals(myConcept, annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);

    annotation.setAnnotates(myCI);
    assertEquals(myCI, annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);

    annotation.setAnnotates(otherAnnotation);
    assertEquals(otherAnnotation, annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);

    annotation.setAnnotates(null);
    assertEquals(null, annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsNotValid(language);

    annotation.setExtendedAnnotation(superAnnotation);
    assertEquals(myConcept, annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);
  }

  @Test
  public void containmentLinks() {
    Language language = new Language("LangFoo", "lf", "lf");
    Concept myConcept = new Concept(language, "MyConcept", "c", "c");

    Annotation annotation = new Annotation(language, "MyAnnotation", "MyAnnotation-ID", "ma");
    annotation.setAnnotates(myConcept);
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);

    annotation.addFeature(Containment.createOptional("cont", myConcept, "cont"));
    assertNodeTreeIsValid(annotation);
    assertLanguageIsNotValid(language);
  }
}
