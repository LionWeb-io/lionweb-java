package io.lionweb.language;

import static io.lionweb.LionWebVersion.v2023_1;
import static io.lionweb.LionWebVersion.v2024_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.lioncore.LionCore;
import org.junit.jupiter.api.Test;

public class BuiltinIDsAndKeysTest {

  @Test
  public void M3ElementsHasExpectedIDsIn2023_1() {
    assertEquals("-id-Concept", LionCore.getConcept(v2023_1).getID());
    assertEquals(
        "-id-Concept-abstract", LionCore.getConcept(v2023_1).getPropertyByName("abstract").getID());
    assertEquals(
        "-id-Concept-extends", LionCore.getConcept(v2023_1).getReferenceByName("extends").getID());
    assertEquals(
        "-id-Concept-implements",
        LionCore.getConcept(v2023_1).getReferenceByName("implements").getID());

    assertEquals("-id-Interface", LionCore.getInterface(v2023_1).getID());
    assertEquals(
        "-id-Interface-extends",
        LionCore.getInterface(v2023_1).getReferenceByName("extends").getID());

    assertEquals("-id-Containment", LionCore.getContainment(v2023_1).getID());

    assertEquals("-id-DataType", LionCore.getDataType(v2023_1).getID());

    assertEquals("-id-Enumeration", LionCore.getEnumeration(v2023_1).getID());
    assertEquals(
        "-id-Enumeration-literals",
        LionCore.getEnumeration(v2023_1).getContainmentByName("literals").getID());

    assertEquals("-id-EnumerationLiteral", LionCore.getEnumerationLiteral(v2023_1).getID());

    assertEquals("-id-Feature", LionCore.getFeature(v2023_1).getID());
    assertEquals(
        "-id-Feature-optional", LionCore.getFeature(v2023_1).getPropertyByName("optional").getID());

    assertEquals("-id-Classifier", LionCore.getClassifier(v2023_1).getID());
    assertEquals(
        "-id-Classifier-features",
        LionCore.getClassifier(v2023_1).getContainmentByName("features").getID());

    assertEquals("-id-Link", LionCore.getLink(v2023_1).getID());
    assertEquals(
        "-id-Link-multiple", LionCore.getLink(v2023_1).getPropertyByName("multiple").getID());
    assertEquals("-id-Link-type", LionCore.getLink(v2023_1).getReferenceByName("type").getID());

    assertEquals("-id-Language", LionCore.getLanguage(v2023_1).getID());
    assertEquals(
        "LionCore-builtins-INamed-name",
        LionCore.getLanguage(v2023_1).getPropertyByName("name").getID());
    assertEquals("-id-IKeyed-key", LionCore.getLanguage(v2023_1).getPropertyByName("key").getID());
    assertEquals(
        "-id-Language-dependsOn",
        LionCore.getLanguage(v2023_1).getReferenceByName("dependsOn").getID());
    assertEquals(
        "-id-Language-entities",
        LionCore.getLanguage(v2023_1).getContainmentByName("entities").getID());

    assertEquals("-id-LanguageEntity", LionCore.getLanguageEntity(v2023_1).getID());

    assertEquals("-id-PrimitiveType", LionCore.getPrimitiveType(v2023_1).getID());

    assertEquals("-id-Property", LionCore.getProperty(v2023_1).getID());
    assertEquals(
        "-id-Property-type", LionCore.getProperty(v2023_1).getReferenceByName("type").getID());

    assertEquals("-id-Reference", LionCore.getReference(v2023_1).getID());
  }

  @Test
  public void M3ElementsHasExpectedIDsIn2024_1() {
    assertEquals("-id-Concept-2024-1", LionCore.getConcept(v2024_1).getID());
    assertEquals(
        "-id-Concept-abstract-2024-1",
        LionCore.getConcept(v2024_1).getPropertyByName("abstract").getID());
    assertEquals(
        "-id-Concept-extends-2024-1",
        LionCore.getConcept(v2024_1).getReferenceByName("extends").getID());
    assertEquals(
        "-id-Concept-implements-2024-1",
        LionCore.getConcept(v2024_1).getReferenceByName("implements").getID());

    assertEquals("-id-Interface-2024-1", LionCore.getInterface(v2024_1).getID());
    assertEquals(
        "-id-Interface-extends-2024-1",
        LionCore.getInterface(v2024_1).getReferenceByName("extends").getID());

    assertEquals("-id-Containment-2024-1", LionCore.getContainment(v2024_1).getID());

    assertEquals("-id-DataType-2024-1", LionCore.getDataType(v2024_1).getID());

    assertEquals("-id-Enumeration-2024-1", LionCore.getEnumeration(v2024_1).getID());
    assertEquals(
        "-id-Enumeration-literals-2024-1",
        LionCore.getEnumeration(v2024_1).getContainmentByName("literals").getID());

    assertEquals("-id-EnumerationLiteral-2024-1", LionCore.getEnumerationLiteral(v2024_1).getID());

    assertEquals("-id-Feature-2024-1", LionCore.getFeature(v2024_1).getID());
    assertEquals(
        "-id-Feature-optional-2024-1",
        LionCore.getFeature(v2024_1).getPropertyByName("optional").getID());

    assertEquals("-id-Classifier-2024-1", LionCore.getClassifier(v2024_1).getID());
    assertEquals(
        "-id-Classifier-features-2024-1",
        LionCore.getClassifier(v2024_1).getContainmentByName("features").getID());

    assertEquals("-id-Link-2024-1", LionCore.getLink(v2024_1).getID());
    assertEquals(
        "-id-Link-multiple-2024-1",
        LionCore.getLink(v2024_1).getPropertyByName("multiple").getID());
    assertEquals(
        "-id-Link-type-2024-1", LionCore.getLink(v2024_1).getReferenceByName("type").getID());

    assertEquals("-id-Language-2024-1", LionCore.getLanguage(v2024_1).getID());
    assertEquals(
        "LionCore-builtins-INamed-name-2024-1",
        LionCore.getLanguage(v2024_1).getPropertyByName("name").getID());
    assertEquals(
        "-id-IKeyed-key-2024-1", LionCore.getLanguage(v2024_1).getPropertyByName("key").getID());
    assertEquals(
        "-id-Language-dependsOn-2024-1",
        LionCore.getLanguage(v2024_1).getReferenceByName("dependsOn").getID());
    assertEquals(
        "-id-Language-entities-2024-1",
        LionCore.getLanguage(v2024_1).getContainmentByName("entities").getID());

    assertEquals("-id-LanguageEntity-2024-1", LionCore.getLanguageEntity(v2024_1).getID());

    assertEquals("-id-PrimitiveType-2024-1", LionCore.getPrimitiveType(v2024_1).getID());

    assertEquals("-id-Property-2024-1", LionCore.getProperty(v2024_1).getID());
    assertEquals(
        "-id-Property-type-2024-1",
        LionCore.getProperty(v2024_1).getReferenceByName("type").getID());

    assertEquals("-id-Reference-2024-1", LionCore.getReference(v2024_1).getID());
  }

  @Test
  public void M3ElementsHasExpectedKeys() {
    assertEquals("Concept", LionCore.getConcept().getKey());
    assertEquals("Concept-abstract", LionCore.getConcept().getPropertyByName("abstract").getKey());
    assertEquals("Concept-extends", LionCore.getConcept().getReferenceByName("extends").getKey());
    assertEquals(
        "Concept-implements", LionCore.getConcept().getReferenceByName("implements").getKey());

    assertEquals("Interface", LionCore.getInterface().getKey());
    assertEquals(
        "Interface-extends", LionCore.getInterface().getReferenceByName("extends").getKey());

    assertEquals("Containment", LionCore.getContainment().getKey());

    assertEquals("DataType", LionCore.getDataType().getKey());

    assertEquals("Enumeration", LionCore.getEnumeration().getKey());
    assertEquals(
        "Enumeration-literals",
        LionCore.getEnumeration().getContainmentByName("literals").getKey());

    assertEquals("EnumerationLiteral", LionCore.getEnumerationLiteral().getKey());

    assertEquals("Feature", LionCore.getFeature().getKey());
    assertEquals("Feature-optional", LionCore.getFeature().getPropertyByName("optional").getKey());

    assertEquals("Classifier", LionCore.getClassifier().getKey());
    assertEquals(
        "Classifier-features", LionCore.getClassifier().getContainmentByName("features").getKey());

    assertEquals("Link", LionCore.getLink().getKey());
    assertEquals("Link-multiple", LionCore.getLink().getPropertyByName("multiple").getKey());
    assertEquals("Link-type", LionCore.getLink().getReferenceByName("type").getKey());

    assertEquals("Language", LionCore.getLanguage().getKey());
    assertEquals(
        "LionCore-builtins-INamed-name", LionCore.getLanguage().getPropertyByName("name").getKey());
    assertEquals("IKeyed-key", LionCore.getLanguage().getPropertyByName("key").getKey());
    assertEquals(
        "Language-dependsOn", LionCore.getLanguage().getReferenceByName("dependsOn").getKey());
    assertEquals(
        "Language-entities", LionCore.getLanguage().getContainmentByName("entities").getKey());

    assertEquals("LanguageEntity", LionCore.getLanguageEntity().getKey());

    assertEquals("PrimitiveType", LionCore.getPrimitiveType().getKey());

    assertEquals("Property", LionCore.getProperty().getKey());
    assertEquals("Property-type", LionCore.getProperty().getReferenceByName("type").getKey());

    assertEquals("Reference", LionCore.getReference().getKey());
  }
}
