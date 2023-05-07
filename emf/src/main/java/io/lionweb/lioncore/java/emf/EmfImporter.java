package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.Node;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EmfImporter extends AbstractEmfImporter  {

  private NodeInstantiator nodeInstantiator;
  private EClassMapper eClassMapper;

  public NodeInstantiator getNodeInstantiator() {
    return nodeInstantiator;
  }

  public EmfImporter() {
    nodeInstantiator = new NodeInstantiator();
    eClassMapper = new EClassMapper();
  }

  @Override
  public List<Node> importResource(Resource resource) {
    List<Node> nodes = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      Concept concept = eClassMapper.getCorrespondingConcept(content.eClass());
      Node node = nodeInstantiator.instantiate(concept, content, Collections.emptyMap(), Collections.emptyMap());
      nodes.add(node);
      populateNode(content, node);
    }
    return nodes;
  }

  private void populateNode(EObject eObject, Node node) {
    eObject.eClass().getEAllStructuralFeatures().forEach(eStructuralFeature -> {
      Object sfValue = eObject.eGet(eStructuralFeature);
      if (sfValue != null) {
        if (eStructuralFeature instanceof EAttribute) {
          EAttribute eAttribute = (EAttribute) eStructuralFeature;
          throw new UnsupportedOperationException();
        } else if (eStructuralFeature instanceof EReference) {
          EReference eReference = (EReference) eStructuralFeature;
          if (eReference.isMany()) {
            List<EObject> values = (List<EObject>) sfValue;
            values.forEach(v -> {
              throw new UnsupportedOperationException();
            });
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
