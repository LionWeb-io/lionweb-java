package io.lionweb.emf;

import io.lionweb.LionWebVersion;
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.emf.support.AnnotationsEncoding;
import io.lionweb.language.*;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

/** Export LionWeb's metamodels into EMF's metamodels. */
public class EMFMetamodelExporter extends AbstractEMFExporter {

  public EMFMetamodelExporter() {
    this(LionWebVersion.currentVersion);
  }

  public EMFMetamodelExporter(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public EMFMetamodelExporter(
      @Nonnull LionWebVersion lionWebVersion,
      LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    super(entitiesToEElementsMapping);
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
  }

  /**
   * Return the registry used to look up existing EPackages before creating new ones. This is stored
   * in the mapping, so that it is also used when Languages are exported implicitly.
   */
  public @Nullable EPackage.Registry getEPackageRegistry() {
    return entitiesToEElementsMapping.getEPackageRegistry();
  }

  /**
   * Set the registry used to look up existing EPackages before creating new ones. See {@link
   * LanguageEntitiesToEElementsMapping#setEPackageRegistry(EPackage.Registry)}.
   */
  public void setEPackageRegistry(@Nullable EPackage.Registry ePackageRegistry) {
    entitiesToEElementsMapping.setEPackageRegistry(ePackageRegistry);
  }

  /** Return the namespace URI used for the EPackage corresponding to the given Language. */
  public static @Nonnull String nsURIFor(@Nonnull Language language) {
    return "https://lionweb.io/" + language.getKey();
  }

  /** This export all the languages received to a single Resource. */
  public Resource exportResource(List<Language> languages) {
    Resource resource = new ResourceImpl();
    languages.forEach(m -> resource.getContents().add(exportLanguage(m)));
    return resource;
  }

  /**
   * This export the Language received to a single EPackage.
   *
   * <p>If the Language has already been mapped to an EPackage, that EPackage is returned. If an
   * EPackage with the corresponding namespace URI is present in the EPackage registry (see {@link
   * #setEPackageRegistry(EPackage.Registry)}), that EPackage is mapped to the Language and
   * returned. Only otherwise a new EPackage is created. This avoids creating duplicate EClasses for
   * Languages which already have an EMF counterpart (e.g., generated EPackages).
   */
  public EPackage exportLanguage(Language language) {
    EPackage existing = findExistingEPackage(language);
    if (existing != null) {
      return existing;
    }

    EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

    // Ecore expects valid Java identifiers as package name, see
    // https://github.com/eclipse-emf/org.eclipse.emf/blob/d761f373bda75cccc7adfd79783304762fd3affa/plugins/org.eclipse.emf.ecore/src/org/eclipse/emf/ecore/util/EcoreValidator.java#L2218
    // and
    // https://github.com/eclipse-emf/org.eclipse.emf/blob/d761f373bda75cccc7adfd79783304762fd3affa/plugins/org.eclipse.emf.ecore/src/org/eclipse/emf/ecore/util/EcoreValidator.java#L683
    // Otherwise, generated Java code might not compile.
    ePackage.setName(language.getName().replaceAll("[^\\p{Alnum}_]", "_"));
    ePackage.setNsURI(nsURIFor(language));
    ePackage.setNsPrefix(language.getName());

    // We first create all EClasses and only later we draw relationships
    // among them
    createEClasses(language, ePackage);
    entitiesToEElementsMapping.registerMapping(language, ePackage);
    populateEClasses(language);

    return ePackage;
  }

  private @Nullable EPackage findExistingEPackage(Language language) {
    EPackage known = entitiesToEElementsMapping.getKnownEPackage(language);
    if (known != null) {
      return known;
    }
    EPackage.Registry ePackageRegistry = entitiesToEElementsMapping.getEPackageRegistry();
    if (ePackageRegistry != null) {
      EPackage registered = ePackageRegistry.getEPackage(nsURIFor(language));
      if (registered != null) {
        entitiesToEElementsMapping.registerMapping(language, registered);
        return registered;
      }
    }
    return null;
  }

  private void createEClasses(Language language, EPackage ePackage) {
    language
        .getElements()
        .forEach(
            e -> {
              if (e instanceof Concept) {
                Concept concept = (Concept) e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(concept.getName());
                eClass.setInterface(false);
                eClass.setAbstract(concept.isAbstract());

                ePackage.getEClassifiers().add(eClass);
                entitiesToEElementsMapping.registerMapping(concept, eClass);
              } else if (e instanceof Interface) {
                Interface iface = (Interface) e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(iface.getName());
                eClass.setInterface(true);

                ePackage.getEClassifiers().add(eClass);
                entitiesToEElementsMapping.registerMapping(iface, eClass);
              } else if (e instanceof Annotation) {
                Annotation annotation = (Annotation) e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(annotation.getName());
                eClass.setInterface(false);
                eClass.setAbstract(false);
                EAnnotation marker = EcoreFactory.eINSTANCE.createEAnnotation();
                marker.setSource(AnnotationsEncoding.ANNOTATION_SOURCE);
                eClass.getEAnnotations().add(marker);

                ePackage.getEClassifiers().add(eClass);
                entitiesToEElementsMapping.registerMapping(annotation, eClass);
              } else if (e instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) e;

                EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
                eEnum.setName(enumeration.getName());

                ePackage.getEClassifiers().add(eEnum);
                entitiesToEElementsMapping.registerMapping(enumeration, eEnum);
              } else if (e instanceof PrimitiveType) {
                PrimitiveType primitiveType = (PrimitiveType) e;

                EDataType eDataType = EcoreFactory.eINSTANCE.createEDataType();
                eDataType.setName(primitiveType.getName());

                ePackage.getEClassifiers().add(eDataType);
                entitiesToEElementsMapping.registerMapping(primitiveType, eDataType);
              } else {
                throw new UnsupportedOperationException(
                    "Cannot handle " + e.getClass() + " yet. Instance: " + e.getName());
              }
            });
  }

  private void considerLinkMultiplicity(Link<?> link, EReference eReference) {
    if (link.isMultiple()) {
      eReference.setUpperBound(-1);
    } else {
      eReference.setUpperBound(1);
    }
    if (link.isOptional()) {
      eReference.setLowerBound(0);
    } else {
      eReference.setLowerBound(1);
    }
  }

  private EStructuralFeature convertFeatureToEStructuralFeature(Feature<?> feature) {
    if (feature instanceof Property) {
      Property property = (Property) feature;

      EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
      eAttribute.setName(property.getName());
      if (property.isOptional()) {
        eAttribute.setLowerBound(0);
      } else {
        eAttribute.setLowerBound(1);
      }
      eAttribute.setUpperBound(1);
      eAttribute.setEType(entitiesToEElementsMapping.getCorrespondingEDataType(property.getType()));
      return eAttribute;
    } else if (feature instanceof Containment) {
      Containment containment = (Containment) feature;

      EReference eReference = EcoreFactory.eINSTANCE.createEReference();
      eReference.setName(containment.getName());
      eReference.setContainment(true);
      considerLinkMultiplicity(containment, eReference);
      eReference.setEType(entitiesToEElementsMapping.getCorrespondingEClass(containment.getType()));

      return eReference;
    } else if (feature instanceof Reference) {
      Reference reference = (Reference) feature;

      EReference eReference = EcoreFactory.eINSTANCE.createEReference();
      eReference.setName(reference.getName());
      eReference.setContainment(false);
      considerLinkMultiplicity(reference, eReference);
      eReference.setEType(entitiesToEElementsMapping.getCorrespondingEClass(reference.getType()));

      return eReference;
    } else {
      throw new IllegalStateException(
          "Unexpected feature " + feature.getClass() + ". Instance: " + feature.getName());
    }
  }

  private void populateEClassFromConcept(Concept concept) {
    EClass eClass = (EClass) entitiesToEElementsMapping.getCorrespondingEClass(concept);

    if (concept.getExtendedConcept() != null) {
      EClass superEClass =
          (EClass) entitiesToEElementsMapping.getCorrespondingEClass(concept.getExtendedConcept());
      eClass.getESuperTypes().add(superEClass);
    } else {
      // The fact that EObject is extended should always be specified
      eClass.getESuperTypes().add(EcorePackage.eINSTANCE.getEObject());
    }
    concept
        .getImplemented()
        .forEach(
            implemented -> {
              EClass implementedEClass =
                  (EClass) entitiesToEElementsMapping.getCorrespondingEClass(implemented);
              eClass.getESuperTypes().add(implementedEClass);
            });

    concept
        .getFeatures()
        .forEach(f -> eClass.getEStructuralFeatures().add(convertFeatureToEStructuralFeature(f)));
  }

  private void populateEClassFromInterface(Interface iface) {
    EClass eClass = (EClass) entitiesToEElementsMapping.getCorrespondingEClass(iface);

    iface
        .getExtendedInterfaces()
        .forEach(
            extended -> {
              throw new UnsupportedOperationException("Cannot handle extended interfaces yet.");
            });

    iface
        .getFeatures()
        .forEach(f -> eClass.getEStructuralFeatures().add(convertFeatureToEStructuralFeature(f)));
  }

  private void populateEClassFromAnnotation(Annotation annotation) {
    EClass eClass = (EClass) entitiesToEElementsMapping.getCorrespondingEClass(annotation);

    if (annotation.getAnnotates() != null) {
      EClass annotatedEClass =
          (EClass) entitiesToEElementsMapping.getCorrespondingEClass(annotation.getAnnotates());
      eClass
          .getEAnnotation(AnnotationsEncoding.ANNOTATION_SOURCE)
          .getReferences()
          .add(annotatedEClass);
    }

    if (annotation.getExtendedAnnotation() != null) {
      EClass superEClass =
          (EClass)
              entitiesToEElementsMapping.getCorrespondingEClass(annotation.getExtendedAnnotation());
      eClass.getESuperTypes().add(superEClass);
    } else {
      // The reference to the annotated element is inherited when extending another annotation
      Classifier<?> annotated = annotation.getEffectivelyAnnotated();
      EReference annotatedEReference = EcoreFactory.eINSTANCE.createEReference();
      annotatedEReference.setName(AnnotationsEncoding.ANNOTATED_EREFERENCE_NAME);
      annotatedEReference.setContainment(false);
      annotatedEReference.setLowerBound(0);
      annotatedEReference.setUpperBound(1);
      annotatedEReference.setEType(
          annotated == null
              ? EcorePackage.eINSTANCE.getEObject()
              : entitiesToEElementsMapping.getCorrespondingEClass(annotated));
      eClass.getEStructuralFeatures().add(annotatedEReference);
    }
    annotation
        .getImplemented()
        .forEach(
            implemented -> {
              EClass implementedEClass =
                  (EClass) entitiesToEElementsMapping.getCorrespondingEClass(implemented);
              eClass.getESuperTypes().add(implementedEClass);
            });

    annotation
        .getFeatures()
        .forEach(
            f -> {
              if (AnnotationsEncoding.ANNOTATED_EREFERENCE_NAME.equals(f.getName())) {
                throw new UnsupportedOperationException(
                    "Cannot export annotation "
                        + annotation.getName()
                        + " because it has a feature named "
                        + AnnotationsEncoding.ANNOTATED_EREFERENCE_NAME
                        + ", which is reserved to represent the annotated element");
              }
              eClass.getEStructuralFeatures().add(convertFeatureToEStructuralFeature(f));
            });
  }

  private void populateEEnumFromEnumerration(Enumeration enumeration) {
    EEnum eEnum = entitiesToEElementsMapping.getCorrespondingEEnum(enumeration);

    enumeration
        .getLiterals()
        .forEach(
            literal -> {
              EEnumLiteral eEnumLiteral = EcoreFactory.eINSTANCE.createEEnumLiteral();
              eEnumLiteral.setName(literal.getName());
              eEnum.getELiterals().add(eEnumLiteral);
            });
  }

  private void populateEClasses(Language language) {
    language
        .getElements()
        .forEach(
            e -> {
              if (e instanceof Concept) {
                populateEClassFromConcept((Concept) e);
              } else if (e instanceof Interface) {
                populateEClassFromInterface((Interface) e);
              } else if (e instanceof Annotation) {
                populateEClassFromAnnotation((Annotation) e);
              } else if (e instanceof Enumeration) {
                populateEEnumFromEnumerration((Enumeration) e);
              } else {
                throw new UnsupportedOperationException(
                    "Cannot handle " + e.getClass() + " yet. Instance: " + e.getName());
              }
            });
  }
}
