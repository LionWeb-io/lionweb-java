package io.lionweb.model.impl;

import static org.junit.Assert.*;

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
    Containment c = new Containment();
    ProxyNode anotherProxyNode = new ProxyNode("id-124");

    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getAnnotations);
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getClassifier);
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getParent);
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, proxyNode::getContainmentFeature);
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.getPropertyValue(p));
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.setPropertyValue(p, "foo"));
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.getChildren(c));
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class,
        () -> proxyNode.addChild(c, anotherProxyNode));
    assertThrows(
        ProxyNode.CannotDoBecauseProxyException.class,
        () -> proxyNode.removeChild(anotherProxyNode));
    assertThrows(ProxyNode.CannotDoBecauseProxyException.class, () -> proxyNode.removeChild(c, 0));
  }

  @Test
  public void auxiliaryMethods() {
    ProxyNode proxyNode1 = new ProxyNode("id-123");
    ProxyNode proxyNode2 = new ProxyNode("id-123");
    ProxyNode proxyNode3 = new ProxyNode("id-124");

    assertEquals(proxyNode1, proxyNode2);
    assertNotEquals(proxyNode1, proxyNode3);

    assertEquals(proxyNode1.hashCode(), proxyNode2.hashCode());
    assertNotEquals(proxyNode1.hashCode(), proxyNode3.hashCode());

    assertEquals("ProxyNode(id-123)", proxyNode1.toString());
  }
}
