package io.lionweb.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.lionweb.language.assigners.CommonIDAssigners;
import io.lionweb.language.assigners.CommonKeyAssigners;
import io.lionweb.lioncore.LionCore;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.ReferenceValue;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AnnotationTest extends BaseTest {

  @Test
  public void getPropertyValueName() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation", "my-id");
    assertEquals(
        "MyAnnotation",
        annotation.getPropertyValue(LionCore.getAnnotation().getPropertyByName("name")));
  }

  @Test
  public void setPropertyValueName() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation", "my-id");
    annotation.setPropertyValue(
        LionCore.getAnnotation().getPropertyByName("name"), "MyAmazingAnnotation");
    assertEquals("MyAmazingAnnotation", annotation.getName());
  }

  @Test
  public void getReferenceValueTarget() {
    Language language = new Language("mymm");
    Annotation annotation = new Annotation(language, "MyAnnotation", "my-id");
    assertEquals(
        Arrays.asList(),
        ClassifierInstanceUtils.getReferredNodes(
            annotation, LionCore.getAnnotation().getReferenceByName("annotates")));

    Concept myConcept = new Concept(language, "myc", "my-id2");
    annotation.setAnnotates(myConcept);
    assertEquals(
        Arrays.asList(myConcept),
        ClassifierInstanceUtils.getReferredNodes(
            annotation, LionCore.getAnnotation().getReferenceByName("annotates")));
  }

  @Test
  public void setReferenceValueTarget() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation", "my-id");

    Concept myConcept = new Concept();
    annotation.addReferenceValue(
        LionCore.getAnnotation().getReferenceByName("annotates"),
        new ReferenceValue(myConcept, null));
    assertEquals(myConcept, annotation.getAnnotates());
  }

  @Test
  public void getPropertyValueFeatures() {
    Language language = new Language();
    Annotation annotation = new Annotation(language, "MyAnnotation", "my-id");
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
  public void annotates() {
    Language language = new Language("LangFoo", "lf", "lf");
    Concept myConcept = new Concept(language, "MyConcept", "c", "c");
    Annotation otherAnnotation = new Annotation(language, "OtherAnnotation", "oa", "oa");
    otherAnnotation.setAnnotates(myConcept);
    Annotation superAnnotation = new Annotation(language, "SuperAnnotation", "sa", "sa");
    superAnnotation.setAnnotates(myConcept);
    Interface myCI = new Interface(language, "MyCI", "ci", "ci");

    Annotation annotation = new Annotation(language, "MyAnnotation", "MyAnnotation-ID", "ma");
    assertNull(annotation.getAnnotates());
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
    assertNull(annotation.getAnnotates());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsNotValid(language);

    annotation.setExtendedAnnotation(superAnnotation);
    assertEquals(null, annotation.getAnnotates());
    assertEquals(myConcept, annotation.getEffectivelyAnnotated());
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);
  }

  @Test
  public void containmentLinks() {
    Language language = new Language("LangFoo", "lf", "lf").setVersion("1");
    Concept myConcept = new Concept(language, "MyConcept", "c", "c");

    Annotation annotation = new Annotation(language, "MyAnnotation", "MyAnnotation-ID", "ma");
    annotation.setAnnotates(myConcept);
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);

    annotation.addFeature(Containment.createOptional("cont", myConcept, "cont", "cont-key"));
    assertNodeTreeIsValid(annotation);
    assertLanguageIsValid(language);
  }

  @Test
  public void inheritedFeatures() {
    Language language = new Language("LangFoo").setVersion("1");
    Annotation ann1 = new Annotation(language, "Ann1", "my-id1");
    ann1.addProperty("p1", LionCoreBuiltins.getString());
    Annotation ann2 = new Annotation(language, "Ann2", "my-id2").setExtendedAnnotation(ann1);
    ann2.addProperty("p2", LionCoreBuiltins.getInteger());

    CommonKeyAssigners.qualifiedKeyAssigner.assignKeys(language);
    CommonIDAssigners.qualifiedIDAssigner.assignIDs(language);

    assertEquals(1, ann1.getFeatures().size());
    assertEquals(1, ann2.getFeatures().size());
    assertEquals(1, ann1.allFeatures().size());
    assertEquals(2, ann2.allFeatures().size());
  }
}
