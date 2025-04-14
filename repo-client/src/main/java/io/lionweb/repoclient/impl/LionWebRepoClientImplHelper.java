package io.lionweb.repoclient.impl;

import io.lionweb.repoclient.RequestFailureException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public abstract class LionWebRepoClientImplHelper {
  protected final RepoClientConfiguration conf;

  public LionWebRepoClientImplHelper(RepoClientConfiguration repoClientConfiguration) {
    this.conf = repoClientConfiguration;
  }

  public interface ResponseHandler<R> {
    R handleResponse(Response response, String body);
  }

  protected HttpUrl.Builder buildURL(String api) {
    return buildURL(api, true, true);
  }

  protected HttpUrl.Builder buildURL(
      String api, boolean specifyingClientID, boolean specifyingRepository) {
    String url = conf.getProtocol() + "://" + conf.getHostname() + ":" + conf.getPort() + api;
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    if (specifyingClientID) {
      urlBuilder.addQueryParameter("clientId", conf.getClientID());
    }
    if (specifyingRepository) {
      urlBuilder.addQueryParameter("repository", conf.getRepository());
    }
    return urlBuilder;
  }

  protected Request.Builder buildRequest(String api) {
    return buildRequest(api, true, true, true, Collections.emptyMap());
  }

  protected Request.Builder buildRequest(String api, Map<String, String> additionalParams) {
    return buildRequest(api, true, true, true, additionalParams);
  }

  protected Request.Builder buildRequest(
      String api,
      boolean specifyingClientID,
      boolean specifyingRepository,
      boolean considerAuthenticationToken,
      Map<String, String> additionalParams) {
    HttpUrl.Builder urlBuilder = buildURL(api, specifyingClientID, specifyingRepository);
    additionalParams
        .entrySet()
        .forEach(entry -> urlBuilder.addQueryParameter(entry.getKey(), entry.getValue()));
    Request.Builder rq = new Request.Builder().url(urlBuilder.build());
    if (considerAuthenticationToken) {
      rq = considerAuthenticationToken(rq);
    }
    return rq;
  }

  protected <R> R performCall(Request request, ResponseHandler<R> responseHandler)
      throws IOException {
    try (Response response = conf.getHttpClient().newCall(request).execute()) {
      String body = Objects.requireNonNull(response.body()).string();
      if (response.code() == HttpURLConnection.HTTP_OK) {
        return responseHandler.handleResponse(response, body);
      } else {
        throw new RequestFailureException(request.url().toString(), response.code(), body);
      }
    }
  }

  protected Request.Builder considerAuthenticationToken(Request.Builder builder) {
    return (conf.getAuthorizationToken() == null)
        ? builder
        : builder.addHeader("Authorization", conf.getAuthorizationToken());
  }
}
