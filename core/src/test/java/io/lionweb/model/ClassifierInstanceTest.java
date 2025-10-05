package io.lionweb.model;

import static org.junit.Assert.assertEquals;

import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.model.impl.ProxyNode;
import org.junit.Test;

public class ClassifierInstanceTest {

  @Test
  public void countSelfAndDescendants_singleNodeWithoutAnnotations() {
    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("single", concept);

    int count = ClassifierInstance.countSelfAndDescendants(node, false);

    assertEquals(1, count);
  }

  @Test
  public void countSelfAndDescendants_singleNodeWithAnnotations() {
    Concept concept = new Concept();
    DynamicNode node = new DynamicNode("single", concept);

    int countWithoutAnnotations = ClassifierInstance.countSelfAndDescendants(node, false);
    int countWithAnnotations = ClassifierInstance.countSelfAndDescendants(node, true);

    assertEquals(1, countWithoutAnnotations);
    assertEquals(1, countWithAnnotations); // No annotations added yet
  }

  @Test
  public void countSelfAndDescendants_nodeWithChildren() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode child1 = new DynamicNode("child1", concept);
    DynamicNode child2 = new DynamicNode("child2", concept);

    parent.addChild(containment, child1);
    parent.addChild(containment, child2);

    int count = ClassifierInstance.countSelfAndDescendants(parent, false);

    assertEquals(3, count); // parent + child1 + child2
  }

  @Test
  public void countSelfAndDescendants_deepHierarchy() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode root = new DynamicNode("root", concept);
    DynamicNode level1 = new DynamicNode("level1", concept);
    DynamicNode level2a = new DynamicNode("level2a", concept);
    DynamicNode level2b = new DynamicNode("level2b", concept);
    DynamicNode level3 = new DynamicNode("level3", concept);

    root.addChild(containment, level1);
    level1.addChild(containment, level2a);
    level1.addChild(containment, level2b);
    level2a.addChild(containment, level3);

    int count = ClassifierInstance.countSelfAndDescendants(root, false);

    assertEquals(5, count); // root + level1 + level2a + level2b + level3
  }

  @Test
  public void countSelfAndDescendants_withAnnotations() {
    Concept concept = new Concept();
    Annotation annotationType = new Annotation();

    DynamicNode node = new DynamicNode("node", concept);
    DynamicAnnotationInstance annotation1 = new DynamicAnnotationInstance("ann1", annotationType);
    DynamicAnnotationInstance annotation2 = new DynamicAnnotationInstance("ann2", annotationType);

    node.addAnnotation(annotation1);
    node.addAnnotation(annotation2);

    int countWithoutAnnotations = ClassifierInstance.countSelfAndDescendants(node, false);
    int countWithAnnotations = ClassifierInstance.countSelfAndDescendants(node, true);

    assertEquals(1, countWithoutAnnotations);
    assertEquals(3, countWithAnnotations); // node + annotation1 + annotation2
  }

  @Test
  public void countSelfAndDescendants_annotationsOnChildrenToo() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    Annotation annotationType = new Annotation();

    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode child = new DynamicNode("child", concept);

    DynamicAnnotationInstance parentAnnotation =
        new DynamicAnnotationInstance("parentAnn", annotationType);
    DynamicAnnotationInstance childAnnotation =
        new DynamicAnnotationInstance("childAnn", annotationType);

    parent.addAnnotation(parentAnnotation);
    child.addAnnotation(childAnnotation);

    parent.addChild(containment, child);

    int countWithoutAnnotations = ClassifierInstance.countSelfAndDescendants(parent, false);
    int countWithAnnotations = ClassifierInstance.countSelfAndDescendants(parent, true);

    assertEquals(2, countWithoutAnnotations); // parent + child
    assertEquals(4, countWithAnnotations); // parent + child + parentAnnotation + childAnnotation
  }

  @Test
  public void countSelfAndDescendants_ignoresProxyNodes() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    DynamicNode parent = new DynamicNode("parent", concept);
    DynamicNode realChild = new DynamicNode("realChild", concept);
    ProxyNode proxyChild = new ProxyNode("proxyChild");

    parent.addChild(containment, realChild);
    parent.addChild(containment, proxyChild);

    int count = ClassifierInstance.countSelfAndDescendants(parent, false);

    assertEquals(2, count); // parent + realChild (proxy ignored)
  }

  @Test
  public void countSelfAndDescendants_nestedAnnotations() {
    Concept concept = new Concept();
    Annotation annotationType = new Annotation();

    DynamicNode node = new DynamicNode("node", concept);
    DynamicAnnotationInstance outerAnnotation =
        new DynamicAnnotationInstance("outer", annotationType);
    DynamicAnnotationInstance innerAnnotation =
        new DynamicAnnotationInstance("inner", annotationType);

    // Add nested annotation
    outerAnnotation.addAnnotation(innerAnnotation);
    node.addAnnotation(outerAnnotation);

    int countWithAnnotations = ClassifierInstance.countSelfAndDescendants(node, true);

    assertEquals(3, countWithAnnotations); // node + outerAnnotation + innerAnnotation
  }

  @Test
  public void countSelfAndDescendants_complexTree() {
    Concept concept = new Concept();
    Containment containment = new Containment();
    containment.setName("children");
    containment.setKey("children-key");
    containment.setMultiple(true);
    concept.addFeature(containment);

    Annotation annotationType = new Annotation();

    DynamicNode root = new DynamicNode("root", concept);
    DynamicNode child1 = new DynamicNode("child1", concept);
    DynamicNode child2 = new DynamicNode("child2", concept);
    DynamicNode grandchild = new DynamicNode("grandchild", concept);
    ProxyNode proxy = new ProxyNode("proxy");

    DynamicAnnotationInstance rootAnnotation =
        new DynamicAnnotationInstance("rootAnn", annotationType);
    DynamicAnnotationInstance childAnnotation =
        new DynamicAnnotationInstance("childAnn", annotationType);

    // Build tree
    root.addChild(containment, child1);
    root.addChild(containment, child2);
    root.addChild(containment, proxy); // Should be ignored
    child1.addChild(containment, grandchild);

    // Add annotations
    root.addAnnotation(rootAnnotation);
    child1.addAnnotation(childAnnotation);

    int countWithoutAnnotations = ClassifierInstance.countSelfAndDescendants(root, false);
    int countWithAnnotations = ClassifierInstance.countSelfAndDescendants(root, true);

    assertEquals(4, countWithoutAnnotations); // root + child1 + child2 + grandchild (proxy ignored)
    assertEquals(6, countWithAnnotations); // + rootAnnotation + childAnnotation
  }
}
