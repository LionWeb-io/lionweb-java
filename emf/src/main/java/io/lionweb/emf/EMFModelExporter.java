package io.lionweb.emf;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.emf.support.AnnotationsEncoding;
import io.lionweb.language.Reference;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

/** Exports LionWeb model nodes to EMF {@link org.eclipse.emf.ecore.resource.Resource} instances. */
public class EMFModelExporter extends AbstractEMFExporter {

  public EMFModelExporter() {
    super();
  }

  public EMFModelExporter(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public EMFModelExporter(LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    super(entitiesToEElementsMapping);
  }

  /**
   * Export the given roots into a new Resource. Annotation instances are exported as additional
   * root EObjects, placed after all the given roots. See {@link AnnotationsEncoding}.
   */
  public Resource exportResource(List<Node> roots) {
    Resource resource = new ResourceImpl();
    ReferencesPostponer referencesPostponer = new ReferencesPostponer();
    List<EObject> exportedAnnotations = new ArrayList<>();
    roots.forEach(
        m -> resource.getContents().add(exportTree(m, referencesPostponer, exportedAnnotations)));
    resource.getContents().addAll(exportedAnnotations);
    referencesPostponer.addLinks();
    return resource;
  }

  private Object convertAttributeValue(Object propertyValue) {
    // TODO improve...
    return propertyValue;
  }

  /**
   * This export the root received to a single EObject tree. Note that the EObjects representing
   * annotation instances are not contained in the returned tree: use {@link #exportTree(Node,
   * ReferencesPostponer, List)} to collect them.
   */
  public EObject exportTree(Node root, ReferencesPostponer referencesPostponer) {
    return exportTree(root, referencesPostponer, new ArrayList<>());
  }

  /**
   * This export the root received to a single EObject tree. The EObjects representing the
   * annotation instances attached to the exported nodes (or to other annotation instances) are
   * added to exportedAnnotations.
   */
  public EObject exportTree(
      Node root, ReferencesPostponer referencesPostponer, List<EObject> exportedAnnotations) {
    return exportClassifierInstance(root, referencesPostponer, exportedAnnotations);
  }

  private EObject exportClassifierInstance(
      ClassifierInstance<?> root,
      ReferencesPostponer referencesPostponer,
      List<EObject> exportedAnnotations) {
    EClass eClass =
        (EClass) entitiesToEElementsMapping.getCorrespondingEClass(root.getClassifier());
    if (eClass == null) {
      throw new IllegalStateException(
          "Cannot find EClass corresponding to "
              + root.getClassifier().getName()
              + ". Node: "
              + root);
    }
    EObject eObject = eClass.getEPackage().getEFactoryInstance().create(eClass);
    if (root instanceof Node) {
      referencesPostponer.trackMapping((Node) root, eObject);
    }

    eObject
        .eClass()
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              if (AnnotationsEncoding.isAnnotatedEReference(eStructuralFeature)) {
                // This is set by the annotated element when exporting its annotations
              } else if (eStructuralFeature instanceof EAttribute) {
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
                            .map(clw -> exportTree(clw, referencesPostponer, exportedAnnotations))
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
                          eReference,
                          exportTree(
                              childrenInLW.get(0), referencesPostponer, exportedAnnotations));
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

    for (AnnotationInstance annotationInstance : root.getAnnotations()) {
      // We want each annotation instance to precede the annotations attached to it
      int index = exportedAnnotations.size();
      EObject annotationEObject =
          exportClassifierInstance(annotationInstance, referencesPostponer, exportedAnnotations);
      EReference annotatedEReference =
          AnnotationsEncoding.getAnnotatedEReference(annotationEObject.eClass());
      if (annotatedEReference == null) {
        throw new IllegalStateException(
            "Cannot find the EReference to the annotated element in EClass "
                + annotationEObject.eClass().getName());
      }
      annotationEObject.eSet(annotatedEReference, eObject);
      exportedAnnotations.add(index, annotationEObject);
    }

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

    public void recordReference(
        ClassifierInstance<?> node, EObject eObject, EReference eReference) {
      postponedReferences.add(new PostponedReference(node, eObject, eReference));
    }

    static class PostponedReference {
      final ClassifierInstance<?> node;
      final EObject eObject;
      final EReference eReference;

      public PostponedReference(
          ClassifierInstance<?> node, EObject eObject, EReference eReference) {
        this.node = node;
        this.eObject = eObject;
        this.eReference = eReference;
      }
    }
  }
}
