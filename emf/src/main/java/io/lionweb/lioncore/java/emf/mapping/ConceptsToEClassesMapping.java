package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.lioncore.java.emf.EcoreImporter;
import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.metamodel.FeaturesContainer;
import io.lionweb.lioncore.java.metamodel.Metamodel;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

public class ConceptsToEClassesMapping {

  private Map<EPackage, Metamodel> ePackagesToMetamodels = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private Map<Concept, EClass> conceptsToEClasses = new HashMap<>();
  private EcoreImporter ecoreImporter = new EcoreImporter();

  private void processEPackage(EPackage ePackage) {
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

  public EClassifier getCorrespondingEClass(FeaturesContainer type) {
    return conceptsToEClasses.get(type);
  }

  public void registerMapping(Concept concept, EClass eClass) {
    eClassesToConcepts.put(eClass, concept);
    conceptsToEClasses.put(concept, eClass);
  }
}
