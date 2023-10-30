package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.impl.M3Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.util.*;

public class LionCore {

  private LionCore() {
    // prevent instantiation of instances outside of this class
  }

  private static Language INSTANCE;

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

  public static Language getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Language("LionCore_M3");
      INSTANCE.setID("-id-LionCore-M3");
      INSTANCE.setKey("LionCore-M3");
      INSTANCE.setVersion(JsonSerialization.DEFAULT_SERIALIZATION_FORMAT);

      // We first instantiate all Concepts and Interfaces
      // we add features only after as the features will have references to these elements
      Concept annotation = INSTANCE.addElement(new Concept("Annotation"));
      Concept concept = INSTANCE.addElement(new Concept("Concept"));
      Concept iface = INSTANCE.addElement(new Concept("Interface"));
      Concept containment = INSTANCE.addElement(new Concept("Containment"));
      Concept dataType = INSTANCE.addElement(new Concept("DataType"));
      Concept enumeration = INSTANCE.addElement(new Concept("Enumeration"));
      Concept enumerationLiteral = INSTANCE.addElement(new Concept("EnumerationLiteral"));
      Concept feature = INSTANCE.addElement(new Concept("Feature"));
      Concept classifier = INSTANCE.addElement(new Concept("Classifier"));
      Concept link = INSTANCE.addElement(new Concept("Link"));
      Concept language = INSTANCE.addElement(new Concept("Language"));
      Concept languageEntity = INSTANCE.addElement(new Concept("LanguageEntity"));
      Interface iKeyed = INSTANCE.addElement(new Interface("IKeyed"));
      Concept primitiveType = INSTANCE.addElement(new Concept("PrimitiveType"));
      Concept property = INSTANCE.addElement(new Concept("Property"));
      Concept reference = INSTANCE.addElement(new Concept("Reference"));

      // Now we start adding the features to all the Concepts and Interfaces

      concept.setExtendedConcept(classifier);
      concept.addFeature(
          Property.createRequired(
              "abstract", LionCoreBuiltins.getBoolean(), "-id-Concept-abstract"));
      concept.addFeature(
          Property.createRequired(
              "partition", LionCoreBuiltins.getBoolean(), "-id-Concept-partition"));
      concept.addFeature(Reference.createOptional("extends", concept, "-id-Concept-extends"));
      concept.addFeature(Reference.createMultiple("implements", iface, "-id-Concept-implements"));

      iface.setExtendedConcept(classifier);
      iface.addFeature(Reference.createMultiple("extends", iface, "-id-Interface-extends"));

      containment.setExtendedConcept(link);

      dataType.setExtendedConcept(languageEntity);
      dataType.setAbstract(true);

      enumeration.setExtendedConcept(dataType);
      enumeration.addFeature(
          Containment.createMultiple("literals", enumerationLiteral)
              .setID("-id-Enumeration-literals"));

      enumerationLiteral.addImplementedInterface(iKeyed);

      feature.setAbstract(true);
      feature.addImplementedInterface(iKeyed);
      feature.addFeature(
          Property.createRequired(
              "optional", LionCoreBuiltins.getBoolean(), "-id-Feature-optional"));

      classifier.setAbstract(true);
      classifier.setExtendedConcept(languageEntity);
      classifier.addFeature(
          Containment.createMultiple("features", feature, "-id-Classifier-features"));

      link.setAbstract(true);
      link.setExtendedConcept(feature);
      link.addFeature(
          Property.createRequired("multiple", LionCoreBuiltins.getBoolean(), "-id-Link-multiple"));
      link.addFeature(Reference.createRequired("type", classifier, "-id-Link-type"));

      language.setPartition(true);
      language.addImplementedInterface(iKeyed);
      language.addFeature(
          Property.createRequired("version", LionCoreBuiltins.getString(), "-id-Language-version"));
      language.addFeature(
          Reference.createMultiple("dependsOn", language).setID("-id-Language-dependsOn"));
      language.addFeature(
          Containment.createMultiple("entities", languageEntity, "-id-Language-entities")
              .setKey("Language-entities"));

      languageEntity.setAbstract(true);
      languageEntity.addImplementedInterface(iKeyed);

      primitiveType.setExtendedConcept(dataType);

      property.setExtendedConcept(feature);
      property.addFeature(
          Reference.createRequired("type", dataType, "-id-Property-type").setKey("Property-type"));

      reference.setExtendedConcept(link);

      iKeyed.addExtendedInterface(LionCoreBuiltins.getINamed());
      iKeyed.addFeature(
          Property.createRequired("key", LionCoreBuiltins.getString()).setID("-id-IKeyed-key"));

      annotation.setExtendedConcept(classifier);
      annotation.addFeature(
          Reference.createOptional("annotates", classifier, "-id-Annotation-annotates"));
      annotation.addFeature(
          Reference.createOptional("extends", annotation, "-id-Annotation-extends"));
      annotation.addFeature(
          Reference.createMultiple("implements", iface, "-id-Annotation-implements"));

      checkIDs(INSTANCE);
    }
    checkIDs(INSTANCE);
    return INSTANCE;
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
