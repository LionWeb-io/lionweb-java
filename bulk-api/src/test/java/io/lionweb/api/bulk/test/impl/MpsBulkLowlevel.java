package io.lionweb.api.bulk.test.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.lionweb.api.bulk.lowlevel.*;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MpsBulkLowlevel implements IBulkLowlevel {
    private static final String URI_BASE = "http://127.0.0.1:63320/lionweb/api/bulk/";

    @Override
    public IPartitionsResponse partitions() {
        HttpRequest request = buildRequest().uri(URI.create(URI_BASE + "partitions?onlyPartitions=true")).GET().build();
        HttpResponse<InputStream> response = send(request);
        if (HttpURLConnection.HTTP_OK != response.statusCode()) {
            return new IPartitionsResponse() {
                @Override
                public SerializedChunk getResult() {
                    return null;
                }

                @Override
                public boolean isOk() {
                    return false;
                }

                @Override
                public String getErrorMessage() {
                    return Integer.toString(response.statusCode());
                }
            };
        }

        SerializedChunk chunk = parseChunk(response);

        return new IPartitionsResponse() {
            @Override
            public SerializedChunk getResult() {
                return chunk;
            }

            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public String getErrorMessage() {
                return "";
            }
        };
    }

    @Override
    public IRetrieveResponse retrieve(List<String> nodeIds, String depthLimit) {
        String ids = nodeIds.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",", "[", "]"));
        String limit = depthLimit != null ? "?depthLimit=" + depthLimit : "";

        HttpRequest request = buildRequest().uri(URI.create(URI_BASE + "retrieve" + limit)).POST(HttpRequest.BodyPublishers.ofString(ids)).build();
        HttpResponse<InputStream> response = send(request);
        if (HttpURLConnection.HTTP_OK != response.statusCode()) {
            return new IRetrieveResponse() {
                @Override
                public SerializedChunk getResult() {
                    return null;
                }

                @Override
                public boolean isValidNodeIds() {
                    return true;
                }

                @Override
                public boolean isValidDepthLimit() {
                    return true;
                }

                @Override
                public List<String> getUnknownNodeIds() {
                    return Collections.emptyList();
                }

                @Override
                public boolean isOk() {
                    return false;
                }

                @Override
                public String getErrorMessage() {
                    return Integer.toString(response.statusCode());
                }
            };
        }


        SerializedChunk chunk = parseChunk(response);

        return new IRetrieveResponse() {
            @Override
            public SerializedChunk getResult() {
                return chunk;
            }

            @Override
            public boolean isValidNodeIds() {
                return true;
            }

            @Override
            public boolean isValidDepthLimit() {
                return true;
            }

            @Override
            public List<String> getUnknownNodeIds() {
                return Collections.emptyList();
            }

            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public String getErrorMessage() {
                return "";
            }
        };
    }

    @Override
    public IStoreResponse store(SerializedChunk nodes, String mode) {
        String jsonString = new LowLevelJsonSerialization().serializeToJsonString(nodes);

        HttpRequest request = buildRequest().uri(URI.create(URI_BASE + "store")).POST(HttpRequest.BodyPublishers.ofString(jsonString)).build();
        HttpResponse<InputStream> response = send(request);
        if (HttpURLConnection.HTTP_OK != response.statusCode()) {
            return new IStoreResponse() {
                @Override
                public boolean isValidNodes() {
                    return true;
                }

                @Override
                public boolean isValidMode() {
                    return true;
                }

                @Override
                public boolean isOk() {
                    return false;
                }

                @Override
                public String getErrorMessage() {
                    return Integer.toString(response.statusCode());
                }
            };
        }

        return new IStoreResponse() {
            @Override
            public boolean isValidNodes() {
                return true;
            }

            @Override
            public boolean isValidMode() {
                return true;
            }

            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public String getErrorMessage() {
                return "";
            }
        };
    }

    @Override
    public IDeleteResponse delete(List<String> nodeIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIdsResponse ids(String count) {
        throw new UnsupportedOperationException();
    }

    private HttpClient createClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    private static HttpRequest.Builder buildRequest() {
        return HttpRequest.newBuilder().setHeader("Content-Type", "application/json");
    }

    private HttpResponse<InputStream> send(HttpRequest request) {
        try {
            HttpClient client = createClient();
            return client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SerializedChunk parseChunk(HttpResponse<InputStream> response) {
        InputStreamReader reader = new InputStreamReader(response.body());
        JsonReader json = new JsonReader(reader);
        json.setLenient(true);
        JsonElement jsonElement = JsonParser.parseReader(json);
        LowLevelJsonSerialization serialization = new LowLevelJsonSerialization();
        SerializedChunk chunk = serialization.deserializeSerializationBlock(jsonElement);
        return chunk;
    }
}
