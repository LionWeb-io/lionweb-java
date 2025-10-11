package io.lionweb.serialization.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class MetaPointerConcurrencyTest {

  @Test
  public void sameTripleConcurrentlyReturnsSameInstance() throws Exception {
    final int threads = Runtime.getRuntime().availableProcessors() * 4;
    final int callsPerThread = 10_000;

    final String lang = "L";
    final String ver = "1.0";
    final String key = "K";

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CyclicBarrier start = new CyclicBarrier(threads);
    CountDownLatch done = new CountDownLatch(threads);

    final MetaPointer ref = MetaPointer.get(lang, ver, key);

    final Map<MetaPointer, Boolean> identities = new IdentityHashMap<>();

    final AtomicReference<Throwable> firstError = new AtomicReference<>(null);

    for (int t = 0; t < threads; t++) {
      pool.execute(
          () -> {
            try {
              start.await();
              for (int i = 0; i < callsPerThread; i++) {
                MetaPointer mp = MetaPointer.get(lang, ver, key);
                assertSame("Must return the same canonical instance", ref, mp);
                synchronized (identities) {
                  identities.put(mp, Boolean.TRUE);
                }
              }
            } catch (Throwable e) {
              firstError.compareAndSet(null, e);
            } finally {
              done.countDown();
            }
          });
    }

    done.await(60, TimeUnit.SECONDS);
    pool.shutdownNow();

    // if any worker failed, surface it now (keeps stacktrace)
    if (firstError.get() != null) {
      throw new AssertionError(
          "Worker failed", firstError.get()); // <— replaces MetaPointerProxy logic
    }

    // Identity set must contain exactly one MetaPointer
    assertEquals("There must be exactly one canonical MetaPointer", 1, identities.size());
    assertTrue(identities.containsKey(ref));
  }

  @Test
  public void differentTriplesYieldDifferentInstancesButStablePerTriple() throws Exception {
    final int threads = Runtime.getRuntime().availableProcessors() * 4;
    final List<String[]> triples = new ArrayList<>();
    // include nulls to exercise NULL-sentinel path
    triples.add(new String[] {"L", "1.0", "K"});
    triples.add(new String[] {"L", "1.0", "K2"});
    triples.add(new String[] {"L", null, "K"});
    triples.add(new String[] {null, "1.0", "K"});
    triples.add(new String[] {null, null, "K"});
    triples.add(new String[] {"L2", "1.0", "K"});
    triples.add(new String[] {"L", "2.0", "K"});

    // warm up & capture references
    List<MetaPointer> refs = new ArrayList<>();
    for (String[] t : triples) {
      refs.add(MetaPointer.get(t[0], t[1], t[2]));
    }

    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CyclicBarrier start = new CyclicBarrier(threads);
    CountDownLatch done = new CountDownLatch(threads);

    // per-triple identity sets
    final List<Map<MetaPointer, Boolean>> identitySets = new ArrayList<>();
    for (int i = 0; i < triples.size(); i++) {
      identitySets.add(new IdentityHashMap<>());
    }

    for (int t = 0; t < threads; t++) {
      final int idx = t % triples.size();
      pool.execute(
          () -> {
            try {
              start.await();
              String[] triple = triples.get(idx);
              MetaPointer expected = refs.get(idx);
              for (int i = 0; i < 10_000; i++) {
                MetaPointer mp = MetaPointer.get(triple[0], triple[1], triple[2]);
                assertSame("Canonicalization must hold per triple", expected, mp);
                synchronized (identitySets.get(idx)) {
                  identitySets.get(idx).put(mp, Boolean.TRUE);
                }
              }
            } catch (Exception e) {
              fail(e.getMessage());
            } finally {
              done.countDown();
            }
          });
    }

    done.await(60, TimeUnit.SECONDS);
    pool.shutdownNow();

    // each triple should have exactly one canonical instance
    for (int i = 0; i < triples.size(); i++) {
      assertEquals(
          "Triple " + i + " must have exactly one instance", 1, identitySets.get(i).size());
      assertTrue(identitySets.get(i).containsKey(refs.get(i)));
    }

    // and different triples must be different identities
    for (int i = 0; i < refs.size(); i++) {
      for (int j = i + 1; j < refs.size(); j++) {
        assertNotSame(
            "Different triples must map to different instances", refs.get(i), refs.get(j));
      }
    }
  }
}
