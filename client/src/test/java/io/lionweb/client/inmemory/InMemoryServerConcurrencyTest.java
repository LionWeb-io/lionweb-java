package io.lionweb.client.inmemory;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.library.Book;
import io.lionweb.client.inmemory.library.Library;
import io.lionweb.client.inmemory.library.Writer;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.utils.ValidationResult;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class InMemoryServerConcurrencyTest {

  private static final String REPO = "ConcurrentRepo";

  private InMemoryServer createServerWithRepo() {
    InMemoryServer server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration(REPO, LionWebVersion.v2023_1, HistorySupport.DISABLED));
    return server;
  }

  /** Multiple threads create separate partitions concurrently — no data loss or corruption. */
  @Test
  public void concurrentPartitionCreation() throws InterruptedException {
    InMemoryServer server = createServerWithRepo();
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    int threadCount = 20;
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch go = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < threadCount; i++) {
      final int idx = i;
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  Library library = new Library("lib-" + idx, "Library " + idx);
                  SerializationChunk chunk =
                      serialization.serializeTreeToSerializationChunk(library);
                  server.createPartitionFromChunk(REPO, chunk.getClassifierInstances());
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    ready.await();
    go.countDown();
    done.await();

    assertTrue(errors.isEmpty(), "Unexpected errors during concurrent partition creation: " + errors);
    List<String> partitionIDs = server.listPartitionIDs(REPO);
    assertEquals(threadCount, partitionIDs.size(), "All partitions should have been created");

    ValidationResult consistency = server.checkConsistency();
    assertTrue(
        consistency.isSuccessful(),
        "Repository should be consistent after concurrent creation: " + consistency.getIssues());
  }

  /** Multiple threads store nodes into the same partition concurrently. */
  @Test
  public void concurrentStoreToSamePartition() throws InterruptedException {
    InMemoryServer server = createServerWithRepo();
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    // Create the root partition first
    Library library = new Library("lib-root", "Root Library");
    SerializationChunk initialChunk = serialization.serializeTreeToSerializationChunk(library);
    server.createPartitionFromChunk(REPO, initialChunk.getClassifierInstances());

    int threadCount = 20;
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch go = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < threadCount; i++) {
      final int idx = i;
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  // Each thread adds a book to the library and stores the whole tree
                  Library libCopy = new Library("lib-root", "Root Library");
                  Writer writer = new Writer("writer-" + idx, "Author " + idx);
                  Book book = new Book("book-" + idx, "Book " + idx, writer);
                  libCopy.addBook(book);
                  SerializationChunk chunk =
                      serialization.serializeTreeToSerializationChunk(libCopy);
                  server.store(REPO, chunk.getClassifierInstances());
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    ready.await();
    go.countDown();
    done.await();

    assertTrue(errors.isEmpty(), "Unexpected errors during concurrent stores: " + errors);
  }

  /** ID generation from multiple threads must never return duplicates. */
  @Test
  public void concurrentIdGenerationNoDuplicates() throws InterruptedException {
    InMemoryServer server = createServerWithRepo();

    int threadCount = 10;
    int idsPerThread = 100;
    List<String> allIds = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch go = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < threadCount; i++) {
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  List<String> ids = server.ids(REPO, idsPerThread);
                  allIds.addAll(ids);
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    ready.await();
    go.countDown();
    done.await();

    assertTrue(errors.isEmpty(), "Unexpected errors during concurrent ID generation: " + errors);
    assertEquals(threadCount * idsPerThread, allIds.size(), "Should have generated the expected number of IDs");

    Set<String> unique = new HashSet<>(allIds);
    assertEquals(allIds.size(), unique.size(), "All generated IDs should be unique");
  }

  /** Multiple threads create and delete repositories concurrently. */
  @Test
  public void concurrentRepositoryCreateAndDelete() throws InterruptedException {
    InMemoryServer server = new InMemoryServer();

    int threadCount = 20;
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch go = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger created = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      final int idx = i;
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  String repoName = "repo-" + idx;
                  server.createRepository(
                      new RepositoryConfiguration(
                          repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED));
                  created.incrementAndGet();
                  server.deleteRepository(repoName);
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    ready.await();
    go.countDown();
    done.await();

    assertTrue(errors.isEmpty(), "Unexpected errors during concurrent repo create/delete: " + errors);
    assertEquals(threadCount, created.get(), "All repositories should have been created");
    assertTrue(server.listRepositories().isEmpty(), "All repositories should have been deleted");
  }

  /** Concurrent reads and writes to the same repository don't cause exceptions. */
  @Test
  public void concurrentReadsAndWrites() throws InterruptedException {
    InMemoryServer server = createServerWithRepo();
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

    // Set up an initial partition
    Library library = new Library("lib-rw", "RW Library");
    Writer writer = new Writer("writer-rw", "RW Author");
    Book book = new Book("book-rw", "RW Book", writer);
    library.addBook(book);
    SerializationChunk initialChunk = serialization.serializeTreeToSerializationChunk(library);
    server.createPartitionFromChunk(REPO, initialChunk.getClassifierInstances());

    int writerCount = 5;
    int readerCount = 10;
    int totalThreads = writerCount + readerCount;
    CountDownLatch ready = new CountDownLatch(totalThreads);
    CountDownLatch go = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(totalThreads);
    List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

    for (int i = 0; i < writerCount; i++) {
      final int idx = i;
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  for (int j = 0; j < 10; j++) {
                    Library libCopy = new Library("lib-rw", "RW Library");
                    Writer w = new Writer("writer-rw-" + idx + "-" + j, "Author");
                    Book b = new Book("book-rw-" + idx + "-" + j, "Book " + j, w);
                    libCopy.addBook(b);
                    SerializationChunk chunk =
                        serialization.serializeTreeToSerializationChunk(libCopy);
                    server.store(REPO, chunk.getClassifierInstances());
                  }
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    for (int i = 0; i < readerCount; i++) {
      new Thread(
              () -> {
                ready.countDown();
                try {
                  go.await();
                  for (int j = 0; j < 20; j++) {
                    server.listPartitionIDs(REPO);
                    server.retrieve(REPO, Collections.singletonList("lib-rw"), 1);
                  }
                } catch (Throwable t) {
                  errors.add(t);
                } finally {
                  done.countDown();
                }
              })
          .start();
    }

    ready.await();
    go.countDown();
    done.await();

    assertTrue(errors.isEmpty(), "Unexpected errors during concurrent reads and writes: " + errors);
  }
}
