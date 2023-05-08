package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.support.NodeInstantiator;
import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.Node;
import java.util.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

public class EmfImporter extends AbstractEmfImporter {
  private NodeInstantiator nodeInstantiator;
  private ConceptsToEClassesMapping eClassMapper;

  public NodeInstantiator getNodeInstantiator() {
    return nodeInstantiator;
  }

  public EmfImporter() {
    nodeInstantiator = new NodeInstantiator();
    eClassMapper = new ConceptsToEClassesMapping();
  }

  @Override
  public List<Node> importResource(Resource resource) {
    List<Node> nodes = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      nodes.add(eObjectToNode(content));
    }
    return nodes;
  }

  private Node eObjectToNode(EObject eObject) {
    Concept concept = eClassMapper.getCorrespondingConcept(eObject.eClass());
    Node node =
        nodeInstantiator.instantiate(
            concept, eObject, Collections.emptyMap(), Collections.emptyMap());
    populateNode(eObject, node);
    return node;
  }

  private void populateNode(EObject eObject, Node node) {
    eObject
        .eClass()
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              Object sfValue = eObject.eGet(eStructuralFeature);
              if (sfValue != null) {
                if (eStructuralFeature instanceof EAttribute) {
                  EAttribute eAttribute = (EAttribute) eStructuralFeature;
                  if (eAttribute.isMany()) {
                    throw new UnsupportedOperationException();
                  }
                  if (eAttribute.getEAttributeType().equals(EcorePackage.eINSTANCE.getEInt())) {
                    node.setPropertyValueByName(eStructuralFeature.getName(), sfValue);
                  } else if (eAttribute
                      .getEAttributeType()
                      .equals(EcorePackage.eINSTANCE.getEString())) {
                    node.setPropertyValueByName(eStructuralFeature.getName(), sfValue);
                  } else if (eAttribute
                      .getEAttributeType()
                      .equals(EcorePackage.eINSTANCE.getEBoolean())) {
                    node.setPropertyValueByName(eStructuralFeature.getName(), sfValue);
                  } else {
                    throw new UnsupportedOperationException();
                  }
                } else if (eStructuralFeature instanceof EReference) {
                  EReference eReference = (EReference) eStructuralFeature;
                  if (eReference.isContainment()) {
                    Containment containment =
                        node.getConcept().requireContainmentByName(eStructuralFeature.getName());
                    if (eReference.isMany()) {
                      List<EObject> values = (List<EObject>) sfValue;
                      values.forEach(
                          v -> {
                            Node childNode = eObjectToNode(v);
                            node.addChild(containment, childNode);
                          });
                    } else {
                      EObject childValue = (EObject) sfValue;
                      Node childNode = eObjectToNode(childValue);
                      node.addChild(containment, childNode);
                    }
                  } else {
                    throw new UnsupportedOperationException();
                  }
                } else {
                  throw new IllegalStateException();
                }
              }
            });
  }
}
