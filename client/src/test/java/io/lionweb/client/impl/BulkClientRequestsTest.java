package io.lionweb.client.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import io.lionweb.LionWebVersion;
import io.lionweb.client.BulkRequestFailureException;
import io.lionweb.client.LionWebBulkClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Checks the requests sent by the bulk client and how it handles the responses. */
public class BulkClientRequestsTest {

  private static final String EMPTY_CHUNK_RESPONSE =
      "{\"success\":true,\"messages\":[],\"chunk\":{\"serializationFormatVersion\":\"2024.1\","
          + "\"languages\":[],\"nodes\":[]}}";
  private static final String FAILURE_RESPONSE =
      "{\"success\":false,\"messages\":[{\"kind\":\"Error\",\"message\":\"boom\"}]}";

  private HttpServer server;
  private volatile String lastQuery;
  private volatile String responseBody;
  private LionWebBulkClient client;

  @BeforeEach
  public void setUp() throws IOException {
    server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        "/",
        exchange -> {
          lastQuery = exchange.getRequestURI().getQuery();
          try (InputStream is = exchange.getRequestBody()) {
            is.readAllBytes();
          }
          byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });
    server.start();
    client =
        new LionWebBulkClient(
            LionWebVersion.v2024_1, "localhost", server.getAddress().getPort(), "MyRepo");
  }

  @AfterEach
  public void tearDown() {
    server.stop(0);
  }

  @Test
  public void retrieveWithInfiniteDepthOmitsDepthLimit() throws IOException {
    responseBody = EMPTY_CHUNK_RESPONSE;

    client.retrieve(List.of("n1"));
    assertFalse(lastQuery.contains("depthLimit"), lastQuery);

    client.rawRetrieve(List.of("n1"));
    assertFalse(lastQuery.contains("depthLimit"), lastQuery);

    client.retrieveAsChunk(List.of("n1"));
    assertFalse(lastQuery.contains("depthLimit"), lastQuery);
  }

  @Test
  public void retrieveWithFiniteDepthSendsDepthLimit() throws IOException {
    responseBody = EMPTY_CHUNK_RESPONSE;

    client.retrieve(List.of("n1"), 3);
    assertTrue(lastQuery.contains("depthLimit=3"), lastQuery);

    client.rawRetrieve(List.of("n1"), 0);
    assertTrue(lastQuery.contains("depthLimit=0"), lastQuery);
  }

  @Test
  public void createPartitionsChecksSuccess() {
    responseBody = FAILURE_RESPONSE;
    assertThrows(
        BulkRequestFailureException.class,
        () ->
            client.rawCreatePartitions(
                "{\"serializationFormatVersion\":\"2024.1\",\"languages\":[],\"nodes\":[]}"));
  }

  @Test
  public void deletePartitionsChecksSuccess() {
    responseBody = FAILURE_RESPONSE;
    assertThrows(BulkRequestFailureException.class, () -> client.deletePartitions(List.of("p1")));
  }

  @Test
  public void deletePartitionsSucceeds() throws IOException {
    responseBody =
        "{\"success\":true,\"messages\":[{\"kind\":\"RepoVersion\",\"message\":\"\","
            + "\"data\":{\"version\":7}}]}";
    assertEquals("7", client.deletePartitions(List.of("p1")).getToken());
  }
}
