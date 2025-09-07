package io.lionweb.client.impl;

import static io.lionweb.serialization.extensions.CompressionSupport.considerCompression;

import com.google.gson.*;
import io.lionweb.client.RequestFailureException;
import io.lionweb.protobuf.PBBulkImport;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.extensions.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import okhttp3.*;

public class ClientForAdditionalAPIs extends LionWebClientImplHelper
    implements AdditionalAPIClient {

  private static final MediaType PROTOBUF = MediaType.get("application/protobuf");
  private static final MediaType FLATBUFFERS = MediaType.get("application/x-flatbuffers");

  public ClientForAdditionalAPIs(ClientConfiguration clientConfiguration) {
    super(clientConfiguration);
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
  public List<NodeInfo> getNodeTree(
      List<String> nodeIDs, @javax.annotation.Nullable Integer depthLimit) throws IOException {
    if (nodeIDs.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, String> params = new HashMap<>();
    if (depthLimit != null) {
      params.put("depthLimit", depthLimit.toString());
    }
    Request.Builder rq = buildRequest("/additional/getNodeTree", true, true, true, params);
    JsonObject bodyJO = new JsonObject();
    JsonArray ids = new JsonArray();
    nodeIDs.forEach(ids::add);
    bodyJO.add("ids", ids);
    String bodyJson = gson.toJson(bodyJO);
    RequestBody requestBody = RequestBody.create(bodyJson, JSON);
    Request request = rq.post(requestBody).build();
    return performCall(
        request,
        (response, responseBody) -> {
          JsonObject responseData = JsonParser.parseString(responseBody).getAsJsonObject();
          boolean success = responseData.get("success").getAsBoolean();
          if (!success) {
            throw new RequestFailureException(
                request.url().toString(), response.code(), responseBody);
          }
          JsonArray data = responseData.get("data").getAsJsonArray();
          return data.asList().stream()
              .map(
                  entry -> {
                    JsonObject entryJO = entry.getAsJsonObject();
                    String id = entryJO.get("id").getAsString();
                    JsonElement parentValue = entryJO.get("parent");
                    String parent =
                        parentValue.isJsonNull() ? null : entryJO.get("parent").getAsString();
                    int depth = entryJO.get("depth").getAsInt();
                    return new NodeInfo(id, parent, depth);
                  })
              .collect(Collectors.toList());
        });
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
    JsonElement serializedChunkAsJson =
        new LowLevelJsonSerialization()
            .serializeToJsonElement(
                LowLevelJsonSerialization.groupNodesIntoSerializationBlock(
                    bulkImport.getNodes(), conf.getJsonSerialization().getLionWebVersion()));
    JsonArray bodyNodes = serializedChunkAsJson.getAsJsonObject().get("nodes").getAsJsonArray();
    body.add("attachPoints", bodyAttachPoints);
    body.add("nodes", bodyNodes);
    String bodyJson = new Gson().toJson(body);

    RequestBody requestBody = RequestBody.create(JSON, bodyJson);
    requestBody = considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImportUsingProtobuf(BulkImport bulkImport, Compression compression)
      throws IOException {
    JsonSerialization jsonSerialization = conf.getJsonSerialization();

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
    requestBody = considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImportUsingFlatbuffers(BulkImport bulkImport, Compression compression)
      throws IOException {
    JsonSerialization jsonSerialization = conf.getJsonSerialization();

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
    requestBody = considerCompression(requestBody, compression);
    bulkImport(requestBody, compression);
  }

  private void bulkImport(RequestBody requestBody, Compression compression) throws IOException {
    Request.Builder rq = buildRequest("/additional/bulkImport");
    if (compression == Compression.ENABLED) {
      rq = rq.addHeader("Content-Encoding", "gzip");
    }
    rq.post(requestBody);
    Request request = rq.build();

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
}
