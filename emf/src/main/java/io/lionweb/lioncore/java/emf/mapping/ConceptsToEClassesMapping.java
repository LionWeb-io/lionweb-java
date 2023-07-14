package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.lioncore.java.emf.EMFMetamodelExporter;
import io.lionweb.lioncore.java.emf.EMFMetamodelImporter;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.ConceptInterface;
import io.lionweb.lioncore.java.language.Classifier;
import io.lionweb.lioncore.java.language.Language;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.jetbrains.annotations.Nullable;

public class ConceptsToEClassesMapping {

  private Map<EPackage, Language> ePackagesToLanguages = new HashMap<>();
  private Map<Language, EPackage> languagesToEPackages = new HashMap<>();
  private Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private Map<EClass, ConceptInterface> eClassesToConceptInterfaces = new HashMap<>();
  private Map<Concept, EClass> conceptsToEClasses = new HashMap<>();
  private Map<ConceptInterface, EClass> conceptInterfacesToEClasses = new HashMap<>();

  private void processEPackage(EPackage ePackage) {
    Objects.requireNonNull(ePackage, "ePackage should not be null");
    EMFMetamodelImporter EMFMetamodelImporter = new EMFMetamodelImporter(this);
    Language language = EMFMetamodelImporter.importEPackage(ePackage);
    ePackagesToLanguages.put(ePackage, language);
    languagesToEPackages.put(language, ePackage);
    ePackage
        .eAllContents()
        .forEachRemaining(
            eObject -> {
              if (eObject instanceof EClass) {
                EClass eClass = (EClass) eObject;
                if (!eClass.isInterface()) {
                  registerMapping(language.getConceptByName(eClass.getName()), eClass);
                }
              }
            });
  }

  private void processMetamodel(Language language) {
    Objects.requireNonNull(language, "Language should not be null");
    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter(this);
    EPackage ePackage = ecoreExporter.exportLanguage(language);
    ePackagesToLanguages.put(ePackage, language);
    languagesToEPackages.put(language, ePackage);
    ePackage
        .eAllContents()
        .forEachRemaining(
            eObject -> {
              if (eObject instanceof EClass) {
                EClass eClass = (EClass) eObject;
                if (!eClass.isInterface()) {
                  registerMapping(language.getConceptByName(eClass.getName()), eClass);
                }
              }
            });
  }

  public Concept getCorrespondingConcept(EClass eClass) {
    if (!eClassesToConcepts.containsKey(eClass)) {
      if (ePackagesToLanguages.containsKey(eClass.getEPackage())) {
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
      if (ePackagesToLanguages.containsKey(eClass.getEPackage())) {
        throw new IllegalStateException();
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToConceptInterfaces.containsKey(eClass)) {
      throw new IllegalStateException();
    }
    return eClassesToConceptInterfaces.get(eClass);
  }

  public EClassifier getCorrespondingEClass(Classifier type) {
    if (!conceptsToEClasses.containsKey(type) && !conceptInterfacesToEClasses.containsKey(type)) {
      if (languagesToEPackages.containsKey(type.getLanguage())) {
        throw new IllegalStateException();
      }
      processMetamodel(type.getLanguage());
    }
    if (conceptsToEClasses.containsKey(type)) {
      return conceptsToEClasses.get(type);
    } else if (conceptInterfacesToEClasses.containsKey(type)) {
      return conceptInterfacesToEClasses.get(type);
    } else {
      throw new IllegalStateException();
    }
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
    return eClassesToConcepts.containsKey(eClassifier)
        || eClassesToConceptInterfaces.containsKey(eClassifier);
  }

  public @Nullable Classifier getCorrespondingFeaturesContainer(EClassifier eClassifier) {
    if (eClassesToConcepts.containsKey(eClassifier)) {
      return eClassesToConcepts.get(eClassifier);
    }
    if (eClassesToConceptInterfaces.containsKey(eClassifier)) {
      return eClassesToConceptInterfaces.get(eClassifier);
    }
    return null;
  }
}
