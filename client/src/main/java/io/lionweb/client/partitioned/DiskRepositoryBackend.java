package io.lionweb.client.partitioned;

import io.lionweb.serialization.data.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Disk-based {@link RepositoryBackend} that stores each partition in a compact flat binary format
 * (.lwb) designed for minimal allocation on read.
 *
 * <h3>File layout</h3>
 *
 * <pre>
 *   &lt;storageDir&gt;/
 *     &lt;repositoryName&gt;/
 *       &lt;partitionId&gt;.lwb
 * </pre>
 *
 * <h3>Format</h3>
 *
 * A custom binary format (magic "LWB\1") with a string intern table and a metapointer table,
 * followed by node data expressed entirely as integer indices. Reads directly into domain objects
 * with no intermediate ProtoBuf builder objects.
 *
 * <p>Per-node layout: idIdx, parentIdx, classifierMpIdx, propCount, contCount, [props], [conts],
 * refCount, [refs], annCount, [anns]. propCount and contCount are written together before their
 * data so the read side can pre-size both backing lists before constructing the instance.
 *
 * <h3>Write strategy</h3>
 *
 * Each save writes to a .tmp file then atomically renames to avoid partial reads on crash.
 */
public final class DiskRepositoryBackend implements RepositoryBackend {

  private static final String EXTENSION = ".lwb";
  private static final byte[] MAGIC = {0x4C, 0x57, 0x42, 0x01}; // "LWB\1"

  private final Path storageDir;

  public DiskRepositoryBackend(Path storageDir) {
    this.storageDir = storageDir;
  }

  // ---------------------------------------------------------------------------
  // RepositoryBackend interface
  // ---------------------------------------------------------------------------

  @Override
  public List<String> listPersistedPartitionIds(String repositoryName) throws IOException {
    Path repoDir = repoDir(repositoryName);
    if (!Files.isDirectory(repoDir)) return Collections.emptyList();
    Set<String> ids = new LinkedHashSet<>();
    try (Stream<Path> files = Files.list(repoDir)) {
      for (Path p : files.collect(Collectors.toList())) {
        String name = p.getFileName().toString();
        if (name.endsWith(EXTENSION)) {
          ids.add(name.substring(0, name.length() - EXTENSION.length()));
        }
      }
    }
    return new ArrayList<>(ids);
  }

  @Override
  public List<SerializedClassifierInstance> loadPartition(String repositoryName, String partitionId)
      throws IOException {
    Path lwbFile = partitionFile(repositoryName, partitionId);
    if (Files.exists(lwbFile)) {
      return readLwb(lwbFile);
    }
    return Collections.emptyList();
  }

  @Override
  public void savePartition(String repositoryName, String partitionId, SerializationChunk chunk)
      throws IOException {
    Path repoDir = repoDir(repositoryName);
    Files.createDirectories(repoDir);
    Path target = repoDir.resolve(partitionId + EXTENSION);
    Path tmp = repoDir.resolve(partitionId + EXTENSION + ".tmp");
    writeLwb(chunk, tmp);
    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
  }

  @Override
  public void deletePartition(String repositoryName, String partitionId) throws IOException {
    Files.deleteIfExists(partitionFile(repositoryName, partitionId));
  }

  @Override
  public boolean hasPartition(String repositoryName, String partitionId) throws IOException {
    return Files.exists(partitionFile(repositoryName, partitionId));
  }

  @Override
  public void deleteRepository(String repositoryName) throws IOException {
    Path repoDir = repoDir(repositoryName);
    if (!Files.isDirectory(repoDir)) return;
    try (Stream<Path> files = Files.list(repoDir)) {
      for (Path f : files.collect(Collectors.toList())) Files.deleteIfExists(f);
    }
    Files.deleteIfExists(repoDir);
  }

  @Override
  public void close() throws IOException {
    // No resources to release
  }

  public Path getStorageDir() {
    return storageDir;
  }

  // ---------------------------------------------------------------------------
  // Write
  // ---------------------------------------------------------------------------

  private static void writeLwb(SerializationChunk chunk, Path dest) throws IOException {
    try (OutputStream fos =
            Files.newOutputStream(
                dest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        BufferedOutputStream bos = new BufferedOutputStream(fos, 1 << 16);
        DataOutputStream out = new DataOutputStream(bos)) {
      writeChunk(chunk, out);
    }
  }

  /**
   * Writes the chunk to {@code out} in the LWB binary format.
   *
   * <p>String table: index 0 = null sentinel; real strings start at index 1. MetaPointer table:
   * 0-based indices.
   *
   * <p>Per-node: propCount and contCount are written together before their data blocks so the read
   * side can pre-size both lists before constructing the instance.
   */
  private static void writeChunk(SerializationChunk chunk, DataOutputStream out)
      throws IOException {

    List<SerializedClassifierInstance> nodes = chunk.getClassifierInstances();

    // Build string intern table.
    // Index 0 is reserved for null; real strings start at index 1.
    List<String> strings = new ArrayList<>();
    strings.add(null); // placeholder so real strings start at index 1
    Map<String, Integer> stringIndex = new HashMap<>();

    // MetaPointer uses identity equality, so IdentityHashMap is correct and faster.
    List<MetaPointer> metaPointers = new ArrayList<>();
    Map<MetaPointer, Integer> mpIndex = new IdentityHashMap<>();

    // Single pre-scan pass to populate both tables
    for (SerializedClassifierInstance sci : nodes) {
      internString(sci.getID(), strings, stringIndex);
      internString(sci.getParentNodeID(), strings, stringIndex);
      internMp(sci.getClassifier(), metaPointers, mpIndex, strings, stringIndex);
      for (SerializedPropertyValue p : sci.getProperties()) {
        internMp(p.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        internString(p.getValue(), strings, stringIndex);
      }
      for (SerializedContainmentValue c : sci.getContainments()) {
        internMp(c.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        // This should be unneccessary: all the node IDs are also nodes of the chunk, or should be
        for (String childId : c.getChildrenIds()) internString(childId, strings, stringIndex);
      }
      for (SerializedReferenceValue r : sci.getReferences()) {
        internMp(r.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        for (SerializedReferenceValue.Entry e : r.getValue()) {
          internString(e.getReference(), strings, stringIndex);
          internString(e.getResolveInfo(), strings, stringIndex);
        }
      }
      for (String ann : sci.getAnnotations()) internString(ann, strings, stringIndex);
    }

    // Write magic
    out.write(MAGIC);

    // Write string table (strings[0] is the null placeholder, not written)
    out.writeInt(strings.size() - 1);
    for (int i = 1; i < strings.size(); i++) writeString(out, strings.get(i));

    // Write metapointer table
    out.writeShort(metaPointers.size());
    for (MetaPointer mp : metaPointers) {
      out.writeInt(idxOf(mp.getLanguage(), stringIndex));
      out.writeInt(idxOf(mp.getVersion(), stringIndex));
      out.writeInt(idxOf(mp.getKey(), stringIndex));
    }

    // Write nodes
    out.writeInt(nodes.size());
    for (SerializedClassifierInstance sci : nodes) {
      out.writeInt(idxOf(sci.getID(), stringIndex));
      out.writeInt(idxOf(sci.getParentNodeID(), stringIndex));
      out.writeShort(mpIndex.get(sci.getClassifier()));

      List<SerializedPropertyValue> props = sci.getProperties();
      List<SerializedContainmentValue> conts = sci.getContainments();
      List<SerializedReferenceValue> refs = sci.getReferences();
      List<String> anns = sci.getAnnotations();

      // propCount and contCount written together so the reader can pre-size both lists
      // before constructing the SerializedClassifierInstance.
      out.writeShort(props.size());
      out.writeShort(conts.size());

      for (SerializedPropertyValue p : props) {
        out.writeShort(mpIndex.get(p.getMetaPointer()));
        out.writeInt(idxOf(p.getValue(), stringIndex));
      }

      for (SerializedContainmentValue c : conts) {
        out.writeShort(mpIndex.get(c.getMetaPointer()));
        List<String> children = c.getChildrenIds();
        out.writeShort(children.size());
        for (String childId : children) out.writeInt(idxOf(childId, stringIndex));
      }

      out.writeShort(refs.size());
      for (SerializedReferenceValue r : refs) {
        out.writeShort(mpIndex.get(r.getMetaPointer()));
        List<SerializedReferenceValue.Entry> entries = r.getValue();
        out.writeShort(entries.size());
        for (SerializedReferenceValue.Entry e : entries) {
          out.writeInt(idxOf(e.getReference(), stringIndex));
          out.writeInt(idxOf(e.getResolveInfo(), stringIndex));
        }
      }

      out.writeShort(anns.size());
      for (String ann : anns) out.writeInt(idxOf(ann, stringIndex));
    }
  }

  // ---------------------------------------------------------------------------
  // Read
  // ---------------------------------------------------------------------------

  private static List<SerializedClassifierInstance> readLwb(Path file) throws IOException {
    try (InputStream fis = Files.newInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis, 1 << 16);
        DataInputStream in = new DataInputStream(bis)) {
      return readNodes(in);
    }
  }

  /**
   * Reads a chunk written by {@link #writeChunk} and returns its nodes. Allocates only:
   *
   * <ul>
   *   <li>one String[] for the string table
   *   <li>one MetaPointer[] for the metapointer table
   *   <li>one SerializedClassifierInstance per node (with pre-sized backing lists)
   * </ul>
   *
   * No intermediate ProtoBuf builder objects are created.
   */
  private static List<SerializedClassifierInstance> readNodes(DataInputStream in)
      throws IOException {

    // Verify magic
    byte[] magic = new byte[4];
    in.readFully(magic);
    if (magic[0] != MAGIC[0]
        || magic[1] != MAGIC[1]
        || magic[2] != MAGIC[2]
        || magic[3] != MAGIC[3]) {
      throw new IOException("Not a valid LWB file (bad magic bytes)");
    }

    // String table — index 0 stays null (sentinel)
    int strCount = in.readInt();
    String[] strings = new String[strCount + 1]; // strings[0] = null
    for (int i = 1; i <= strCount; i++) strings[i] = readString(in);

    // MetaPointer table — 0-based
    int mpCount = in.readShort() & 0xFFFF;
    MetaPointer[] metaPointers = new MetaPointer[mpCount];
    for (int i = 0; i < mpCount; i++) {
      String language = strings[in.readInt()];
      String version = strings[in.readInt()];
      String key = strings[in.readInt()];
      metaPointers[i] = MetaPointer.get(language, version, key);
    }

    // Nodes
    int nodeCount = in.readInt();
    List<SerializedClassifierInstance> result = new ArrayList<>(nodeCount);
    for (int n = 0; n < nodeCount; n++) {
      String id = strings[in.readInt()];
      String parentId = strings[in.readInt()];
      MetaPointer classifier = metaPointers[in.readShort() & 0xFFFF];

      // propCount and contCount are read together before construction so both
      // backing lists can be pre-sized in the constructor.
      int propCount = in.readShort() & 0xFFFF;
      int contCount = in.readShort() & 0xFFFF;
      SerializedClassifierInstance sci =
          new SerializedClassifierInstance(propCount, contCount, 0, 0);
      sci.setID(id);
      sci.setParentNodeID(parentId);
      sci.setClassifier(classifier);

      for (int p = 0; p < propCount; p++) {
        MetaPointer mp = metaPointers[in.readShort() & 0xFFFF];
        String value = strings[in.readInt()];
        sci.unsafeAppendPropertyValue(SerializedPropertyValue.get(mp, value));
      }

      for (int c = 0; c < contCount; c++) {
        MetaPointer mp = metaPointers[in.readShort() & 0xFFFF];
        int childCount = in.readShort() & 0xFFFF;
        List<String> children = new ArrayList<>(childCount);
        for (int ch = 0; ch < childCount; ch++) children.add(strings[in.readInt()]);
        sci.unsafeAppendContainmentValue(mp, children);
      }

      int refCount = in.readShort() & 0xFFFF;
      for (int r = 0; r < refCount; r++) {
        MetaPointer mp = metaPointers[in.readShort() & 0xFFFF];
        int entryCount = in.readShort() & 0xFFFF;
        SerializedReferenceValue srv = new SerializedReferenceValue(mp);
        for (int e = 0; e < entryCount; e++) {
          SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
          entry.setReference(strings[in.readInt()]);
          entry.setResolveInfo(strings[in.readInt()]);
          srv.addValue(entry);
        }
        sci.unsafeAppendReferenceValue(srv);
      }

      int annCount = in.readShort() & 0xFFFF;
      for (int a = 0; a < annCount; a++) sci.addAnnotation(strings[in.readInt()]);

      result.add(sci);
    }
    return result;
  }

  // ---------------------------------------------------------------------------
  // Path helpers
  // ---------------------------------------------------------------------------

  private Path repoDir(String repositoryName) {
    return storageDir.resolve(repositoryName);
  }

  private Path partitionFile(String repositoryName, String partitionId) {
    return repoDir(repositoryName).resolve(partitionId + EXTENSION);
  }

  // ---------------------------------------------------------------------------
  // String I/O helpers — int-prefixed UTF-8, no 65535-byte limit
  // ---------------------------------------------------------------------------

  private static void writeString(DataOutputStream out, String s) throws IOException {
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    out.writeInt(bytes.length);
    out.write(bytes);
  }

  private static String readString(DataInputStream in) throws IOException {
    int length = in.readInt();
    byte[] bytes = new byte[length];
    in.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  // ---------------------------------------------------------------------------
  // Intern helpers (write side only)
  // ---------------------------------------------------------------------------

  /** Interns {@code s} into the string table. Returns 0 for null, 1-based index otherwise. */
  private static int internString(String s, List<String> strings, Map<String, Integer> index) {
    if (s == null) return 0;
    return index.computeIfAbsent(
        s,
        k -> {
          strings.add(k);
          return strings.size() - 1; // 1-based because strings[0] is the null placeholder
        });
  }

  /** Returns the string table index for {@code s}, which must already be interned. */
  private static int idxOf(String s, Map<String, Integer> index) {
    if (s == null) return 0;
    Integer i = index.get(s);
    if (i == null) throw new IllegalStateException("String not interned: " + s);
    return i;
  }

  /** Interns {@code mp} into the metapointer table, interning its constituent strings too. */
  private static void internMp(
      MetaPointer mp,
      List<MetaPointer> metaPointers,
      Map<MetaPointer, Integer> mpIndex,
      List<String> strings,
      Map<String, Integer> stringIndex) {
    if (mp == null) return;
    if (mpIndex.containsKey(mp)) return;
    internString(mp.getLanguage(), strings, stringIndex);
    internString(mp.getVersion(), strings, stringIndex);
    internString(mp.getKey(), strings, stringIndex);
    mpIndex.put(mp, metaPointers.size());
    metaPointers.add(mp);
  }
}
