package io.lionweb.lioncore.java.emf.mapping;

import io.lionweb.java.emf.builtins.BuiltinsPackage;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.emf.EMFMetamodelExporter;
import io.lionweb.lioncore.java.emf.EMFMetamodelImporter;
import io.lionweb.lioncore.java.language.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.xml.type.impl.XMLTypePackageImpl;
import org.jetbrains.annotations.Nullable;

public class LanguageEntitiesToEElementsMapping {

  private final Map<EPackage, Language> ePackagesToLanguages = new HashMap<>();
  private final Map<Language, EPackage> languagesToEPackages = new HashMap<>();
  private final Map<EClass, Concept> eClassesToConcepts = new HashMap<>();
  private final Map<EClass, Interface> eClassesToInterfaces = new HashMap<>();
  private final Map<Concept, EClass> conceptsToEClasses = new HashMap<>();
  private final Map<Interface, EClass> interfacesToEClasses = new HashMap<>();

  private final Map<EEnum, Enumeration> eEnumsToEnumerations = new HashMap<>();
  private final Map<Enumeration, EEnum> enumerationsToEEnums = new HashMap<>();
  private final Map<EDataType, PrimitiveType> eDataTypesToPrimitiveTypes = new HashMap<>();
  private final Map<PrimitiveType, EDataType> primitiveTypesToEDataTypes = new HashMap<>();

  private @Nonnull LionWebVersion lionWebVersion;

  /** Creates a mapping with pre-populated builtins. */
  public LanguageEntitiesToEElementsMapping() {
    this(LionWebVersion.currentVersion, true);
  }

  public LanguageEntitiesToEElementsMapping(@Nonnull LionWebVersion lionWebVersion) {
    this(lionWebVersion, true);
  }

  /** @param prePopulateBuiltins Whether builtins should be pre-populated in this mapping. */
  public LanguageEntitiesToEElementsMapping(
      @Nonnull LionWebVersion lionWebVersion, boolean prePopulateBuiltins) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
    if (prePopulateBuiltins) {
      prePopulateBuiltins(lionWebVersion);
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
    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter(lionWebVersion, this);
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

  public Enumeration getCorrespondingEnumeration(EEnum eEnum) {
    if (!eEnumsToEnumerations.containsKey(eEnum)) {
      if (ePackagesToLanguages.containsKey(eEnum.getEPackage())) {
        throw new IllegalStateException(
                "Cannot find corresponding Enumeration for EEnum "
                        + eEnum.getName()
                        + " in EPackage "
                        + eEnum.getEPackage().getName());
      }
      processEPackage(eEnum.getEPackage());
    }
    if (!eEnumsToEnumerations.containsKey(eEnum)) {
      throw new IllegalStateException(
              "Cannot find corresponding Enumeration for EEnum " + eEnum.getName());
    }
    return eEnumsToEnumerations.get(eEnum);
  }

  public EEnum getCorrespondingEEnum(Enumeration enumeration) {
    if (!enumerationsToEEnums.containsKey(enumeration)) {
      if (languagesToEPackages.containsKey(enumeration.getLanguage())) {
        throw new IllegalStateException(
                "Cannot find corresponding EEnum for Enumeration "
                        + enumeration.getName()
                        + " in Language "
                        + enumeration.getLanguage().getName());
      }
      processMetamodel(enumeration.getLanguage());
    }
    if (!enumerationsToEEnums.containsKey(enumeration)) {
      throw new IllegalStateException(
              "Cannot find corresponding EEnum for Enumeration " + enumeration.getName());
    }
    return enumerationsToEEnums.get(enumeration);
  }

  public EDataType getCorrespondingEDataType(DataType dataType) {
    if (dataType instanceof Enumeration) {
      return getCorrespondingEEnum((Enumeration) dataType);
    }
    if(!primitiveTypesToEDataTypes.containsKey(dataType)) {
      if (languagesToEPackages.containsKey(dataType.getLanguage())) {
        throw new IllegalStateException(
                "Cannot find corresponding EDataType for DataType "
                        + dataType.getName()
                        + " in Language "
                        + dataType.getLanguage().getName());
      }
      processMetamodel(dataType.getLanguage());
    }
    if(!primitiveTypesToEDataTypes.containsKey(dataType)) {
      throw new IllegalStateException(
              "Cannot find corresponding EDataType for DataType " + dataType.getName());
    }
    return primitiveTypesToEDataTypes.get(dataType);
  }

  public DataType getCorrespondingDataType(EDataType eDataType) {
    if (eDataType instanceof EEnum) {
      return getCorrespondingEnumeration((EEnum) eDataType);
    }
    if(!eDataTypesToPrimitiveTypes.containsKey(eDataType)) {
      if (ePackagesToLanguages.containsKey(eDataType.getEPackage())) {
        throw new IllegalStateException(
                "Cannot find corresponding PrimitiveType for EDataType "
                        + eDataType.getName()
                        + " in EPackage "
                        + eDataType.getEPackage().getName());
      }
      processEPackage(eDataType.getEPackage());
    }
    if(!eDataTypesToPrimitiveTypes.containsKey(eDataType)) {
      throw new IllegalStateException(
              "Cannot find corresponding PrimitiveType for EDataType " + eDataType.getName());
    }
    return eDataTypesToPrimitiveTypes.get(eDataType);
  }

  public void registerMapping(Concept concept, EClass eClass) {
    eClassesToConcepts.put(eClass, concept);
    conceptsToEClasses.put(concept, eClass);
  }

  public void registerMapping(Interface iface, EClass eClass) {
    eClassesToInterfaces.put(eClass, iface);
    interfacesToEClasses.put(iface, eClass);
  }

  public void registerMapping(Enumeration enumeration, EEnum eEnum) {
    eEnumsToEnumerations.put(eEnum, enumeration);
    enumerationsToEEnums.put(enumeration, eEnum);
  }

  public void registerMapping(PrimitiveType primitiveType, EDataType eDataType) {
    eDataTypesToPrimitiveTypes.put(eDataType, primitiveType);
    primitiveTypesToEDataTypes.put(primitiveType, eDataType);
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

  public void prePopulateBuiltins(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    ePackagesToLanguages.put(
        BuiltinsPackage.eINSTANCE, LionCoreBuiltins.getInstance(lionWebVersion));
    languagesToEPackages.put(
        LionCoreBuiltins.getInstance(lionWebVersion), BuiltinsPackage.eINSTANCE);
    registerMapping(
            LionCoreBuiltins.getNode(lionWebVersion), EcorePackage.eINSTANCE.getEObject());
    registerMapping(
        LionCoreBuiltins.getINamed(lionWebVersion), BuiltinsPackage.eINSTANCE.getINamed());
    registerMapping(LionCoreBuiltins.getBoolean(lionWebVersion), EcorePackage.eINSTANCE.getEBoolean());
    registerMapping(LionCoreBuiltins.getInteger(lionWebVersion), EcorePackage.eINSTANCE.getEInt());
    registerMapping(LionCoreBuiltins.getString(lionWebVersion), EcorePackage.eINSTANCE.getEString());

    // Also add type literals from XMLTypePackageImpl
    eDataTypesToPrimitiveTypes.put(XMLTypePackageImpl.Literals.STRING, LionCoreBuiltins.getString());
    eDataTypesToPrimitiveTypes.put(XMLTypePackageImpl.Literals.INT, LionCoreBuiltins.getInteger());
    eDataTypesToPrimitiveTypes.put(XMLTypePackageImpl.Literals.BOOLEAN, LionCoreBuiltins.getBoolean());
  }

  public @Nonnull LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }
}
