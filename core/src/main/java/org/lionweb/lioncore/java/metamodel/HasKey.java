package org.lionweb.lioncore.java.metamodel;

public interface HasKey<T> {
    String getKey();
    T setKey(String value);
}
