package io.lionweb.lioncore.java.language;

/**
 * Any element in a Language (M2) that can be referred from an instance (M1).
 *
 * @param <T> Type of keyed element.
 */
public interface IKeyed<T> extends INamed {
  String getKey();

  T setKey(String value);
}
