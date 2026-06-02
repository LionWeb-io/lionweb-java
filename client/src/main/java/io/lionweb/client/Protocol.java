package io.lionweb.client;

import org.jetbrains.annotations.NotNull;

/** Enum representing communication protocols for commonly used protocols HTTP and HTTPS. */
public enum Protocol {
  HTTP("http"),
  HTTPS("https");
  public final @NotNull String value;

  Protocol(@NotNull String value) {
    this.value = value;
  }
}
