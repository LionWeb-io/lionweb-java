package io.lionweb.serialization.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class ConcurrencyScenario {
  public int getThreads() {
    return threads;
  }

  private final int threads = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
  private ExecutorService pool = Executors.newFixedThreadPool(threads);
  private CyclicBarrier start = new CyclicBarrier(threads);
  private CountDownLatch done = new CountDownLatch(threads);

  public ConcurrencyScenario() {}

  public CyclicBarrier getStart() {
    return start;
  }

  public CountDownLatch getDone() {
    return done;
  }

  public void run(Runnable command) throws InterruptedException {
    for (int t = 0; t < threads; t++) {
      pool.execute(command);
    }
    assertTrue(done.await(60, TimeUnit.SECONDS), "Workers did not finish in time");
    pool.shutdownNow();
  }

  public void run(Consumer<Integer> command) throws InterruptedException {
    for (int t = 0; t < threads; t++) {
      int index = t;
      pool.execute(() -> command.accept(index));
    }
    assertTrue(done.await(60, TimeUnit.SECONDS), "Workers did not finish in time");
    pool.shutdownNow();
  }
}
