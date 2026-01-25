package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.language.Concept;
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
}
