package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.emf.ecore.impl.EcorePackageImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

public class EcoreImporter {
  private Map<EPackage, Metamodel> packagesToMetamodels = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private Map<EClass, ConceptInterface> eClassesToConceptInterfacess = new HashMap<>();

  public List<Metamodel> importEcoreFile(File ecoreFile) {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
    extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());

    ResourceSet resourceSet = new ResourceSetImpl();

    Resource resource =
        resourceSet.getResource(URI.createFileURI(ecoreFile.getAbsolutePath()), true);
    return importResource(resource);
  }

  enum ResourceType {
    XML,
    JSON,
    ECORE
  }

  public List<Metamodel> importEcoreInputStream(InputStream inputStream) throws IOException {
    return importEcoreInputStream(inputStream, ResourceType.ECORE);
  }

  public List<Metamodel> importEcoreInputStream(InputStream inputStream, ResourceType resourceType) throws IOException {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
    extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());
    extensionsToFactoryMap.put("json", new JsonResourceFactory());

    ResourceSet resourceSet = new ResourceSetImpl();

    URI uri;
    switch (resourceType) {
      case ECORE:
        uri = URI.createFileURI("dummy.ecore");
        break;
      case XML:
        uri = URI.createFileURI("dummy.xml");
        break;
      case JSON:
        uri = URI.createFileURI("dummy.json");
        break;
      default:
        throw new UnsupportedOperationException();
    }

    Resource resource = resourceSet.createResource(uri);
    resourceSet.getPackageRegistry().put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
    resource.load(inputStream, new HashMap<>());
    return importResource(resource);
  }

  public List<Metamodel> importResource(Resource resource) {
    List<Metamodel> metamodels = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      if (content.eClass().getName().equals(EcorePackage.Literals.EPACKAGE.getName())) {
        metamodels.add(importEPackage((EPackage) content));
      }
    }
    return metamodels;
  }

  private DataType convertEClassifierToDataType(EClassifier eClassifier) {
    if (eClassifier.equals(EcorePackage.Literals.ESTRING)) {
      return LionCoreBuiltins.getString();
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
          throw new UnsupportedOperationException();
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

          for (EStructuralFeature eFeature : eClass.getEStructuralFeatures()) {
            if (eFeature.eClass().getName().equals(EcorePackage.Literals.EATTRIBUTE.getName())) {
              EAttribute eAttribute = (EAttribute) eFeature;
              Property property = new Property(eFeature.getName(), concept);
              property.setID(
                  ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
              property.setKey(
                  ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
              concept.addFeature(property);
              property.setOptional(!eAttribute.isRequired());
              property.setDerived(eAttribute.isDerived());
              property.setType(convertEClassifierToDataType(eFeature.getEType()));
              if (eAttribute.isMany()) {
                throw new IllegalArgumentException(
                    "EAttributes with upper bound > 1 are not supported");
              }
            } else if (eFeature
                .eClass()
                .getName()
                .equals(EcorePackage.Literals.EREFERENCE.getName())) {
              EReference eReference = (EReference) eFeature;
              if (eReference.isContainment()) {
                Containment containment = new Containment(eFeature.getName(), concept);
                containment.setID(
                    ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
                containment.setKey(
                    ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
                containment.setOptional(!eReference.isRequired());
                containment.setMultiple(eReference.isMany());
                concept.addFeature(containment);
                containment.setType(convertEClassifierToFeaturesContainer(eReference.getEType()));
              } else {
                Reference reference = new Reference(eFeature.getName(), concept);
                reference.setID(
                    ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
                reference.setKey(
                    ePackage.getName() + "-" + concept.getName() + "-" + eFeature.getName());
                reference.setOptional(!eReference.isRequired());
                reference.setMultiple(eReference.isMany());
                concept.addFeature(reference);
                reference.setType(convertEClassifierToFeaturesContainer(eReference.getEType()));
              }
            } else {
              throw new UnsupportedOperationException();
            }
          }
        }
      } else {
        throw new UnsupportedOperationException();
      }
    }
    return metamodel;
  }
}
