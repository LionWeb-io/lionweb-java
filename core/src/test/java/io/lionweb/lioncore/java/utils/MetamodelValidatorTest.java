package io.lionweb.lioncore.java.utils;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.self.LionCore;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;

public class MetamodelValidatorTest {

  @Test
  @Ignore
  public void anEmptyAnnotationIsInvalid() {
    Metamodel metamodel = new Metamodel().setKey("mm-key");
    Annotation annotation = new Annotation().setKey("aa-key");
    metamodel.addElement(annotation);

    assertFalse(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
    assertFalse(new MetamodelValidator().isMetamodelValid(metamodel));
    assertEquals(2, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
    assertTrue(
        new MetamodelValidator()
            .validateMetamodel(metamodel).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(Arrays.asList("Simple name not set", "Qualified name not set")),
        new MetamodelValidator()
            .validateMetamodel(metamodel).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  @Ignore
  public void anAnnotationCanBeValid() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Annotation annotation = new Annotation(metamodel, "MyAnnotation").setKey("annotation-key");
    metamodel.addElement(annotation);

    assertTrue(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
    assertTrue(new MetamodelValidator().isMetamodelValid(metamodel));
    assertEquals(0, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
  }

  @Test
  public void anEmptyPrimitiveTypeIsInvalid() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType = new PrimitiveType().setKey("pt-key");
    metamodel.addElement(primitiveType);

    assertFalse(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
    assertFalse(new MetamodelValidator().isMetamodelValid(metamodel));
    assertEquals(1, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
    assertTrue(
        new MetamodelValidator()
            .validateMetamodel(metamodel).getIssues().stream().allMatch(issue -> issue.isError()));
    assertEquals(
        new HashSet<>(Arrays.asList("Simple name not set")),
        new MetamodelValidator()
            .validateMetamodel(metamodel).getIssues().stream()
                .map(issue -> issue.getMessage())
                .collect(Collectors.toSet()));
  }

  @Test
  public void aPrimitiveTypeCanBeValid() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    PrimitiveType primitiveType = new PrimitiveType(metamodel, "PrimitiveType").setKey("pt-key");
    metamodel.addElement(primitiveType);

    assertTrue(new MetamodelValidator().validateMetamodel(metamodel).isSuccessful());
    assertTrue(new MetamodelValidator().isMetamodelValid(metamodel));
    assertEquals(0, new MetamodelValidator().validateMetamodel(metamodel).getIssues().size());
  }

  @Test
  public void simpleSelfInheritanceIsCaught() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(metamodel, "a").setKey("key-a");
    a.setExtendedConcept(a);
    metamodel.addElement(a);

    assertEquals(
        new HashSet<>(Arrays.asList(new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a))),
        new MetamodelValidator().validateMetamodel(metamodel).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptsIsCaught() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(metamodel, "a").setKey("key-a");
    Concept b = new Concept(metamodel, "b").setKey("key-b");
    a.setExtendedConcept(b);
    b.setExtendedConcept(a);
    metamodel.addElement(a);
    metamodel.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
        new MetamodelValidator().validateMetamodel(metamodel).getIssues());
  }

  @Test
  public void indirectSelfInheritanceOfConceptInterfacesIsCaught() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    ConceptInterface a = new ConceptInterface(metamodel, "a").setKey("a-key");
    ConceptInterface b = new ConceptInterface(metamodel, "b").setKey("b-key");
    a.addExtendedInterface(b);
    b.addExtendedInterface(a);
    metamodel.addElement(a);
    metamodel.addElement(b);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", a),
                new Issue(IssueSeverity.Error, "Cyclic hierarchy found", b))),
        new MetamodelValidator().validateMetamodel(metamodel).getIssues());
  }

  @Test
  public void multipleDirectImplementationsOfTheSameInterfaceAreNotAllowed() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setKey("mm-key");
    Concept a = new Concept(metamodel, "a").setKey("key-a");
    ConceptInterface i = new ConceptInterface(metamodel, "I").setKey("key-i");

    a.addImplementedInterface(i);
    a.addImplementedInterface(i);

    metamodel.addElement(a);
    metamodel.addElement(i);

    assertEquals(
        new HashSet<>(
            Arrays.asList(
                new Issue(
                    IssueSeverity.Error,
                    "The same interface has been implemented multiple times",
                    a))),
        new MetamodelValidator().validateMetamodel(metamodel).getIssues());
  }

  @Test
  public void multipleIndirectImplementationsOfTheSameInterfaceAreAllowed() {
    Metamodel metamodel = new Metamodel("MyMetamodel").setID("myM3ID").setKey("myM3key");
    Concept a = new Concept(metamodel, "A").setKey("a-key");
    Concept b = new Concept(metamodel, "B").setKey("b-key");
    ConceptInterface i = new ConceptInterface(metamodel, "I").setKey("i-key");

    a.setExtendedConcept(b);
    a.addImplementedInterface(i);
    b.addImplementedInterface(i);

    metamodel.addElement(a);
    metamodel.addElement(b);
    metamodel.addElement(i);

    assertEquals(
        new HashSet<>(Arrays.asList()),
        new MetamodelValidator().validateMetamodel(metamodel).getIssues());
  }

  @Test
  public void ensuringLionCoreIsValidated() {
    ;
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new MetamodelValidator().validateMetamodel(LionCore.getInstance()).getIssues());
  }

  @Test
  public void ensuringLionCoreBuiltinsIsValidated() {
    assertEquals(
        new HashSet<>(Arrays.asList()),
        new MetamodelValidator().validateMetamodel(LionCoreBuiltins.getInstance()).getIssues());
  }
}
