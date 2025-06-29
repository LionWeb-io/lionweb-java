package io.lionweb.client;

public class RequestFailureException extends RuntimeException {
  private final String url;
  private final int responseCode;
  private final String responseBody;

  public RequestFailureException(String url, int responseCode, String responseBody) {
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
