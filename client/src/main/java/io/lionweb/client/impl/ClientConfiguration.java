package io.lionweb.client.impl;

import io.lionweb.client.Protocol;
import io.lionweb.serialization.JsonSerialization;
import java.util.Objects;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Connection and authentication settings shared by all HTTP client implementations. */
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
      @NotNull Protocol protocol,
      @NotNull String hostname,
      int port,
      @Nullable String authorizationToken,
      @NotNull String clientID,
      @NotNull String repository,
      @NotNull OkHttpClient httpClient,
      @NotNull JsonSerialization jsonSerialization) {
    Objects.requireNonNull(protocol, "protocol cannot be null");
    Objects.requireNonNull(hostname, "hostname cannot be null");
    Objects.requireNonNull(clientID, "clientID cannot be null");
    Objects.requireNonNull(repository, "repository cannot be null");
    Objects.requireNonNull(httpClient, "httpClient cannot be null");
    Objects.requireNonNull(jsonSerialization, "jsonSerialization cannot be null");
    this.protocol = protocol;
    this.hostname = hostname;
    this.port = port;
    this.clientID = clientID;
    this.repository = repository;
    this.authorizationToken = authorizationToken;
    this.httpClient = httpClient;
    this.jsonSerialization = jsonSerialization;
  }

  public @NotNull Protocol getProtocol() {
    return protocol;
  }

  public @NotNull String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  public @NotNull String getClientID() {
    return clientID;
  }

  public @NotNull String getRepository() {
    return repository;
  }

  public @Nullable String getAuthorizationToken() {
    return authorizationToken;
  }

  public @NotNull OkHttpClient getHttpClient() {
    return httpClient;
  }

  public @NotNull JsonSerialization getJsonSerialization() {
    return jsonSerialization;
  }
}
