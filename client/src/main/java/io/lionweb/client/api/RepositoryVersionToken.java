package io.lionweb.client.api;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/** Value distinguishing a version of a particular repository. */
public class RepositoryVersionToken {
  private final @NotNull String token;

  public RepositoryVersionToken(@NotNull String token) {
    Objects.requireNonNull(token, "token must not be null");
    this.token = token;
  }

  public @NotNull String getToken() {
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
