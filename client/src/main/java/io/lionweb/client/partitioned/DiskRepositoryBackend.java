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
    for (int ni = 0, nn = nodes.size(); ni < nn; ni++) {
      SerializedClassifierInstance sci = nodes.get(ni);
      internString(sci.getID(), strings, stringIndex);
      internString(sci.getParentNodeID(), strings, stringIndex);
      internMp(sci.getClassifier(), metaPointers, mpIndex, strings, stringIndex);
      List<SerializedPropertyValue> sciProps = sci.getProperties();
      for (int pi = 0, pn = sciProps.size(); pi < pn; pi++) {
        SerializedPropertyValue p = sciProps.get(pi);
        internMp(p.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        internString(p.getValue(), strings, stringIndex);
      }
      List<SerializedContainmentValue> sciConts = sci.getContainments();
      for (int ci = 0, cn = sciConts.size(); ci < cn; ci++) {
        SerializedContainmentValue c = sciConts.get(ci);
        internMp(c.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        // This should be unneccessary: all the node IDs are also nodes of the chunk, or should be
        List<String> childIds = c.getChildrenIds();
        for (int chi = 0, chn = childIds.size(); chi < chn; chi++)
          internString(childIds.get(chi), strings, stringIndex);
      }
      List<SerializedReferenceValue> sciRefs = sci.getReferences();
      for (int ri = 0, rn = sciRefs.size(); ri < rn; ri++) {
        SerializedReferenceValue r = sciRefs.get(ri);
        internMp(r.getMetaPointer(), metaPointers, mpIndex, strings, stringIndex);
        List<SerializedReferenceValue.Entry> refEntries = r.getValue();
        for (int ei = 0, en = refEntries.size(); ei < en; ei++) {
          SerializedReferenceValue.Entry e = refEntries.get(ei);
          internString(e.getReference(), strings, stringIndex);
          internString(e.getResolveInfo(), strings, stringIndex);
        }
      }
      List<String> sciAnns = sci.getAnnotations();
      for (int ai = 0, an = sciAnns.size(); ai < an; ai++)
        internString(sciAnns.get(ai), strings, stringIndex);
    }

    // Pre-encode all string table entries once so getBytes() is called only once per string
    List<byte[]> encodedStrings = new ArrayList<>(strings.size());
    encodedStrings.add(null); // index 0 placeholder
    for (int i = 1; i < strings.size(); i++) {
      encodedStrings.add(strings.get(i).getBytes(StandardCharsets.UTF_8));
    }

    // Write magic
    out.write(MAGIC);

    // Write string table (strings[0] is the null placeholder, not written)
    out.writeInt(strings.size() - 1);
    for (int i = 1; i < strings.size(); i++) writeEncodedString(out, encodedStrings.get(i));

    // Write metapointer table
    out.writeShort(metaPointers.size());
    for (MetaPointer mp : metaPointers) {
      out.writeInt(idxOf(mp.getLanguage(), stringIndex));
      out.writeInt(idxOf(mp.getVersion(), stringIndex));
      out.writeInt(idxOf(mp.getKey(), stringIndex));
    }

    // Write nodes
    out.writeInt(nodes.size());
    for (int ni = 0, nn = nodes.size(); ni < nn; ni++) {
      SerializedClassifierInstance sci = nodes.get(ni);
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

      for (int pi = 0, pn = props.size(); pi < pn; pi++) {
        SerializedPropertyValue p = props.get(pi);
        out.writeShort(mpIndex.get(p.getMetaPointer()));
        out.writeInt(idxOf(p.getValue(), stringIndex));
      }

      for (int ci = 0, cn = conts.size(); ci < cn; ci++) {
        SerializedContainmentValue c = conts.get(ci);
        out.writeShort(mpIndex.get(c.getMetaPointer()));
        List<String> children = c.getChildrenIds();
        out.writeShort(children.size());
        for (int chi = 0, chn = children.size(); chi < chn; chi++)
          out.writeInt(idxOf(children.get(chi), stringIndex));
      }

      out.writeShort(refs.size());
      for (int ri = 0, rn = refs.size(); ri < rn; ri++) {
        SerializedReferenceValue r = refs.get(ri);
        out.writeShort(mpIndex.get(r.getMetaPointer()));
        List<SerializedReferenceValue.Entry> entries = r.getValue();
        out.writeShort(entries.size());
        for (int ei = 0, en = entries.size(); ei < en; ei++) {
          SerializedReferenceValue.Entry e = entries.get(ei);
          out.writeInt(idxOf(e.getReference(), stringIndex));
          out.writeInt(idxOf(e.getResolveInfo(), stringIndex));
        }
      }

      out.writeShort(anns.size());
      for (int ai = 0, an = anns.size(); ai < an; ai++) out.writeInt(idxOf(anns.get(ai), stringIndex));
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
    byte[][] scratch = {new byte[256]};
    for (int i = 1; i <= strCount; i++) strings[i] = readString(in, scratch);

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
  // Intern helpers (write side only)
  // ---------------------------------------------------------------------------

  /** Interns {@code s} into the string table. Returns 0 for null, 1-based index otherwise. */
  private static int internString(String s, List<String> strings, Map<String, Integer> index) {
    if (s == null) return 0;
    Integer existing = index.get(s);
    if (existing != null) return existing;
    strings.add(s);
    int idx = strings.size() - 1;
    index.put(s, idx);
    return idx;
  }

  /** Returns the string table index for {@code s}, which must already be interned. */
  private static int idxOf(String s, Map<String, Integer> index) {
    if (s == null) return 0;
    Integer i = index.get(s);
    if (i == null) throw new IllegalStateException("String not interned: " + s);
    return i;
  }

  private static void writeEncodedString(DataOutputStream out, byte[] encoded) throws IOException {
    out.writeInt(encoded.length);
    out.write(encoded);
  }

  private static String readString(DataInputStream in, byte[][] scratch) throws IOException {
    int length = in.readInt();
    if (scratch[0].length < length) scratch[0] = new byte[length];
    in.readFully(scratch[0], 0, length);
    return new String(scratch[0], 0, length, StandardCharsets.UTF_8);
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
