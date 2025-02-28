package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.mapping.LanguageEntitiesToEElementsMapping;
import io.lionweb.lioncore.java.language.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

/** EMF importer which produces LionWeb's Metamodels. */
public class EMFMetamodelImporter extends AbstractEMFImporter<Language> {

  public EMFMetamodelImporter() {
    this(LionWebVersion.currentVersion);
  }

  public EMFMetamodelImporter(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public EMFMetamodelImporter(LanguageEntitiesToEElementsMapping entitiesToEElementsMapping) {
    super(entitiesToEElementsMapping);
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
    Language metamodel = new Language(getLionWebVersion(), ePackage.getName());
    metamodel.setVersion("1");
    setIDAndKey(metamodel, ePackage.getName());

    // Initially we just create empty concepts, later we populate the features as they could refer
    // to EClasses which we meet later on in the EPackage
    for (EClassifier eClassifier : ePackage.getEClassifiers()) {
      if (eClassifier.eClass().getName().equals(EcorePackage.Literals.ECLASS.getName())) {
        EClass eClass = (EClass) eClassifier;
        if (eClass.isInterface()) {
          Interface iface = new Interface(metamodel, eClass.getName());
          setIDAndKey(iface, ePackage.getName() + "-" + iface.getName());
          metamodel.addElement(iface);
          entitiesToEElementsMapping.registerMapping(iface, eClass);
        } else {
          Concept concept = new Concept(metamodel, eClass.getName());
          setIDAndKey(concept, ePackage.getName() + "-" + concept.getName());
          concept.setAbstract(eClass.isAbstract());
          metamodel.addElement(concept);
          entitiesToEElementsMapping.registerMapping(concept, eClass);
        }
      } else if (eClassifier.eClass().getName().equals(EcorePackage.Literals.EENUM.getName())) {
        EEnum eEnum = (EEnum) eClassifier;
        Enumeration enumeration = new Enumeration(metamodel, eEnum.getName());
        setIDAndKey(enumeration, ePackage.getName() + "-" + eEnum.getName());
        metamodel.addElement(enumeration);
        entitiesToEElementsMapping.registerMapping(enumeration, eEnum);
      } else if (eClassifier instanceof EDataType) {
        EDataType eDataType = (EDataType) eClassifier;
        PrimitiveType primitiveType = new PrimitiveType(metamodel, eDataType.getName());
        setIDAndKey(primitiveType, ePackage.getName() + "-" + eDataType.getName());
        metamodel.addElement(primitiveType);
        entitiesToEElementsMapping.registerMapping(primitiveType, eDataType);
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
          Interface iface = entitiesToEElementsMapping.getCorrespondingInterface(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              Interface superInterface =
                  entitiesToEElementsMapping.getCorrespondingInterface(supertype);
              addLanguageDependency(metamodel, superInterface);
              iface.addExtendedInterface(superInterface);
            } else {
              throw new UnsupportedOperationException();
            }
          }

          processStructuralFeatures(ePackage, eClass, iface);

        } else {
          Concept concept = entitiesToEElementsMapping.getCorrespondingConcept(eClass);

          for (EClass supertype : eClass.getESuperTypes()) {
            if (supertype.isInterface()) {
              Interface superInterface =
                  entitiesToEElementsMapping.getCorrespondingInterface(supertype);
              addLanguageDependency(metamodel, superInterface);
              concept.addImplementedInterface(superInterface);
            } else {
              Concept superConcept = entitiesToEElementsMapping.getCorrespondingConcept(supertype);
              if (concept.getExtendedConcept() != null) {
                throw new IllegalStateException("Cannot set more than one extended concept");
              }
              addLanguageDependency(metamodel, superConcept);
              concept.setExtendedConcept(superConcept);
            }
          }

          processStructuralFeatures(ePackage, eClass, concept);
        }
      } else if (eClassifier.eClass().getName().equals(EcorePackage.Literals.EENUM.getName())) {
        EEnum eEnum = (EEnum) eClassifier;
        Enumeration enumeration = entitiesToEElementsMapping.getCorrespondingEnumeration(eEnum);
        for (EEnumLiteral enumLiteral : eEnum.getELiterals()) {
          EnumerationLiteral enumerationLiteral =
              new EnumerationLiteral(enumeration, enumLiteral.getName());
          setIDAndKey(enumerationLiteral, enumeration.getID() + "-" + enumLiteral.getName());
        }
      } else if (eClassifier instanceof EDataType) {
        // Nothing to do here
      } else {
        throw new UnsupportedOperationException();
      }
    }
    return metamodel;
  }

  private void addLanguageDependency(Language metamodel, LanguageEntity langEntity) {
    // We should use lionweb version to instantiate proper builtins version here
    if (langEntity.getLanguage().getKey() != LionCoreBuiltins.getInstance().getKey()
        && langEntity.getLanguage() != metamodel
        && !metamodel.dependsOn().contains(langEntity.getLanguage())) {
      metamodel.addDependency(langEntity.getLanguage());
    }
  }

  private Classifier convertEClassifierToClassifier(EClassifier eClassifier) {
    if (entitiesToEElementsMapping.knows(eClassifier)) {
      return entitiesToEElementsMapping.getCorrespondingClassifier(eClassifier);
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
        DataType<DataType> propertyType =
            entitiesToEElementsMapping.getCorrespondingDataType(eAttribute.getEAttributeType());
        Objects.requireNonNull(propertyType, "Cannot convert type " + eFeature.getEType());
        addLanguageDependency(classifier.getLanguage(), propertyType);

        if (!eAttribute.isMany()) {
          Property property = new Property(eFeature.getName(), classifier);
          setIDAndKey(
              property, ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          classifier.addFeature(property);
          property.setOptional(!eAttribute.isRequired());
          property.setType(propertyType);
        }
        // The work-around for multiple EAttributes: introduce an intermediate containment with the
        // upper bound > 1
        else {
          String featureName =
              eFeature.getName().substring(0, 1).toUpperCase() + eFeature.getName().substring(1);
          Concept holderConcept = new Concept(classifier.getLanguage(), featureName + "Container");
          setIDAndKey(holderConcept, ePackage.getName() + "-" + holderConcept.getName());
          holderConcept.setAbstract(false);

          Property property = new Property("content", holderConcept);
          setIDAndKey(
              property,
              ePackage.getName() + "-" + holderConcept.getName() + "-" + property.getName());
          holderConcept.addFeature(property);
          property.setOptional(false);
          property.setType(propertyType);

          Containment containment = new Containment(eFeature.getName(), classifier);
          setIDAndKey(
              containment,
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          classifier.addFeature(containment);
          containment.setOptional(!eAttribute.isRequired());
          containment.setMultiple(eAttribute.isMany());
          containment.setType(holderConcept);
        }
      } else if (eFeature.eClass().getName().equals(EcorePackage.Literals.EREFERENCE.getName())) {
        EReference eReference = (EReference) eFeature;
        if (eReference.isContainment()) {
          Containment containment = new Containment(eFeature.getName(), classifier);
          setIDAndKey(
              containment,
              ePackage.getName() + "-" + classifier.getName() + "-" + eFeature.getName());
          containment.setOptional(!eReference.isRequired());
          containment.setMultiple(eReference.isMany());
          classifier.addFeature(containment);
          containment.setType(convertEClassifierToClassifier(eReference.getEType()));
        } else {
          Reference reference = new Reference(eFeature.getName(), classifier);
          setIDAndKey(
              reference,
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

  private void setIDAndKey(LanguageEntity modelElement, String idAndKey) {
    modelElement.setID(idAndKey);
    modelElement.setKey(idAndKey);
  }

  private void setIDAndKey(EnumerationLiteral modelElement, String idAndKey) {
    modelElement.setID(idAndKey);
    modelElement.setKey(idAndKey);
  }

  private void setIDAndKey(Language modelElement, String idAndKey) {
    modelElement.setID(idAndKey);
    modelElement.setKey(idAndKey);
  }

  private void setIDAndKey(Feature modelElement, String idAndKey) {
    modelElement.setID(idAndKey);
    modelElement.setKey(idAndKey);
  }
}
