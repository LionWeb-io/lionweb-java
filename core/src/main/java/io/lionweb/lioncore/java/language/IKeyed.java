package io.lionweb.lioncore.java.language;

/**
 * Implementation detail of the Java implementation. Not part of the official M3. TODO: Is this
 * statement still correct?
 *
 * @param <T> Type of keyed element.
 */
public interface IKeyed<T> extends INamed {
  String getKey();

  T setKey(String value);
}
