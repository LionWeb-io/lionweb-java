package io.lionweb.repoclient.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lionweb.repoclient.RequestFailureException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;
import okhttp3.*;

abstract class LionWebRepoClientImplHelper {
  protected static final MediaType JSON = MediaType.get("application/json");

  protected final RepoClientConfiguration conf;
  protected final Gson gson = new GsonBuilder().serializeNulls().create();

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
    if (!api.startsWith("/") || api.length() < 2) {
      throw new IllegalArgumentException(
          "api path expected to start with a slash and be at least two characters long");
    }
    HttpUrl.Builder urlBuilder =
        new HttpUrl.Builder()
            .scheme(conf.getProtocol().value)
            .host(conf.getHostname())
            .port(conf.getPort())
            .addPathSegments(api.substring(1));
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

  protected Request.Builder addGZipCompressionHeader(Request.Builder builder) {
    return builder.addHeader("Content-Encoding", "gzip");
  }
}
