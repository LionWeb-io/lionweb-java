package io.lionweb.client.api;

import java.util.Objects;

/** Value distinguishing a version of a particular repository. */
public class RepositoryVersionToken {
  private final String token;

  public RepositoryVersionToken(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RepositoryVersionToken)) return false;
    RepositoryVersionToken that = (RepositoryVersionToken) o;
    return Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(token);
  }
}
