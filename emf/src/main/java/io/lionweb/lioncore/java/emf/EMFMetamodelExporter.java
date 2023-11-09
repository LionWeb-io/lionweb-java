package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.mapping.DataTypeMapping;
import io.lionweb.lioncore.java.language.*;
import java.util.List;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

/** Export LionWeb's metamodels into EMF's metamodels. */
public class EMFMetamodelExporter extends AbstractEMFExporter {

  private final DataTypeMapping dataTypeMapping = new DataTypeMapping();

  public EMFMetamodelExporter() {
    super();
  }

  public EMFMetamodelExporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    super(conceptsToEClassesMapping);
  }

  /** This export all the languages received to a single Resource. */
  public Resource exportResource(List<Language> languages) {
    Resource resource = new ResourceImpl();
    languages.forEach(m -> resource.getContents().add(exportLanguage(m)));
    return resource;
  }

  /** This export the Language received to a single EPackage. */
  public EPackage exportLanguage(Language language) {
    EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

    // Ecore expects valid Java identifiers as package name, see
    // https://github.com/eclipse-emf/org.eclipse.emf/blob/d761f373bda75cccc7adfd79783304762fd3affa/plugins/org.eclipse.emf.ecore/src/org/eclipse/emf/ecore/util/EcoreValidator.java#L2218
    // and
    // https://github.com/eclipse-emf/org.eclipse.emf/blob/d761f373bda75cccc7adfd79783304762fd3affa/plugins/org.eclipse.emf.ecore/src/org/eclipse/emf/ecore/util/EcoreValidator.java#L683
    // Otherwise, generated Java code might not compile.
    ePackage.setName(language.getName().replaceAll("[^\\p{Alnum}_]", "_"));
    ePackage.setNsURI("https://lionweb.io/" + language.getKey());
    ePackage.setNsPrefix(language.getName());

    // We first create all EClasses and only later we draw relationships
    // among them
    createEClasses(language, ePackage);
    populateEClasses(language);

    return ePackage;
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
                conceptsToEClassesMapping.registerMapping(concept, eClass);
              } else if (e instanceof Interface) {
                Interface iface = (Interface) e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(iface.getName());
                eClass.setInterface(true);

                ePackage.getEClassifiers().add(eClass);
                conceptsToEClassesMapping.registerMapping(iface, eClass);
              } else if (e instanceof Enumeration) {
                Enumeration enumeration = (Enumeration) e;

                EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
                eEnum.setName(enumeration.getName());

                ePackage.getEClassifiers().add(eEnum);
                dataTypeMapping.registerMapping(eEnum, enumeration);
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
      eAttribute.setEType(dataTypeMapping.toEDataType(property.getType()));
      return eAttribute;
    } else if (feature instanceof Containment) {
      Containment containment = (Containment) feature;

      EReference eReference = EcoreFactory.eINSTANCE.createEReference();
      eReference.setName(containment.getName());
      eReference.setContainment(true);
      considerLinkMultiplicity(containment, eReference);
      eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(containment.getType()));

      return eReference;
    } else if (feature instanceof Reference) {
      Reference reference = (Reference) feature;

      EReference eReference = EcoreFactory.eINSTANCE.createEReference();
      eReference.setName(reference.getName());
      eReference.setContainment(false);
      considerLinkMultiplicity(reference, eReference);
      eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(reference.getType()));

      return eReference;
    } else {
      throw new IllegalStateException(
          "Unexpected feature " + feature.getClass() + ". Instance: " + feature.getName());
    }
  }

  private void populateEClassFromConcept(Concept concept) {
    EClass eClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept);

    if (concept.getExtendedConcept() != null) {
      EClass superEClass =
          (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept.getExtendedConcept());
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
                  (EClass) conceptsToEClassesMapping.getCorrespondingEClass(implemented);
              eClass.getESuperTypes().add(implementedEClass);
            });

    concept
        .getFeatures()
        .forEach(f -> eClass.getEStructuralFeatures().add(convertFeatureToEStructuralFeature(f)));
  }

  private void populateEClassFromInterface(Interface iface) {
    EClass eClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(iface);

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

  private void populateEEnumFromEnumerration(Enumeration enumeration) {
    EEnum eEnum = dataTypeMapping.getEEnumForEnumeration(enumeration);

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
              } else if (e instanceof Enumeration) {
                populateEEnumFromEnumerration((Enumeration) e);
              } else {
                throw new UnsupportedOperationException(
                    "Cannot handle " + e.getClass() + " yet. Instance: " + e.getName());
              }
            });
  }
}
