package io.lionweb.client;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;

public class PropertiesLanguage {
  public static final Concept propertiesPartition;
  public static final Concept propertiesFile;
  public static final Concept property;
  public static final Language propertiesLanguage;
  private static LionWebVersion lionWebVersionUsed = LionWebVersion.v2023_1;

  static {
    // Create language
    String name = "Properties";
    String cleanedName = name.toLowerCase().replace(".", "_");
    propertiesLanguage = new Language(lionWebVersionUsed, name);
    propertiesLanguage.setID("language-" + cleanedName + "-id");
    propertiesLanguage.setVersion("1");
    propertiesLanguage.setKey("language-" + cleanedName + "-key");

    // Create concepts
    propertiesPartition = createConcept(propertiesLanguage, "PropertiesPartition");
    propertiesPartition.setPartition(true);
    propertiesFile = createConcept(propertiesLanguage, "PropertiesFile");
    property = createConcept(propertiesLanguage, "Property");

    // Register concept features
    propertiesPartition.setPartition(true);
    addContainment(propertiesPartition, "files", propertiesFile, Multiplicity.ZERO_TO_MANY);
    Property filePath = new Property("path", propertiesFile, propertiesFile.getID() + "-path");
    filePath.setKey(propertiesFile.getKey() + "-path");
    filePath.setType(LionCoreBuiltins.getString(lionWebVersionUsed));
    propertiesFile.addFeature(filePath);

    addContainment(propertiesFile, "properties", property, Multiplicity.ZERO_TO_MANY);
    property.addImplementedInterface(LionCoreBuiltins.getINamed(lionWebVersionUsed));
  }

  private static Concept createConcept(Language language, String name) {
    Concept concept =
        new Concept(
            language,
            name,
            language.getID().replace("language-", "").replace("-id", "") + "-" + name + "-id");
    concept.setKey(
        language.getKey().replace("language-", "").replace("-key", "") + "-" + name + "-key");
    language.addElement(concept);
    return concept;
  }

  private static Containment addContainment(
      Classifier<?> owner, String name, Classifier<?> target, Multiplicity multiplicity) {
    Containment containment = new Containment(lionWebVersionUsed);
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
