package io.lionweb.client.delta.messages;

import org.jetbrains.annotations.Nullable;

public class DeltaCommandResponse {
  public final boolean accepted;
  public final @Nullable String errorMessage;

  private DeltaCommandResponse(boolean accepted, @Nullable String errorMessage) {
    this.accepted = accepted;
    this.errorMessage = errorMessage;
  }

  public static DeltaCommandResponse accepted() {
    return new DeltaCommandResponse(true, null);
  }

  public static DeltaCommandResponse rejected(String errorMessage) {
    return new DeltaCommandResponse(false, errorMessage);
  }
}
