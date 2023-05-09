package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.support.NodeInstantiator;
import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.metamodel.Containment;
import io.lionweb.lioncore.java.metamodel.Metamodel;
import io.lionweb.lioncore.java.metamodel.Reference;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

import java.util.*;
import java.util.stream.Collectors;

public class EmfExporter {

  private ConceptsToEClassesMapping eClassMapper = new ConceptsToEClassesMapping();
  public EmfExporter() {
  }

  private class ReferencesPostponer {

    private Map<Node, EObject> nodesToEObjects = new HashMap<>();

    public void trackMapping(Node node, EObject eObject) {
      nodesToEObjects.put(node, eObject);
    }

    class PostponedReference {
      Node node; EObject eObject; EReference eReference;

      public PostponedReference(Node node, EObject eObject, EReference eReference) {
        this.node = node;
        this.eObject = eObject;
        this.eReference = eReference;
      }
    }

    private List<PostponedReference> postponedReferences = new ArrayList<>();

    private EObject nodeToEObject(Node node) {
      if (node == null) {
        return null;
      }
      if (!nodesToEObjects.containsKey(node)) {
        throw new IllegalStateException("Unknown node " + node);
      }
      return nodesToEObjects.get(node);
    }

    public void addLinks() {
      postponedReferences.forEach(postponedReference -> {
        Reference reference = postponedReference.node.getConcept().getReferenceByName(postponedReference.eReference.getName());
        List<ReferenceValue> referenceValues = postponedReference.node.getReferenceValues(reference);
        if (postponedReference.eReference.isMany()) {
          List<EObject> referredEObjects = referenceValues.stream().map(rv -> nodeToEObject(rv.getReferred())).collect(Collectors.toList());
          postponedReference.eObject.eSet(postponedReference.eReference, referredEObjects);
        } else {
          EObject referredEObject;
          if (referenceValues.size() == 0) {
            referredEObject = null;
          } else if (referenceValues.size() == 1) {
            referredEObject = nodeToEObject(referenceValues.get(0).getReferred());
          } else {
            throw new IllegalStateException();
          }

          postponedReference.eObject.eSet(postponedReference.eReference, referredEObject);
        }
      });
    }

    public void recordReference(Node node, EObject eObject, EReference eReference) {
      postponedReferences.add(new PostponedReference(node, eObject, eReference));
    }
  }

  public Resource exportResource(List<Node> roots) {
    Resource resource = new ResourceImpl();
    ReferencesPostponer referencesPostponer = new ReferencesPostponer();
    roots.forEach(m -> resource.getContents().add(exportTree(m, referencesPostponer)));
    referencesPostponer.addLinks();
    return resource;
  }

  private Object convertAttributeValue(Object propertyValue) {
    // TODO improve...
    return propertyValue;
  }

  /** This export the root received to a single EObject tree. */
  public EObject exportTree(Node root, ReferencesPostponer referencesPostponer) {
    EClass eClass = (EClass) eClassMapper.getCorrespondingEClass(root.getConcept());
    if (eClass == null) {
      throw new IllegalStateException();
    }
    EObject eObject = eClass.getEPackage().getEFactoryInstance().create(eClass);
    referencesPostponer.trackMapping(root, eObject);

    eObject.eClass().getEAllStructuralFeatures().forEach(eStructuralFeature -> {
      if (eStructuralFeature instanceof EAttribute) {
        EAttribute eAttribute = (EAttribute) eStructuralFeature;
        Object propertyValue = root.getPropertyValueByName(eAttribute.getName());
        Object attributeValue = convertAttributeValue(propertyValue);
        eObject.eSet(eAttribute, attributeValue);
      } else if (eStructuralFeature instanceof EReference) {
        EReference eReference = (EReference) eStructuralFeature;
        if (eReference.isContainment()) {
          if (eReference.isMany()) {
            List<? extends Node> childrenInLW = root.getChildrenByContainmentName(eReference.getName());
            List<EObject> childrenInEmf = childrenInLW.stream().map(clw -> exportTree(clw, referencesPostponer)).collect(Collectors.toList());
            eObject.eSet(eReference, childrenInEmf);
          } else {
            throw new UnsupportedOperationException();
          }
        } else {
          referencesPostponer.recordReference(root, eObject, eReference);
        }
      } else {
        throw new IllegalStateException();
      }
    });

    return eObject;
  }
}
