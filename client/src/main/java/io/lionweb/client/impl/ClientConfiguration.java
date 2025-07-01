package io.lionweb.client.impl;

import io.lionweb.client.Protocol;
import io.lionweb.serialization.JsonSerialization;
import okhttp3.OkHttpClient;

public class ClientConfiguration {
  protected final Protocol protocol;
  private final String hostname;
  private final int port;
  private final String clientID;
  private final String repository;
  private final String authorizationToken;
  private final OkHttpClient httpClient;
  private final JsonSerialization jsonSerialization;

  public ClientConfiguration(
      Protocol protocol,
      String hostname,
      int port,
      String authorizationToken,
      String clientID,
      String repository,
      OkHttpClient httpClient,
      JsonSerialization jsonSerialization) {
    this.protocol = protocol;
    this.hostname = hostname;
    this.port = port;
    this.clientID = clientID;
    this.repository = repository;
    this.authorizationToken = authorizationToken;
    this.httpClient = httpClient;
    this.jsonSerialization = jsonSerialization;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public String getClientID() {
    return clientID;
  }

  public String getRepository() {
    return repository;
  }

  public String getAuthorizationToken() {
    return authorizationToken;
  }

  public OkHttpClient getHttpClient() {
    return httpClient;
  }

  public JsonSerialization getJsonSerialization() {
    return jsonSerialization;
  }
}
