package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Language;
import io.lionweb.model.Node;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an LionWeb Archive.
 *
 * <p>The LionWeb Archive is a container file used to package a set or related LionWeb partitions
 * (e.g., the content of a LionWeb Repository) in a single archive. It is technically a ZIP file
 * that can have any extension, although the standard extension is <code>.lwa</code> (for
 * <em>LionWeb Archive</em>).
 *
 * <p>The archive follows a predefined directory structure containing the following directories:
 *
 * <ul>
 *   <li><b>partitions/</b> — Contains a collection of LionWeb partitions.
 *   <li><b>languages/</b> — Contains the language definitions required to load and interpret the
 *       partitions.
 *   <li><b>metadata/</b> — Contains metadata files describing the archive. This directory always
 *       includes a file named <code>metadata.properties</code>, which defines the archive’s
 *       properties.
 * </ul>
 *
 * <p>One of the properties defined in <code>metadata.properties</code> is <code>LionWeb-Version
 * </code>, which indicates the version of the LionWeb format used.
 *
 * <p>All files contained in the LionWeb Archive are serialized using the <b>Protocol Buffers
 * (Protobuf)</b> format. The paths and names used by the format are exposed as constants (see
 * {@link #STANDARD_EXTENSION}, {@link #METADATA_PATH}, {@link #LANGUAGES_DIRECTORY}, {@link
 * #PARTITIONS_DIRECTORY} and {@link #CHUNK_EXTENSION}).
 *
 * <p>Chunks are stored in the compact form produced by {@link
 * SerializationProvider#getEfficientProtoBufSerialization(LionWebVersion)}: empty containments and
 * empty references are not stored. Therefore, chunks loaded from an archive do not contain them and
 * may not be {@code equals} to the chunks originally stored, although they represent the same
 * nodes.
 */
public class LionWebArchive {

  /** Key of the property, in the metadata file, indicating the LionWeb version used. */
  public static final String LW_VERSION_KEY = "LionWeb-Version";

  /** The standard extension of LionWeb Archive files (without the leading dot). */
  public static final String STANDARD_EXTENSION = "lwa";

  /** Path, within the archive, of the metadata file. */
  public static final String METADATA_PATH = "metadata/metadata.properties";

  /** Directory, within the archive, containing the language chunks (including trailing slash). */
  public static final String LANGUAGES_DIRECTORY = "languages/";

  /** Directory, within the archive, containing the partition chunks (including trailing slash). */
  public static final String PARTITIONS_DIRECTORY = "partitions/";

  /** Extension of the entries containing chunks (including the leading dot). */
  public static final String CHUNK_EXTENSION = ".binpb";

  /**
   * Compression level used when none is specified. It corresponds to no compression, favoring
   * speed.
   */
  public static final int DEFAULT_COMPRESSION_LEVEL = 0;

  private static final int FOUR_MIB = 4 << 20;

  private LionWebArchive() {}

  /**
   * The Loader interface defines methods for loading and processing language and partition
   * serialization chunks, as well as setting the LionWeb version. It acts as a bridge between
   * deserialization and data handling for LionWeb archives.
   *
   * <p>All methods are invoked on the thread calling {@link #load(File, Loader)}, in the order in
   * which the chunks appear in the archive, even if chunks are deserialized in parallel.
   */
  public interface Loader {
    void setLwVersion(@Nonnull LionWebVersion lionWebVersion);

    void addLanguageChunk(@Nonnull SerializationChunk chunk);

    void languagesLoaded();

    void addPartitionChunk(@Nonnull SerializationChunk chunk);

    void partitionsLoaded();
  }

  /**
   * Loads a LionWeb archive from the specified file. This method extracts metadata, language
   * chunks, and partition chunks from the archive and processes them using the provided consumer
   * functions.
   *
   * <p>The archive is expected to contain a metadata file ({@link #METADATA_PATH}), language data
   * under {@link #LANGUAGES_DIRECTORY}, and partition data under {@link #PARTITIONS_DIRECTORY}.
   *
   * <p>The archive is opened once, and the entries are accessed through its central directory.
   * Chunks are deserialized in parallel, but they are passed to the loader sequentially, in the
   * order in which they appear in the archive.
   *
   * @param file the LionWeb archive file to be loaded. Must not be null, a directory, or
   *     non-existent.
   * @param loader the consumer function to be called for each language and partition chunk.
   * @throws IOException if an error occurs reading the file or processing its contents.
   * @throws IllegalArgumentException if the file does not exist, is a directory, or the archive
   *     does not meet the required structural assumptions.
   * @throws RuntimeException if an error occurs during chunk deserialization.
   */
  public static void load(@Nonnull File file, @Nonnull Loader loader) throws IOException {
    Objects.requireNonNull(file, "file should not be null");
    Objects.requireNonNull(loader, "loader should not be null");
    if (!file.exists()) {
      throw new IllegalArgumentException("The given file does not exist");
    }
    if (file.isDirectory()) {
      throw new IllegalArgumentException("The given file is a directory");
    }

    try (ZipFile zipFile = new ZipFile(file)) {
      ZipEntry metadataEntry = null;
      List<ZipEntry> languageEntries = new ArrayList<>();
      List<ZipEntry> partitionEntries = new ArrayList<>();
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          continue;
        }
        String name = entry.getName();
        if (metadataEntry == null && name.equalsIgnoreCase(METADATA_PATH)) {
          metadataEntry = entry;
        } else if (name.startsWith(LANGUAGES_DIRECTORY)) {
          languageEntries.add(entry);
        } else if (name.startsWith(PARTITIONS_DIRECTORY)) {
          partitionEntries.add(entry);
        }
      }

      if (metadataEntry == null) {
        throw new IllegalArgumentException("No " + METADATA_PATH + " file found in archive");
      }
      Properties metadata = new Properties();
      try (InputStream in = zipFile.getInputStream(metadataEntry)) {
        metadata.load(in);
      }
      if (!metadata.containsKey(LW_VERSION_KEY)) {
        throw new IllegalArgumentException(
            "No " + LW_VERSION_KEY + " property found in metadata.properties");
      }
      LionWebVersion lionWebVersion =
          LionWebVersion.fromValue(metadata.getProperty(LW_VERSION_KEY));
      loader.setLwVersion(lionWebVersion);

      int totalChunks = languageEntries.size() + partitionEntries.size();
      try (OrderedPipeline<SerializationChunk> pipeline =
          new OrderedPipeline<>(Math.min(defaultParallelism(), Math.max(1, totalChunks)))) {
        ThreadLocal<ProtoBufSerialization> serialization =
            ThreadLocal.withInitial(
                () -> SerializationProvider.getEfficientProtoBufSerialization(lionWebVersion));

        deserializeEntries(
            zipFile, languageEntries, pipeline, serialization, loader::addLanguageChunk);
        loader.languagesLoaded();

        deserializeEntries(
            zipFile, partitionEntries, pipeline, serialization, loader::addPartitionChunk);
        loader.partitionsLoaded();
      }
    }
  }

  /**
   * Reads the given entries sequentially, deserializes them using the pipeline, and passes the
   * resulting chunks to the consumer, in order.
   */
  private static void deserializeEntries(
      ZipFile zipFile,
      List<ZipEntry> entries,
      OrderedPipeline<SerializationChunk> pipeline,
      ThreadLocal<ProtoBufSerialization> serialization,
      Consumer<SerializationChunk> consumer)
      throws IOException {
    OrderedPipeline.ResultConsumer<SerializationChunk> resultConsumer = consumer::accept;
    for (ZipEntry entry : entries) {
      final byte[] bytes;
      try (InputStream in = zipFile.getInputStream(entry)) {
        bytes = readEntryBytes(in, entry);
      }
      final String entryName = entry.getName();
      pipeline.submit(
          () -> {
            try {
              return serialization.get().deserializeToChunk(bytes);
            } catch (Exception e) {
              throw new RuntimeException("Failed to deserialize chunk from " + entryName, e);
            }
          },
          resultConsumer);
    }
    pipeline.drain(resultConsumer);
  }

  /**
   * Loads nodes from a LionWeb archive file. This method processes language chunks and partition
   * chunks contained in the archive and uses the provided consumer to handle deserialized nodes
   * from partition chunks.
   *
   * <p>Languages contained in the archive are registered in the given serialization, after being
   * sorted according to their dependencies. A language may depend on languages not contained in the
   * archive only if they are already registered in the given serialization.
   *
   * @param file the LionWeb archive file to be loaded. Must not be null, a directory, or
   *     non-existent.
   * @param protoBufSerialization the ProtoBufSerialization instance used for deserializing chunks.
   * @param partitionConsumer the consumer function to be called with each partition node
   *     deserialized from the archive.
   * @throws IOException if an error occurs while reading the file or processing its contents.
   * @throws IllegalArgumentException if the file does not exist, is a directory, or the archive
   *     does not meet the required structural assumptions.
   * @throws RuntimeException if an error occurs during chunk deserialization or if data is invalid.
   */
  public static void loadNodes(
      @Nonnull File file,
      @Nonnull ProtoBufSerialization protoBufSerialization,
      @Nonnull Consumer<Node> partitionConsumer)
      throws IOException {
    loadNodes(file, protoBufSerialization, partitionConsumer, false);
  }

  /**
   * Loads nodes from a LionWeb archive file. This method processes language chunks and partition
   * chunks contained in the archive and uses the provided consumer to handle deserialized nodes
   * from partition chunks.
   *
   * <p>Languages contained in the archive are registered in the given serialization, after being
   * sorted according to their dependencies. Dependencies on languages already registered in the
   * given serialization are always accepted.
   *
   * @param file the LionWeb archive file to be loaded. Must not be null, a directory, or
   *     non-existent.
   * @param protoBufSerialization the ProtoBufSerialization instance used for deserializing chunks.
   * @param partitionConsumer the consumer function to be called with each partition node
   *     deserialized from the archive.
   * @param ignoreUnknownLanguageDependencies when true, dependencies on languages neither contained
   *     in the archive nor registered in the serialization are ignored. When false, they cause an
   *     error.
   * @throws IOException if an error occurs while reading the file or processing its contents.
   * @throws IllegalArgumentException if the file does not exist, is a directory, or the archive
   *     does not meet the required structural assumptions.
   * @throws RuntimeException if an error occurs during chunk deserialization or if data is invalid.
   */
  public static void loadNodes(
      @Nonnull File file,
      @Nonnull ProtoBufSerialization protoBufSerialization,
      @Nonnull Consumer<Node> partitionConsumer,
      boolean ignoreUnknownLanguageDependencies)
      throws IOException {
    Objects.requireNonNull(file, "file should not be null");
    Objects.requireNonNull(protoBufSerialization, "protoBufSerialization should not be null");
    Objects.requireNonNull(partitionConsumer, "partitionConsumer should not be null");

    Predicate<String> isKnownDependency =
        ignoreUnknownLanguageDependencies
            ? languageID -> true
            : languageID ->
                protoBufSerialization.getInstanceResolver().resolve(languageID) instanceof Language;

    class MyLoader implements Loader {
      List<SerializationChunk> languageChunks = new ArrayList<>();
      TopologicalLanguageSorter topologicalSorter = null;

      public void setLwVersion(@Nonnull LionWebVersion lionWebVersion) {
        topologicalSorter = new TopologicalLanguageSorter(lionWebVersion);
      }

      public void addLanguageChunk(@Nonnull SerializationChunk chunk) {
        languageChunks.add(chunk);
      }

      @Override
      public void languagesLoaded() {
        languageChunks = topologicalSorter.topologicalSort(languageChunks, isKnownDependency);
        languageChunks.forEach(
            languageChunk -> {
              Language language =
                  (Language)
                      protoBufSerialization.deserializeSerializationChunk(languageChunk).get(0);
              protoBufSerialization.registerLanguage(language);
            });
      }

      public void addPartitionChunk(@Nonnull SerializationChunk chunk) {
        partitionConsumer.accept(
            (Node) protoBufSerialization.deserializeSerializationChunk(chunk).get(0));
      }

      @Override
      public void partitionsLoaded() {}
    }

    load(file, new MyLoader());
  }

  /**
   * Loads nodes from the given LionWeb archive file using the specified ProtoBufSerialization
   * instance. This method processes the archive and retrieves deserialized nodes from its contents.
   *
   * @param file the LionWeb archive file to be loaded; must not be null, a directory, or
   *     non-existent
   * @param protoBufSerialization the instance of ProtoBufSerialization used to deserialize the
   *     archive's content
   * @return a list of deserialized Node objects
   * @throws IOException if an error occurs while reading the file or processing its contents
   */
  public static List<Node> loadNodes(
      @Nonnull File file, @Nonnull ProtoBufSerialization protoBufSerialization) throws IOException {
    Objects.requireNonNull(file, "file should not be null");
    Objects.requireNonNull(protoBufSerialization, "protoBufSerialization should not be null");
    List<Node> nodes = new ArrayList<>();
    loadNodes(file, protoBufSerialization, nodes::add);
    return nodes;
  }

  /**
   * Store a LionWeb archive, using the {@link #DEFAULT_COMPRESSION_LEVEL default compression
   * level}.
   *
   * @see #store(File, LionWebVersion, Iterable, Iterable, int)
   */
  public static void store(
      @Nonnull File file,
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable Iterable<SerializationChunk> languageChunks,
      @Nullable Iterable<SerializationChunk> partitionChunks)
      throws IOException {
    store(file, lionWebVersion, languageChunks, partitionChunks, DEFAULT_COMPRESSION_LEVEL);
  }

  /**
   * Store a LionWeb archive.
   *
   * <p>Chunks are consumed from the given iterables and written through a {@link Writer}: they are
   * serialized in parallel, within a bounded window, and written to the archive as soon as they are
   * ready. Therefore, the memory used does not depend on the size of the whole archive.
   *
   * @param compressionLevel the compression level, from 0 (no compression) to 9 (best compression),
   *     or {@link Deflater#DEFAULT_COMPRESSION}
   */
  public static void store(
      @Nonnull File file,
      @Nonnull LionWebVersion lionWebVersion,
      @Nullable Iterable<SerializationChunk> languageChunks,
      @Nullable Iterable<SerializationChunk> partitionChunks,
      int compressionLevel)
      throws IOException {
    try (Writer writer = new Writer(file, lionWebVersion, compressionLevel)) {
      if (languageChunks != null) {
        for (SerializationChunk chunk : languageChunks) {
          writer.addLanguageChunk(chunk);
        }
      }
      if (partitionChunks != null) {
        for (SerializationChunk chunk : partitionChunks) {
          writer.addPartitionChunk(chunk);
        }
      }
    }
  }

  /**
   * Writes a LionWeb archive incrementally.
   *
   * <p>Chunks added to the writer are serialized in parallel and written to the archive, in the
   * order in which they were added, as soon as they are ready. At most a bounded number of chunks
   * are kept in memory at any time, so arbitrarily large archives can be written. The archive is
   * complete only once the writer has been closed.
   *
   * <p>Instances of this class are not thread-safe: chunks should be added from a single thread.
   *
   * <pre>{@code
   * try (LionWebArchive.Writer writer = new LionWebArchive.Writer(file, LionWebVersion.v2023_1)) {
   *   writer.addLanguageChunk(languageChunk);
   *   for (SerializationChunk partitionChunk : partitionChunks) {
   *     writer.addPartitionChunk(partitionChunk);
   *   }
   * }
   * }</pre>
   */
  public static final class Writer implements Closeable {
    private final ZipOutputStream zipOut;
    private final OrderedPipeline<ZipChunk> pipeline;
    private final ThreadLocal<ProtoBufSerialization> serialization;
    private final OrderedPipeline.ResultConsumer<ZipChunk> entryWriter = this::writeEntry;
    private boolean closed = false;

    /**
     * Creates a writer using the {@link #DEFAULT_COMPRESSION_LEVEL default compression level}.
     *
     * @see #Writer(File, LionWebVersion, int)
     */
    public Writer(@Nonnull File file, @Nonnull LionWebVersion lionWebVersion) throws IOException {
      this(file, lionWebVersion, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Creates a writer. The file is created (or truncated, if it exists already) and the metadata
     * are written immediately.
     *
     * @param file the file to write. Its parent directories are created, if needed.
     * @param lionWebVersion the LionWeb version of the chunks to be written
     * @param compressionLevel the compression level, from 0 (no compression) to 9 (best
     *     compression), or {@link Deflater#DEFAULT_COMPRESSION}
     */
    public Writer(@Nonnull File file, @Nonnull LionWebVersion lionWebVersion, int compressionLevel)
        throws IOException {
      Objects.requireNonNull(file, "file should not be null");
      Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
      if (compressionLevel != Deflater.DEFAULT_COMPRESSION
          && (compressionLevel < Deflater.NO_COMPRESSION
              || compressionLevel > Deflater.BEST_COMPRESSION)) {
        throw new IllegalArgumentException("Invalid compression level: " + compressionLevel);
      }

      // Ensure parent directory exists
      File parent = file.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }

      Properties metadata = new Properties();
      metadata.setProperty(LW_VERSION_KEY, lionWebVersion.getVersionString());

      this.zipOut =
          new ZipOutputStream(
              new BufferedOutputStream(
                  Files.newOutputStream(
                      file.toPath(),
                      StandardOpenOption.CREATE,
                      StandardOpenOption.TRUNCATE_EXISTING),
                  FOUR_MIB));
      try {
        zipOut.setLevel(compressionLevel);
        zipOut.putNextEntry(new ZipEntry(METADATA_PATH));
        metadata.store(zipOut, null);
        zipOut.closeEntry();
      } catch (IOException | RuntimeException e) {
        zipOut.close();
        throw e;
      }
      this.serialization =
          ThreadLocal.withInitial(
              () -> SerializationProvider.getEfficientProtoBufSerialization(lionWebVersion));
      this.pipeline = new OrderedPipeline<>(defaultParallelism());
    }

    /** Adds a chunk containing a language. */
    public void addLanguageChunk(@Nonnull SerializationChunk chunk) throws IOException {
      add(chunk, LANGUAGES_DIRECTORY);
    }

    /** Adds a chunk containing a partition. */
    public void addPartitionChunk(@Nonnull SerializationChunk chunk) throws IOException {
      add(chunk, PARTITIONS_DIRECTORY);
    }

    private void add(@Nonnull SerializationChunk chunk, @Nonnull String directory)
        throws IOException {
      Objects.requireNonNull(chunk, "chunk should not be null");
      if (closed) {
        throw new IllegalStateException("The writer has been closed");
      }
      final String entryName = directory + getPartitionId(chunk) + CHUNK_EXTENSION;
      pipeline.submit(
          () -> {
            try {
              return new ZipChunk(entryName, serialization.get().serializeToByteArray(chunk));
            } catch (Exception e) {
              throw new RuntimeException("Failed to serialize chunk for " + entryName, e);
            }
          },
          entryWriter);
    }

    private void writeEntry(ZipChunk zipChunk) throws IOException {
      zipOut.putNextEntry(new ZipEntry(zipChunk.entryName));
      zipOut.write(zipChunk.bytes);
      zipOut.closeEntry();
    }

    /** Writes the chunks still pending and completes the archive. */
    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      closed = true;
      try (ZipOutputStream out = zipOut;
          OrderedPipeline<ZipChunk> p = pipeline) {
        p.drain(entryWriter);
      }
    }
  }

  /** Simple holder for a prepared ZIP entry. */
  private static final class ZipChunk {
    final String entryName;
    final byte[] bytes;

    ZipChunk(String entryName, byte[] bytes) {
      this.entryName = entryName;
      this.bytes = bytes;
    }
  }

  private static int defaultParallelism() {
    return Math.max(1, Runtime.getRuntime().availableProcessors());
  }

  /**
   * Executes tasks in parallel, within a bounded window, and delivers their results in submission
   * order, on the thread submitting the tasks.
   */
  private static final class OrderedPipeline<T> implements AutoCloseable {
    interface ResultConsumer<T> {
      void accept(T result) throws IOException;
    }

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private final @Nullable ExecutorService executor;
    private final ArrayDeque<Future<T>> pending = new ArrayDeque<>();
    private final int window;

    OrderedPipeline(int parallelism) {
      if (parallelism <= 1) {
        this.executor = null;
        this.window = 0;
      } else {
        this.executor =
            Executors.newFixedThreadPool(
                parallelism,
                runnable -> {
                  Thread thread =
                      new Thread(runnable, "lionweb-archive-" + THREAD_COUNTER.incrementAndGet());
                  thread.setDaemon(true);
                  return thread;
                });
        this.window = parallelism * 2;
      }
    }

    /**
     * Submits a task. The results of previously submitted tasks may be passed to the consumer, to
     * keep the number of pending tasks within the window.
     */
    void submit(Callable<T> task, ResultConsumer<T> consumer) throws IOException {
      if (executor == null) {
        consumer.accept(call(task));
        return;
      }
      pending.addLast(executor.submit(task));
      while (pending.size() > window) {
        consumer.accept(takeFirst());
      }
    }

    /** Passes the results of all pending tasks to the consumer. */
    void drain(ResultConsumer<T> consumer) throws IOException {
      while (!pending.isEmpty()) {
        consumer.accept(takeFirst());
      }
    }

    private T takeFirst() {
      Future<T> future = pending.removeFirst();
      try {
        return future.get();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while processing chunks", e);
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException) cause;
        }
        if (cause instanceof Error) {
          throw (Error) cause;
        }
        throw new RuntimeException("Failed while processing chunks", cause);
      }
    }

    private static <T> T call(Callable<T> task) {
      try {
        return task.call();
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException("Failed while processing chunks", e);
      }
    }

    @Override
    public void close() {
      if (executor != null) {
        pending.forEach(f -> f.cancel(true));
        pending.clear();
        executor.shutdownNow();
      }
    }
  }

  /** Extracts the root partition id from a chunk (the classifier instance with no parent). */
  private static String getPartitionId(SerializationChunk chunk) {
    for (SerializedClassifierInstance n : chunk.getClassifierInstances()) {
      if (n.getParentNodeID() == null) {
        return n.getID();
      }
    }
    throw new IllegalStateException("No root classifier instance found in chunk");
  }

  /**
   * Reads the content of an entry. When the size of the entry is known, the buffer is allocated
   * with the right size, avoiding further copies.
   */
  private static byte[] readEntryBytes(InputStream in, ZipEntry entry) throws IOException {
    long size = entry.getSize();
    if (size < 0 || size > Integer.MAX_VALUE - 8) {
      return in.readAllBytes();
    }
    byte[] bytes = new byte[(int) size];
    int read = in.readNBytes(bytes, 0, bytes.length);
    if (read != bytes.length || in.read() != -1) {
      throw new IOException("Unexpected size for entry " + entry.getName());
    }
    return bytes;
  }
}
