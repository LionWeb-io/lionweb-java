package io.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ConceptReflectionTest {

  @Test
  public void getPropertyValuename() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    assertEquals(
        "MyConcept", concept.getPropertyValue(LionCore.getConcept().getPropertyByName("name")));
  }

  @Test
  public void setPropertyValuename() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    concept.setPropertyValue(LionCore.getConcept().getPropertyByName("name"), "MyAmazingConcept");
    assertEquals("MyAmazingConcept", concept.getName());
  }

  @Test
  public void getPropertyValueAbstract() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    Property property = LionCore.getConcept().getPropertyByName("abstract");
    concept.setAbstract(true);
    assertEquals(true, concept.getPropertyValue(property));
    concept.setAbstract(false);
    assertEquals(false, concept.getPropertyValue(property));
  }

  @Test
  public void setPropertyValueAbstract() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    Property property = LionCore.getConcept().getPropertyByName("abstract");
    concept.setPropertyValue(property, true);
    assertEquals(true, concept.isAbstract());
    concept.setPropertyValue(property, false);
    assertEquals(false, concept.isAbstract());
  }

  @Test
  public void getReferenceExtended() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    Concept otherConcept = new Concept(metamodel, "OtherConcept");
    Reference reference = LionCore.getConcept().getReferenceByName("extends");
    concept.setExtendedConcept(null);
    assertEquals(Collections.emptyList(), concept.getReferredNodes(reference));
    concept.setExtendedConcept(otherConcept);
    assertEquals(Arrays.asList(otherConcept), concept.getReferredNodes(reference));
  }

  @Test
  public void setReferenceExtended() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    Concept otherConcept = new Concept(metamodel, "OtherConcept");
    Reference reference = LionCore.getConcept().getReferenceByName("extends");
    concept.addReferenceValue(reference, null);
    assertEquals(null, concept.getExtendedConcept());
    concept.addReferenceValue(reference, new ReferenceValue(otherConcept, null));
    assertEquals(otherConcept, concept.getExtendedConcept());
  }

  @Test
  public void getReferenceImplemented() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    ConceptInterface i1 = new ConceptInterface(metamodel, "I1");
    ConceptInterface i2 = new ConceptInterface(metamodel, "I2");
    Reference reference = LionCore.getConcept().getReferenceByName("implements");
    assertEquals(Collections.emptyList(), concept.getReferredNodes(reference));
    concept.addImplementedInterface(i1);
    assertEquals(Arrays.asList(i1), concept.getReferredNodes(reference));
    concept.addImplementedInterface(i2);
    assertEquals(Arrays.asList(i1, i2), concept.getReferredNodes(reference));
  }

  @Test
  public void setReferenceImplemented() {
    Metamodel metamodel = new Metamodel();
    Concept concept = new Concept(metamodel, "MyConcept");
    ConceptInterface i1 = new ConceptInterface(metamodel, "I1");
    ConceptInterface i2 = new ConceptInterface(metamodel, "I2");
    Reference reference = LionCore.getConcept().getReferenceByName("implements");
    assertEquals(Collections.emptyList(), concept.getImplemented());
    concept.addReferenceValue(reference, new ReferenceValue(i1, null));
    assertEquals(Arrays.asList(i1), concept.getImplemented());
    concept.addReferenceValue(reference, new ReferenceValue(i2, null));
    assertEquals(Arrays.asList(i1, i2), concept.getImplemented());
  }
}
