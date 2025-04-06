package io.lionweb.repoclient;

public class RequestFailureException extends RuntimeException {
  private final String url;
  private final String uncompressedBody;
  private final int responseCode;
  private final String responseBody;

  public RequestFailureException(
      String url, String uncompressedBody, int responseCode, String responseBody) {
    super("Request to " + url + " failed with code " + responseCode + ": " + responseBody);
    this.url = url;
    this.uncompressedBody = uncompressedBody;
    this.responseCode = responseCode;
    this.responseBody = responseBody;
  }

  public String getUrl() {
    return url;
  }

  public String getUncompressedBody() {
    return uncompressedBody;
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
        + ", uncompressedBody='"
        + uncompressedBody
        + '\''
        + ", responseCode="
        + responseCode
        + ", responseBody='"
        + responseBody
        + '\''
        + '}';
  }
}
