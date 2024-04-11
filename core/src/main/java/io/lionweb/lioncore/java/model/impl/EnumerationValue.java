package io.lionweb.lioncore.java.model.impl;

import io.lionweb.lioncore.java.language.EnumerationLiteral;

/**
 * An enumeration value represented through this interface can be automatically supported by the
 * serialization mechanism. Enumeration values can be represented otherwise, but in that case the
 * specific serializers and deserializers should be registered.
 */
public interface EnumerationValue {
  EnumerationLiteral getEnumerationLiteral();
}
