package io.lionweb.client.delta.messages.events;

import io.lionweb.client.delta.messages.BaseDeltaEvent;

public class ErrorEvent extends BaseDeltaEvent {
  public String errorCode;
  public String message;

  public ErrorEvent(int sequenceNumber, String errorCode, String message) {
    super(sequenceNumber);
    this.errorCode = errorCode;
    this.message = message;
  }
}
