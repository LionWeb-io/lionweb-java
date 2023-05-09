package io.lionweb.lioncore.java.language;

import io.lionweb.lioncore.java.self.LionCore;
import org.junit.Assert;
import org.junit.Test;

public class BuiltinIDsTest {

  @Test
  public void M3ElementsHasExpectedIDs() {
    Assert.assertEquals("LIonCore_M3_Concept", LionCore.getConcept().getID());
    Assert.assertEquals(
        "LIonCore_M3_Concept_abstract",
        LionCore.getConcept().getPropertyByName("abstract").getID());
    Assert.assertEquals(
        "LIonCore_M3_Concept_extends", LionCore.getConcept().getReferenceByName("extends").getID());
    Assert.assertEquals(
        "LIonCore_M3_Concept_implements",
        LionCore.getConcept().getReferenceByName("implements").getID());

    Assert.assertEquals("LIonCore_M3_ConceptInterface", LionCore.getConceptInterface().getID());
    Assert.assertEquals(
        "LIonCore_M3_ConceptInterface_extends",
        LionCore.getConceptInterface().getReferenceByName("extends").getID());

    Assert.assertEquals("LIonCore_M3_Containment", LionCore.getContainment().getID());

    Assert.assertEquals("LIonCore_M3_DataType", LionCore.getDataType().getID());

    Assert.assertEquals("LIonCore_M3_Enumeration", LionCore.getEnumeration().getID());
    Assert.assertEquals(
        "LIonCore_M3_Enumeration_literals",
        LionCore.getEnumeration().getContainmentByName("literals").getID());

    Assert.assertEquals("LIonCore_M3_EnumerationLiteral", LionCore.getEnumerationLiteral().getID());

    Assert.assertEquals("LIonCore_M3_Feature", LionCore.getFeature().getID());
    Assert.assertEquals(
        "LIonCore_M3_Feature_optional",
        LionCore.getFeature().getPropertyByName("optional").getID());

    Assert.assertEquals("LIonCore_M3_FeaturesContainer", LionCore.getFeaturesContainer().getID());
    Assert.assertEquals(
        "LIonCore_M3_FeaturesContainer_allFeatures",
        LionCore.getFeaturesContainer().getContainmentByName("allFeatures").getID());

    Assert.assertEquals("LIonCore_M3_Link", LionCore.getLink().getID());
    Assert.assertEquals(
        "LIonCore_M3_Link_multiple", LionCore.getLink().getPropertyByName("multiple").getID());
    Assert.assertEquals(
        "LIonCore_M3_Link_type", LionCore.getLink().getReferenceByName("type").getID());

    Assert.assertEquals("LIonCore_M3_Language", LionCore.getLanguage().getID());
    Assert.assertEquals(
        "LIonCore_M3_Language_name", LionCore.getLanguage().getPropertyByName("name").getID());
    Assert.assertEquals(
        "LIonCore_M3_HasKey_key", LionCore.getLanguage().getPropertyByName("key").getID());
    Assert.assertEquals(
        "LIonCore_M3_Language_dependsOn",
        LionCore.getLanguage().getReferenceByName("dependsOn").getID());
    Assert.assertEquals(
        "LIonCore_M3_Language_elements",
        LionCore.getLanguage().getContainmentByName("elements").getID());

    Assert.assertEquals("LIonCore_M3_LanguageElement", LionCore.getLanguageElement().getID());

    Assert.assertEquals("LIonCore_M3_NamespacedEntity", LionCore.getNamespacedEntity().getID());
    Assert.assertEquals(
        "LIonCore_M3_NamespacedEntity_name",
        LionCore.getNamespacedEntity().getPropertyByName("name").getID());

    Assert.assertEquals("LIonCore_M3_NamespaceProvider", LionCore.getNamespaceProvider().getID());

    Assert.assertEquals("LIonCore_M3_PrimitiveType", LionCore.getPrimitiveType().getID());

    Assert.assertEquals("LIonCore_M3_Property", LionCore.getProperty().getID());
    Assert.assertEquals(
        "LIonCore_M3_Property_type", LionCore.getProperty().getReferenceByName("type").getID());

    Assert.assertEquals("LIonCore_M3_Reference", LionCore.getReference().getID());
  }
}
