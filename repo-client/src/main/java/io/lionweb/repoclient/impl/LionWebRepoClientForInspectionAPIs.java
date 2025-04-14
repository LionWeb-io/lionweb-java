package io.lionweb.repoclient.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lionweb.repoclient.Protocol;
import io.lionweb.repoclient.api.ClassifierKey;
import io.lionweb.repoclient.api.ClassifierResult;
import io.lionweb.repoclient.api.InspectionAPIClient;
import java.io.IOException;
import java.util.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class LionWebRepoClientForInspectionAPIs extends LionWebRepoClientImplHelper
    implements InspectionAPIClient {

  public LionWebRepoClientForInspectionAPIs(
      Protocol protocol,
      String hostname,
      int port,
      String clientID,
      String repository,
      String authorizationToken,
      OkHttpClient httpClient) {
    super(protocol, hostname, port, clientID, repository, authorizationToken, httpClient);
  }

  @Override
  public Map<ClassifierKey, ClassifierResult> nodesByClassifier() throws IOException {
    Request.Builder rq = buildRequest("/inspection/nodesByClassifier");
    Request request = rq.get().build();
    return performCall(
        request,
        (response, requestBody) -> {
          JsonArray responseData = JsonParser.parseString(requestBody).getAsJsonArray();
          Map<ClassifierKey, ClassifierResult> result = new HashMap<>();
          responseData.forEach(
              entry -> {
                JsonObject entryJO = entry.getAsJsonObject();

                String language = entryJO.get("language").getAsString();
                String classifier = entryJO.get("classifier").getAsString();
                ClassifierKey classifierKey = new ClassifierKey(language, classifier);

                Set<String> ids = new HashSet<>();
                entryJO.get("ids").getAsJsonArray().forEach(el -> ids.add(el.getAsString()));
                int size = entryJO.get("size").getAsInt();
                ClassifierResult classifierResult = new ClassifierResult(ids, size);

                result.put(classifierKey, classifierResult);
              });
          return result;
        });
  }

  @Override
  public Map<String, ClassifierResult> nodesByLanguage() throws IOException {
    Request.Builder rq = buildRequest("/inspection/nodesByLanguage");
    Request request = rq.get().build();
    return performCall(
        request,
        (response, requestBody) -> {
          JsonArray responseData = JsonParser.parseString(requestBody).getAsJsonArray();
          Map<String, ClassifierResult> result = new HashMap<>();
          responseData.forEach(
              entry -> {
                JsonObject entryJO = entry.getAsJsonObject();

                String language = entryJO.get("language").getAsString();

                Set<String> ids = new HashSet<>();
                entryJO.get("ids").getAsJsonArray().forEach(el -> ids.add(el.getAsString()));
                int size = entryJO.get("size").getAsInt();
                ClassifierResult classifierResult = new ClassifierResult(ids, size);

                result.put(language, classifierResult);
              });
          return result;
        });
  }
}
