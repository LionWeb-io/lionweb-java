package io.lionweb.serialization.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class SerializedPropertyValueConcurrencyTest {
  @Test
  public void smallValuesAreCanonicalPerMetaPointer_concurrent() throws Exception {
    ConcurrencyScenario concurrencyScenario = new ConcurrencyScenario();

    final int callsPerThread = 20_000;

    final MetaPointer mp = MetaPointer.get("L", "1.0", "K");
    final String smallValue = "1"; // < THRESHOLD, should be cached
    final SerializedPropertyValue ref = SerializedPropertyValue.get(mp, smallValue);

    // track distinct identities we observed (must stay at 1)
    final Map<SerializedPropertyValue, Boolean> identities = new IdentityHashMap<>();
    final AtomicReference<Throwable> firstError = new AtomicReference<>(null);

    concurrencyScenario.run(
        () -> {
          try {
            concurrencyScenario.getStart().await();
            for (int i = 0; i < callsPerThread; i++) {
              SerializedPropertyValue spv = SerializedPropertyValue.get(mp, smallValue);
              // identity must match the canonical instance
              assertSame(ref, spv);
              synchronized (identities) {
                identities.put(spv, Boolean.TRUE);
              }
            }
          } catch (Throwable e) {
            firstError.compareAndSet(null, e);
          } finally {
            concurrencyScenario.getDone().countDown();
          }
        });

    if (firstError.get() != null) {
      throw new AssertionError("Worker failed", firstError.get());
    }

    // Exactly one unique identity must have been observed.
    assertEquals(1, identities.size());
    assertTrue(identities.containsKey(ref));
  }

  @Test
  public void largeValuesAreNotCanonical_evenConcurrently() throws Exception {

    final int callsPerThread = 5_000;

    final MetaPointer mp = MetaPointer.get("L", "1.0", "K");
    // Build a value > THRESHOLD (128) so it bypasses caching.
    final String largeValue = new String(new char[1024]).replace('\0', 'X');

    ConcurrencyScenario concurrencyScenario = new ConcurrencyScenario();
    final Map<SerializedPropertyValue, Boolean> identities = new IdentityHashMap<>();
    final AtomicReference<Throwable> firstError = new AtomicReference<>(null);

    concurrencyScenario.run(
        () -> {
          try {
            concurrencyScenario.getStart().await();
            SerializedPropertyValue prev = null;
            for (int i = 0; i < callsPerThread; i++) {
              SerializedPropertyValue spv = SerializedPropertyValue.get(mp, largeValue);
              // equals() must be true (same meta + same value),
              // but identity is NOT guaranteed (and should differ frequently).
              if (prev != null) {
                assertEquals(prev, spv);
              }
              prev = spv;
              synchronized (identities) {
                identities.put(spv, Boolean.TRUE);
              }
            }
          } catch (Throwable e) {
            firstError.compareAndSet(null, e);
          } finally {
            concurrencyScenario.getDone().countDown();
          }
        });

    if (firstError.get() != null) {
      throw new AssertionError("Worker failed", firstError.get());
    }

    // Since large values are not cached, we should observe many distinct instances.
    // We don’t assert an exact number (to avoid being brittle) but > threads is a safe lower bound.
    assertTrue(
        identities.size() > concurrencyScenario.getThreads(),
        "Expected multiple distinct instances for large values, got " + identities.size());
  }

  @Test
  public void sameSmallValueDifferentMetaPointer_isNotSameInstance() {
    final String smallValue = "true";

    MetaPointer mp1 = MetaPointer.get("L", "1.0", "K1");
    MetaPointer mp2 = MetaPointer.get("L", "1.0", "K2");

    SerializedPropertyValue v1 = SerializedPropertyValue.get(mp1, smallValue);
    SerializedPropertyValue v2 = SerializedPropertyValue.get(mp2, smallValue);

    // Different meta-pointers => distinct logical values; identity must not be the same
    assertNotSame(v1, v2);
    // And equals() must be false because metaPointer participates in equality.
    assertNotEquals(v1, v2);
  }
}
