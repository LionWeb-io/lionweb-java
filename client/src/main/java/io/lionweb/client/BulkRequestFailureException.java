package io.lionweb.client;

/**
 * This exception is thrown to signal that a request has failed. It includes relevant details about
 * the failed request, such as the URL, response code, and the response body to provide context for
 * the failure.
 */
public class BulkRequestFailureException extends RuntimeException {
  private final String url;
  private final int responseCode;
  private final String responseBody;

  public BulkRequestFailureException(String url, int responseCode, String responseBody) {
    super("Request to " + url + " failed with code " + responseCode + ": " + responseBody);
    this.url = url;
    this.responseCode = responseCode;
    this.responseBody = responseBody;
  }

  public String getUrl() {
    return url;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getResponseBody() {
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
