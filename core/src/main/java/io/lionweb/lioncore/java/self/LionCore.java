package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.impl.M3Node;
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

  public static Concept getConceptInterface() {
    return getInstance().requireConceptByName("ConceptInterface");
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

  public static Concept getFeaturesContainer() {
    return getInstance().requireConceptByName("FeaturesContainer");
  }

  public static Concept getLink() {
    return getInstance().requireConceptByName("Link");
  }

  public static Concept getLanguage() {
    return getInstance().requireConceptByName("Language");
  }

  public static Concept getLanguageElement() {
    return getInstance().requireConceptByName("LanguageElement");
  }

  public static Concept getNamespacedEntity() {
    return getInstance().getConceptByName("NamespacedEntity");
  }

  public static ConceptInterface getNamespaceProvider() {
    return getInstance().getConceptInterfaceByName("NamespaceProvider");
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
      INSTANCE = new Language("LIonCore.M3");
      INSTANCE.setID("LIonCore_M3");
      INSTANCE.setKey("LIonCore_M3");
      INSTANCE.setVersion("1");

      // We first instantiate all Concepts and ConceptInterfaces
      // we add features only after as the features will have references to these elements
      Concept concept = INSTANCE.addElement(new Concept("Concept"));
      Concept conceptInterface = INSTANCE.addElement(new Concept("ConceptInterface"));
      Concept containment = INSTANCE.addElement(new Concept("Containment"));
      Concept dataType = INSTANCE.addElement(new Concept("DataType"));
      Concept enumeration = INSTANCE.addElement(new Concept("Enumeration"));
      Concept enumerationLiteral = INSTANCE.addElement(new Concept("EnumerationLiteral"));
      Concept feature = INSTANCE.addElement(new Concept("Feature"));
      Concept featuresContainer = INSTANCE.addElement(new Concept("FeaturesContainer"));
      ConceptInterface hasKey = INSTANCE.addElement(new ConceptInterface("HasKey"));
      Concept link = INSTANCE.addElement(new Concept("Link"));
      Concept language = INSTANCE.addElement(new Concept("Language"));
      Concept languageElement = INSTANCE.addElement(new Concept("LanguageElement"));
      Concept namespacedEntity = INSTANCE.addElement(new Concept("NamespacedEntity"));
      ConceptInterface namespaceProvider =
          INSTANCE.addElement(new ConceptInterface("NamespaceProvider"));
      Concept primitiveType = INSTANCE.addElement(new Concept("PrimitiveType"));
      Concept property = INSTANCE.addElement(new Concept("Property"));
      Concept reference = INSTANCE.addElement(new Concept("Reference"));

      // Now we start adding the features to all the Concepts and ConceptInterfaces

      concept.setExtendedConcept(featuresContainer);
      concept.addFeature(
          Property.createRequired(
              "abstract", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Concept_abstract"));
      concept.addFeature(
          Reference.createOptional("extends", concept, "LIonCore_M3_Concept_extends"));
      concept.addFeature(
          Reference.createMultiple(
              "implements", conceptInterface, "LIonCore_M3_Concept_implements"));

      conceptInterface.setExtendedConcept(featuresContainer);
      conceptInterface.addFeature(
          Reference.createMultiple(
              "extends", conceptInterface, "LIonCore_M3_ConceptInterface_extends"));

      containment.setExtendedConcept(link);

      dataType.setExtendedConcept(languageElement);
      dataType.setAbstract(true);

      enumeration.setExtendedConcept(dataType);
      enumeration.addImplementedInterface(namespaceProvider);
      enumeration.addFeature(Containment.createMultiple("literals", enumerationLiteral));

      enumerationLiteral.setExtendedConcept(namespacedEntity);
      enumerationLiteral.addImplementedInterface(hasKey);

      feature.setExtendedConcept(namespacedEntity);
      feature.addImplementedInterface(hasKey);
      feature.addFeature(
          Property.createRequired(
              "optional", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Feature_optional"));
      feature.addFeature(
          Property.createRequired(
              "derived", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Feature_derived"));

      featuresContainer.setExtendedConcept(languageElement);
      featuresContainer.addImplementedInterface(namespaceProvider);
      featuresContainer.addFeature(
          Containment.createMultiple(
              "allFeatures", feature, "LIonCore_M3_FeaturesContainer_allFeatures"));

      hasKey.addFeature(
          Property.createRequired("key", LionCoreBuiltins.getString(), "LIonCore_M3_HasKey_key"));

      link.setExtendedConcept(feature);
      link.addFeature(
          Property.createRequired(
              "multiple", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Link_multiple"));
      link.addFeature(Reference.createRequired("type", featuresContainer, "LIonCore_M3_Link_type"));

      language.addImplementedInterface(namespaceProvider);
      language.addImplementedInterface(hasKey);
      language.addFeature(
          Property.createRequired(
              "name", LionCoreBuiltins.getString(), "LIonCore_M3_Metamodel_name"));
      language.addFeature(
          Property.createRequired(
              "version", LionCoreBuiltins.getString(), "LIonCore_M3_Metamodel_version"));
      language.addFeature(Reference.createMultiple("dependsOn", language));
      language.addFeature(
          Containment.createMultiple(
              "elements", languageElement, "LIonCore_M3_Metamodel_elements"));

      languageElement.setExtendedConcept(namespacedEntity);
      languageElement.addImplementedInterface(hasKey);

      language.setAbstract(true);

      namespacedEntity.setAbstract(true);
      namespacedEntity.addFeature(
          Property.createRequired(
              "name", LionCoreBuiltins.getString(), "LIonCore_M3_NamespacedEntity_name"));

      primitiveType.setExtendedConcept(dataType);

      property.setExtendedConcept(feature);
      property.addFeature(Reference.createRequired("type", dataType, "LIonCore_M3_Property_type"));

      reference.setExtendedConcept(link);

      checkIDs(INSTANCE);
    }
    checkIDs(INSTANCE);
    return INSTANCE;
  }

  private static void checkIDs(M3Node node) {
    Set<String> clashingKeys = new HashSet<>(Arrays.asList("type", "extends", "name"));
    if (node.getID() == null) {
      if (node instanceof NamespacedEntity) {
        NamespacedEntity namespacedEntity = (NamespacedEntity) node;
        node.setID(namespacedEntity.qualifiedName().replaceAll("\\.", "_"));
        if (node instanceof HasKey<?>) {
          ((HasKey<?>) node).setKey(namespacedEntity.getName());
        }
      } else {
        throw new IllegalStateException(node.toString());
      }
    }
    if (node instanceof FeaturesContainer<?>) {
      FeaturesContainer<?> featuresContainer = (FeaturesContainer<?>) node;
      featuresContainer
          .getFeatures()
          .forEach(
              feature -> {
                if (clashingKeys.contains(feature.getName())) {
                  feature.setKey(featuresContainer.getName() + "_" + feature.getName());
                } else {
                  feature.setKey(feature.getName());
                }
              });
    }

    // TODO To be changed once getChildren is implemented correctly
    getChildrenHelper(node).forEach(c -> checkIDs(c));
  }

  private static List<? extends M3Node> getChildrenHelper(M3Node node) {
    if (node instanceof Language) {
      return ((Language) node).getElements();
    } else if (node instanceof FeaturesContainer) {
      return ((FeaturesContainer) node).getFeatures();
    } else if (node instanceof Feature) {
      return Collections.emptyList();
    } else {
      throw new UnsupportedOperationException("Unsupported " + node);
    }
  }
}
