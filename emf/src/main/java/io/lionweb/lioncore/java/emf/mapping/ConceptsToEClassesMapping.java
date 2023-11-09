package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.java.emf.builtins.BuiltinsPackage;
import io.lionweb.lioncore.java.emf.EMFMetamodelExporter;
import io.lionweb.lioncore.java.emf.EMFMetamodelImporter;
import io.lionweb.lioncore.java.language.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.emf.ecore.*;
import org.jetbrains.annotations.Nullable;

public class ConceptsToEClassesMapping {

  private final Map<EPackage, Language> ePackagesToLanguages = new HashMap<>();
  private final Map<Language, EPackage> languagesToEPackages = new HashMap<>();
  private final Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private final Map<EClass, Interface> eClassesToInterfaces = new HashMap<>();
  private final Map<Concept, EClass> conceptsToEClasses = new HashMap<>();
  private final Map<Interface, EClass> interfacesToEClasses = new HashMap<>();

  /** Creates a mapping with pre-populated builtins. */
  public ConceptsToEClassesMapping() {
    this(true);
  }

  /** @param prePopulateBuiltins Whether builtins should be pre-populated in this mapping. */
  public ConceptsToEClassesMapping(boolean prePopulateBuiltins) {
    if (prePopulateBuiltins) {
      prePopulateBuiltins();
    }
  }

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
        throw new IllegalStateException(
            "Cannot find corresponding Concept for EClass "
                + eClass.getName()
                + " in EPackage "
                + eClass.getEPackage().getName());
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToConcepts.containsKey(eClass)) {
      throw new IllegalStateException(
          "Cannot find corresponding Concept for EClass " + eClass.getName());
    }
    return eClassesToConcepts.get(eClass);
  }

  public Interface getCorrespondingInterface(EClass eClass) {
    if (!eClassesToInterfaces.containsKey(eClass)) {
      if (ePackagesToLanguages.containsKey(eClass.getEPackage())) {
        throw new IllegalStateException(
            "Cannot find corresponding Interface for EClass "
                + eClass.getName()
                + " in EPackage "
                + eClass.getEPackage().getName());
      }
      processEPackage(eClass.getEPackage());
    }
    if (!eClassesToInterfaces.containsKey(eClass)) {
      throw new IllegalStateException(
          "Cannot find corresponding Interface for EClass " + eClass.getName());
    }
    return eClassesToInterfaces.get(eClass);
  }

  public EClassifier getCorrespondingEClass(Classifier type) {
    if (!conceptsToEClasses.containsKey(type) && !interfacesToEClasses.containsKey(type)) {
      if (languagesToEPackages.containsKey(type.getLanguage())) {
        throw new IllegalStateException(
            "Cannot find corresponding EClassifier for Classifier "
                + type.getName()
                + " in Language "
                + type.getLanguage().getName());
      }
      processMetamodel(type.getLanguage());
    }
    if (conceptsToEClasses.containsKey(type)) {
      return conceptsToEClasses.get(type);
    } else if (interfacesToEClasses.containsKey(type)) {
      return interfacesToEClasses.get(type);
    } else {
      throw new IllegalStateException(
          "Cannot find corresponding EClassifier for Classifier " + type.getName());
    }
  }

  public void registerMapping(Concept concept, EClass eClass) {
    eClassesToConcepts.put(eClass, concept);
    conceptsToEClasses.put(concept, eClass);
  }

  public void registerMapping(Interface iface, EClass eClass) {
    eClassesToInterfaces.put(eClass, iface);
    interfacesToEClasses.put(iface, eClass);
  }

  public void registerMapping(Language language, EPackage ePackage) {
    for (LanguageEntity entity : language.getElements()) {
      EClassifier eClassifier = ePackage.getEClassifier(entity.getName());
      if (entity instanceof Classifier) {
        Classifier classifier = (Classifier) entity;
        if (eClassifier != null && knows(classifier)) {
          EClassifier correspondingEClass = getCorrespondingEClass(classifier);
          if (!eClassifier.equals(correspondingEClass)) {
            throw new IllegalStateException(
                "Classifier "
                    + classifier.getName()
                    + " is already mapped to "
                    + correspondingEClass
                    + ", but would be re-mapped to "
                    + eClassifier);
          }
          continue;
        }
      }

      if (entity instanceof Concept && eClassifier instanceof EClass) {
        registerMapping((Concept) entity, (EClass) eClassifier);
      } else if (entity instanceof Interface && eClassifier instanceof EClass) {
        registerMapping((Interface) entity, (EClass) eClassifier);
      } else if (entity instanceof Annotation) {
        // fall-through
      } else if (entity instanceof Classifier && eClassifier == null) {
        throw new IllegalStateException(
            "Can't find corresponding EClassifier for Classifier "
                + entity.getName()
                + " in EPackage "
                + ePackage.getName());
      }
    }

    ePackagesToLanguages.put(ePackage, language);
    languagesToEPackages.put(language, ePackage);
  }

  public boolean knows(EClassifier eClassifier) {
    return eClassesToConcepts.containsKey(eClassifier)
        || eClassesToInterfaces.containsKey(eClassifier);
  }

  public boolean knows(Classifier classifier) {
    return conceptsToEClasses.containsKey(classifier)
        || interfacesToEClasses.containsKey(classifier);
  }

  public @Nullable Classifier getCorrespondingClassifier(EClassifier eClassifier) {
    if (eClassesToConcepts.containsKey(eClassifier)) {
      return eClassesToConcepts.get(eClassifier);
    }
    if (eClassesToInterfaces.containsKey(eClassifier)) {
      return eClassesToInterfaces.get(eClassifier);
    }
    return null;
  }

  public void prePopulateBuiltins() {
    ePackagesToLanguages.put(BuiltinsPackage.eINSTANCE, LionCoreBuiltins.getInstance());
    languagesToEPackages.put(LionCoreBuiltins.getInstance(), BuiltinsPackage.eINSTANCE);
    registerMapping(LionCoreBuiltins.getNode(), EcorePackage.eINSTANCE.getEObject());
    registerMapping(LionCoreBuiltins.getINamed(), BuiltinsPackage.eINSTANCE.getINamed());
  }
}
