package io.lionweb.repoclient.impl;

import io.lionweb.repoclient.Protocol;
import io.lionweb.repoclient.RequestFailureException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public abstract class LionWebRepoClientImplHelper {

  protected final Protocol protocol;
  protected final String hostname;
  protected final int port;
  protected final String clientID;
  protected final String repository;

  public LionWebRepoClientImplHelper(
      Protocol protocol,
      String hostname,
      int port,
      String clientID,
      String repository,
      String authorizationToken,
      OkHttpClient httpClient) {
    this.protocol = protocol;
    this.hostname = hostname;
    this.port = port;
    this.clientID = clientID;
    this.repository = repository;
    this.authorizationToken = authorizationToken;
    this.httpClient = httpClient;
  }

  protected final String authorizationToken;
  protected final OkHttpClient httpClient;

  public interface ResponseHandler<R> {
    R handleResponse(Response response, String body);
  }

  protected HttpUrl.Builder buildURL(String api) {
    return buildURL(api, true, true);
  }

  protected HttpUrl.Builder buildURL(
      String api, boolean specifyingClientID, boolean specifyingRepository) {
    String url = protocol + "://" + hostname + ":" + port + api;
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    if (specifyingClientID) {
      urlBuilder.addQueryParameter("clientId", clientID);
    }
    if (specifyingRepository) {
      urlBuilder.addQueryParameter("repository", repository);
    }
    return urlBuilder;
  }

  protected Request.Builder buildRequest(String api) {
    return buildRequest(api, true, true, true);
  }

  protected Request.Builder buildRequest(
      String api,
      boolean specifyingClientID,
      boolean specifyingRepository,
      boolean considerAuthenticationToken) {
    HttpUrl.Builder urlBuilder = buildURL(api, specifyingClientID, specifyingRepository);
    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    rq = considerAuthenticationToken(rq);
    return rq;
  }

  protected <R> R performCall(Request request, ResponseHandler<R> responseHandler)
      throws IOException {
    try (Response response = httpClient.newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        return responseHandler.handleResponse(response, body);
      } else {
        throw new RequestFailureException(request.url().toString(), response.code(), body);
      }
    }
  }

  protected Request.Builder considerAuthenticationToken(Request.Builder builder) {
    return (authorizationToken == null)
        ? builder
        : builder.addHeader("Authorization", authorizationToken);
  }
}
