package io.lionweb.client;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This exception is thrown to signal that a request has failed. It includes relevant details about
 * the failed request, such as the URL, response code, and the response body to provide context for
 * the failure.
 */
public class BulkRequestFailureException extends RuntimeException {
  private final @NotNull String url;
  private final int responseCode;
  private final @Nullable String responseBody;

  public BulkRequestFailureException(
      @NotNull String url, int responseCode, @Nullable String responseBody) {
    super("Request to " + url + " failed with code " + responseCode + ": " + responseBody);
    Objects.requireNonNull(url, "url must not be null");
    this.url = url;
    this.responseCode = responseCode;
    this.responseBody = responseBody;
  }

  public @NotNull String getUrl() {
    return url;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public @Nullable String getResponseBody() {
    return responseBody;
  }

  @Override
  public String toString() {
    return "RequestFailureException{"
        + "url='"
        + url
        + '\''
        + ", responseCode="
        + responseCode
        + ", responseBody='"
        + responseBody
        + '\''
        + '}';
  }
}
