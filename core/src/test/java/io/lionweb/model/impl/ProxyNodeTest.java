package io.lionweb.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.lionweb.language.*;
import org.junit.Test;

public class ProxyNodeTest {

  @Test
  public void canGetID() {
    assertEquals("id-123", new ProxyNode("id-123").getID());
  }

  @Test
  public void operationsCannotBePerformed() {
    ProxyNode proxyNode = new ProxyNode("id-123");

    Property p = new Property();

    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getAnnotations);
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getClassifier);
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getParent);
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.getPropertyValue(p));
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.setPropertyValue(p, "foo"));
  }
}
