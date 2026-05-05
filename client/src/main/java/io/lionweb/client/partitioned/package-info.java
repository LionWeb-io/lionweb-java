/**
 * Partition-oriented, disk-backed replacement for {@link io.lionweb.client.inmemory}.
 *
 * <h2>Purpose</h2>
 *
 * <p>{@link io.lionweb.client.partitioned.PartitionedServer} is an embedded LionWeb repository
 * server that keeps only a bounded window of partitions in memory and persists the rest to disk. It
 * is designed for repositories with tens of millions of nodes where keeping everything in the JVM
 * heap is impractical.
 *
 * <h2>When to use it</h2>
 *
 * <ul>
 *   <li>Repository sizes that do not fit in JVM heap (e.g., ≥ 67 million nodes).
 *   <li>Batch processing pipelines that iterate over partitions sequentially.
 *   <li>Offline tooling that needs durability between runs.
 * </ul>
 *
 * <h2>How it differs from {@code inmemory}</h2>
 *
 * <ul>
 *   <li>Data is persisted to disk in ProtoBuf format, one file per partition.
 *   <li>Only a bounded number of partitions are kept in memory (see {@link
 *       io.lionweb.client.partitioned.CacheConfig}).
 *   <li>A compact {@code nodeId → partitionId} index is always kept in memory so that individual
 *       node lookups can load the right partition without scanning all files.
 *   <li>A classifier index is kept in memory so that {@code nodesByClassifier} / {@code
 *       nodesByLanguage} do not require loading every partition.
 *   <li>Partitions are written lazily: dirty partitions are flushed on {@link
 *       io.lionweb.client.partitioned.PartitionedServer#flush()} or {@code close()}, or when they
 *       are evicted from the cache.
 * </ul>
 *
 * <h2>API compatibility with {@code InMemoryServer}</h2>
 *
 * <p>{@link io.lionweb.client.partitioned.PartitionedServer} exposes the same public methods as
 * {@code InMemoryServer}. Two additional methods are provided:
 *
 * <ul>
 *   <li>{@code flush()} – persist all dirty partitions to disk.
 *   <li>{@code close()} – flush and release resources ({@code AutoCloseable}).
 * </ul>
 *
 * <h2>Persistence format</h2>
 *
 * <p>Each partition is stored as a single ProtoBuf file ({@code
 * <storageDir>/<repositoryName>/<partitionId>.pb}). The file format is the standard LionWeb
 * ProtoBuf serialisation produced by {@code ProtoBufSerialization}.
 *
 * <h2>Cache behaviour</h2>
 *
 * <p>The cache evicts the least-recently-used non-pinned partition when either the partition count
 * or the node count exceeds the configured limits. Dirty partitions are written to disk before
 * eviction.
 *
 * <h2>Thread safety</h2>
 *
 * <p>The same caveats as {@code InMemoryServer} apply: concurrent reads are safe, but <em>mutation
 * methods are not thread-safe</em>.
 *
 * <h2>Current limitations</h2>
 *
 * <ul>
 *   <li>The {@code nodeId → partitionId} index is held entirely in heap. For 67 M nodes this may
 *       itself require several GB of RAM. A future version could use an off-heap or memory-mapped
 *       index.
 *   <li>No Delta protocol support (unlike {@code InMemoryServer}).
 *   <li>One file per partition; no append-only block store yet (the storage layer is abstracted
 *       through {@link io.lionweb.client.partitioned.RepositoryBackend} to allow this later).
 * </ul>
 */
package io.lionweb.client.partitioned;
