package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class NodeTreeValidatorTest {

  @Test
  public void everythingCorrectCase() {
    Concept c = new Concept();
    c.setPartition(true);
    DynamicNode node = new DynamicNode("abc", c);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertTrue(vr.isSuccessful());
    assertEquals(Collections.emptySet(), vr.getIssues());
  }

  @Test
  public void aNodeWithoutIDIsNotValid() {
    Concept c = new Concept();
    c.setPartition(true);
    DynamicNode node = new DynamicNode(null, c);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertFalse(vr.isSuccessful());
    assertEquals(
        new HashSet(Arrays.asList(new Issue(IssueSeverity.Error, "ID null found", node))),
        vr.getIssues());
  }

  @Test
  public void aNodeWithInvalidIDIsNotValid() {
    Concept c = new Concept();
    c.setPartition(true);
    DynamicNode node = new DynamicNode("@@@", c);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertFalse(vr.isSuccessful());
    assertEquals(
        new HashSet(Arrays.asList(new Issue(IssueSeverity.Error, "Invalid ID", node))),
        vr.getIssues());
  }

  @Test
  public void rootNodeWhichIsNotPartition() {
    Concept nonPartitionConcept = new Concept();
    nonPartitionConcept.setPartition(false);
    DynamicNode node = new DynamicNode("N1", nonPartitionConcept);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertFalse(vr.isSuccessful());
    assertEquals(
        new HashSet(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "A root node should be an instance of a Partition concept",
                    node))),
        vr.getIssues());
  }

  @Test
  public void rootNodeWhichIsPartition() {
    Concept nonPartitionConcept = new Concept();
    nonPartitionConcept.setPartition(true);
    DynamicNode node = new DynamicNode("N1", nonPartitionConcept);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertTrue(vr.isSuccessful());
  }

  @Test
  public void nonPartitionChildOfPartitionIsValid() {
    Concept partitionConcept = new Concept("P").setKey("P-key").setID("P-id");
    partitionConcept.setPartition(true);
    Concept childConcept = new Concept("C").setKey("C-key").setID("C-id");
    Containment children = Containment.createMultiple("children", childConcept);
    children.setKey("children-key");
    partitionConcept.addFeature(children);
    DynamicNode root = new DynamicNode("N1", partitionConcept);
    root.addChild(children, new DynamicNode("N2", childConcept));
    ValidationResult vr = new NodeTreeValidator().validate(root);
    assertTrue(vr.isSuccessful());
  }

  @Test
  public void nestedPartitionIsNotValid() {
    Concept partitionConcept = new Concept("P").setKey("P-key").setID("P-id");
    partitionConcept.setPartition(true);
    Containment children = Containment.createMultiple("children", partitionConcept);
    children.setKey("children-key");
    partitionConcept.addFeature(children);
    DynamicNode root = new DynamicNode("N1", partitionConcept);
    DynamicNode nested = new DynamicNode("N2", partitionConcept);
    root.addChild(children, nested);
    ValidationResult vr = new NodeTreeValidator().validate(root);
    assertFalse(vr.isSuccessful());
    assertEquals(
        new HashSet(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "A non-root node should not be an instance of a Partition concept",
                    nested))),
        vr.getIssues());
  }

  @Test
  public void annotationAppliedToAnnotatedConceptIsValid() {
    Concept c = new Concept("C").setKey("C-key").setID("C-id");
    c.setPartition(true);
    Annotation a = new Annotation(null, "A", "A-id").setKey("A-key");
    a.setAnnotates(c);
    DynamicNode node = new DynamicNode("N1", c);
    node.addAnnotation(new DynamicAnnotationInstance("A1", a));
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertTrue(vr.isSuccessful());
  }

  @Test
  public void annotationAppliedToSubConceptIsValid() {
    Concept base = new Concept("Base").setKey("Base-key").setID("Base-id");
    Concept sub = new Concept("Sub").setKey("Sub-key").setID("Sub-id");
    sub.setExtendedConcept(base);
    sub.setPartition(true);
    Annotation a = new Annotation(null, "A", "A-id").setKey("A-key");
    a.setAnnotates(base);
    DynamicNode node = new DynamicNode("N1", sub);
    node.addAnnotation(new DynamicAnnotationInstance("A1", a));
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertTrue(vr.isSuccessful());
  }

  @Test
  public void annotationAppliedToUnrelatedConceptIsNotValid() {
    Concept c = new Concept("C").setKey("C-key").setID("C-id");
    c.setPartition(true);
    Concept other = new Concept("Other").setKey("Other-key").setID("Other-id");
    Annotation a = new Annotation(null, "A", "A-id").setKey("A-key");
    a.setAnnotates(other);
    DynamicNode node = new DynamicNode("N1", c);
    DynamicAnnotationInstance annotationInstance = new DynamicAnnotationInstance("A1", a);
    node.addAnnotation(annotationInstance);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertFalse(vr.isSuccessful());
    assertEquals(
        new HashSet(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "Annotation A cannot be applied to an instance of C, as it annotates Other",
                    annotationInstance))),
        vr.getIssues());
  }
}
