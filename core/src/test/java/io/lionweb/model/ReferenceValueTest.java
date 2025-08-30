package io.lionweb.model;

import static org.junit.Assert.*;

import io.lionweb.language.Concept;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.model.impl.ProxyNode;
import org.junit.Test;

public class ReferenceValueTest {

  @Test
  public void defaultConstructor() {
    ReferenceValue refValue = new ReferenceValue();

    assertNull(refValue.getReferred());
    assertNull(refValue.getResolveInfo());
    assertNull(refValue.getReferredID());
  }

  @Test
  public void constructorWithParameters() {
    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("test-id", concept);

    ReferenceValue refValue = new ReferenceValue(node, "resolve-info");

    assertEquals(node, refValue.getReferred());
    assertEquals("resolve-info", refValue.getResolveInfo());
    assertEquals("test-id", refValue.getReferredID());
  }

  @Test
  public void getReferredID_returnsNullWhenReferredIsNull() {
    ReferenceValue refValue = new ReferenceValue(null, "info");

    assertNull(refValue.getReferredID());
  }

  @Test
  public void setReferred_deprecated() {
    ReferenceValue refValue = new ReferenceValue();
    ProxyNode proxy = new ProxyNode("proxy-id");

    refValue.setReferred(proxy);

    assertEquals(proxy, refValue.getReferred());
    assertEquals("proxy-id", refValue.getReferredID());
  }

  @Test
  public void setResolveInfo_deprecated() {
    ReferenceValue refValue = new ReferenceValue();

    refValue.setResolveInfo("new-info");

    assertEquals("new-info", refValue.getResolveInfo());
  }

  @Test
  public void equals_sameInstance() {
    ReferenceValue refValue = new ReferenceValue();

    assertEquals(refValue, refValue);
  }

  @Test
  public void equals_nullAndDifferentClass() {
    ReferenceValue refValue = new ReferenceValue();

    assertNotEquals(refValue, null);
    assertNotEquals(refValue, "not a reference value");
  }

  @Test
  public void equals_sameContent() {
    ProxyNode node = new ProxyNode("same-id");
    ReferenceValue ref1 = new ReferenceValue(node, "same-info");
    ReferenceValue ref2 = new ReferenceValue(node, "same-info");

    assertEquals(ref1, ref2);
    assertEquals(ref1.hashCode(), ref2.hashCode());
  }

  @Test
  public void equals_differentReferred() {
    ProxyNode node1 = new ProxyNode("id1");
    ProxyNode node2 = new ProxyNode("id2");
    ReferenceValue ref1 = new ReferenceValue(node1, "info");
    ReferenceValue ref2 = new ReferenceValue(node2, "info");

    assertNotEquals(ref1, ref2);
  }

  @Test
  public void equals_differentResolveInfo() {
    ProxyNode node = new ProxyNode("same-id");
    ReferenceValue ref1 = new ReferenceValue(node, "info1");
    ReferenceValue ref2 = new ReferenceValue(node, "info2");

    assertNotEquals(ref1, ref2);
  }

  @Test
  public void toString_withNullReferred() {
    ReferenceValue refValue = new ReferenceValue(null, "test-info");

    String result = refValue.toString();

    assertTrue(result.contains("referred=null"));
    assertTrue(result.contains("resolveInfo='test-info'"));
  }

  @Test
  public void toString_withReferred() {
    ProxyNode node = new ProxyNode("test-id");
    ReferenceValue refValue = new ReferenceValue(node, "test-info");

    String result = refValue.toString();

    assertTrue(result.contains("referred=test-id"));
    assertTrue(result.contains("resolveInfo='test-info'"));
  }

  @Test
  public void withReferred_createsNewInstance() {
    ProxyNode original = new ProxyNode("original");
    ProxyNode replacement = new ProxyNode("replacement");
    ReferenceValue original_ref = new ReferenceValue(original, "info");

    ReferenceValue newRef = original_ref.withReferred(replacement);

    assertNotSame(original_ref, newRef);
    assertEquals(replacement, newRef.getReferred());
    assertEquals("info", newRef.getResolveInfo());
    assertEquals(original, original_ref.getReferred()); // original unchanged
  }

  @Test
  public void withResolveInfo_createsNewInstance() {
    ProxyNode node = new ProxyNode("test");
    ReferenceValue originalRef = new ReferenceValue(node, "original-info");

    ReferenceValue newRef = originalRef.withResolveInfo("new-info");

    assertNotSame(originalRef, newRef);
    assertEquals(node, newRef.getReferred());
    assertEquals("new-info", newRef.getResolveInfo());
    assertEquals("original-info", originalRef.getResolveInfo()); // original unchanged
  }
}
