package io.lionweb.serialization.extensions;

import java.io.IOException;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

public class CompressionSupport {

  public static @NotNull RequestBody considerCompression(
      @NotNull RequestBody original, @NotNull Compression compression) throws IOException {
    Objects.requireNonNull(original, "original cannot be null");
    Objects.requireNonNull(compression, "compression cannot be null");
    return compression == Compression.ENABLED ? forceContentLength(gzip(original)) : original;
  }

  private static RequestBody forceContentLength(final RequestBody requestBody) throws IOException {
    final Buffer buffer = new Buffer();
    requestBody.writeTo(buffer);
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return requestBody.contentType();
      }

      @Override
      public long contentLength() throws IOException {
        return buffer.size();
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        sink.write(buffer.snapshot());
      }
    };
  }

  private static RequestBody gzip(final RequestBody body) {
    return new RequestBody() {
      @Override
      public MediaType contentType() {
        return body.contentType();
      }

      @Override
      public long contentLength() {
        // We don't know the compressed length in advance.
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
}
