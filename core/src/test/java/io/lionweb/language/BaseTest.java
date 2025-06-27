package io.lionweb.language;

import static org.junit.Assert.*;

import io.lionweb.model.Node;
import io.lionweb.utils.LanguageValidator;
import io.lionweb.utils.NodeTreeValidator;
import io.lionweb.utils.ValidationResult;

public abstract class BaseTest {

  public void assertNodeTreeIsValid(Node node) {
    NodeTreeValidator nodeTreeValidator = new NodeTreeValidator();
    ValidationResult validationResult = nodeTreeValidator.validate(node);
    assertTrue(validationResult.toString(), validationResult.isSuccessful());
  }

  public void assertNodeTreeIsNotValid(Node node) {
    NodeTreeValidator nodeTreeValidator = new NodeTreeValidator();
    ValidationResult validationResult = nodeTreeValidator.validate(node);
    assertFalse(validationResult.toString(), validationResult.isSuccessful());
  }

  public void assertLanguageIsValid(Language language) {
    LanguageValidator languageValidator = new LanguageValidator();
    ValidationResult validationResult = languageValidator.validate(language);
    assertTrue(validationResult.toString(), validationResult.isSuccessful());
  }

  public void assertLanguageIsNotValid(Language language) {
    LanguageValidator languageValidator = new LanguageValidator();
    ValidationResult validationResult = languageValidator.validate(language);
    assertFalse(validationResult.toString(), validationResult.isSuccessful());
  }
}
