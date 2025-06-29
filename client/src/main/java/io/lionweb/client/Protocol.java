package io.lionweb.client;

public enum Protocol {
  HTTP("http"),
  HTTPS("https");
  public final String value;

  Protocol(String value) {
    this.value = value;
  }
}
