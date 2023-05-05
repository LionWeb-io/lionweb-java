package io.lionweb.lioncore.java.utils;

public abstract class Validator<E> {
  public abstract ValidationResult validate(E element);

  public boolean isValid(E element) {
    return validate(element).isSuccessful();
  }
}
