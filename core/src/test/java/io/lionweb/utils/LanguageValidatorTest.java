package io.lionweb.utils;

import static org.junit.Assert.*;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.lioncore.LionCore;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
        new Annotation(language, "MyAnnotation", "annotation-id").setKey("annotation-key");
    language.addElement(annotation);

    assertFalse(new LanguageValidator().validate(language).isSuccessful());
    assertFalse(new LanguageValidator().isLanguageValid(language));
    assertEquals(1, new LanguageValidator().validate(language).getIssues().size());
  }

  @Test
  public void anAnnotationCanBeValid() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Annotation annotation =
        new Annotation(language, "MyAnnotation", "annotation-id").setKey("annotation-key");
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
        new PrimitiveType(language, "PrimitiveType", "pt-id").setKey("pt-key");
    language.addElement(primitiveType);

    assertTrue(new LanguageValidator().validate(language).isSuccessful());
    assertTrue(new LanguageValidator().isLanguageValid(language));
    assertEquals(0, new LanguageValidator().validate(language).getIssues().size());
  }

  @Test
  public void simpleSelfInheritanceIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a", "id-a").setKey("key-a");
    a.setExtendedConcept(a);
    language.addElement(a);

    assertEquals(
        new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a))),
        new LanguageValidator().validate(language).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptsIsCaught() {
    Language language = new Language("MyLanguage").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(language, "a", "id-a").setKey("key-a");
    Concept b = new Concept(language, "b", "id-b").setKey("key-b");
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
    Interface a = new Interface(language, "a", "a-id").setKey("a-key");
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
    Interface a = new Interface(language, "a", "a-id").setKey("a-key");
    Interface b = new Interface(language, "b", "b-id").setKey("b-key");
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
    Concept a = new Concept(language, "a", "id-a").setKey("key-a");
    Interface i = new Interface(language, "I", "id-i").setKey("key-i");

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
    Concept a = new Concept(language, "A", "a-id").setKey("a-key");
    Concept b = new Concept(language, "B", "b-id").setKey("b-key");
    Interface i = new Interface(language, "I", "i-id").setKey("i-key");

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

  @Test
  public void anSDTWithNoFieldsIsInvalid() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    l.addDependency(LionCoreBuiltins.getInstance());
    StructuredDataType sdt = new StructuredDataType(l, "SDT", "sdt_id", "sdt_key");
    Set<Issue> issuesA = l.validate().getIssues();
    assertEquals(1, issuesA.size());
    Issue issueA0 = issuesA.iterator().next();
    assertEquals(
        new Issue(
            IssueSeverity.Error,
            "Containment fields is required but no children are specified",
            sdt),
        issueA0);
    sdt.addField(new Field("MyField", LionCoreBuiltins.getString(), "f_id", "f_key"));
    Set<Issue> issuesB = l.validate().getIssues();
    assertEquals(Collections.emptySet(), issuesB);
  }

  @Test
  public void anSDTShouldNotHaveDirectlyCircularReferences() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    l.addDependency(LionCoreBuiltins.getInstance());
    StructuredDataType sdt = new StructuredDataType(l, "SDT", "sdt_id", "sdt_key");
    sdt.addField(new Field("MyStringField", LionCoreBuiltins.getString(), "fs_id", "fs_key"));
    assertEquals(Collections.emptySet(), l.validate().getIssues());

    sdt.addField(new Field("MyCircularField", sdt, "fc_id", "fc_key"));
    Set<Issue> issuesB = l.validate().getIssues();
    assertEquals(1, issuesB.size());
    Issue issueB0 = issuesB.iterator().next();
    assertEquals(
        new Issue(
            IssueSeverity.Error, "Circular references are forbidden in StructuralDataFields", sdt),
        issueB0);
  }

  @Test
  public void anSDTShouldNotHaveIndirectlyCircularReferences() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    l.addDependency(LionCoreBuiltins.getInstance());
    StructuredDataType sdtA = new StructuredDataType(l, "SDTA", "sdta_id", "sdta_key");
    StructuredDataType sdtB = new StructuredDataType(l, "SDTB", "sdtb_id", "sdtb_key");
    sdtA.addField(new Field("MySDTField", sdtB, "fsdt1_id", "fsdt1_key"));
    sdtB.addField(new Field("MyStringField", LionCoreBuiltins.getString(), "fs_id", "fs_key"));
    assertEquals(Collections.emptySet(), l.validate().getIssues());

    sdtB.addField(new Field("MyCircularField", sdtA, "fc_id", "fc_key"));
    Set<Issue> issuesB = l.validate().getIssues();
    assertEquals(2, issuesB.size());
    assert (issuesB.contains(
        new Issue(
            IssueSeverity.Error,
            "Circular references are forbidden in StructuralDataFields",
            sdtA)));
    assert (issuesB.contains(
        new Issue(
            IssueSeverity.Error,
            "Circular references are forbidden in StructuralDataFields",
            sdtB)));
  }

  @Test
  public void checkDirectCircularityOfSDTs() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    StructuredDataType sdt = new StructuredDataType(l, "SDT", "sdt_id", "sdt_key");
    sdt.addField(new Field("MyStringField", LionCoreBuiltins.getString(), "fs_id", "fs_key"));
    assertFalse(LanguageValidator.isCircular(sdt));

    sdt.addField(new Field("MyCircularField", sdt, "fc_id", "fc_key"));
    assertTrue(LanguageValidator.isCircular(sdt));
  }

  @Test
  public void checkIndirectCircularityOfSDTs() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    StructuredDataType sdtA = new StructuredDataType(l, "SDTA", "sdta_id", "sdta_key");
    StructuredDataType sdtB = new StructuredDataType(l, "SDTB", "sdtb_id", "sdtb_key");
    sdtA.addField(new Field("MySDTField", sdtB, "fsdt1_id", "fsdt1_key"));
    sdtB.addField(new Field("MyStringField", LionCoreBuiltins.getString(), "fs_id", "fs_key"));
    assertFalse(LanguageValidator.isCircular(sdtA));
    assertFalse(LanguageValidator.isCircular(sdtB));

    sdtB.addField(new Field("MyCircularField", sdtA, "fc_id", "fc_key"));
    assertTrue(LanguageValidator.isCircular(sdtA));
    assertTrue(LanguageValidator.isCircular(sdtB));
  }

  @Test
  public void thirdLevelCircularityOfSDTs() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    StructuredDataType sdtA = new StructuredDataType(l, "SDTA", "sdta_id", "sdta_key");
    StructuredDataType sdtB = new StructuredDataType(l, "SDTB", "sdtb_id", "sdtb_key");
    StructuredDataType sdtC = new StructuredDataType(l, "SDTC", "sdtc_id", "sdtc_key");
    sdtA.addField(new Field("f1", sdtB, "f1_id", "f1_key"));
    sdtB.addField(new Field("f2", sdtC, "f2_id", "f2_key"));
    assertFalse(LanguageValidator.isCircular(sdtA));
    assertFalse(LanguageValidator.isCircular(sdtB));
    assertFalse(LanguageValidator.isCircular(sdtC));
    sdtC.addField(new Field("f3", sdtA, "f3_id", "f3_key"));
    assertTrue(LanguageValidator.isCircular(sdtA));
    assertTrue(LanguageValidator.isCircular(sdtB));
    assertTrue(LanguageValidator.isCircular(sdtC));
  }

  @Test
  public void fifthLevelCircularityOfSDTs() {
    Language l = new Language("MyLanguage", "my_language_id", "my_language_key");
    StructuredDataType sdtA = new StructuredDataType(l, "SDTA", "sdta_id", "sdta_key");
    StructuredDataType sdtB = new StructuredDataType(l, "SDTB", "sdtb_id", "sdtb_key");
    StructuredDataType sdtC = new StructuredDataType(l, "SDTC", "sdtc_id", "sdtc_key");
    StructuredDataType sdtD = new StructuredDataType(l, "SDTD", "sdtd_id", "sdtd_key");
    StructuredDataType sdtE = new StructuredDataType(l, "SDTE", "sdte_id", "sdte_key");
    sdtA.addField(new Field("f1", sdtB, "f1_id", "f1_key"));
    sdtB.addField(new Field("f2", sdtC, "f2_id", "f2_key"));
    sdtC.addField(new Field("f3", sdtD, "f3_id", "f3_key"));
    sdtD.addField(new Field("f4", sdtE, "f4_id", "f4_key"));
    assertFalse(LanguageValidator.isCircular(sdtA));
    assertFalse(LanguageValidator.isCircular(sdtB));
    assertFalse(LanguageValidator.isCircular(sdtC));
    assertFalse(LanguageValidator.isCircular(sdtD));
    assertFalse(LanguageValidator.isCircular(sdtE));
    sdtE.addField(new Field("f5", sdtA, "f5_id", "f5_key"));
    assertTrue(LanguageValidator.isCircular(sdtA));
    assertTrue(LanguageValidator.isCircular(sdtB));
    assertTrue(LanguageValidator.isCircular(sdtC));
    assertTrue(LanguageValidator.isCircular(sdtD));
    assertTrue(LanguageValidator.isCircular(sdtE));
  }

  @Test
  public void signalLWVersionInconsistencyOnConcept() {
    Language l = new Language(LionWebVersion.v2024_1, "MyLanguage").setID("l-id").setKey("l-key");
    Concept c = new Concept(LionWebVersion.v2023_1, "MyConcept").setID("c-id").setKey("c-key");
    l.addElement(c);

    ValidationResult validationResult = new LanguageValidator().validate(l);
    assertFalse(validationResult.isSuccessful());
    assertEquals(1, validationResult.getIssues().size());
    assertEquals(
        new Issue(IssueSeverity.Error, "Inconsistent LionWeb Versions used", l),
        validationResult.getIssues().iterator().next());
  }

  @Test
  public void signalLWVersionInconsistencyOnFeature() {
    Language l = new Language(LionWebVersion.v2024_1, "MyLanguage").setID("l-id").setKey("l-key");
    Concept c = new Concept(LionWebVersion.v2024_1, "MyConcept").setID("c-id").setKey("c-key");
    l.addElement(c);
    Property p = new Property(LionWebVersion.v2023_1, "MyProperty").setID("p-id").setKey("p-key");
    c.addFeature(p);

    ValidationResult validationResult = new LanguageValidator().validate(l);
    assertFalse(validationResult.isSuccessful());
    assertEquals(1, validationResult.getIssues().size());
    assertEquals(
        new Issue(IssueSeverity.Error, "Inconsistent LionWeb Versions used", l),
        validationResult.getIssues().iterator().next());
  }

  @Test
  public void verifyLanguageDependencies() {
    Language l1 = new Language("MyLanguage1", "my_language1_id", "my_language1_key");

    ValidationResult r1 = new LanguageValidator().validate(l1);
    assertTrue(r1.isSuccessful());

    Language l2 = new Language("MyLanguage2", "my_language2_id", "my_language2_key");
    Concept c2 = new Concept(l2, "MyConcept2", "c2-d").setKey("c2-key");
    Concept c1 = new Concept(l1, "MyConcept1", "c1-d").setKey("c1-key");

    ValidationResult r2 = new LanguageValidator().validate(l1);
    assertTrue(r2.isSuccessful());

    c1.setExtendedConcept(c2);

    ValidationResult r3 = new LanguageValidator().validate(l1);
    assertFalse(r3.isSuccessful());
    assertEquals(1, r3.getIssues().size());
    assertEquals(
        new Issue(
            IssueSeverity.Error,
            "Language my_language2_key version null is not listed among dependencies",
            l1),
        r3.getIssues().iterator().next());
  }
}
