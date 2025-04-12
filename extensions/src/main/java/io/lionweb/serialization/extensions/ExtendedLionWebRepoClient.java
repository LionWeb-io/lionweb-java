package io.lionweb.serialization.extensions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xalan.internal.lib.NodeInfo;
import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.protobuf.PBBulkImport;
import io.lionweb.repoclient.LionWebRepoClient;
import io.lionweb.repoclient.RequestFailureException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExtendedLionWebRepoClient extends LionWebRepoClient implements AdditionalAPIClient {

  private static final MediaType PROTOBUF = MediaType.get("application/protobuf");
  private static final MediaType FLATBUFFERS = MediaType.get("application/flatbuffers");

  public class Builder extends LionWebRepoClient.Builder {
    @Override
    public ExtendedLionWebRepoClient build() {
      return new ExtendedLionWebRepoClient(
          lionWebVersion,
          hostname,
          port,
          authorizationToken,
          clientID,
          repository,
          connectTimeoutInSeconds,
          callTimeoutInSeconds);
    }
  }

  public ExtendedLionWebRepoClient(
      LionWebVersion lionWebVersion, String hostname, int port, String repository) {
    super(lionWebVersion, hostname, port, repository);
  }

  public ExtendedLionWebRepoClient(
      LionWebVersion lionWebVersion,
      String hostname,
      int port,
      String authorizationToken,
      String clientID,
      String repository,
      long connectTimeoutInSeconds,
      long callTimeoutInSeconds) {
    super(
        lionWebVersion,
        hostname,
        port,
        authorizationToken,
        clientID,
        repository,
        connectTimeoutInSeconds,
        callTimeoutInSeconds);
  }

  @Override
  public void bulkImport(
      @Nonnull BulkImport bulkImport, TransferFormat transferFormat, Compression compression)
      throws IOException {
    if (bulkImport.isEmpty()) {
      return;
    }
    switch (transferFormat) {
      case JSON:
        bulkImportUsingJson(bulkImport, compression);
        return;
      case PROTOBUF:
        bulkImportUsingProtobuf(bulkImport, compression);
        return;
      case FLATBUFFERS:
        bulkImportUsingFlatbuffers(bulkImport, compression);
        return;
      default:
        throw new UnsupportedOperationException();
    }
  }

  @Override
  public List<NodeInfo> getNodeTree(List<String> nodeIDs, @Nullable Integer depthLimit)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  private void bulkImportUsingJson(BulkImport bulkImport, Compression compression)
      throws IOException {
    JsonObject body = new JsonObject();
    JsonArray bodyAttachPoints = new JsonArray();
    bulkImport.getAttachPoints().stream()
        .forEach(
            attachPoint -> {
              JsonObject jContainment = new JsonObject();
              jContainment.addProperty("language", attachPoint.containment.getLanguage());
              jContainment.addProperty("version", attachPoint.containment.getVersion());
              jContainment.addProperty("key", attachPoint.containment.getKey());

              JsonObject jEl = new JsonObject();
              jEl.addProperty("container", attachPoint.container);
              jEl.addProperty("root", attachPoint.rootId);
              jEl.add("containment", jContainment);
              bodyAttachPoints.add(jEl);
            });
    JsonArray bodyNodes =
        getJsonSerialization()
            .serializeTreesToJsonElement(
                bulkImport.getNodes().toArray(new ClassifierInstance<?>[0]))
            .getAsJsonObject()
            .get("nodes")
            .getAsJsonArray();
    body.add("attachPoints", bodyAttachPoints);
    body.add("nodes", bodyNodes);
    String bodyJson = new Gson().toJson(body);

    RequestBody requestBody = RequestBody.create(JSON, bodyJson);
    requestBody = CompressionSupport.considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImportUsingProtobuf(BulkImport bulkImport, Compression compression)
      throws IOException {
    ExtraProtoBufSerialization pbSerialization = new ExtraProtoBufSerialization();
    pbSerialization.setUnavailableChildrenPolicy(jsonSerialization.getUnavailableChildrenPolicy());
    pbSerialization.setUnavailableParentPolicy(jsonSerialization.getUnavailableParentPolicy());
    pbSerialization.setUnavailableReferenceTargetPolicy(
        jsonSerialization.getUnavailableReferenceTargetPolicy());
    pbSerialization.setClassifierResolver(jsonSerialization.getClassifierResolver());
    pbSerialization.setInstanceResolver(jsonSerialization.getInstanceResolver());
    pbSerialization.setInstantiator(jsonSerialization.getInstantiator());
    pbSerialization.setPrimitiveValuesSerialization(
        jsonSerialization.getPrimitiveValuesSerialization());
    PBBulkImport pbBulkImport = pbSerialization.serializeBulkImport(bulkImport);
    byte[] bytes = pbBulkImport.toByteArray();
    RequestBody requestBody = RequestBody.create(PROTOBUF, bytes);
    requestBody = CompressionSupport.considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImportUsingFlatbuffers(BulkImport bulkImport, Compression compression)
      throws IOException {
    ExtraFlatBuffersSerialization fbSerialization = new ExtraFlatBuffersSerialization();
    fbSerialization.setUnavailableChildrenPolicy(jsonSerialization.getUnavailableChildrenPolicy());
    fbSerialization.setUnavailableParentPolicy(jsonSerialization.getUnavailableParentPolicy());
    fbSerialization.setUnavailableReferenceTargetPolicy(
        jsonSerialization.getUnavailableReferenceTargetPolicy());
    fbSerialization.setClassifierResolver(jsonSerialization.getClassifierResolver());
    fbSerialization.setInstanceResolver(jsonSerialization.getInstanceResolver());
    fbSerialization.setInstantiator(jsonSerialization.getInstantiator());
    fbSerialization.setPrimitiveValuesSerialization(
        jsonSerialization.getPrimitiveValuesSerialization());
    byte[] bytes = fbSerialization.serializeBulkImport(bulkImport);
    RequestBody requestBody = RequestBody.create(FLATBUFFERS, bytes);
    requestBody = CompressionSupport.considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImport(RequestBody requestBody, Compression compression) throws IOException {
    String url = protocol.value + "://" + hostname + ":" + port + "/additional/bulkImport";
    Request.Builder rq =
        new Request.Builder().url(addRepositoryQueryParam(addClientIdQueryParam(url)));
    rq = considerAuthenticationToken(rq);
    if (compression == Compression.ENABLED) {
      rq = rq.addHeader("Content-Encoding", "gzip");
    }
    rq.post(requestBody);
    Request request = rq.build();

    // Execute the HTTP call and use try-with-resources to ensure the response is closed.
    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body() != null ? response.body().string() : "";
      if (response.code() != HttpURLConnection.HTTP_OK) {
        throw new RequestFailureException(url, response.code(), responseBody);
      }

      JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
      boolean success = responseData.get("success").getAsBoolean();
      if (!success) {
        throw new RequestFailureException(url, response.code(), responseBody);
      }
    }
  }
}
