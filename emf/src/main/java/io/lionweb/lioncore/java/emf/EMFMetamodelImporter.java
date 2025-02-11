package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.mapping.DataTypeMapping;
import io.lionweb.lioncore.java.language.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

/** EMF importer which produces LionWeb's Metamodels. */
public class EMFMetamodelImporter extends AbstractEMFImporter<Language> {
  private @Nonnull DataTypeMapping dataTypeMapping;

  public EMFMetamodelImporter() {
    this(LionWebVersion.currentVersion);
  }

  public EMFMetamodelImporter(@Nonnull LionWebVersion lionWebVersion) {
    super();
    dataTypeMapping = new DataTypeMapping(lionWebVersion);
  }

  public EMFMetamodelImporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    super(conceptsToEClassesMapping);
    dataTypeMapping = new DataTypeMapping(conceptsToEClassesMapping.getLionWebVersion());
  }

  @Override
  public List<Language> importResource(Resource resource) {
    List<Language> metamodels = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      if (content.eClass().getName().equals(EcorePackage.Literals.EPACKAGE.getName())) {
        metamodels.add(importEPackage((EPackage) content));
      }
    }
    return metamodels;
  }

  public Language importEPackage(EPackage ePackage) {
    Language metamodel = new Language(ePackage.getName());
    metamodel.setVersion("1");
    metamodel.setID(ePackage.getName());
    metamodel.setKey(ePackage.getName());

    // Initially we just create empty concepts, later we populate the features as they could refer
    // to EClasses which we meet later on in the EPackage
    for (EClassifier eClassifier : ePackage.getEClassifiers()) {
      if (eClassifier.eClass().getName().equals(EcorePackage.Literals.ECLASS.getName())) {
        EClass eClass = (EClass) eClassifier;
        if (eClass.isInterface()) {
          Interface iface = new Interface(metamodel, eClass.getName());
          iface.setID(ePackage.getName() + "-" + iface.getName());
          iface.setKey(ePackage.getName() + "-" + iface.getName());
          metamodel.addElement(iface);
          conceptsToEClassesMapping.registerMapping(iface, eClass);
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
      } else if (eClassifier instanceof EDataType) {
        EDataType eDataType = (EDataType) eClassifier;
        PrimitiveType primitiveType = new PrimitiveType(metamodel, eDataType.getName());
        primitiveType.setID(ePackage.getName() + "-" + eDataType.getName());
        primitiveType.setKey(ePackage.getName() + "-" + eDataType.getName());
        metamodel.addElement(primitiveType);
        dataTypeMapping.registerMapping(eDataType, primitiveType);
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
          Interface iface = conceptsToEClassesMapping.getCorrespondingInterface(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              Interface superInterface =
                  conceptsToEClassesMapping.getCorrespondingInterface(supertype);
              iface.addExtendedInterface(superInterface);
            } else {
              throw new UnsupportedOperationException();
            }
          }

          processStructuralFeatures(ePackage, eClass, iface);

        } else {
          Concept concept = conceptsToEClassesMapping.getCorrespondingConcept(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              Interface superInterface =
                  conceptsToEClassesMapping.getCorrespondingInterface(supertype);
              concept.addImplementedInterface(superInterface);
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
        Enumeration enumeration = dataTypeMapping.getEnumerationForEEnum(eEnum);
        for (EEnumLiteral enumLiteral : eEnum.getELiterals()) {
          EnumerationLiteral enumerationLiteral = new EnumerationLiteral(enumLiteral.getName());
          enumerationLiteral.setID(enumeration.getID() + "-" + enumLiteral.getName());
          enumerationLiteral.setKey(enumeration.getID() + "-" + enumLiteral.getName());
          enumeration.addLiteral(enumerationLiteral);
        }
      } else if (eClassifier instanceof EDataType) {
        // Nothing to do here
      } else {
        throw new UnsupportedOperationException();
      }
    }
    return metamodel;
  }

  private Classifier convertEClassifierToClassifier(EClassifier eClassifier) {
    if (conceptsToEClassesMapping.knows(eClassifier)) {
      return conceptsToEClassesMapping.getCorrespondingClassifier(eClassifier);
    } else {
      throw new IllegalArgumentException(
          "Reference to an EClassifier we did not met: " + eClassifier);
    }
  }

  private void processStructuralFeatures(
      EPackage ePackage, EClass eClass, Classifier<?> classifier) {
    for (EStructuralFeature eFeature : eClass.getEStructuralFeatures()) {
      if (eFeature.eClass().getName().equals(EcorePackage.Literals.EATTRIBUTE.getName())) {
        EAttribute eAttribute = (EAttribute) eFeature;
        // EAttributes with upper bound > 1 are not supported in LionWeb
        if (!eAttribute.isMany()) {
          Property property = new Property(eFeature.getName(), classifier);
          property.setID(ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          property.setKey(ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          classifier.addFeature(property);
          property.setOptional(!eAttribute.isRequired());
          DataType<DataType> propertyType =
                  dataTypeMapping.convertEClassifierToDataType(eFeature.getEType());
          Objects.requireNonNull(propertyType, "Cannot convert type " + eFeature.getEType());
          property.setType(propertyType);
        }
        // The work-around for multiple EAttributes: introduce an intermediate containment with the upper bound > 1
        else {
          String featureName =
                  eFeature.getName().substring(0, 1).toUpperCase() + eFeature.getName().substring(1);
          Concept holderConcept = new Concept( featureName + "Container");
          holderConcept.setID(ePackage.getName() + "-" + holderConcept.getName());
          holderConcept.setKey(ePackage.getName() + "-" + holderConcept.getName());
          holderConcept.setAbstract(false);
          classifier.getLanguage().addElement(holderConcept);

          Property property = new Property("content", holderConcept);
          property.setID(ePackage.getName() + "-" + holderConcept.getName() + "-" + property.getName());
          property.setKey(ePackage.getName() + "-" + holderConcept.getName() + "-" + property.getName());
          holderConcept.addFeature(property);
          property.setOptional(false);
          DataType<DataType> propertyType =
                  dataTypeMapping.convertEClassifierToDataType(eFeature.getEType());
          Objects.requireNonNull(propertyType, "Cannot convert type " + eFeature.getEType());
          property.setType(propertyType);

          Containment containment = new Containment(eFeature.getName(), classifier);
          containment.setID(ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          containment.setKey(ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          classifier.addFeature(containment);
          containment.setOptional(!eAttribute.isRequired());
          containment.setMultiple(eAttribute.isMany());
          containment.setType(holderConcept);
        }
      } else if (eFeature.eClass().getName().equals(EcorePackage.Literals.EREFERENCE.getName())) {
        EReference eReference = (EReference) eFeature;
        if (eReference.isContainment()) {
          Containment containment = new Containment(eFeature.getName(), classifier);
          containment.setID(
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          containment.setKey(
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          containment.setOptional(!eReference.isRequired());
          containment.setMultiple(eReference.isMany());
          classifier.addFeature(containment);
          containment.setType(convertEClassifierToClassifier(eReference.getEType()));
        } else {
          Reference reference = new Reference(eFeature.getName(), classifier);
          reference.setID(
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          reference.setKey(
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          reference.setOptional(!eReference.isRequired());
          reference.setMultiple(eReference.isMany());
          classifier.addFeature(reference);
          reference.setType(convertEClassifierToClassifier(eReference.getEType()));
        }
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }
}
