package io.lionweb.lioncore.java.utils;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;

public class NodeTreeValidatorTest {

  @Test
  public void everythingCorrectCase() {
    Concept c = new Concept();
    DynamicNode node = new DynamicNode("abc", c);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertEquals(true, vr.isSuccessful());
    assertEquals(Collections.emptySet(), vr.getIssues());
  }

  @Test
  public void aNodeWithoutIDIsNotValid() {
    Concept c = new Concept();
    DynamicNode node = new DynamicNode(null, c);
    ValidationResult vr = new NodeTreeValidator().validate(node);
    assertEquals(false, vr.isSuccessful());
    assertEquals(
        new HashSet(Arrays.asList(new Issue(IssueSeverity.Error, "Invalid ID", node))),
        vr.getIssues());
  }
}
