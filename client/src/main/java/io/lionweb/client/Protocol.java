package io.lionweb.client;

/** Enum representing communication protocols for commonly used protocols HTTP and HTTPS. */
public enum Protocol {
  HTTP("http"),
  HTTPS("https");
  public final String value;

  Protocol(String value) {
    this.value = value;
  }
}
