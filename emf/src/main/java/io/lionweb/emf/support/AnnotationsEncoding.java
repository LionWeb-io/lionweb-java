package io.lionweb.emf.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Describes how LionWeb {@link io.lionweb.language.Annotation}s, and their instances, are
 * represented in EMF.
 *
 * <p>EMF has no native concept equivalent to LionWeb annotations, so we use the following encoding:
 *
 * <ul>
 *   <li>On the metamodel level, a LionWeb Annotation is represented as an {@link EClass} marked by
 *       an {@link EAnnotation} with source {@link #ANNOTATION_SOURCE}. If the Annotation specifies
 *       the classifier it annotates, the corresponding EClass is listed in the {@link
 *       EAnnotation#getReferences() references} of such EAnnotation.
 *   <li>The EClass representing an Annotation that does not extend another Annotation has an
 *       additional non-containment {@link EReference} named {@link #ANNOTATED_EREFERENCE_NAME},
 *       pointing to the annotated EObject. This reference is not imported as a LionWeb feature.
 *   <li>On the model level, annotation instances are exported as root EObjects of the Resource,
 *       with the {@link #ANNOTATED_EREFERENCE_NAME} reference pointing to the annotated EObject.
 * </ul>
 */
public final class AnnotationsEncoding {

  /** Source of the EAnnotation marking EClasses which represent LionWeb Annotations. */
  public static final String ANNOTATION_SOURCE = "https://lionweb.io/annotation";

  /** Name of the EReference connecting an annotation instance to the annotated EObject. */
  public static final String ANNOTATED_EREFERENCE_NAME = "annotated";

  private AnnotationsEncoding() {}

  /** Indicates if the given EClass represents a LionWeb Annotation. */
  public static boolean isAnnotation(@Nonnull EClass eClass) {
    return eClass.getEAnnotation(ANNOTATION_SOURCE) != null;
  }

  /**
   * Indicates if the given feature is the EReference used to connect annotation instances to the
   * annotated EObject.
   */
  public static boolean isAnnotatedEReference(@Nonnull EStructuralFeature eStructuralFeature) {
    return eStructuralFeature instanceof EReference
        && ANNOTATED_EREFERENCE_NAME.equals(eStructuralFeature.getName())
        && eStructuralFeature.getEContainingClass() != null
        && isAnnotation(eStructuralFeature.getEContainingClass());
  }

  /**
   * Returns the EReference used to connect instances of the given annotation EClass to the
   * annotated EObject, or null if it is not present.
   */
  public static @Nullable EReference getAnnotatedEReference(@Nonnull EClass eClass) {
    EStructuralFeature feature = eClass.getEStructuralFeature(ANNOTATED_EREFERENCE_NAME);
    if (feature != null && isAnnotatedEReference(feature)) {
      return (EReference) feature;
    }
    return null;
  }
}
