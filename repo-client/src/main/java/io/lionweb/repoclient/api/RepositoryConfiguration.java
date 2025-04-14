package io.lionweb.repoclient.api;

import io.lionweb.lioncore.java.LionWebVersion;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class RepositoryConfiguration {
  private @NotNull String name;
  private @NotNull LionWebVersion lionWebVersion;
  private @NotNull HistorySupport historySupport;

  public RepositoryConfiguration(
      @NotNull String name,
      @NotNull LionWebVersion lionWebVersion,
      @NotNull HistorySupport historySupport) {
    Objects.requireNonNull(name, "name should not be null");
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    Objects.requireNonNull(historySupport, "historySupport should not be null");
    this.name = name;
    this.lionWebVersion = lionWebVersion;
    this.historySupport = historySupport;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(@NotNull String name) {
    Objects.requireNonNull(name, "name should not be null");
    this.name = name;
  }

  @NotNull
  public LionWebVersion getLionWebVersion() {
    return lionWebVersion;
  }

  public void setLionWebVersion(@NotNull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    this.lionWebVersion = lionWebVersion;
  }

  public @NotNull HistorySupport getHistorySupport() {
    return historySupport;
  }

  public void setHistorySupport(@NotNull HistorySupport historySupport) {
    Objects.requireNonNull(historySupport, "historySupport should not be null");
    this.historySupport = historySupport;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (o == this) {
      return true;
    }
    RepositoryConfiguration that = (RepositoryConfiguration) o;
    return Objects.equals(historySupport, that.historySupport)
        && Objects.equals(name, that.name)
        && lionWebVersion == that.lionWebVersion;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, lionWebVersion, historySupport);
  }

  @Override
  public String toString() {
    return "RepositoryConfiguration{"
        + "name='"
        + name
        + '\''
        + ", lionWebVersion="
        + lionWebVersion
        + ", historySupport="
        + historySupport
        + '}';
  }
}
