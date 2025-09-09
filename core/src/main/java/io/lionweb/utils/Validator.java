package io.lionweb.utils;

import javax.annotation.Nonnull;

/**
 * This abstract class provides a base structure for implementing validation logic on elements of
 * generic type {@code E}. Subclasses are required to implement the {@link #validate} method to
 * define the specific validation rules for the elements of type {@code E}.
 *
 * @param <E> the type of the elements being validated
 */
public abstract class Validator<E> {
  /**
   * Validates the given element of type {@code E} against a defined set of rules. Implementations
   * of this method should provide the specific validation logic for the corresponding element type.
   *
   * @param element the element to be validated
   * @return a {@link ValidationResult} containing the validation outcomes, including any issues
   *     found
   */
  public abstract @Nonnull ValidationResult validate(E element);

  /**
   * Checks if the given element of type {@code E} passes all validation rules. This method
   * internally uses the {@link #validate} method and determines validity based on whether there are
   * any validation issues with severity {@code Error}.
   *
   * @param element the element to be validated
   * @return {@code true} if the element is valid and has no errors; {@code false} otherwise
   */
  public boolean isValid(E element) {
    return validate(element).isSuccessful();
  }
}
