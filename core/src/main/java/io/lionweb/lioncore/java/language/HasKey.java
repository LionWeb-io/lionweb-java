package io.lionweb.lioncore.java.language;

/**
 * Implementation detail of the Java implementation. Not part of the official M3.
 *
 * @param <T>
 */
public interface HasKey<T> {
  String getKey();

  T setKey(String value);
}
