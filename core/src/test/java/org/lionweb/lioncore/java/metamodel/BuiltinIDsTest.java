package org.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;

public class BuiltinIDsTest {

  @Test
  public void M3ElementsHasExpectedIDs() {
    assertEquals("LIonCore_M3_Concept", LionCore.getConcept().getID());
    assertEquals(
        "LIonCore_M3_Concept_abstract",
        LionCore.getConcept().getPropertyByName("abstract").getID());
    assertEquals(
        "LIonCore_M3_Concept_extends", LionCore.getConcept().getReferenceByName("extends").getID());
    assertEquals(
        "LIonCore_M3_Concept_implements",
        LionCore.getConcept().getReferenceByName("implements").getID());

    assertEquals("LIonCore_M3_ConceptInterface", LionCore.getConceptInterface().getID());
    assertEquals(
        "LIonCore_M3_ConceptInterface_extends",
        LionCore.getConceptInterface().getReferenceByName("extends").getID());

    assertEquals("LIonCore_M3_Containment", LionCore.getContainment().getID());

    assertEquals("LIonCore_M3_DataType", LionCore.getDataType().getID());

    assertEquals("LIonCore_M3_Enumeration", LionCore.getEnumeration().getID());
    assertEquals(
        "LIonCore_M3_Enumeration_literals",
        LionCore.getEnumeration().getContainmentByName("literals").getID());

    assertEquals("LIonCore_M3_EnumerationLiteral", LionCore.getEnumerationLiteral().getID());

    assertEquals("LIonCore_M3_Feature", LionCore.getFeature().getID());
    assertEquals(
        "LIonCore_M3_Feature_optional",
        LionCore.getFeature().getPropertyByName("optional").getID());

    assertEquals("LIonCore_M3_FeaturesContainer", LionCore.getFeaturesContainer().getID());
    assertEquals(
        "LIonCore_M3_FeaturesContainer_features",
        LionCore.getFeaturesContainer().getContainmentByName("features").getID());

    assertEquals("LIonCore_M3_Link", LionCore.getLink().getID());
    assertEquals(
        "LIonCore_M3_Link_multiple", LionCore.getLink().getPropertyByName("multiple").getID());
    assertEquals("LIonCore_M3_Link_type", LionCore.getLink().getReferenceByName("type").getID());

    assertEquals("LIonCore_M3_Metamodel", LionCore.getMetamodel().getID());
    assertEquals(
        "LIonCore_M3_Metamodel_name", LionCore.getMetamodel().getPropertyByName("name").getID());
    assertEquals(
        "LIonCore_M3_HasKey_key", LionCore.getMetamodel().getPropertyByName("key").getID());
    assertEquals(
        "LIonCore_M3_Metamodel_dependsOn",
        LionCore.getMetamodel().getReferenceByName("dependsOn").getID());
    assertEquals(
        "LIonCore_M3_Metamodel_elements",
        LionCore.getMetamodel().getContainmentByName("elements").getID());

    assertEquals("LIonCore_M3_MetamodelElement", LionCore.getMetamodelElement().getID());

    assertEquals("LIonCore_M3_NamespacedEntity", LionCore.getNamespacedEntity().getID());
    assertEquals(
        "LIonCore_M3_NamespacedEntity_simpleName",
        LionCore.getNamespacedEntity().getPropertyByName("simpleName").getID());

    assertEquals("LIonCore_M3_NamespaceProvider", LionCore.getNamespaceProvider().getID());

    assertEquals("LIonCore_M3_PrimitiveType", LionCore.getPrimitiveType().getID());

    assertEquals("LIonCore_M3_Property", LionCore.getProperty().getID());
    assertEquals(
        "LIonCore_M3_Property_type", LionCore.getProperty().getReferenceByName("type").getID());

    assertEquals("LIonCore_M3_Reference", LionCore.getReference().getID());
  }
}
