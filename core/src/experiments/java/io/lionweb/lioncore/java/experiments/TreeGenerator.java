package io.lionweb.lioncore.java.experiments;

import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TreeGenerator {

  private Random random;

  public TreeGenerator(long seed) {
    this.random = new Random(seed);
  }

  public Node generate(int size) {
    if (size < 1) {
      throw new IllegalArgumentException();
    }
    Node root = generateNode();
    List<Node> allNodes = new LinkedList<>();
    allNodes.add(root);
    for (int i = 0; i < size - 1; i++) {
      growTree(root, allNodes);
    }
    if (root.thisAndAllDescendants().size() != size) {
      throw new IllegalStateException();
    }
    return root;
  }

  private String CHARS = "123456789 $#^&*()!~ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  // A typical model will use many times the same strings. We simulate that
  private List<String> generatedStrings = new LinkedList<>();

  private String randomString() {
    String s = "";
    if (generatedStrings.size() > 0 && random.nextFloat() < 0.85) {
      // We want to skew towards some strings
      int index = random.nextInt(generatedStrings.size());
      index = Math.min(index, random.nextInt(generatedStrings.size()));
      index = Math.min(index, random.nextInt(generatedStrings.size()));
      return generatedStrings.get(index);
    } else {
      for (int i = 0; i < random.nextInt(25); i++) {
        s += CHARS.charAt(random.nextInt(CHARS.length()));
      }
      generatedStrings.add(s);
    }
    return s;
  }

  private int skewRandomInt() {
    return Math.min(random.nextInt(100), Math.min(random.nextInt(100), random.nextInt(100)));
  }

  private Node generateNode() {
    Concept concept =
        SimpleLanguage.subConcepts.get(random.nextInt(SimpleLanguage.subConcepts.size()));
    Node node = new DynamicNode("node-" + random.nextLong(), concept);
    ClassifierInstanceUtils.setPropertyValueByName(node, "stringProp", randomString());
    ClassifierInstanceUtils.setPropertyValueByName(
        node, "intProp", Integer.toString(skewRandomInt()));
    return node;
  }

  private void growTree(Node root, List<Node> allNodes) {
    attachNode(root, generateNode(), allNodes);
  }

  private void attachNode(Node root, Node newNode, List<Node> allNodes) {
    Node container = allNodes.get(random.nextInt(allNodes.size()));
    allNodes.add(newNode);
    ClassifierInstanceUtils.addChild(container, "myContainment", newNode);
  }
}
