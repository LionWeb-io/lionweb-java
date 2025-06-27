package io.lionweb.emf;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.emf.support.NodeInstantiator;
import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import java.util.*;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

/** Importer which produces LionWeb's Nodes. */
public class EMFModelImporter extends AbstractEMFImporter<Node> {
  private final NodeInstantiator nodeInstantiator;

  public EMFModelImporter() {
    super();
    nodeInstantiator = new NodeInstantiator();
  }

  public EMFModelImporter(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
    nodeInstantiator = new NodeInstantiator();
  }

  public EMFModelImporter(LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    super(entitiesToEElementsMapping);
    nodeInstantiator = new NodeInstantiator();
  }

  public NodeInstantiator getNodeInstantiator() {
    return nodeInstantiator;
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
    Concept concept = entitiesToEElementsMapping.getCorrespondingConcept(eObject.eClass());
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
                    ClassifierInstanceUtils.setPropertyValueByName(
                        node, eStructuralFeature.getName(), sfValue);
                  } else if (eAttribute
                      .getEAttributeType()
                      .equals(EcorePackage.eINSTANCE.getEString())) {
                    ClassifierInstanceUtils.setPropertyValueByName(
                        node, eStructuralFeature.getName(), sfValue);
                  } else if (eAttribute
                      .getEAttributeType()
                      .equals(EcorePackage.eINSTANCE.getEBoolean())) {
                    ClassifierInstanceUtils.setPropertyValueByName(
                        node, eStructuralFeature.getName(), sfValue);
                  } else {
                    throw new UnsupportedOperationException();
                  }
                } else if (eStructuralFeature instanceof EReference) {
                  EReference eReference = (EReference) eStructuralFeature;
                  if (eReference.isContainment()) {
                    Containment containment =
                        node.getClassifier().requireContainmentByName(eStructuralFeature.getName());
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
