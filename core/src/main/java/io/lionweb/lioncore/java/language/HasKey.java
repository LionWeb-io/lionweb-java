package io.lionweb.lioncore.java.language;

public interface HasKey<T> {
  String getKey();

  T setKey(String value);
}
