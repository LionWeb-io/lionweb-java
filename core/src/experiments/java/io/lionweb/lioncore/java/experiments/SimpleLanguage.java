package io.lionweb.lioncore.java.experiments;

import io.lionweb.lioncore.java.language.*;
import java.util.LinkedList;
import java.util.List;

public class SimpleLanguage {

  static Language language;
  static Concept baseConcept;
  static List<Concept> subConcepts;

  static {
    language = new Language("MyLanguage", "simple-language-id", "simple-language-key");
    language.setVersion("1");
    baseConcept = new Concept(language, "Base", "base-id", "base-key");
    subConcepts = new LinkedList<>();
    for (int i = 0; i < 50; i++) {
      Concept subConcept =
          new Concept(
              language, "Subconcept" + i, "subconcept-" + i + "-id", "subconcept-" + i + "-key");

      Containment containment = new Containment("myContainment", baseConcept);
      containment.setID("mycontainment-" + i + "-id");
      containment.setKey("mycontainment-" + i + "-key");
      containment.setMultiple(true);
      containment.setType(baseConcept);

      Property stringProperty = new Property("stringProp", subConcept);
      stringProperty.setType(LionCoreBuiltins.getString());
      stringProperty.setID("stringProp-" + i + "-id");
      stringProperty.setKey("stringProp-" + i + "-key");
      subConcept.addFeature(stringProperty);

      Property intProperty = new Property("intProp", subConcept);
      intProperty.setType(LionCoreBuiltins.getString());
      intProperty.setID("intProp-" + i + "-id");
      intProperty.setKey("intProp-" + i + "-key");
      subConcept.addFeature(intProperty);

      subConcept.addFeature(containment);
      subConcept.setExtendedConcept(baseConcept);
      subConcepts.add(subConcept);
    }
  }
}
