package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.impl.M3Node;
import java.util.*;
import javax.annotation.Nonnull;

public class LionCore {

  private LionCore() {
    // prevent instantiation of instances outside of this class
  }

  private static Map<LionWebVersion, Language> INSTANCES = new HashMap<>();

  public static Concept getAnnotation() {
    return getInstance().requireConceptByName("Annotation");
  }

  public static Concept getConcept() {
    return getInstance().requireConceptByName("Concept");
  }

  public static Concept getInterface() {
    return getInstance().requireConceptByName("Interface");
  }

  public static Concept getContainment() {
    return getInstance().requireConceptByName("Containment");
  }

  public static Concept getDataType() {
    return getInstance().requireConceptByName("DataType");
  }

  public static Concept getEnumeration() {
    return getInstance().requireConceptByName("Enumeration");
  }

  public static Concept getEnumerationLiteral() {
    return getInstance().requireConceptByName("EnumerationLiteral");
  }

  public static Concept getFeature() {
    return getInstance().requireConceptByName("Feature");
  }

  public static Concept getClassifier() {
    return getInstance().requireConceptByName("Classifier");
  }

  public static Concept getLink() {
    return getInstance().requireConceptByName("Link");
  }

  public static Concept getLanguage() {
    return getInstance().requireConceptByName("Language");
  }

  public static Concept getLanguageEntity() {
    return getInstance().requireConceptByName("LanguageEntity");
  }

  public static Concept getPrimitiveType() {
    return getInstance().requireConceptByName("PrimitiveType");
  }

  public static Concept getProperty() {
    return getInstance().requireConceptByName("Property");
  }

  public static Concept getReference() {
    return getInstance().requireConceptByName("Reference");
  }

  public static Concept getAnnotation(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Annotation");
  }

  public static Concept getConcept(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Concept");
  }

  public static Concept getInterface(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Interface");
  }

  public static Concept getContainment(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Containment");
  }

  public static Concept getDataType(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("DataType");
  }

  public static Concept getEnumeration(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Enumeration");
  }

  public static Concept getEnumerationLiteral(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("EnumerationLiteral");
  }

  public static Concept getFeature(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Feature");
  }

  public static Concept getClassifier(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Classifier");
  }

  public static Concept getLink(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Link");
  }

  public static Concept getLanguage(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Language");
  }

  public static Concept getLanguageEntity(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("LanguageEntity");
  }

  public static Concept getPrimitiveType(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("PrimitiveType");
  }

  public static Concept getProperty(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Property");
  }

  public static Concept getReference(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Reference");
  }

  public static Language getInstance() {
    return getInstance(LionWebVersion.currentVersion);
  }

  public static Language getInstance(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    if (!INSTANCES.containsKey(lionWebVersion)) {
      final Language instance = new Language(lionWebVersion, "LionCore_M3");
      instance.setID("-id-LionCore-M3");
      instance.setKey("LionCore-M3");
      instance.setVersion(lionWebVersion.getValue());

      // We first instantiate all Concepts and Interfaces
      // we add features only after as the features will have references to these elements
      Concept annotation = instance.addElement(new Concept(lionWebVersion, "Annotation"));
      Concept concept = instance.addElement(new Concept(lionWebVersion, "Concept"));
      Concept iface = instance.addElement(new Concept(lionWebVersion, "Interface"));
      Concept containment = instance.addElement(new Concept(lionWebVersion, "Containment"));
      Concept dataType = instance.addElement(new Concept(lionWebVersion, "DataType"));
      Concept enumeration = instance.addElement(new Concept(lionWebVersion, "Enumeration"));
      Concept enumerationLiteral =
          instance.addElement(new Concept(lionWebVersion, "EnumerationLiteral"));
      Concept feature = instance.addElement(new Concept(lionWebVersion, "Feature"));
      Concept classifier = instance.addElement(new Concept(lionWebVersion, "Classifier"));
      Concept link = instance.addElement(new Concept(lionWebVersion, "Link"));
      Concept language = instance.addElement(new Concept(lionWebVersion, "Language"));
      Concept languageEntity = instance.addElement(new Concept(lionWebVersion, "LanguageEntity"));
      Interface iKeyed = instance.addElement(new Interface(lionWebVersion, "IKeyed"));
      Concept primitiveType = instance.addElement(new Concept(lionWebVersion, "PrimitiveType"));
      Concept property = instance.addElement(new Concept(lionWebVersion, "Property"));
      Concept reference = instance.addElement(new Concept(lionWebVersion, "Reference"));

      // Now we start adding the features to all the Concepts and Interfaces

      concept.setExtendedConcept(classifier);
      concept.addFeature(
          Property.createRequired(
              lionWebVersion,
              "abstract",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Concept-abstract"));
      concept.addFeature(
          Property.createRequired(
              lionWebVersion,
              "partition",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Concept-partition"));
      concept.addFeature(
          Reference.createOptional(lionWebVersion, "extends", concept, "-id-Concept-extends"));
      concept.addFeature(
          Reference.createMultiple(lionWebVersion, "implements", iface, "-id-Concept-implements"));

      iface.setExtendedConcept(classifier);
      iface.addFeature(
          Reference.createMultiple(lionWebVersion, "extends", iface, "-id-Interface-extends"));

      containment.setExtendedConcept(link);

      dataType.setExtendedConcept(languageEntity);
      dataType.setAbstract(true);

      enumeration.setExtendedConcept(dataType);
      enumeration.addFeature(
          Containment.createMultiple(lionWebVersion, "literals", enumerationLiteral)
              .setID("-id-Enumeration-literals"));

      enumerationLiteral.addImplementedInterface(iKeyed);

      feature.setAbstract(true);
      feature.addImplementedInterface(iKeyed);
      feature.addFeature(
          Property.createRequired(
              lionWebVersion,
              "optional",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Feature-optional"));

      classifier.setAbstract(true);
      classifier.setExtendedConcept(languageEntity);
      classifier.addFeature(
          Containment.createMultiple(
              lionWebVersion, "features", feature, "-id-Classifier-features"));

      link.setAbstract(true);
      link.setExtendedConcept(feature);
      link.addFeature(
          Property.createRequired(
              lionWebVersion,
              "multiple",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Link-multiple"));
      link.addFeature(
          Reference.createRequired(lionWebVersion, "type", classifier, "-id-Link-type"));

      language.setPartition(true);
      language.addImplementedInterface(iKeyed);
      language.addFeature(
          Property.createRequired(
              lionWebVersion,
              "version",
              LionCoreBuiltins.getString(lionWebVersion),
              "-id-Language-version"));
      language.addFeature(
          Reference.createMultiple(lionWebVersion, "dependsOn", language)
              .setID("-id-Language-dependsOn"));
      language.addFeature(
          Containment.createMultiple(
                  lionWebVersion, "entities", languageEntity, "-id-Language-entities")
              .setKey("Language-entities"));

      languageEntity.setAbstract(true);
      languageEntity.addImplementedInterface(iKeyed);

      primitiveType.setExtendedConcept(dataType);

      property.setExtendedConcept(feature);
      property.addFeature(
          Reference.createRequired(lionWebVersion, "type", dataType, "-id-Property-type")
              .setKey("Property-type"));

      reference.setExtendedConcept(link);

      iKeyed.addExtendedInterface(LionCoreBuiltins.getINamed(lionWebVersion));
      iKeyed.addFeature(
          Property.createRequired(lionWebVersion, "key", LionCoreBuiltins.getString(lionWebVersion))
              .setID("-id-IKeyed-key"));

      annotation.setExtendedConcept(classifier);
      annotation.addFeature(
          Reference.createOptional(
              lionWebVersion, "annotates", classifier, "-id-Annotation-annotates"));
      annotation.addFeature(
          Reference.createOptional(
              lionWebVersion, "extends", annotation, "-id-Annotation-extends"));
      annotation.addFeature(
          Reference.createMultiple(
              lionWebVersion, "implements", iface, "-id-Annotation-implements"));

      checkIDs(instance);
      INSTANCES.put(lionWebVersion, instance);
    }
    return INSTANCES.get(lionWebVersion);
  }

  private static void checkIDs(M3Node node) {
    if (node.getID() == null) {
      if (node instanceof NamespacedEntity) {
        NamespacedEntity namespacedEntity = (NamespacedEntity) node;
        node.setID("-id-" + namespacedEntity.getName().replaceAll("\\.", "_"));
        if (node instanceof IKeyed<?> && ((IKeyed<?>) node).getKey() == null) {
          ((IKeyed<?>) node).setKey(namespacedEntity.getName());
        }
      } else {
        throw new IllegalStateException(node.toString());
      }
    }
    if (node instanceof Classifier<?>) {
      Classifier<?> classifier = (Classifier<?>) node;
      classifier
          .getFeatures()
          .forEach(
              feature -> {
                if (feature.getKey() == null) {
                  feature.setKey(classifier.getName() + "-" + feature.getName());
                }
              });
    }

    // TODO To be changed once getChildren is implemented correctly
    getChildrenHelper(node).forEach(c -> checkIDs(c));
  }

  private static List<? extends M3Node> getChildrenHelper(M3Node node) {
    if (node instanceof Language) {
      return ((Language) node).getElements();
    } else if (node instanceof Classifier) {
      return ((Classifier) node).getFeatures();
    } else if (node instanceof Feature) {
      return Collections.emptyList();
    } else {
      throw new UnsupportedOperationException("Unsupported " + node);
    }
  }
}
