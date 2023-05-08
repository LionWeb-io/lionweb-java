package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.lioncore.java.emf.EcoreImporter;
import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.metamodel.ConceptInterface;
import io.lionweb.lioncore.java.metamodel.FeaturesContainer;
import io.lionweb.lioncore.java.metamodel.Metamodel;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.jetbrains.annotations.Nullable;

public class ConceptsToEClassesMapping {

  private Map<EPackage, Metamodel> ePackagesToMetamodels = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private Map<EClass, ConceptInterface> eClassesToConceptInterfaces = new HashMap<>();
  private Map<Concept, EClass> conceptsToEClasses = new HashMap<>();
  private Map<ConceptInterface, EClass> conceptInterfacesToEClasses = new HashMap<>();


  private void processEPackage(EPackage ePackage) {
    EcoreImporter ecoreImporter = new EcoreImporter(this);
    Metamodel metamodel = ecoreImporter.importEPackage(ePackage);
    ePackagesToMetamodels.put(ePackage, metamodel);
    ePackage
        .eAllContents()
        .forEachRemaining(
            eObject -> {
              if (eObject instanceof EClass) {
                EClass eClass = (EClass) eObject;
                if (!eClass.isInterface()) {
                  registerMapping(metamodel.getConceptByName(eClass.getName()), eClass);
                }
              }
            });
  }

  public Concept getCorrespondingConcept(EClass eClass) {
    if (!eClassesToConcepts.containsKey(eClass)) {
      if (ePackagesToMetamodels.containsKey(eClass.getEPackage())) {
        throw new IllegalStateException();
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToConcepts.containsKey(eClass)) {
      throw new IllegalStateException();
    }
    return eClassesToConcepts.get(eClass);
  }

  public ConceptInterface getCorrespondingConceptInterface(EClass eClass) {
    if (!eClassesToConceptInterfaces.containsKey(eClass)) {
      if (ePackagesToMetamodels.containsKey(eClass.getEPackage())) {
        throw new IllegalStateException();
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToConceptInterfaces.containsKey(eClass)) {
      throw new IllegalStateException();
    }
    return eClassesToConceptInterfaces.get(eClass);
  }

  public EClassifier getCorrespondingEClass(FeaturesContainer type) {
    return conceptsToEClasses.get(type);
  }


  public void registerMapping(Concept concept, EClass eClass) {
    eClassesToConcepts.put(eClass, concept);
    conceptsToEClasses.put(concept, eClass);
  }

  public void registerMapping(ConceptInterface conceptInterface, EClass eClass) {
    eClassesToConceptInterfaces.put(eClass, conceptInterface);
    conceptInterfacesToEClasses.put(conceptInterface, eClass);
  }

  public boolean knows(EClassifier eClassifier) {
    return eClassesToConcepts.containsKey(eClassifier) || eClassesToConceptInterfaces.containsKey(eClassifier);
  }

  public @Nullable FeaturesContainer getCorrespondingFeaturesContainer(EClassifier eClassifier) {
    if (eClassesToConcepts.containsKey(eClassifier)) {
      return eClassesToConcepts.get(eClassifier);
    }
    if (eClassesToConceptInterfaces.containsKey(eClassifier)) {
      return eClassesToConceptInterfaces.get(eClassifier);
    }
    return null;
  }
}
