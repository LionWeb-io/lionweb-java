package io.lionweb.lioncore.java.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ConceptReflectionTest {

  @Test
  public void getPropertyValuename() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    assertEquals(
        "MyConcept", concept.getPropertyValue(LionCore.getConcept().getPropertyByName("name")));
  }

  @Test
  public void setPropertyValuename() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    concept.setPropertyValue(LionCore.getConcept().getPropertyByName("name"), "MyAmazingConcept");
    assertEquals("MyAmazingConcept", concept.getName());
  }

  @Test
  public void getPropertyValueAbstract() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Property property = LionCore.getConcept().getPropertyByName("abstract");
    concept.setAbstract(true);
    assertEquals(true, concept.getPropertyValue(property));
    concept.setAbstract(false);
    assertEquals(false, concept.getPropertyValue(property));
  }

  @Test
  public void setPropertyValueAbstract() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Property property = LionCore.getConcept().getPropertyByName("abstract");
    concept.setPropertyValue(property, true);
    assertEquals(true, concept.isAbstract());
    concept.setPropertyValue(property, false);
    assertEquals(false, concept.isAbstract());
  }

  @Test
  public void getReferenceExtended() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Concept otherConcept = new Concept(language, "OtherConcept");
    Reference reference = LionCore.getConcept().getReferenceByName("extends");
    concept.setExtendedConcept(null);
    assertEquals(
        Collections.emptyList(), ClassifierInstanceUtils.getReferredNodes(concept, reference));
    concept.setExtendedConcept(otherConcept);
    assertEquals(
        Arrays.asList(otherConcept), ClassifierInstanceUtils.getReferredNodes(concept, reference));
  }

  @Test
  public void setReferenceExtended() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Concept otherConcept = new Concept(language, "OtherConcept");
    Reference reference = LionCore.getConcept().getReferenceByName("extends");
    concept.addReferenceValue(reference, null);
    assertNull(concept.getExtendedConcept());
    concept.addReferenceValue(reference, new ReferenceValue(otherConcept, null));
    assertEquals(otherConcept, concept.getExtendedConcept());
  }

  @Test
  public void getReferenceImplemented() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Interface i1 = new Interface(language, "I1");
    Interface i2 = new Interface(language, "I2");
    Reference reference = LionCore.getConcept().getReferenceByName("implements");
    assertEquals(
        Collections.emptyList(), ClassifierInstanceUtils.getReferredNodes(concept, reference));
    concept.addImplementedInterface(i1);
    assertEquals(Arrays.asList(i1), ClassifierInstanceUtils.getReferredNodes(concept, reference));
    concept.addImplementedInterface(i2);
    assertEquals(
        Arrays.asList(i1, i2), ClassifierInstanceUtils.getReferredNodes(concept, reference));
  }

  @Test
  public void setReferenceImplemented() {
    Language language = new Language();
    Concept concept = new Concept(language, "MyConcept");
    Interface i1 = new Interface(language, "I1");
    Interface i2 = new Interface(language, "I2");
    Reference reference = LionCore.getConcept().getReferenceByName("implements");
    assertEquals(Collections.emptyList(), concept.getImplemented());
    concept.addReferenceValue(reference, new ReferenceValue(i1, null));
    assertEquals(Arrays.asList(i1), concept.getImplemented());
    concept.addReferenceValue(reference, new ReferenceValue(i2, null));
    assertEquals(Arrays.asList(i1, i2), concept.getImplemented());
  }
}
