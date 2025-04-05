package io.lionweb.repoclient;

import io.lionweb.lioncore.java.language.*;

public class PropertiesLanguage {
  public static final Concept propertiesPartition;
  public static final Concept propertiesFile;
  public static final Concept property;
  public static final Language propertiesLanguage;

  static {
    // Create language
    String name = "Properties";
    String cleanedName = name.toLowerCase().replace(".", "_");
    propertiesLanguage =
        new Language(
            name, "language-" + cleanedName + "-id", "language-" + cleanedName + "-key", "1");

    // Create concepts
    propertiesPartition = createConcept(propertiesLanguage, "PropertiesPartition");
    propertiesFile = createConcept(propertiesLanguage, "PropertiesFile");
    property = createConcept(propertiesLanguage, "Property");

    // Register concept features
    propertiesPartition.setPartition(true);
    addContainment(propertiesPartition, "files", propertiesFile, Multiplicity.ZERO_TO_MANY);
    addContainment(propertiesFile, "properties", property, Multiplicity.ZERO_TO_MANY);
    property.addImplementedInterface(LionCoreBuiltins.getINamed());
  }

  private static Concept createConcept(Language language, String name) {
    Concept concept =
        new Concept(
            language,
            name,
            language.getID().replace("language-", "").replace("-id", "") + "-" + name + "-id",
            language.getKey().replace("language-", "").replace("-key", "") + "-" + name + "-key");
    language.addElement(concept);
    return concept;
  }

  private static Containment addContainment(
      Classifier<?> owner, String name, Classifier<?> target, Multiplicity multiplicity) {
    Containment containment = new Containment();
    containment.setName(name);
    containment.setID(owner.getID().replace("-id", "") + "-" + name + "-id");
    containment.setKey(owner.getKey().replace("-key", "") + "-" + name + "-key");
    containment.setType(target);
    containment.setOptional(multiplicity.optional);
    containment.setMultiple(multiplicity.multiple);
    owner.addFeature(containment);
    return containment;
  }

  public enum Multiplicity {
    OPTIONAL(true, false),
    SINGLE(false, false),
    ZERO_TO_MANY(true, true),
    ONE_TO_MANY(false, true);

    public final boolean optional;
    public final boolean multiple;

    Multiplicity(boolean optional, boolean multiple) {
      this.optional = optional;
      this.multiple = multiple;
    }
  }
}
