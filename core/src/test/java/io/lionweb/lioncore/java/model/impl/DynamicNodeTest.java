package io.lionweb.lioncore.java.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import io.lionweb.lioncore.java.serialization.MyNodeWithProperties;
import org.junit.Test;

public class DynamicNodeTest {

  @Test
  public void equalityPositiveCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    assertTrue(n1.equals(n2));
  }

  @Test
  public void equalityNegativeCaseEmptyNodes() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    MyNodeWithProperties n2 = new MyNodeWithProperties("id2");
    assertFalse(n1.equals(n2));
  }

  @Test
  public void equalityPositiveCaseWithProperties() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("foo");
    n2.setP4(new JsonArray());
    assertTrue(n1.equals(n2));
  }

  @Test
  public void equalityNegativrCaseWithProperties() {
    MyNodeWithProperties n1 = new MyNodeWithProperties("id1");
    n1.setP1(true);
    n1.setP2(123);
    n1.setP3("foo");
    n1.setP4(new JsonArray());
    MyNodeWithProperties n2 = new MyNodeWithProperties("id1");
    n2.setP1(true);
    n2.setP2(123);
    n2.setP3("bar");
    n2.setP4(new JsonArray());
    assertFalse(n1.equals(n2));
  }
}
