package io.lionweb.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.language.Concept;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.impl.DynamicNode;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ValidationResultTest {

  private ValidationResult validationResult;
  private ClassifierInstance<?> mockSubject;

  @BeforeEach
  public void setUp() {
    validationResult = new ValidationResult();
    Concept myConcept = new Concept();
    mockSubject = new DynamicNode("mockNode", myConcept);
  }

  @Test
  public void testNewValidationResultIsEmpty() {
    assertTrue(validationResult.getIssues().isEmpty());
    assertTrue(validationResult.isSuccessful());
  }

  @Test
  public void testGetIssues() {
    Set<Issue> issues = validationResult.getIssues();
    assertNotNull(issues);
    assertTrue(issues.isEmpty());

    // Verify it returns the same instance
    assertSame(issues, validationResult.getIssues());
  }

  @Test
  public void testIsSuccessfulWithNoIssues() {
    assertTrue(validationResult.isSuccessful());
  }

  @Test
  public void testIsSuccessfulWithWarnings() {
    // Add a warning issue directly to test isSuccessful logic
    validationResult
        .getIssues()
        .add(new Issue(IssueSeverity.Warning, "Warning message", mockSubject));

    assertTrue(validationResult.isSuccessful());
  }

  @Test
  public void testIsSuccessfulWithErrors() {
    validationResult.addError("Error message");

    assertFalse(validationResult.isSuccessful());
  }

  @Test
  public void testIsSuccessfulWithMixedIssues() {
    // Add warning
    validationResult
        .getIssues()
        .add(new Issue(IssueSeverity.Warning, "Warning message", mockSubject));
    // Add error
    validationResult.addError("Error message");

    assertFalse(validationResult.isSuccessful());
  }

  @Test
  public void testAddErrorWithMessage() {
    ValidationResult result = validationResult.addError("Test error message");

    // Should return this for method chaining
    assertSame(validationResult, result);

    Set<Issue> issues = validationResult.getIssues();
    assertEquals(1, issues.size());

    Issue issue = issues.iterator().next();
    assertEquals("Test error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
    assertNull(issue.getSubject());
  }

  @Test
  public void testAddErrorWithMessageAndClassifierInstanceSubject() {
    ValidationResult result = validationResult.addError("Test error message", mockSubject);

    assertSame(validationResult, result);

    Set<Issue> issues = validationResult.getIssues();
    assertEquals(1, issues.size());

    Issue issue = issues.iterator().next();
    assertEquals("Test error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
    assertSame(mockSubject, issue.getSubject());
  }

  @Test
  public void testAddErrorWithMessageAndStringSubject() {
    ValidationResult result = validationResult.addError("Test error message", "subject-id");

    assertSame(validationResult, result);

    Set<Issue> issues = validationResult.getIssues();
    assertEquals(1, issues.size());

    Issue issue = issues.iterator().next();
    assertEquals("Test error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
    // Subject should be null for string constructor based on Issue class structure
  }

  @Test
  public void testAddErrorWithNullMessage() {
    try {
      validationResult.addError(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorWithNullMessageAndClassifierInstance() {
    try {
      validationResult.addError(null, mockSubject);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorWithNullMessageAndString() {
    try {
      validationResult.addError(null, "subject");
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorWithNullClassifierInstanceSubject() {
    ValidationResult result =
        validationResult.addError("Test message", (ClassifierInstance<?>) null);

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());
  }

  @Test
  public void testAddErrorWithNullStringSubject() {
    ValidationResult result = validationResult.addError("Test message", (String) null);

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());
  }

  @Test
  public void testAddErrorIfWithTrueCondition() {
    ValidationResult result = validationResult.addErrorIf(true, "Error message", mockSubject);

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());

    Issue issue = validationResult.getIssues().iterator().next();
    assertEquals("Error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
    assertSame(mockSubject, issue.getSubject());
  }

  @Test
  public void testAddErrorIfWithFalseCondition() {
    ValidationResult result = validationResult.addErrorIf(false, "Error message", mockSubject);

    assertSame(validationResult, result);
    assertTrue(validationResult.getIssues().isEmpty());
  }

  @Test
  public void testAddErrorIfWithTrueConditionAndStringSubject() {
    ValidationResult result = validationResult.addErrorIf(true, "Error message", "subject-id");

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());
  }

  @Test
  public void testAddErrorIfWithFalseConditionAndStringSubject() {
    ValidationResult result = validationResult.addErrorIf(false, "Error message", "subject-id");

    assertSame(validationResult, result);
    assertTrue(validationResult.getIssues().isEmpty());
  }

  @Test
  public void testAddErrorIfWithTrueConditionAndNoSubject() {
    ValidationResult result = validationResult.addErrorIf(true, "Error message");

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());

    Issue issue = validationResult.getIssues().iterator().next();
    assertEquals("Error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
  }

  @Test
  public void testAddErrorIfWithFalseConditionAndNoSubject() {
    ValidationResult result = validationResult.addErrorIf(false, "Error message");

    assertSame(validationResult, result);
    assertTrue(validationResult.getIssues().isEmpty());
  }

  @Test
  public void testAddErrorIfWithNullMessage() {
    try {
      validationResult.addErrorIf(true, (String) null, mockSubject);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorIfWithNullMessageAndStringSubject() {
    try {
      validationResult.addErrorIf(true, null, "subject");
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorIfWithNullMessageAndNoSubject() {
    try {
      validationResult.addErrorIf(true, (String) null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      assertEquals("message should not be null", e.getMessage());
    }
  }

  @Test
  public void testAddErrorIfWithSupplierAndTrueCondition() {
    Supplier<String> messageSupplier = () -> "Supplied error message";
    ValidationResult result = validationResult.addErrorIf(true, messageSupplier, mockSubject);

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());

    Issue issue = validationResult.getIssues().iterator().next();
    assertEquals("Supplied error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
    assertSame(mockSubject, issue.getSubject());
  }

  @Test
  public void testAddErrorIfWithSupplierAndFalseCondition() {
    Supplier<String> messageSupplier =
        () -> {
          fail("Supplier should not be called when condition is false");
          return "Should not be called";
        };

    ValidationResult result = validationResult.addErrorIf(false, messageSupplier, mockSubject);

    assertSame(validationResult, result);
    assertTrue(validationResult.getIssues().isEmpty());
  }

  @Test
  public void testAddErrorIfWithSupplierAndNoSubject() {
    Supplier<String> messageSupplier = () -> "Supplied error message";
    ValidationResult result = validationResult.addErrorIf(true, messageSupplier);

    assertSame(validationResult, result);
    assertEquals(1, validationResult.getIssues().size());

    Issue issue = validationResult.getIssues().iterator().next();
    assertEquals("Supplied error message", issue.getMessage());
    assertEquals(IssueSeverity.Error, issue.getSeverity());
  }

  @Test
  public void testAddErrorIfWithSupplierAndFalseConditionNoSubject() {
    Supplier<String> messageSupplier =
        () -> {
          fail("Supplier should not be called when condition is false");
          return "Should not be called";
        };

    ValidationResult result = validationResult.addErrorIf(false, messageSupplier);

    assertSame(validationResult, result);
    assertTrue(validationResult.getIssues().isEmpty());
  }

  @Test
  public void testMethodChaining() {
    ValidationResult result =
        validationResult
            .addError("First error")
            .addError("Second error", mockSubject)
            .addErrorIf(true, "Third error")
            .addErrorIf(false, "Fourth error"); // This should not be added

    assertSame(validationResult, result);
    assertEquals(3, validationResult.getIssues().size());
    assertFalse(validationResult.isSuccessful());
  }

  @Test
  public void testMultipleErrors() {
    validationResult.addError("Error 1");
    validationResult.addError("Error 2", mockSubject);
    validationResult.addError("Error 3", "subject-id");

    assertEquals(3, validationResult.getIssues().size());
    assertFalse(validationResult.isSuccessful());

    // Verify all errors are present
    boolean found1 = false, found2 = false, found3 = false;
    for (Issue issue : validationResult.getIssues()) {
      if ("Error 1".equals(issue.getMessage())) found1 = true;
      if ("Error 2".equals(issue.getMessage())) found2 = true;
      if ("Error 3".equals(issue.getMessage())) found3 = true;
    }
    assertTrue(found1, "Error 1 should be found");
    assertTrue(found2, "Error 2 should be found");
    assertTrue(found3, "Error 3 should be found");
  }

  @Test
  public void testToStringWithNoIssues() {
    String result = validationResult.toString();
    assertEquals("ValidationResult()", result);
  }

  @Test
  public void testToStringWithOneIssue() {
    validationResult.addError("Test error");
    String result = validationResult.toString();

    assertTrue(result.startsWith("ValidationResult("), "Should contain ValidationResult");
    assertTrue(result.contains("Test error"), "Should contain error message");
    assertTrue(result.endsWith(")"), "Should end with closing parenthesis");
  }

  @Test
  public void testToStringWithMultipleIssues() {
    validationResult.addError("First error");
    validationResult.addError("Second error");
    String result = validationResult.toString();

    assertTrue(result.startsWith("ValidationResult("), "Should contain ValidationResult");
    assertTrue(result.contains("First error"), "Should contain first error");
    assertTrue(result.contains("Second error"), "Should contain second error");
    assertTrue(result.contains(", "), "Should contain separator");
    assertTrue(result.endsWith(")"), "Should end with closing parenthesis");
  }

  @Test
  public void testIssuesSetIsNotNull() {
    Set<Issue> issues = validationResult.getIssues();
    assertNotNull(issues, "Issues set should never be null");
  }

  @Test
  public void testEmptyValidationResultIsSuccessful() {
    ValidationResult emptyResult = new ValidationResult();
    assertTrue(emptyResult.isSuccessful(), "Empty validation result should be successful");
    assertEquals(
        0, emptyResult.getIssues().size(), "Empty validation result should have no issues");
  }

  @Test
  public void testConditionalErrorAddition() {
    String value = "test";

    validationResult
        .addErrorIf(value == null, "Value should not be null")
        .addErrorIf(
            value.length() < 5, "Value should be at least 5 characters", "validation-subject")
        .addErrorIf(value.startsWith("invalid"), "Value should not start with 'invalid'");

    // Only the second condition should add an error
    assertEquals(1, validationResult.getIssues().size());
    assertFalse(validationResult.isSuccessful());

    Issue issue = validationResult.getIssues().iterator().next();
    assertEquals("Value should be at least 5 characters", issue.getMessage());
  }

  @Test
  public void testSupplierNotCalledWhenConditionFalse() {
    boolean[] supplierCalled = {false};
    Supplier<String> expensiveSupplier =
        () -> {
          supplierCalled[0] = true;
          return "Expensive message computation";
        };

    validationResult.addErrorIf(false, expensiveSupplier);

    assertFalse(supplierCalled[0], "Supplier should not be called when condition is false");
    assertTrue(validationResult.isSuccessful(), "Validation should remain successful");
  }

  @Test
  public void testSupplierCalledWhenConditionTrue() {
    boolean[] supplierCalled = {false};
    Supplier<String> messageSupplier =
        () -> {
          supplierCalled[0] = true;
          return "Dynamic error message";
        };

    validationResult.addErrorIf(true, messageSupplier);

    assertTrue(supplierCalled[0], "Supplier should be called when condition is true");
    assertFalse(validationResult.isSuccessful(), "Validation should not be successful");
    assertEquals(1, validationResult.getIssues().size(), "Should have one error");
    assertEquals(
        "Dynamic error message", validationResult.getIssues().iterator().next().getMessage());
  }
}
