package io.lionweb.lioncore.java.language;

import static io.lionweb.lioncore.java.LionWebVersion.v2023_1;
import static io.lionweb.lioncore.java.LionWebVersion.v2024_1;

import io.lionweb.lioncore.java.self.LionCore;
import org.junit.Assert;
import org.junit.Test;

public class BuiltinIDsAndKeysTest {

  @Test
  public void M3ElementsHasExpectedIDsIn2023_1() {
    Assert.assertEquals("-id-Concept", LionCore.getConcept(v2023_1).getID());
    Assert.assertEquals(
        "-id-Concept-abstract", LionCore.getConcept(v2023_1).getPropertyByName("abstract").getID());
    Assert.assertEquals(
        "-id-Concept-extends", LionCore.getConcept(v2023_1).getReferenceByName("extends").getID());
    Assert.assertEquals(
        "-id-Concept-implements",
        LionCore.getConcept(v2023_1).getReferenceByName("implements").getID());

    Assert.assertEquals("-id-Interface", LionCore.getInterface(v2023_1).getID());
    Assert.assertEquals(
        "-id-Interface-extends",
        LionCore.getInterface(v2023_1).getReferenceByName("extends").getID());

    Assert.assertEquals("-id-Containment", LionCore.getContainment(v2023_1).getID());

    Assert.assertEquals("-id-DataType", LionCore.getDataType(v2023_1).getID());

    Assert.assertEquals("-id-Enumeration", LionCore.getEnumeration(v2023_1).getID());
    Assert.assertEquals(
        "-id-Enumeration-literals",
        LionCore.getEnumeration(v2023_1).getContainmentByName("literals").getID());

    Assert.assertEquals("-id-EnumerationLiteral", LionCore.getEnumerationLiteral(v2023_1).getID());

    Assert.assertEquals("-id-Feature", LionCore.getFeature(v2023_1).getID());
    Assert.assertEquals(
        "-id-Feature-optional", LionCore.getFeature(v2023_1).getPropertyByName("optional").getID());

    Assert.assertEquals("-id-Classifier", LionCore.getClassifier(v2023_1).getID());
    Assert.assertEquals(
        "-id-Classifier-features",
        LionCore.getClassifier(v2023_1).getContainmentByName("features").getID());

    Assert.assertEquals("-id-Link", LionCore.getLink(v2023_1).getID());
    Assert.assertEquals(
        "-id-Link-multiple", LionCore.getLink(v2023_1).getPropertyByName("multiple").getID());
    Assert.assertEquals(
        "-id-Link-type", LionCore.getLink(v2023_1).getReferenceByName("type").getID());

    Assert.assertEquals("-id-Language", LionCore.getLanguage(v2023_1).getID());
    Assert.assertEquals(
        "LionCore-builtins-INamed-name",
        LionCore.getLanguage(v2023_1).getPropertyByName("name").getID());
    Assert.assertEquals(
        "-id-IKeyed-key", LionCore.getLanguage(v2023_1).getPropertyByName("key").getID());
    Assert.assertEquals(
        "-id-Language-dependsOn",
        LionCore.getLanguage(v2023_1).getReferenceByName("dependsOn").getID());
    Assert.assertEquals(
        "-id-Language-entities",
        LionCore.getLanguage(v2023_1).getContainmentByName("entities").getID());

    Assert.assertEquals("-id-LanguageEntity", LionCore.getLanguageEntity(v2023_1).getID());

    Assert.assertEquals("-id-PrimitiveType", LionCore.getPrimitiveType(v2023_1).getID());

    Assert.assertEquals("-id-Property", LionCore.getProperty(v2023_1).getID());
    Assert.assertEquals(
        "-id-Property-type", LionCore.getProperty(v2023_1).getReferenceByName("type").getID());

    Assert.assertEquals("-id-Reference", LionCore.getReference(v2023_1).getID());
  }

  @Test
  public void M3ElementsHasExpectedIDsIn2024_1() {
    Assert.assertEquals("-id-Concept-2024-1", LionCore.getConcept(v2024_1).getID());
    Assert.assertEquals(
        "-id-Concept-abstract-2024-1",
        LionCore.getConcept(v2024_1).getPropertyByName("abstract").getID());
    Assert.assertEquals(
        "-id-Concept-extends-2024-1",
        LionCore.getConcept(v2024_1).getReferenceByName("extends").getID());
    Assert.assertEquals(
        "-id-Concept-implements-2024-1",
        LionCore.getConcept(v2024_1).getReferenceByName("implements").getID());

    Assert.assertEquals("-id-Interface-2024-1", LionCore.getInterface(v2024_1).getID());
    Assert.assertEquals(
        "-id-Interface-extends-2024-1",
        LionCore.getInterface(v2024_1).getReferenceByName("extends").getID());

    Assert.assertEquals("-id-Containment-2024-1", LionCore.getContainment(v2024_1).getID());

    Assert.assertEquals("-id-DataType-2024-1", LionCore.getDataType(v2024_1).getID());

    Assert.assertEquals("-id-Enumeration-2024-1", LionCore.getEnumeration(v2024_1).getID());
    Assert.assertEquals(
        "-id-Enumeration-literals-2024-1",
        LionCore.getEnumeration(v2024_1).getContainmentByName("literals").getID());

    Assert.assertEquals(
        "-id-EnumerationLiteral-2024-1", LionCore.getEnumerationLiteral(v2024_1).getID());

    Assert.assertEquals("-id-Feature-2024-1", LionCore.getFeature(v2024_1).getID());
    Assert.assertEquals(
        "-id-Feature-optional-2024-1",
        LionCore.getFeature(v2024_1).getPropertyByName("optional").getID());

    Assert.assertEquals("-id-Classifier-2024-1", LionCore.getClassifier(v2024_1).getID());
    Assert.assertEquals(
        "-id-Classifier-features-2024-1",
        LionCore.getClassifier(v2024_1).getContainmentByName("features").getID());

    Assert.assertEquals("-id-Link-2024-1", LionCore.getLink(v2024_1).getID());
    Assert.assertEquals(
        "-id-Link-multiple-2024-1",
        LionCore.getLink(v2024_1).getPropertyByName("multiple").getID());
    Assert.assertEquals(
        "-id-Link-type-2024-1", LionCore.getLink(v2024_1).getReferenceByName("type").getID());

    Assert.assertEquals("-id-Language-2024-1", LionCore.getLanguage(v2024_1).getID());
    Assert.assertEquals(
        "LionCore-builtins-INamed-name-2024-1",
        LionCore.getLanguage(v2024_1).getPropertyByName("name").getID());
    Assert.assertEquals(
        "-id-IKeyed-key-2024-1", LionCore.getLanguage(v2024_1).getPropertyByName("key").getID());
    Assert.assertEquals(
        "-id-Language-dependsOn-2024-1",
        LionCore.getLanguage(v2024_1).getReferenceByName("dependsOn").getID());
    Assert.assertEquals(
        "-id-Language-entities-2024-1",
        LionCore.getLanguage(v2024_1).getContainmentByName("entities").getID());

    Assert.assertEquals("-id-LanguageEntity-2024-1", LionCore.getLanguageEntity(v2024_1).getID());

    Assert.assertEquals("-id-PrimitiveType-2024-1", LionCore.getPrimitiveType(v2024_1).getID());

    Assert.assertEquals("-id-Property-2024-1", LionCore.getProperty(v2024_1).getID());
    Assert.assertEquals(
        "-id-Property-type-2024-1",
        LionCore.getProperty(v2024_1).getReferenceByName("type").getID());

    Assert.assertEquals("-id-Reference-2024-1", LionCore.getReference(v2024_1).getID());
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

    Assert.assertEquals("Interface", LionCore.getInterface().getKey());
    Assert.assertEquals(
        "Interface-extends", LionCore.getInterface().getReferenceByName("extends").getKey());

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
