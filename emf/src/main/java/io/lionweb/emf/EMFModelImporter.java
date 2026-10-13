package io.lionweb.emf;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.emf.support.AnnotationsEncoding;
import io.lionweb.emf.support.NodeInstantiator;
import io.lionweb.language.*;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.impl.DynamicAnnotationInstance;
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

  /**
   * Import the contents of the Resource. Root EObjects representing annotation instances (see
   * {@link AnnotationsEncoding}) are attached to the corresponding annotated nodes, and they are
   * therefore not returned among the roots.
   */
  @Override
  public List<Node> importResource(Resource resource) {
    List<Node> nodes = new LinkedList<>();
    Map<EObject, ClassifierInstance<?>> imported = new IdentityHashMap<>();
    List<EObject> annotationEObjects = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      if (AnnotationsEncoding.isAnnotation(content.eClass())) {
        annotationEObjects.add(content);
      } else {
        nodes.add(eObjectToNode(content, imported));
      }
    }
    // We first instantiate all annotation instances, as annotations can be annotated too
    Map<EObject, DynamicAnnotationInstance> annotationInstances = new LinkedHashMap<>();
    for (EObject annotationEObject : annotationEObjects) {
      annotationInstances.put(
          annotationEObject, eObjectToAnnotationInstance(annotationEObject, imported));
    }
    annotationInstances.forEach(
        (annotationEObject, annotationInstance) -> {
          EReference annotatedEReference =
              AnnotationsEncoding.getAnnotatedEReference(annotationEObject.eClass());
          EObject annotatedEObject =
              annotatedEReference == null
                  ? null
                  : (EObject) annotationEObject.eGet(annotatedEReference);
          if (annotatedEObject == null) {
            throw new IllegalStateException(
                "Annotation instance of "
                    + annotationEObject.eClass().getName()
                    + " does not specify the annotated element");
          }
          ClassifierInstance<?> annotated = imported.get(annotatedEObject);
          if (annotated == null) {
            throw new IllegalStateException(
                "Annotation instance of "
                    + annotationEObject.eClass().getName()
                    + " annotates an element which has not been imported: "
                    + annotatedEObject);
          }
          annotated.addAnnotation(annotationInstance);
        });
    return nodes;
  }

  private Node eObjectToNode(EObject eObject, Map<EObject, ClassifierInstance<?>> imported) {
    Concept concept = entitiesToEElementsMapping.getCorrespondingConcept(eObject.eClass());
    Node node =
        nodeInstantiator.instantiate(
            concept, eObject, Collections.emptyMap(), Collections.emptyMap());
    imported.put(eObject, node);
    populateClassifierInstance(eObject, node, imported);
    return node;
  }

  private DynamicAnnotationInstance eObjectToAnnotationInstance(
      EObject eObject, Map<EObject, ClassifierInstance<?>> imported) {
    Annotation annotation = entitiesToEElementsMapping.getCorrespondingAnnotation(eObject.eClass());
    DynamicAnnotationInstance annotationInstance = new DynamicAnnotationInstance(null, annotation);
    imported.put(eObject, annotationInstance);
    populateClassifierInstance(eObject, annotationInstance, imported);
    return annotationInstance;
  }

  private void populateClassifierInstance(
      EObject eObject, ClassifierInstance<?> node, Map<EObject, ClassifierInstance<?>> imported) {
    eObject
        .eClass()
        .getEAllStructuralFeatures()
        .forEach(
            eStructuralFeature -> {
              if (AnnotationsEncoding.isAnnotatedEReference(eStructuralFeature)) {
                // This is handled when attaching annotation instances
                return;
              }
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
                            Node childNode = eObjectToNode(v, imported);
                            node.addChild(containment, childNode);
                          });
                    } else {
                      EObject childValue = (EObject) sfValue;
                      Node childNode = eObjectToNode(childValue, imported);
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
