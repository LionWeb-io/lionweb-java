package io.lionweb.repoclient.impl;

import io.lionweb.repoclient.Protocol;
import okhttp3.OkHttpClient;

public class RepoClientConfiguration {
  protected final Protocol protocol;

  public RepoClientConfiguration(
      Protocol protocol,
      String hostname,
      int port,
      String authorizationToken,
      String clientID,
      String repository,
      OkHttpClient httpClient) {
    this.protocol = protocol;
    this.hostname = hostname;
    this.port = port;
    this.clientID = clientID;
    this.repository = repository;
    this.authorizationToken = authorizationToken;
    this.httpClient = httpClient;
  }

  private final String hostname;
  private final int port;
  private final String clientID;
  private final String repository;
  private final String authorizationToken;
  private final OkHttpClient httpClient;

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
}
