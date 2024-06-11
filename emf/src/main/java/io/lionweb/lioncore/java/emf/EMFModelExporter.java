package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

public class EMFModelExporter extends AbstractEMFExporter {

  public EMFModelExporter() {
    super();
  }

  public EMFModelExporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    super(conceptsToEClassesMapping);
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
    EClass eClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(root.getClassifier());
    if (eClass == null) {
      throw new IllegalStateException(
          "Cannot find EClass corresponding to "
              + root.getClassifier().getName()
              + ". Node: "
              + root);
    }
    EObject eObject = eClass.getEPackage().getEFactoryInstance().create(eClass);
    referencesPostponer.trackMapping(root, eObject);

    eObject
        .eClass()
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              if (eStructuralFeature instanceof EAttribute) {
                EAttribute eAttribute = (EAttribute) eStructuralFeature;
                Object propertyValue =
                    ClassifierInstanceUtils.getPropertyValueByName(root, eAttribute.getName());
                Object attributeValue = convertAttributeValue(propertyValue);
                eObject.eSet(eAttribute, attributeValue);
              } else if (eStructuralFeature instanceof EReference) {
                EReference eReference = (EReference) eStructuralFeature;
                if (eReference.isContainment()) {
                  if (eReference.isMany()) {
                    List<? extends Node> childrenInLW =
                        ClassifierInstanceUtils.getChildrenByContainmentName(
                            root, eReference.getName());
                    List<EObject> childrenInEmf =
                        childrenInLW.stream()
                            .map(clw -> exportTree(clw, referencesPostponer))
                            .collect(Collectors.toList());
                    eObject.eSet(eReference, childrenInEmf);
                  } else {
                    List<? extends Node> childrenInLW =
                        ClassifierInstanceUtils.getChildrenByContainmentName(
                            root, eReference.getName());
                    if (childrenInLW.size() > 1) {
                      throw new IllegalStateException(
                          "More than one child found in eReference "
                              + eReference.getEContainingClass().getName()
                              + "."
                              + eReference.getName()
                              + ", where up to one children was expected, "
                              + "as the relation has not multiplicity many");
                    } else if (childrenInLW.size() == 1) {
                      eObject.eSet(
                          eReference, exportTree(childrenInLW.get(0), referencesPostponer));
                    } else {
                      eObject.eSet(eReference, null);
                    }
                  }
                } else {
                  referencesPostponer.recordReference(root, eObject, eReference);
                }
              } else {
                throw new IllegalStateException(
                    "Unexpected feature "
                        + eStructuralFeature.getClass()
                        + ". Instance: "
                        + eStructuralFeature.getName());
              }
            });

    return eObject;
  }

  public static class ReferencesPostponer {

    private final Map<Node, EObject> nodesToEObjects = new HashMap<>();
    private final List<PostponedReference> postponedReferences = new ArrayList<>();

    public void trackMapping(Node node, EObject eObject) {
      nodesToEObjects.put(node, eObject);
    }

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
      postponedReferences.forEach(
          postponedReference -> {
            Reference reference =
                postponedReference
                    .node
                    .getClassifier()
                    .getReferenceByName(postponedReference.eReference.getName());
            List<ReferenceValue> referenceValues =
                postponedReference.node.getReferenceValues(reference);
            if (postponedReference.eReference.isMany()) {
              List<EObject> referredEObjects =
                  referenceValues.stream()
                      .map(rv -> nodeToEObject(rv.getReferred()))
                      .collect(Collectors.toList());
              postponedReference.eObject.eSet(postponedReference.eReference, referredEObjects);
            } else {
              EObject referredEObject;
              if (referenceValues.isEmpty()) {
                referredEObject = null;
              } else if (referenceValues.size() == 1) {
                referredEObject = nodeToEObject(referenceValues.get(0).getReferred());
              } else {
                throw new IllegalStateException(
                    "More than one reference found in reference "
                        + reference.getContainmentFeature().getName()
                        + "."
                        + reference.getName()
                        + ", where up to one reference was expected, "
                        + "as the relation has not multiplicity many");
              }

              postponedReference.eObject.eSet(postponedReference.eReference, referredEObject);
            }
          });
    }

    public void recordReference(Node node, EObject eObject, EReference eReference) {
      postponedReferences.add(new PostponedReference(node, eObject, eReference));
    }

    static class PostponedReference {
      final Node node;
      final EObject eObject;
      final EReference eReference;

      public PostponedReference(Node node, EObject eObject, EReference eReference) {
        this.node = node;
        this.eObject = eObject;
        this.eReference = eReference;
      }
    }
  }
}
