package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.mapping.DataTypeMapping;
import io.lionweb.lioncore.java.metamodel.*;
import java.util.List;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

public class EcoreExporter {

  private DataTypeMapping dataTypeMapping = new DataTypeMapping();
  private ConceptsToEClassesMapping conceptsToEClassesMapping;

  public EcoreExporter() {
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping();
  }

  public EcoreExporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }

  /** This export all the metamodels received to a single Resource. */
  public Resource exportResource(List<Metamodel> metamodels) {
    Resource resource = new ResourceImpl();
    metamodels.forEach(m -> resource.getContents().add(exportMetamodel(m)));
    return resource;
  }

  /** This export the Metamodel received to a single EPackage. */
  public EPackage exportMetamodel(Metamodel metamodel) {
    EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

    ePackage.setName(metamodel.getName());
    ePackage.setNsURI("https://lionweb.io/" + metamodel.getKey());
    ePackage.setNsPrefix(metamodel.getName());

    // We first create all EClasses and only later we draw relationships
    // among them
    createEClasses(metamodel, ePackage);
    populateEClasses(metamodel);

    return ePackage;
  }

  private void createEClasses(Metamodel metamodel, EPackage ePackage) {
    metamodel
        .getElements()
        .forEach(
            e -> {
              if (e instanceof Concept) {
                Concept concept = (Concept) e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(concept.getName());

                ePackage.getEClassifiers().add(eClass);
                conceptsToEClassesMapping.registerMapping(concept, eClass);
              } else if (e instanceof ConceptInterface) {
                throw new UnsupportedOperationException();
              } else if (e instanceof Enumeration) {
                throw new UnsupportedOperationException();
              } else {
                throw new UnsupportedOperationException();
              }
            });
  }

  private void considerMultiplicity(Link<?> link, EReference eReference) {
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
      considerMultiplicity(containment, eReference);
      eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(containment.getType()));

      return eReference;
    } else if (feature instanceof Reference) {
      Reference reference = (Reference) feature;

      EReference eReference = EcoreFactory.eINSTANCE.createEReference();
      eReference.setName(reference.getName());
      eReference.setContainment(false);
      considerMultiplicity(reference, eReference);
      eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(reference.getType()));

      return eReference;
    } else {
      throw new IllegalStateException();
    }
  }

  private void populateEClassFromConcept(Concept concept) {
    EClass eClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept);

    if (concept.getExtendedConcept() != null) {
      EClass superEClass =
          (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept.getExtendedConcept());
      eClass.getESuperTypes().add(superEClass);
    }
    concept
        .getImplemented()
        .forEach(
            implemented -> {
              throw new UnsupportedOperationException();
            });

    concept
        .getFeatures()
        .forEach(
            f ->
              eClass.getEStructuralFeatures().add(convertFeatureToEStructuralFeature(f))
            );
  }

  private void populateEClasses(Metamodel metamodel) {
    metamodel
        .getElements()
        .forEach(
            e -> {
              if (e instanceof Concept) {
                populateEClassFromConcept((Concept) e);
              } else if (e instanceof ConceptInterface) {
                throw new UnsupportedOperationException();
              } else if (e instanceof Enumeration) {
                throw new UnsupportedOperationException();
              } else {
                throw new UnsupportedOperationException();
              }
            });
  }
}
