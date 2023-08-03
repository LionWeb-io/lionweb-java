package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.Enumeration;

public class DynamicEnumerationValue {
  private Enumeration enumeration;
  private String serializedValue;

  public DynamicEnumerationValue(Enumeration enumeration, String serializedValue) {
    this.enumeration = enumeration;
    this.serializedValue = serializedValue;
  }

  public Enumeration getEnumeration() {
    return enumeration;
  }

  public String getSerializedValue() {
    return serializedValue;
  }
}
