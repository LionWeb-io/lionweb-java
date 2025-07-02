package io.lionweb.client.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.LionWebVersion;
import io.lionweb.client.RequestFailureException;
import io.lionweb.client.api.*;
import io.lionweb.client.api.DBAdminAPIClient;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

public class ClientForDBAdminAPIs extends LionWebClientImplHelper implements DBAdminAPIClient {

  public ClientForDBAdminAPIs(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
  }

  @Override
  public void createRepository(@NotNull RepositoryConfiguration repositoryConfiguration)
      throws IOException {
    Objects.requireNonNull(repositoryConfiguration, "repositoryConfiguration should not be null");
    Map<String, String> params = new HashMap<>();
    params.put("lionWebVersion", repositoryConfiguration.getLionWebVersion().getVersionString());
    params.put(
        "history", Boolean.toString(repositoryConfiguration.getHistorySupport().toBoolean()));
    params.put("repository", repositoryConfiguration.getName());
    Request.Builder rq = buildRequest("/createRepository", true, false, true, params);
    Request request = rq.post(RequestBody.create(new byte[0])).build();
    performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return null;
        });
  }

  @Override
  public void deleteRepository(@NotNull String repositoryName) throws IOException {
    Objects.requireNonNull(repositoryName, "repositoryName should not be null");

    Map<String, String> params = new HashMap<>();
    params.put("repository", repositoryName);
    Request.Builder rq = buildRequest("/deleteRepository", true, false, true, params);
    Request request = rq.post(RequestBody.create(new byte[0])).build();
    performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return null;
        });
  }

  @Override
  public void createDatabase() throws IOException {
    Request.Builder rq =
        buildRequest("/createDatabase", false, false, true, Collections.emptyMap());
    Request request = rq.post(RequestBody.create(new byte[0])).build();
    performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return null;
        });
  }

  @Override
  public @NotNull Set<RepositoryConfiguration> listRepositories() throws IOException {
    Request.Builder rq =
        buildRequest("/listRepositories", false, false, true, Collections.emptyMap());
    Request request = rq.post(RequestBody.create(new byte[0])).build();
    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          return responseData.get("repositories").getAsJsonArray().asList().stream()
              .map(
                  el -> {
                    JsonObject elJO = el.getAsJsonObject();
                    String name = elJO.get("name").getAsString();
                    LionWebVersion lionWebVersion =
                        LionWebVersion.fromValue(elJO.get("lionweb_version").getAsString());
                    HistorySupport historySupport =
                        HistorySupport.fromBoolean(elJO.get("history").getAsBoolean());
                    return new RepositoryConfiguration(name, lionWebVersion, historySupport);
                  })
              .collect(Collectors.toSet());
        });
  }
}
