package io.lionweb.client.inmemory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

class ParticipationManager {
  private int nextParticipationId = 1;

  /** Participations that are currently active (signed on, not yet signed off). */
  private final Set<String> activeParticipations = Collections.synchronizedSet(new HashSet<>());

  boolean isActiveParticipation(@NotNull String participationId) {
    Objects.requireNonNull(participationId, "participationId must not be null");
    return activeParticipations.contains(participationId);
  }

  @NotNull
  String createParticipationId() {
    String participationId = "participation-" + nextParticipationId++;
    activeParticipations.add(participationId);
    return participationId;
  }

  public void drop(@NotNull String currentParticipationId) {
    activeParticipations.remove(currentParticipationId);
  }
}
