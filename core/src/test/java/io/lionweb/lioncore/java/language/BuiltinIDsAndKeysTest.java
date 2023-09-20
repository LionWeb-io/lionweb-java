package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;
import org.junit.Assert;
import org.junit.Test;

public class BuiltinIDsAndKeysTest {

  @Test
  public void M3ElementsHasExpectedIDs() {
    Assert.assertEquals("-id-Concept", LionCore.getConcept().getID());
    Assert.assertEquals(
        "-id-Concept-abstract", LionCore.getConcept().getPropertyByName("abstract").getID());
    Assert.assertEquals(
        "-id-Concept-extends", LionCore.getConcept().getReferenceByName("extends").getID());
    Assert.assertEquals(
        "-id-Concept-implements", LionCore.getConcept().getReferenceByName("implements").getID());

    Assert.assertEquals("-id-ConceptInterface", LionCore.getConceptInterface().getID());
    Assert.assertEquals(
        "-id-ConceptInterface-extends",
        LionCore.getConceptInterface().getReferenceByName("extends").getID());

    Assert.assertEquals("-id-Containment", LionCore.getContainment().getID());

    Assert.assertEquals("-id-DataType", LionCore.getDataType().getID());

    Assert.assertEquals("-id-Enumeration", LionCore.getEnumeration().getID());
    Assert.assertEquals(
        "-id-Enumeration-literals",
        LionCore.getEnumeration().getContainmentByName("literals").getID());

    Assert.assertEquals("-id-EnumerationLiteral", LionCore.getEnumerationLiteral().getID());

    Assert.assertEquals("-id-Feature", LionCore.getFeature().getID());
    Assert.assertEquals(
        "-id-Feature-optional", LionCore.getFeature().getPropertyByName("optional").getID());

    Assert.assertEquals("-id-Classifier", LionCore.getClassifier().getID());
    Assert.assertEquals(
        "-id-Classifier-features",
        LionCore.getClassifier().getContainmentByName("features").getID());

    Assert.assertEquals("-id-Link", LionCore.getLink().getID());
    Assert.assertEquals(
        "-id-Link-multiple", LionCore.getLink().getPropertyByName("multiple").getID());
    Assert.assertEquals("-id-Link-type", LionCore.getLink().getReferenceByName("type").getID());

    Assert.assertEquals("-id-Language", LionCore.getLanguage().getID());
    Assert.assertEquals(
        "LionCore-builtins-INamed-name", LionCore.getLanguage().getPropertyByName("name").getID());
    Assert.assertEquals("-id-IKeyed-key", LionCore.getLanguage().getPropertyByName("key").getID());
    Assert.assertEquals(
        "-id-Language-dependsOn", LionCore.getLanguage().getReferenceByName("dependsOn").getID());
    Assert.assertEquals(
        "-id-Language-entities", LionCore.getLanguage().getContainmentByName("entities").getID());

    Assert.assertEquals("-id-LanguageEntity", LionCore.getLanguageEntity().getID());

    Assert.assertEquals("-id-PrimitiveType", LionCore.getPrimitiveType().getID());

    Assert.assertEquals("-id-Property", LionCore.getProperty().getID());
    Assert.assertEquals(
        "-id-Property-type", LionCore.getProperty().getReferenceByName("type").getID());

    Assert.assertEquals("-id-Reference", LionCore.getReference().getID());
  }

  @Test
  public void M3ElementsHasExpectedKeys() {
    Assert.assertEquals("Concept", LionCore.getConcept().getKey());
    Assert.assertEquals(
        "Concept-abstract", LionCore.getConcept().getPropertyByName("abstract").getKey());
    Assert.assertEquals(
        "Concept-extends", LionCore.getConcept().getReferenceByName("extends").getKey());
    Assert.assertEquals(
        "Concept-implements", LionCore.getConcept().getReferenceByName("implements").getKey());

    Assert.assertEquals("ConceptInterface", LionCore.getConceptInterface().getKey());
    Assert.assertEquals(
        "ConceptInterface-extends",
        LionCore.getConceptInterface().getReferenceByName("extends").getKey());

    Assert.assertEquals("Containment", LionCore.getContainment().getKey());

    Assert.assertEquals("DataType", LionCore.getDataType().getKey());

    Assert.assertEquals("Enumeration", LionCore.getEnumeration().getKey());
    Assert.assertEquals(
        "Enumeration-literals",
        LionCore.getEnumeration().getContainmentByName("literals").getKey());

    Assert.assertEquals("EnumerationLiteral", LionCore.getEnumerationLiteral().getKey());

    Assert.assertEquals("Feature", LionCore.getFeature().getKey());
    Assert.assertEquals(
        "Feature-optional", LionCore.getFeature().getPropertyByName("optional").getKey());

    Assert.assertEquals("Classifier", LionCore.getClassifier().getKey());
    Assert.assertEquals(
        "Classifier-features", LionCore.getClassifier().getContainmentByName("features").getKey());

    Assert.assertEquals("Link", LionCore.getLink().getKey());
    Assert.assertEquals("Link-multiple", LionCore.getLink().getPropertyByName("multiple").getKey());
    Assert.assertEquals("Link-type", LionCore.getLink().getReferenceByName("type").getKey());

    Assert.assertEquals("Language", LionCore.getLanguage().getKey());
    Assert.assertEquals(
        "LionCore-builtins-INamed-name", LionCore.getLanguage().getPropertyByName("name").getKey());
    Assert.assertEquals("IKeyed-key", LionCore.getLanguage().getPropertyByName("key").getKey());
    Assert.assertEquals(
        "Language-dependsOn", LionCore.getLanguage().getReferenceByName("dependsOn").getKey());
    Assert.assertEquals(
        "Language-entities", LionCore.getLanguage().getContainmentByName("entities").getKey());

    Assert.assertEquals("LanguageEntity", LionCore.getLanguageEntity().getKey());

    Assert.assertEquals("PrimitiveType", LionCore.getPrimitiveType().getKey());

    Assert.assertEquals("Property", LionCore.getProperty().getKey());
    Assert.assertEquals(
        "Property-type", LionCore.getProperty().getReferenceByName("type").getKey());

    Assert.assertEquals("Reference", LionCore.getReference().getKey());
  }
}
