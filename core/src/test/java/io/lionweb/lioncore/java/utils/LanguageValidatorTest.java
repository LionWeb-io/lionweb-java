package io.lionweb.lioncore.java.utils;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.Test;

public class LanguageValidatorTest {

  @Test
  public void anEmptyAnnotationIsInvalid() {
    Language language = new Language("MyLanguageName").setKey("mm-key").setID("mm-id");
    Annotation annotation = new Annotation().setKey("aa-key").setID("aa-id");
    language.addElement(annotation);

    assertFalse(new LanguageValidator().validate(language).isSuccessful());
    assertFalse(new LanguageValidator().isLanguageValid(language));
    assertEquals(2, new LanguageValidator().validate(language).getIssues().size());
    assertTrue(
        new LanguageValidator()
            .validate(language).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(
            Arrays.asList(
                "Simple name not set", "An annotation should specify annotates or inherit it")),
        new LanguageValidator()
            .validate(language).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  public void anAnnotationMustSpecifyAnnotated() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Annotation annotation =
        new Annotation(language, "MyAnnotation").setKey("annotation-key").setID("annotation-id");
    language.addElement(annotation);

    assertFalse(new LanguageValidator().validate(language).isSuccessful());
    assertFalse(new LanguageValidator().isLanguageValid(language));
    assertEquals(1, new LanguageValidator().validate(language).getIssues().size());
  }

  @Test
  public void anAnnotationCanBeValid() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Annotation annotation =
        new Annotation(language, "MyAnnotation").setKey("annotation-key").setID("annotation-id");
    language.addElement(annotation);
    Concept c = new Concept(language, "C", "c-id", "c-key");
    annotation.setAnnotates(c);

    assertTrue(new LanguageValidator().validate(language).isSuccessful());
    assertTrue(new LanguageValidator().isLanguageValid(language));
    assertEquals(0, new LanguageValidator().validate(language).getIssues().size());
  }

  @Test
  public void anEmptyPrimitiveTypeIsInvalid() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType = new PrimitiveType().setKey("pt-key").setID("pt-id");
    language.addElement(primitiveType);

    assertFalse(new LanguageValidator().validate(language).isSuccessful());
    assertFalse(new LanguageValidator().isLanguageValid(language));
    assertEquals(1, new LanguageValidator().validate(language).getIssues().size());
    assertTrue(
        new LanguageValidator()
            .validate(language).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(Arrays.asList("Simple name not set")),
        new LanguageValidator()
            .validate(language).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  public void aPrimitiveTypeCanBeValid() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType =
        new PrimitiveType(language, "PrimitiveType").setKey("pt-key").setID("pt-id");
    language.addElement(primitiveType);

    assertTrue(new LanguageValidator().validate(language).isSuccessful());
    assertTrue(new LanguageValidator().isLanguageValid(language));
    assertEquals(0, new LanguageValidator().validate(language).getIssues().size());
  }

  @Test
  public void simpleSelfInheritanceIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a").setKey("key-a").setID("id-a");
    a.setExtendedConcept(a);
    language.addElement(a);

    assertEquals(
        new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a))),
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptsIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a").setKey("key-a").setID("id-a");
    Concept b = new Concept(language, "b").setKey("key-b").setID("id-b");
    a.setExtendedConcept(b);
    b.setExtendedConcept(a);
    language.addElement(a);
    language.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void directSelfInheritanceOfInterfacesIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Interface a = new Interface(language, "a").setKey("a-key").setID("a-id");
    a.addExtendedInterface(a);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "Cyclic hierarchy found: the interface extends itself",
                    a))),
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfInterfacesIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Interface a = new Interface(language, "a").setKey("a-key").setID("a-id");
    Interface b = new Interface(language, "b").setKey("b-key").setID("b-id");
    a.addExtendedInterface(b);
    b.addExtendedInterface(a);
    language.addElement(a);
    language.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error, "Cyclic hierarchy found: the interface extends itself", a),
                new Issue(
                    IssueSeverity.Error,
                    "Cyclic hierarchy found: the interface extends itself",
                    b))),
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void multipleDirectImplementationsOfTheSameInterfaceAreNotAllowed() {
    Language language = new Language("MyLanguage").setKey("mm-key").setID("mm-id");
    Concept a = new Concept(language, "a").setKey("key-a").setID("id-a");
    Interface i = new Interface(language, "I").setKey("key-i").setID("id-i");

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
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void multipleIndirectImplementationsOfTheSameInterfaceAreAllowed() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "A").setKey("a-key").setID("a-id");
    Concept b = new Concept(language, "B").setKey("b-key").setID("b-id");
    Interface i = new Interface(language, "I").setKey("i-key").setID("i-id");

    a.setExtendedConcept(b);
    a.addImplementedInterface(i);
    b.addImplementedInterface(i);

    language.addElement(a);
    language.addElement(b);
    language.addElement(i);

    assertEquals(
        new HashSet<>(Arrays.asList()), new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void ensuringLionCoreIsValidated() {
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new LanguageValidator().validate(LionCore.getInstance()).getIssues());
  }

  @Test
  public void ensuringLionCoreBuiltinsIsValidated() {
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new LanguageValidator().validate(LionCoreBuiltins.getInstance()).getIssues());
  }

  @Test
  public void diamondWithInterfaces() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    Interface base = new Interface(l, "Base", "base_id", "base_key");
    Interface branchA = new Interface(l, "BranchA", "branchA_id", "branchA_key");
    branchA.addExtendedInterface(base);
    Interface branchB = new Interface(l, "BranchB", "branchB_id", "branchB_key");
    branchB.addExtendedInterface(base);
    Interface top = new Interface(l, "Top", "top_id", "top_key");
    top.addExtendedInterface(branchA);
    top.addExtendedInterface(branchB);
    assertEquals(new HashSet<>(Arrays.asList()), l.validate().getIssues());
  }

  // This is not fine
  // interface I1 extends I2
  // interface I2 extends I1
  @Test
  public void mutualExtensionOfInterfaces() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    Interface branchA = new Interface(l, "BranchA", "branchA_id", "branchA_key");
    Interface branchB = new Interface(l, "BranchB", "branchB_id", "branchB_key");
    branchA.addExtendedInterface(branchB);
    branchB.addExtendedInterface(branchA);
    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "Cyclic hierarchy found: the interface extends itself",
                    branchA),
                new Issue(
                    IssueSeverity.Error,
                    "Cyclic hierarchy found: the interface extends itself",
                    branchB))),
        l.validate().getIssues());
  }

  @Test
  public void aSubAnnotationShouldNotDefineAnnotatesToADifferentValue() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    Concept c = new Concept(l, "C", "c_id", "c_key");
    Concept c2 = new Concept(l, "C2", "c2_id", "c2_key");
    Annotation a = new Annotation(l, "A", "branchA_id", "branchA_key");
    a.setAnnotates(c);
    Annotation b = new Annotation(l, "B", "branchB_id", "branchB_key");
    b.setAnnotates(c2);
    b.setExtendedAnnotation(a);
    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "When a sub annotation specify a value for annotates it must be the same value the super annotation specifies",
                    b))),
        l.validate().getIssues());
  }

  @Test
  public void aSubAnnotationCanReDefineAnnotatesToTheSameValue() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    Concept c = new Concept(l, "C", "c_id", "c_key");
    Annotation a = new Annotation(l, "A", "branchA_id", "branchA_key");
    a.setAnnotates(c);
    Annotation b = new Annotation(l, "B", "branchB_id", "branchB_key");
    b.setAnnotates(c);
    b.setExtendedAnnotation(a);
    assertEquals(Collections.emptySet(), l.validate().getIssues());
  }
}
