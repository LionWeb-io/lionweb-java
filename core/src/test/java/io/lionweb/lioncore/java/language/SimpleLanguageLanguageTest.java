package io.lionweb.lioncore.java.language;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleLanguageLanguageTest {

  @Test
  public void emptyMetamodelDefinition() {
    Language language = new Language("SimpleLanguage").setID("myM3ID");
    assertEquals("SimpleLanguage", language.getName());
    assertEquals("SimpleLanguage", language.namespaceQualifier());
    assertEquals(0, language.dependsOn().size());
    assertEquals(0, language.getElements().size());
  }

  @Test
  public void emptyConceptDefinition() {
    Language language = new Language("SimpleLanguage").setID("myM3ID");
    Concept expression = new Concept(language, "Expression");
    assertEquals("Expression", expression.getName());
    assertSame(language, expression.getContainer());
    assertSame(language, expression.getLanguage());
    assertEquals("SimpleLanguage.Expression", expression.qualifiedName());
    assertEquals("SimpleLanguage.Expression", expression.namespaceQualifier());
    assertNull(expression.getExtendedConcept());
    assertEquals(0, expression.getImplemented().size());
    assertEquals(0, expression.getFeatures().size());
    assertFalse(expression.isAbstract());
  }

  @Test
  public void emptyConceptInterfaceDefinition() {
    Language language = new Language("SimpleLanguage").setID("myM3ID");
    ConceptInterface deprecated = new ConceptInterface(language, "Deprecated");
    assertEquals("Deprecated", deprecated.getName());
    assertSame(language, deprecated.getContainer());
    assertSame(language, deprecated.getLanguage());
    assertEquals("SimpleLanguage.Deprecated", deprecated.qualifiedName());
    assertEquals("SimpleLanguage.Deprecated", deprecated.namespaceQualifier());
    assertEquals(0, deprecated.getExtendedInterfaces().size());
    assertEquals(0, deprecated.getFeatures().size());
  }
}
