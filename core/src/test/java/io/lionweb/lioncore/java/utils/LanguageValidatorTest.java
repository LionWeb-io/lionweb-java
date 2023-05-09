package io.lionweb.lioncore.java.utils;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;

public class LanguageValidatorTest {

  @Test
  @Ignore
  public void anEmptyAnnotationIsInvalid() {
    Language language = new Language().setKey("mm-key");
    Annotation annotation = new Annotation().setKey("aa-key");
    language.addElement(annotation);

    assertFalse(new LanguageValidator().validateMetamodel(language).isSuccessful());
    assertFalse(new LanguageValidator().isMetamodelValid(language));
    assertEquals(2, new LanguageValidator().validateMetamodel(language).getIssues().size());
    assertTrue(
        new LanguageValidator()
            .validateMetamodel(language).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(Arrays.asList("Simple name not set", "Qualified name not set")),
        new LanguageValidator()
            .validateMetamodel(language).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  @Ignore
  public void anAnnotationCanBeValid() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Annotation annotation = new Annotation(language, "MyAnnotation").setKey("annotation-key");
    language.addElement(annotation);

    assertTrue(new LanguageValidator().validateMetamodel(language).isSuccessful());
    assertTrue(new LanguageValidator().isMetamodelValid(language));
    assertEquals(0, new LanguageValidator().validateMetamodel(language).getIssues().size());
  }

  @Test
  public void anEmptyPrimitiveTypeIsInvalid() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType = new PrimitiveType().setKey("pt-key");
    language.addElement(primitiveType);

    assertFalse(new LanguageValidator().validateMetamodel(language).isSuccessful());
    assertFalse(new LanguageValidator().isMetamodelValid(language));
    assertEquals(1, new LanguageValidator().validateMetamodel(language).getIssues().size());
    assertTrue(
        new LanguageValidator()
            .validateMetamodel(language).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(Arrays.asList("Simple name not set")),
        new LanguageValidator()
            .validateMetamodel(language).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  public void aPrimitiveTypeCanBeValid() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType = new PrimitiveType(language, "PrimitiveType").setKey("pt-key");
    language.addElement(primitiveType);

    assertTrue(new LanguageValidator().validateMetamodel(language).isSuccessful());
    assertTrue(new LanguageValidator().isMetamodelValid(language));
    assertEquals(0, new LanguageValidator().validateMetamodel(language).getIssues().size());
  }

  @Test
  public void simpleSelfInheritanceIsCaught() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a").setKey("key-a");
    a.setExtendedConcept(a);
    language.addElement(a);

    assertEquals(
        new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a))),
        new LanguageValidator().validateMetamodel(language).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptsIsCaught() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a").setKey("key-a");
    Concept b = new Concept(language, "b").setKey("key-b");
    a.setExtendedConcept(b);
    b.setExtendedConcept(a);
    language.addElement(a);
    language.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
        new LanguageValidator().validateMetamodel(language).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptInterfacesIsCaught() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    ConceptInterface a = new ConceptInterface(language, "a").setKey("a-key");
    ConceptInterface b = new ConceptInterface(language, "b").setKey("b-key");
    a.addExtendedInterface(b);
    b.addExtendedInterface(a);
    language.addElement(a);
    language.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
        new LanguageValidator().validateMetamodel(language).getIssues());
  }

  @Test
  public void multipleDirectImplementationsOfTheSameInterfaceAreNotAllowed() {
    Language language = new Language("MyMetamodel").setKey("mm-key");
    Concept a = new Concept(language, "a").setKey("key-a");
    ConceptInterface i = new ConceptInterface(language, "I").setKey("key-i");

    a.addImplementedInterface(i);
    a.addImplementedInterface(i);

    language.addElement(a);
    language.addElement(i);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "The same interface has been implemented multiple times",
                    a))),
        new LanguageValidator().validateMetamodel(language).getIssues());
  }

  @Test
  public void multipleIndirectImplementationsOfTheSameInterfaceAreAllowed() {
    Language language = new Language("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "A").setKey("a-key");
    Concept b = new Concept(language, "B").setKey("b-key");
    ConceptInterface i = new ConceptInterface(language, "I").setKey("i-key");

    a.setExtendedConcept(b);
    a.addImplementedInterface(i);
    b.addImplementedInterface(i);

    language.addElement(a);
    language.addElement(b);
    language.addElement(i);

    assertEquals(
        new HashSet<>(Arrays.asList()),
        new LanguageValidator().validateMetamodel(language).getIssues());
  }

  @Test
  public void ensuringLionCoreIsValidated() {
    ;
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new LanguageValidator().validateMetamodel(LionCore.getInstance()).getIssues());
  }

  @Test
  public void ensuringLionCoreBuiltinsIsValidated() {
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new LanguageValidator().validateMetamodel(LionCoreBuiltins.getInstance()).getIssues());
  }
}
