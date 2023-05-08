package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.mapping.DataTypeMapping;
import io.lionweb.lioncore.java.metamodel.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

public class EcoreImporter extends AbstractEmfImporter {
  //private Map<EPackage, Metamodel> packagesToMetamodels = new HashMap<>();
  private DataTypeMapping dataTypeMapping = new DataTypeMapping();
  private ConceptsToEClassesMapping conceptsToEClassesMapping;

  public EcoreImporter() {
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping();
  }

  public EcoreImporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }

  @Override
  public List<Metamodel> importResource(Resource resource) {
    List<Metamodel> metamodels = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      if (content.eClass().getName().equals(EcorePackage.Literals.EPACKAGE.getName())) {
        metamodels.add(importEPackage((EPackage) content));
      }
    }
    return metamodels;
  }

  public Metamodel importEPackage(EPackage ePackage) {
    Metamodel metamodel = new Metamodel(ePackage.getName());
    metamodel.setVersion("1");
    metamodel.setID(ePackage.getName());
    metamodel.setKey(ePackage.getName());
    //packagesToMetamodels.put(ePackage, metamodel);

    // Initially we just create empty concepts, later we populate the features as they could refer
    // to
    // EClasses which we meet later on in the EPackage
    for (EClassifier eClassifier : ePackage.getEClassifiers()) {
      if (eClassifier.eClass().getName().equals(EcorePackage.Literals.ECLASS.getName())) {
        EClass eClass = (EClass) eClassifier;
        if (eClass.isInterface()) {
          ConceptInterface conceptInterface = new ConceptInterface(metamodel, eClass.getName());
          conceptInterface.setID(ePackage.getName() + "-" + conceptInterface.getName());
          conceptInterface.setKey(ePackage.getName() + "-" + conceptInterface.getName());
          metamodel.addElement(conceptInterface);
          conceptsToEClassesMapping.registerMapping(conceptInterface, eClass);
        } else {
          Concept concept = new Concept(metamodel, eClass.getName());
          concept.setID(ePackage.getName() + "-" + concept.getName());
          concept.setKey(ePackage.getName() + "-" + concept.getName());
          concept.setAbstract(false);
          metamodel.addElement(concept);
          conceptsToEClassesMapping.registerMapping(concept, eClass);
        }
      } else if (eClassifier.eClass().getName().equals(EcorePackage.Literals.EENUM.getName())) {
        EEnum eEnum = (EEnum) eClassifier;
        Enumeration enumeration = new Enumeration(metamodel, eEnum.getName());
        enumeration.setID(ePackage.getName() + "-" + eEnum.getName());
        enumeration.setKey(ePackage.getName() + "-" + eEnum.getName());
        metamodel.addElement(enumeration);
        dataTypeMapping.registerMapping(eEnum, enumeration);
      } else {
        throw new UnsupportedOperationException(eClassifier.toString());
      }
    }

    // Now that all Concepts have been created we can draw links to them when processing features
    // or supertypes
    for (EClassifier eClassifier : ePackage.getEClassifiers()) {
      if (eClassifier.eClass().getName().equals(EcorePackage.Literals.ECLASS.getName())) {
        EClass eClass = (EClass) eClassifier;
        if (eClass.isInterface()) {
          ConceptInterface conceptInterface = conceptsToEClassesMapping.getCorrespondingConceptInterface(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              ConceptInterface superConceptInterface = conceptsToEClassesMapping.getCorrespondingConceptInterface(supertype);
              conceptInterface.addExtendedInterface(superConceptInterface);
            } else {
              throw new UnsupportedOperationException();
            }
          }

          processStructuralFeatures(ePackage, eClass, conceptInterface);

        } else {
          Concept concept = conceptsToEClassesMapping.getCorrespondingConcept(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              ConceptInterface superConceptInterface = conceptsToEClassesMapping.getCorrespondingConceptInterface(supertype);
              concept.addImplementedInterface(superConceptInterface);
            } else {
              Concept superConcept = conceptsToEClassesMapping.getCorrespondingConcept(supertype);
              if (concept.getExtendedConcept() != null) {
                throw new IllegalStateException("Cannot set more than one extended concept");
              }
              concept.setExtendedConcept(superConcept);
            }
          }

          processStructuralFeatures(ePackage, eClass, concept);
        }
      } else if (eClassifier.eClass().getName().equals(EcorePackage.Literals.EENUM.getName())) {
        EEnum eEnum = (EEnum) eClassifier;
        Enumeration enumeration = dataTypeMapping.getEnumeratorForEEnum(eEnum);
        for (EEnumLiteral enumLiteral : eEnum.getELiterals()) {
          EnumerationLiteral enumerationLiteral = new EnumerationLiteral(enumLiteral.getName());
          enumerationLiteral.setID(enumeration.getID() + "-" + enumLiteral.getName());
          enumeration.addLiteral(enumerationLiteral);
        }
      } else {
        throw new UnsupportedOperationException();
      }
    }
    return metamodel;
  }

  private FeaturesContainer convertEClassifierToFeaturesContainer(EClassifier eClassifier) {
    if (conceptsToEClassesMapping.knows(eClassifier)) {
      return conceptsToEClassesMapping.getCorrespondingFeaturesContainer(eClassifier);
    } else {
      throw new IllegalArgumentException(
          "Reference to an EClassifier we did not met: " + eClassifier);
    }
  }


  private void processStructuralFeatures(
      EPackage ePackage, EClass eClass, FeaturesContainer<?> featuresContainer) {
    for (EStructuralFeature eFeature : eClass.getEStructuralFeatures()) {
      if (eFeature.eClass().getName().equals(EcorePackage.Literals.EATTRIBUTE.getName())) {
        EAttribute eAttribute = (EAttribute) eFeature;
        Property property = new Property(eFeature.getName(), featuresContainer);
        property.setID(
            ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
        property.setKey(
            ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
        featuresContainer.addFeature(property);
        property.setOptional(!eAttribute.isRequired());
        property.setDerived(eAttribute.isDerived());
        property.setType(dataTypeMapping.convertEClassifierToDataType(eFeature.getEType()));
        if (eAttribute.isMany()) {
          throw new IllegalArgumentException("EAttributes with upper bound > 1 are not supported");
        }
      } else if (eFeature.eClass().getName().equals(EcorePackage.Literals.EREFERENCE.getName())) {
        EReference eReference = (EReference) eFeature;
        if (eReference.isContainment()) {
          Containment containment = new Containment(eFeature.getName(), featuresContainer);
          containment.setID(
              ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
          containment.setKey(
              ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
          containment.setOptional(!eReference.isRequired());
          containment.setMultiple(eReference.isMany());
          featuresContainer.addFeature(containment);
          containment.setType(convertEClassifierToFeaturesContainer(eReference.getEType()));
        } else {
          Reference reference = new Reference(eFeature.getName(), featuresContainer);
          reference.setID(
              ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
          reference.setKey(
              ePackage.getName() + "-" + featuresContainer.getName() + "-" + eFeature.getName());
          reference.setOptional(!eReference.isRequired());
          reference.setMultiple(eReference.isMany());
          featuresContainer.addFeature(reference);
          reference.setType(convertEClassifierToFeaturesContainer(eReference.getEType()));
        }
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }
}
