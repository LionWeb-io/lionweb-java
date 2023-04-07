package org.lionweb.lioncore.java.metamodel;

import static org.junit.Assert.*;

import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;

public class MetaCircularityTest {

  @Test
  public void eachElementOfM3HasRightConcept() {
    assertSame(LionCore.getConcept(), LionCore.getConcept().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getConceptInterface().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getContainment().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getDataType().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getEnumeration().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getEnumerationLiteral().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getFeature().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getFeaturesContainer().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getLink().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getMetamodel().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getMetamodelElement().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getPrimitiveType().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getProperty().getConcept());
    assertSame(LionCore.getConcept(), LionCore.getReference().getConcept());
  }

  @Test
  public void eachElementOfM2HasRightConcept() {
    assertSame(LionCore.getConcept(), new Concept().getConcept());
    assertSame(LionCore.getConceptInterface(), new ConceptInterface().getConcept());
    assertSame(LionCore.getContainment(), new Containment().getConcept());
    assertSame(LionCore.getEnumeration(), new Enumeration().getConcept());
    assertSame(LionCore.getEnumerationLiteral(), new EnumerationLiteral().getConcept());
    assertSame(LionCore.getMetamodel(), new Metamodel().getConcept());
    assertSame(LionCore.getPrimitiveType(), new PrimitiveType().getConcept());
    assertSame(LionCore.getProperty(), new Property().getConcept());
    assertSame(LionCore.getReference(), new Reference().getConcept());
  }
}
