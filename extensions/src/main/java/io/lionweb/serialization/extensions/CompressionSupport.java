package io.lionweb.serialization.extensions;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

public class CompressionSupport {

  public static RequestBody considerCompression(RequestBody original, Compression compression)
      throws IOException {
    if (compression == Compression.ENABLED) {
      return forceContentLength(gzip(original));
    } else {
      return original;
    }
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
