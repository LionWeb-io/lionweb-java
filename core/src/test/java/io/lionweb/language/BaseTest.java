package io.lionweb.language;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.model.Node;
import io.lionweb.utils.LanguageValidator;
import io.lionweb.utils.NodeTreeValidator;
import io.lionweb.utils.ValidationResult;

public abstract class BaseTest {

  public void assertNodeTreeIsValid(Node node) {
    NodeTreeValidator nodeTreeValidator = new NodeTreeValidator();
    ValidationResult validationResult = nodeTreeValidator.validate(node);
    assertTrue(validationResult.isSuccessful(), validationResult.toString());
  }

  public void assertNodeTreeIsNotValid(Node node) {
    NodeTreeValidator nodeTreeValidator = new NodeTreeValidator();
    ValidationResult validationResult = nodeTreeValidator.validate(node);
    assertFalse(validationResult.isSuccessful(), validationResult.toString());
  }

  public void assertLanguageIsValid(Language language) {
    LanguageValidator languageValidator = new LanguageValidator();
    ValidationResult validationResult = languageValidator.validate(language);
    assertTrue(validationResult.isSuccessful(), validationResult.toString());
  }

  public void assertLanguageIsNotValid(Language language) {
    LanguageValidator languageValidator = new LanguageValidator();
    ValidationResult validationResult = languageValidator.validate(language);
    assertFalse(validationResult.isSuccessful(), validationResult.toString());
  }
}
