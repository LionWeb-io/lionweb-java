package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

public class EcoreImporter extends AbstractEmfImporter {
  private Map<EPackage, Metamodel> packagesToMetamodels = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private Map<EClass, ConceptInterface> eClassesToConceptInterfacess = new HashMap<>();

  private Map<EEnum, Enumeration> eEnumsToEnumerations = new HashMap<>();

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
    packagesToMetamodels.put(ePackage, metamodel);

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
          eClassesToConceptInterfacess.put(eClass, conceptInterface);
        } else {
          Concept concept = new Concept(metamodel, eClass.getName());
          concept.setID(ePackage.getName() + "-" + concept.getName());
          concept.setKey(ePackage.getName() + "-" + concept.getName());
          concept.setAbstract(false);
          metamodel.addElement(concept);
          eClassesToConcepts.put(eClass, concept);
        }
      } else if (eClassifier.eClass().getName().equals(EcorePackage.Literals.EENUM.getName())) {
        EEnum eEnum = (EEnum) eClassifier;
        Enumeration enumeration = new Enumeration(metamodel, eEnum.getName());
        enumeration.setID(ePackage.getName() + "-" + eEnum.getName());
        enumeration.setKey(ePackage.getName() + "-" + eEnum.getName());
        metamodel.addElement(enumeration);
        eEnumsToEnumerations.put(eEnum, enumeration);
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
          ConceptInterface conceptInterface = eClassesToConceptInterfacess.get(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              ConceptInterface superConceptInterface = eClassesToConceptInterfacess.get(supertype);
              conceptInterface.addExtendedInterface(superConceptInterface);
            } else {
              throw new UnsupportedOperationException();
            }
          }

          processStructuralFeatures(ePackage, eClass, conceptInterface);

        } else {
          Concept concept = eClassesToConcepts.get(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              ConceptInterface superConceptInterface = eClassesToConceptInterfacess.get(supertype);
              concept.addImplementedInterface(superConceptInterface);
            } else {
              Concept superConcept = eClassesToConcepts.get(supertype);
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
        Enumeration enumeration = eEnumsToEnumerations.get(eEnum);
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

  private DataType convertEClassifierToDataType(EClassifier eClassifier) {
    if (eClassifier.equals(EcorePackage.Literals.ESTRING)) {
      return LionCoreBuiltins.getString();
    }
    if (eClassifier.equals(EcorePackage.Literals.EINT)) {
      return LionCoreBuiltins.getInteger();
    }
    if (eClassifier.equals(EcorePackage.Literals.EBOOLEAN)) {
      return LionCoreBuiltins.getBoolean();
    }
    if (eClassifier.eClass().equals(EcorePackage.Literals.EENUM)) {
      return eEnumsToEnumerations.get((EEnum) eClassifier);
    }
    if (eClassifier.getEPackage().getNsURI().equals("http://www.eclipse.org/emf/2003/XMLType")) {
      if (eClassifier.getName().equals("String")) {
        return LionCoreBuiltins.getString();
      }
      if (eClassifier.getName().equals("Int")) {
        return LionCoreBuiltins.getInteger();
      }
    }
    throw new UnsupportedOperationException();
  }

  private FeaturesContainer convertEClassifierToFeaturesContainer(EClassifier eClassifier) {
    if (eClassesToConcepts.containsKey(eClassifier)) {
      return eClassesToConcepts.get(eClassifier);
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
        property.setType(convertEClassifierToDataType(eFeature.getEType()));
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
