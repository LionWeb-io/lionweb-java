package io.lionweb.client;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class CompressionSupport {

  public static final MediaType JSON = MediaType.get("application/json");
  public static final MediaType PROTOBUF = MediaType.get("application/protobuf");
  public static final MediaType FLATBUFFERS = MediaType.get("application/flatbuffers");

  /** Converts a String to a compressed RequestBody using JSON MediaType. */
  public static RequestBody compress(String content) {
    return compress(RequestBody.create(content, JSON));
  }

  /** Compresses a given RequestBody using GZIP and ensures the content length is known. */
  public static RequestBody compress(RequestBody body) {
    return forceContentLength(gzip(body));
  }

  private static RequestBody gzip(final RequestBody body) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return body.contentType();
      }

      @Override
      public long contentLength() {
        // Length not known beforehand due to GZIP compression
        return -1;
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
        body.writeTo(gzipSink);
        gzipSink.close();
      }
    };
  }

  private static RequestBody forceContentLength(final RequestBody requestBody) {
    try {
      final Buffer buffer = new Buffer();
      requestBody.writeTo(buffer);

      return new RequestBody() {
        @Override
        public MediaType contentType() {
          return requestBody.contentType();
        }

        @Override
        public long contentLength() {
          return buffer.size();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
          sink.write(buffer.snapshot());
        }
      };
    } catch (IOException e) {
      throw new RuntimeException("Failed to determine content length", e);
    }
  }
}
