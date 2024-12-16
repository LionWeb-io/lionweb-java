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

  // We may have one instance of this class per LionWeb version, and we initialize
  // them lazily
  private static Map<LionWebVersion, Language> INSTANCES = new HashMap<>();

  public static @Nonnull Concept getAnnotation() {
    return getInstance().requireConceptByName("Annotation");
  }

  public static @Nonnull Concept getAnnotation(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Annotation");
  }

  public static @Nonnull Concept getConcept() {
    return getInstance().requireConceptByName("Concept");
  }

  public static @Nonnull Concept getConcept(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Concept");
  }

  public static @Nonnull Concept getInterface() {
    return getInstance().requireConceptByName("Interface");
  }

  public static @Nonnull Concept getInterface(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Interface");
  }

  public static @Nonnull Concept getContainment() {
    return getInstance().requireConceptByName("Containment");
  }

  public static @Nonnull Concept getContainment(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Containment");
  }

  public static @Nonnull Concept getDataType() {
    return getInstance().requireConceptByName("DataType");
  }

  public static @Nonnull Concept getDataType(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("DataType");
  }

  public static @Nonnull Concept getEnumeration() {
    return getInstance().requireConceptByName("Enumeration");
  }

  public static @Nonnull Concept getEnumeration(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Enumeration");
  }

  public static @Nonnull Concept getEnumerationLiteral() {
    return getInstance().requireConceptByName("EnumerationLiteral");
  }

  public static @Nonnull Concept getEnumerationLiteral(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("EnumerationLiteral");
  }

  public static @Nonnull Concept getFeature() {
    return getInstance().requireConceptByName("Feature");
  }

  public static @Nonnull Concept getFeature(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Feature");
  }

  public static @Nonnull Concept getClassifier() {
    return getInstance().requireConceptByName("Classifier");
  }

  public static @Nonnull Concept getClassifier(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Classifier");
  }

  public static @Nonnull Concept getLink() {
    return getInstance().requireConceptByName("Link");
  }

  public static @Nonnull Concept getLink(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Link");
  }

  public static @Nonnull Concept getLanguage() {
    return getInstance().requireConceptByName("Language");
  }

  public static @Nonnull Concept getLanguage(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Language");
  }

  public static @Nonnull Concept getLanguageEntity() {
    return getInstance().requireConceptByName("LanguageEntity");
  }

  public static @Nonnull Concept getLanguageEntity(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("LanguageEntity");
  }

  public static @Nonnull Concept getPrimitiveType() {
    return getInstance().requireConceptByName("PrimitiveType");
  }

  public static @Nonnull Concept getPrimitiveType(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("PrimitiveType");
  }

  public static @Nonnull Concept getProperty() {
    return getInstance().requireConceptByName("Property");
  }

  public static @Nonnull Concept getProperty(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Property");
  }

  public static @Nonnull Concept getReference() {
    return getInstance().requireConceptByName("Reference");
  }

  public static @Nonnull Concept getReference(@Nonnull LionWebVersion lionWebVersion) {
    return getInstance(lionWebVersion).requireConceptByName("Reference");
  }

  public static @Nonnull Concept getStructuredDataType() {
    return getInstance().requireConceptByName("StructuredDataType");
  }

  public static @Nonnull Concept getStructuredDataType(@Nonnull LionWebVersion lionWebVersion) {
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      throw new IllegalArgumentException("StructuredDataTypes have been introduced in v2024.1");
    }
    return getInstance(lionWebVersion).requireConceptByName("StructuredDataType");
  }

  public static @Nonnull Concept getField() {
    return getInstance().requireConceptByName("Field");
  }

  public static @Nonnull Concept getField(@Nonnull LionWebVersion lionWebVersion) {
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      throw new IllegalArgumentException("StructuredDataTypes have been introduced in v2024.1");
    }
    return getInstance(lionWebVersion).requireConceptByName("Field");
  }

  public static @Nonnull Language getInstance() {
    return getInstance(LionWebVersion.currentVersion);
  }

  public static @Nonnull Language getInstance(@Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    String versionIDSuffix = "";
    if (lionWebVersion != LionWebVersion.v2023_1) {
      versionIDSuffix = "-" + lionWebVersion.getVersionString().replaceAll("\\.", "_");
    }
    if (!INSTANCES.containsKey(lionWebVersion)) {
      final Language instance = new Language(lionWebVersion, "LionCore_M3");
      instance.setID("-id-LionCore-M3" + versionIDSuffix);
      instance.setKey("LionCore-M3");
      instance.setVersion(lionWebVersion.getVersionString());

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
      Concept field = null;
      if (lionWebVersion != LionWebVersion.v2023_1) {
        field = instance.addElement(new Concept(lionWebVersion, "Field"));
      }
      Concept classifier = instance.addElement(new Concept(lionWebVersion, "Classifier"));
      Concept link = instance.addElement(new Concept(lionWebVersion, "Link"));
      Concept language = instance.addElement(new Concept(lionWebVersion, "Language"));
      Concept languageEntity = instance.addElement(new Concept(lionWebVersion, "LanguageEntity"));
      Interface iKeyed = instance.addElement(new Interface(lionWebVersion, "IKeyed"));
      Concept primitiveType = instance.addElement(new Concept(lionWebVersion, "PrimitiveType"));
      Concept property = instance.addElement(new Concept(lionWebVersion, "Property"));
      Concept reference = instance.addElement(new Concept(lionWebVersion, "Reference"));
      Concept structuredDataType = null;
      if (lionWebVersion != LionWebVersion.v2023_1) {
        structuredDataType = instance.addElement(new Concept(lionWebVersion, "StructuredDataType"));
      }

      // Now we start adding the features to all the Concepts and Interfaces

      concept.setExtendedConcept(classifier);
      concept.addFeature(
          Property.createRequired(
              lionWebVersion,
              "abstract",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Concept-abstract" + versionIDSuffix));
      concept.addFeature(
          Property.createRequired(
              lionWebVersion,
              "partition",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Concept-partition" + versionIDSuffix));
      concept.addFeature(
          Reference.createOptional(
              lionWebVersion, "extends", concept, "-id-Concept-extends" + versionIDSuffix));
      concept.addFeature(
          Reference.createMultiple(
              lionWebVersion, "implements", iface, "-id-Concept-implements" + versionIDSuffix));

      iface.setExtendedConcept(classifier);
      iface.addFeature(
          Reference.createMultiple(
              lionWebVersion, "extends", iface, "-id-Interface-extends" + versionIDSuffix));

      containment.setExtendedConcept(link);

      dataType.setExtendedConcept(languageEntity);
      dataType.setAbstract(true);

      enumeration.setExtendedConcept(dataType);
      enumeration.addFeature(
          Containment.createMultiple(lionWebVersion, "literals", enumerationLiteral)
              .setID("-id-Enumeration-literals" + versionIDSuffix));

      enumerationLiteral.addImplementedInterface(iKeyed);

      feature.setAbstract(true);
      feature.addImplementedInterface(iKeyed);
      feature.addFeature(
          Property.createRequired(
              lionWebVersion,
              "optional",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Feature-optional" + versionIDSuffix));

      classifier.setAbstract(true);
      classifier.setExtendedConcept(languageEntity);
      classifier.addFeature(
          Containment.createMultiple(
              lionWebVersion, "features", feature, "-id-Classifier-features" + versionIDSuffix));

      link.setAbstract(true);
      link.setExtendedConcept(feature);
      link.addFeature(
          Property.createRequired(
              lionWebVersion,
              "multiple",
              LionCoreBuiltins.getBoolean(lionWebVersion),
              "-id-Link-multiple" + versionIDSuffix));
      link.addFeature(
          Reference.createRequired(
              lionWebVersion, "type", classifier, "-id-Link-type" + versionIDSuffix));

      language.setPartition(true);
      language.addImplementedInterface(iKeyed);
      language.addFeature(
          Property.createRequired(
              lionWebVersion,
              "version",
              LionCoreBuiltins.getString(lionWebVersion),
              "-id-Language-version" + versionIDSuffix));
      language.addFeature(
          Reference.createMultiple(lionWebVersion, "dependsOn", language)
              .setID("-id-Language-dependsOn" + versionIDSuffix));
      language.addFeature(
          Containment.createMultiple(
                  lionWebVersion,
                  "entities",
                  languageEntity,
                  "-id-Language-entities" + versionIDSuffix)
              .setKey("Language-entities"));

      languageEntity.setAbstract(true);
      languageEntity.addImplementedInterface(iKeyed);

      primitiveType.setExtendedConcept(dataType);

      property.setExtendedConcept(feature);
      property.addFeature(
          Reference.createRequired(
                  lionWebVersion, "type", dataType, "-id-Property-type" + versionIDSuffix)
              .setKey("Property-type"));

      reference.setExtendedConcept(link);

      iKeyed.addExtendedInterface(LionCoreBuiltins.getINamed(lionWebVersion));
      iKeyed.addFeature(
          Property.createRequired(lionWebVersion, "key", LionCoreBuiltins.getString(lionWebVersion))
              .setID("-id-IKeyed-key" + versionIDSuffix));

      annotation.setExtendedConcept(classifier);
      annotation.addFeature(
          Reference.createOptional(
              lionWebVersion,
              "annotates",
              classifier,
              "-id-Annotation-annotates" + versionIDSuffix));
      annotation.addFeature(
          Reference.createOptional(
              lionWebVersion, "extends", annotation, "-id-Annotation-extends" + versionIDSuffix));
      annotation.addFeature(
          Reference.createMultiple(
              lionWebVersion, "implements", iface, "-id-Annotation-implements" + versionIDSuffix));

      if (lionWebVersion != LionWebVersion.v2023_1) {
        structuredDataType.setExtendedConcept(dataType);
        structuredDataType.addFeature(
            Containment.createMultiple(
                    lionWebVersion,
                    "fields",
                    field,
                    "-id-StructuredDataType-fields" + versionIDSuffix)
                .setOptional(false));

        field.addImplementedInterface(iKeyed);
        field.addFeature(
            Reference.createRequired(
                lionWebVersion, "type", dataType, "-id-Field-type" + versionIDSuffix));
      }

      checkIDs(instance, versionIDSuffix);
      INSTANCES.put(lionWebVersion, instance);
    }
    return INSTANCES.get(lionWebVersion);
  }

  private static void checkIDs(@Nonnull M3Node node, String versionIDSuffix) {
    if (node.getID() == null) {
      if (node instanceof NamespacedEntity) {
        NamespacedEntity namespacedEntity = (NamespacedEntity) node;
        node.setID("-id-" + namespacedEntity.getName().replaceAll("\\.", "_") + versionIDSuffix);
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
    getChildrenHelper(node).forEach(c -> checkIDs(c, versionIDSuffix));
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
