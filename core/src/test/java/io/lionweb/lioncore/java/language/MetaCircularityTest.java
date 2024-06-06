package io.lionweb.lioncore.java.language;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.self.LionCore;
import org.junit.Test;

public class MetaCircularityTest {

  @Test
  public void eachElementOfM3HasRightConcept() {
    assertSame(LionCore.getConcept(), LionCore.getConcept().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getInterface().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getContainment().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getDataType().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getEnumeration().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getEnumerationLiteral().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getFeature().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getClassifier().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getLink().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getLanguage().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getLanguageEntity().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getPrimitiveType().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getProperty().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getReference().getClassifier());
    assertSame(LionCore.getConcept(), LionCore.getAnnotation().getClassifier());
  }

  @Test
  public void eachElementOfM2HasRightConcept() {
    assertSame(LionCore.getConcept(), new Concept().getClassifier());
    assertSame(LionCore.getInterface(), new Interface().getClassifier());
    assertSame(LionCore.getContainment(), new Containment().getClassifier());
    assertSame(LionCore.getEnumeration(), new Enumeration().getClassifier());
    assertSame(LionCore.getEnumerationLiteral(), new EnumerationLiteral().getClassifier());
    assertSame(LionCore.getLanguage(), new Language().getClassifier());
    assertSame(LionCore.getPrimitiveType(), new PrimitiveType().getClassifier());
    assertSame(LionCore.getProperty(), new Property().getClassifier());
    assertSame(LionCore.getReference(), new Reference().getClassifier());
    assertSame(LionCore.getAnnotation(), new Annotation().getClassifier());
  }
}
