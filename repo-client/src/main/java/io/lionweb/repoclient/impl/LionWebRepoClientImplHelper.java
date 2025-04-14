package io.lionweb.repoclient.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lionweb.repoclient.CompressionSupport;
import io.lionweb.repoclient.RequestFailureException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.*;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public abstract class LionWebRepoClientImplHelper {
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

  protected void nodesStoringOperation(final String json, final String operation) {
    // Build the request
    Request.Builder rb = buildRequest("/bulk/" + operation);
    rb = addGZipCompressionHeader(rb);
    RequestBody body =
        CompressionSupport.compress(
            json); // assuming CompressUtil.compress(String) handles JSON compression
    Request request = rb.post(body).build();

    String url = request.url().toString();
    try {
      try (Response response = conf.getHttpClient().newCall(request).execute()) {
        if (response.code() != HttpURLConnection.HTTP_OK) {
          String responseBody = response.body() != null ? response.body().string() : null;
          throw new RequestFailureException(url, response.code(), responseBody);
        }
      }
    } catch (ConnectException e) {
      String jsonExcerpt = json.length() > 10000 ? json.substring(0, 1000) + "..." : json;
      throw new RuntimeException(
          "Cannot get answer from the client when contacting at URL "
              + url
              + ". Body: "
              + jsonExcerpt,
          e);
    } catch (IOException e) {
      throw new RuntimeException("IO error while contacting URL " + url, e);
    }
  }

  protected Request.Builder addGZipCompressionHeader(Request.Builder builder) {
    return builder.addHeader("Content-Encoding", "gzip");
  }

  protected RequestBody gzipCompress(RequestBody original) throws IOException {
    Buffer buffer = new Buffer();
    original.writeTo(buffer);

    RequestBody gzippedBody =
        new RequestBody() {
          @Override
          public MediaType contentType() {
            return original.contentType();
          }

          @Override
          public long contentLength() {
            return -1; // unknown
          }

          @Override
          public void writeTo(BufferedSink sink) throws IOException {
            GzipSink gzipSink = new GzipSink(sink);
            BufferedSink compressedSink = Okio.buffer(gzipSink);
            buffer.copyTo(compressedSink.buffer(), 0, buffer.size());
            compressedSink.close();
          }
        };

    return gzippedBody;
  }
}
