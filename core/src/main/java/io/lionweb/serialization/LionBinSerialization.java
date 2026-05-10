package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.serialization.data.*;
import io.lionweb.serialization.data.MetaPointer;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Binary serialization format ("LionBin") that exploits the structural regularity of LionWeb
 * nodes: every node with the same classifier always has the same set of properties, containments,
 * and references (features are fixed by the language). Annotations may vary per node.
 *
 * <p>A <em>type table</em> captures (classifier metapointer + ordered feature metapointers) exactly
 * once per distinct classifier encountered in the chunk. Each node record stores only a type-table
 * index plus its data, eliminating per-node metapointer repetition and enabling exact pre-sizing of
 * {@link SerializedClassifierInstance} collections at deserialization time.
 *
 * <h2>Binary layout</h2>
 *
 * <pre>
 * HEADER
 *   magic:                    5 bytes "LWBIN"
 *   format version:           1 byte  (= 1)
 *   serialization format ver: int(len) + UTF-8 bytes
 *
 * STRING TABLE              [count: int]  ([len: int] [bytes])*
 *   index 0 = null (not stored); stored entries start at index 1
 *
 * LANGUAGE TABLE            [count: int]  ([siKey: int] [siVersion: int])*
 *   index 0 = null sentinel (not stored)
 *
 * METAPOINTER TABLE         [count: int]  ([liLanguage: int] [siKey: int])*
 *
 * TYPE TABLE                [count: int]  (
 *   [mpiClassifier: int]
 *   [propertyCount: int]    [mpiProp: int]*
 *   [containmentCount: int] [mpiCont: int]*
 *   [referenceCount: int]   [mpiRef:  int]*
 * )*
 *
 * NODE DATA                 [count: int]  (
 *   [typeIndex:      int]
 *   [siId:           int]   0 = null ID
 *   [siParent:       int]   0 = no parent
 *   [annotationCount: int]  written first so reader can pre-size correctly
 *   [siPropertyValue: int]* one per property in type order; 0 = null value
 *   ( [childCount: int] [siChild: int]* )*     one section per containment
 *   ( [entryCount: int] ([siReferred: int] [siResolveInfo: int])* )*  one per reference
 *   [siAnnotation: int]*    annotationCount entries
 * )*
 * </pre>
 */
public class LionBinSerialization extends AbstractSerialization {

  static final byte[] MAGIC = {'L', 'W', 'B', 'I', 'N'};
  static final byte FORMAT_VERSION = 1;

  public LionBinSerialization() {
    super();
  }

  public LionBinSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  // ===========================================================================
  // Public serialization API
  // ===========================================================================

  public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(1024);
    Set<String> seenIDs = new HashSet<>(1024);
    for (ClassifierInstance<?> root : roots) {
      collectNonProxyNoDup(root, allNodes, seenIDs);
    }
    return serializeNodesToByteArray(allNodes);
  }

  public byte[] serializeNodesToByteArray(@Nonnull List<ClassifierInstance<?>> nodes) {
    for (int i = 0, sz = nodes.size(); i < sz; i++) {
      if (nodes.get(i) instanceof ProxyNode) {
        throw new IllegalArgumentException("Proxy nodes cannot be serialized");
      }
    }
    try {
      return serializeToBytes(nodes);
    } catch (IOException e) {
      throw new RuntimeException("Unexpected IO error during serialization", e);
    }
  }

  // ===========================================================================
  // Public deserialization API
  // ===========================================================================

  public SerializationChunk deserializeToChunk(@Nonnull byte[] bytes) throws IOException {
    return deserializeBinaryChunk(ByteBuffer.wrap(bytes));
  }

  public SerializationChunk deserializeToChunk(@Nonnull File file) throws IOException {
    return deserializeToChunk(Files.readAllBytes(file.toPath()));
  }

  public SerializationChunk deserializeToChunk(@Nonnull InputStream inputStream)
      throws IOException {
    return deserializeToChunk(readAllBytes(inputStream));
  }

  public List<Node> deserializeToNodes(@Nonnull byte[] bytes) throws IOException {
    SerializationChunk chunk = deserializeToChunk(bytes);
    validateSerializationBlock(chunk);
    List<ClassifierInstance<?>> all = deserializeSerializationChunk(chunk);
    List<Node> nodes = new ArrayList<>(all.size());
    for (ClassifierInstance<?> ci : all) {
      if (ci instanceof Node) nodes.add((Node) ci);
    }
    return nodes;
  }

  public List<Node> deserializeToNodes(@Nonnull File file) throws IOException {
    return deserializeToNodes(Files.readAllBytes(file.toPath()));
  }

  public List<Node> deserializeToNodes(@Nonnull InputStream inputStream) throws IOException {
    return deserializeToNodes(readAllBytes(inputStream));
  }

  // ===========================================================================
  // Serialization internals
  // ===========================================================================

  private byte[] serializeToBytes(List<ClassifierInstance<?>> nodes) throws IOException {
    int sz = nodes.size();
    Intern intern = new Intern();

    // --- Pass 1: build type table and intern all strings ---
    List<TypeEntry> types = new ArrayList<>();
    Map<MetaPointer, Integer> typeByClassifierMP = new HashMap<>();
    int[] typeIndexPerNode = new int[sz];

    for (int i = 0; i < sz; i++) {
      ClassifierInstance<?> node = nodes.get(i);
      Classifier<?> classifier = node.getClassifier();
      MetaPointer classifierMP = MetaPointer.from(classifier);

      Integer ti = typeByClassifierMP.get(classifierMP);
      if (ti == null) {
        ti = types.size();
        TypeEntry te = new TypeEntry();
        te.classifierMPI = intern.metaPointer(classifierMP);

        List<Property> props = classifier.allProperties();
        te.propertyMPIs = new int[props.size()];
        for (int j = 0; j < props.size(); j++) {
          te.propertyMPIs[j] = intern.metaPointer(MetaPointer.from(props.get(j)));
        }
        List<Containment> conts = classifier.allContainments();
        te.containmentMPIs = new int[conts.size()];
        for (int j = 0; j < conts.size(); j++) {
          te.containmentMPIs[j] = intern.metaPointer(MetaPointer.from(conts.get(j)));
        }
        List<Reference> refs = classifier.allReferences();
        te.referenceMPIs = new int[refs.size()];
        for (int j = 0; j < refs.size(); j++) {
          te.referenceMPIs[j] = intern.metaPointer(MetaPointer.from(refs.get(j)));
        }

        typeByClassifierMP.put(classifierMP, ti);
        types.add(te);
      }
      typeIndexPerNode[i] = ti;

      // Intern all strings for this node
      intern.string(node.getID());
      ClassifierInstance<?> parent = node.getParent();
      if (parent != null) intern.string(parent.getID());

      List<Property> props = classifier.allProperties();
      for (int j = 0, n = props.size(); j < n; j++) {
        Property prop = props.get(j);
        Object val = node.getPropertyValue(prop);
        if (val != null) {
          intern.string(dataTypesValuesSerialization.serialize(prop.getType().getID(), val));
        }
      }
      List<Containment> conts = classifier.allContainments();
      for (int j = 0, n = conts.size(); j < n; j++) {
        List<? extends Node> children = node.getChildren(conts.get(j));
        for (int k = 0, m = children.size(); k < m; k++) {
          intern.string(children.get(k).getID());
        }
      }
      List<Reference> refs = classifier.allReferences();
      for (int j = 0, n = refs.size(); j < n; j++) {
        List<ReferenceValue> rvs = node.getReferenceValues(refs.get(j));
        for (int k = 0, m = rvs.size(); k < m; k++) {
          ReferenceValue rv = rvs.get(k);
          String refID = rv.getReferred() == null ? null : rv.getReferred().getID();
          if (builtinsReferenceDangling
              && ClassifierInstanceUtils.isBuiltinElement(rv.getReferred())) {
            refID = null;
          }
          intern.string(refID);
          intern.string(rv.getResolveInfo());
        }
      }
      List<AnnotationInstance> anns = node.getAnnotations();
      for (int j = 0, n = anns.size(); j < n; j++) {
        intern.string(anns.get(j).getID());
      }
    }

    // --- Pass 2: write binary ---
    ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(4096, sz * 48));
    DataOutputStream out = new DataOutputStream(baos);

    // Header
    out.write(MAGIC);
    out.writeByte(FORMAT_VERSION);
    writeUtf8(out, getLionWebVersion().getVersionString());

    // String table (index 0 = null, not written; entries start at 1)
    List<String> strings = intern.strings;
    out.writeInt(strings.size() - 1);
    for (int i = 1; i < strings.size(); i++) {
      writeUtf8(out, strings.get(i));
    }

    // Language table (index 0 = null sentinel, not written)
    List<LanguageVersion> langs = intern.languages;
    out.writeInt(langs.size() - 1);
    for (int i = 1; i < langs.size(); i++) {
      LanguageVersion lv = langs.get(i);
      out.writeInt(intern.string(lv.getKey()));
      out.writeInt(intern.string(lv.getVersion()));
    }

    // MetaPointer table
    List<MetaPointer> mps = intern.metaPointers;
    out.writeInt(mps.size());
    for (int i = 0; i < mps.size(); i++) {
      MetaPointer mp = mps.get(i);
      out.writeInt(intern.language(mp.getLanguageVersion()));
      out.writeInt(intern.string(mp.getKey()));
    }

    // Type table
    out.writeInt(types.size());
    for (int i = 0; i < types.size(); i++) {
      TypeEntry te = types.get(i);
      out.writeInt(te.classifierMPI);
      out.writeInt(te.propertyMPIs.length);
      for (int j = 0; j < te.propertyMPIs.length; j++) out.writeInt(te.propertyMPIs[j]);
      out.writeInt(te.containmentMPIs.length);
      for (int j = 0; j < te.containmentMPIs.length; j++) out.writeInt(te.containmentMPIs[j]);
      out.writeInt(te.referenceMPIs.length);
      for (int j = 0; j < te.referenceMPIs.length; j++) out.writeInt(te.referenceMPIs[j]);
    }

    // Node data
    out.writeInt(sz);
    for (int i = 0; i < sz; i++) {
      ClassifierInstance<?> node = nodes.get(i);
      Classifier<?> classifier = node.getClassifier();
      TypeEntry te = types.get(typeIndexPerNode[i]);

      out.writeInt(typeIndexPerNode[i]);
      out.writeInt(intern.string(node.getID()));
      ClassifierInstance<?> parent = node.getParent();
      out.writeInt(parent == null ? 0 : intern.string(parent.getID()));

      // Annotation count written first for exact pre-sizing on read
      List<AnnotationInstance> anns = node.getAnnotations();
      out.writeInt(anns.size());

      // Property values (one slot per property, in type order; 0 = null)
      List<Property> props = classifier.allProperties();
      for (int j = 0; j < te.propertyMPIs.length; j++) {
        Property prop = props.get(j);
        Object val = node.getPropertyValue(prop);
        String strVal =
            val == null ? null : dataTypesValuesSerialization.serialize(prop.getType().getID(), val);
        out.writeInt(intern.string(strVal));
      }

      // Containments (childCount + child IDs per containment)
      List<Containment> conts = classifier.allContainments();
      for (int j = 0; j < te.containmentMPIs.length; j++) {
        List<? extends Node> children = node.getChildren(conts.get(j));
        out.writeInt(children.size());
        for (int k = 0, m = children.size(); k < m; k++) {
          out.writeInt(intern.string(children.get(k).getID()));
        }
      }

      // References (entryCount + (siReferred, siResolveInfo) pairs per reference)
      List<Reference> refs = classifier.allReferences();
      for (int j = 0; j < te.referenceMPIs.length; j++) {
        List<ReferenceValue> rvs = node.getReferenceValues(refs.get(j));
        out.writeInt(rvs.size());
        for (int k = 0, m = rvs.size(); k < m; k++) {
          ReferenceValue rv = rvs.get(k);
          String refID = rv.getReferred() == null ? null : rv.getReferred().getID();
          if (builtinsReferenceDangling
              && ClassifierInstanceUtils.isBuiltinElement(rv.getReferred())) {
            refID = null;
          }
          out.writeInt(intern.string(refID));
          out.writeInt(intern.string(rv.getResolveInfo()));
        }
      }

      // Annotation IDs
      for (int j = 0, n = anns.size(); j < n; j++) {
        out.writeInt(intern.string(anns.get(j).getID()));
      }
    }

    out.flush();
    return baos.toByteArray();
  }

  private static void writeUtf8(DataOutputStream out, String s) throws IOException {
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    out.writeInt(bytes.length);
    out.write(bytes);
  }

  // ===========================================================================
  // Deserialization internals
  // ===========================================================================

  private SerializationChunk deserializeBinaryChunk(ByteBuffer buf) throws IOException {
    // Header
    byte[] magic = new byte[MAGIC.length];
    buf.get(magic);
    if (!Arrays.equals(magic, MAGIC)) {
      throw new DeserializationException("Not a LionBin stream: bad magic bytes");
    }
    byte fmtVersion = buf.get();
    if (fmtVersion != FORMAT_VERSION) {
      throw new DeserializationException("Unsupported LionBin format version: " + fmtVersion);
    }
    String serializationFormatVersion = readUtf8(buf);

    // String table
    int stringCount = buf.getInt();
    String[] strings = new String[stringCount + 1];
    strings[0] = null;
    for (int i = 1; i <= stringCount; i++) {
      strings[i] = readUtf8(buf);
    }

    // Language table
    int languageCount = buf.getInt();
    LanguageVersion[] languages = new LanguageVersion[languageCount + 1];
    languages[0] = null;
    for (int i = 1; i <= languageCount; i++) {
      String key = strings[buf.getInt()];
      String version = strings[buf.getInt()];
      languages[i] = LanguageVersion.of(key, version);
    }

    // MetaPointer table
    int mpCount = buf.getInt();
    MetaPointer[] metaPointers = new MetaPointer[mpCount];
    for (int i = 0; i < mpCount; i++) {
      LanguageVersion lv = languages[buf.getInt()];
      String key = strings[buf.getInt()];
      metaPointers[i] = MetaPointer.get(lv.getKey(), lv.getVersion(), key);
    }

    // Type table
    int typeCount = buf.getInt();
    TypeEntry[] typeEntries = new TypeEntry[typeCount];
    for (int i = 0; i < typeCount; i++) {
      TypeEntry te = new TypeEntry();
      te.classifierMPI = buf.getInt();
      int propCount = buf.getInt();
      te.propertyMPIs = new int[propCount];
      for (int j = 0; j < propCount; j++) te.propertyMPIs[j] = buf.getInt();
      int contCount = buf.getInt();
      te.containmentMPIs = new int[contCount];
      for (int j = 0; j < contCount; j++) te.containmentMPIs[j] = buf.getInt();
      int refCount = buf.getInt();
      te.referenceMPIs = new int[refCount];
      for (int j = 0; j < refCount; j++) te.referenceMPIs[j] = buf.getInt();
      typeEntries[i] = te;
    }

    // Node data
    int nodeCount = buf.getInt();
    SerializationChunk chunk = new SerializationChunk(nodeCount);
    chunk.setSerializationFormatVersion(serializationFormatVersion);
    for (LanguageVersion lv : languages) {
      if (lv != null) chunk.addLanguage(lv);
    }

    for (int ni = 0; ni < nodeCount; ni++) {
      TypeEntry te = typeEntries[buf.getInt()];
      int propCount = te.propertyMPIs.length;
      int contCount = te.containmentMPIs.length;
      int refCount = te.referenceMPIs.length;

      String id = strings[buf.getInt()];
      String parentID = strings[buf.getInt()];
      int annCount = buf.getInt();

      SerializedClassifierInstance sci =
          new SerializedClassifierInstance(propCount, contCount, refCount, annCount);
      sci.setID(id);
      sci.setParentNodeID(parentID);
      sci.setClassifier(metaPointers[te.classifierMPI]);

      // Properties
      for (int pi = 0; pi < propCount; pi++) {
        int siValue = buf.getInt();
        if (serializeEmptyFeatures || siValue != 0) {
          sci.unsafeAppendPropertyValue(
              SerializedPropertyValue.get(metaPointers[te.propertyMPIs[pi]], strings[siValue]));
        }
      }

      // Containments
      for (int ci = 0; ci < contCount; ci++) {
        int childrenCount = buf.getInt();
        List<String> children = new ArrayList<>(childrenCount);
        for (int chi = 0; chi < childrenCount; chi++) {
          int childIdx = buf.getInt();
          if (childIdx == 0) {
            throw new DeserializationException(
                "Unable to deserialize child identified by Null ID");
          }
          children.add(strings[childIdx]);
        }
        if (serializeEmptyFeatures || !children.isEmpty()) {
          sci.unsafeAppendContainmentValue(
              new SerializedContainmentValue(metaPointers[te.containmentMPIs[ci]], children));
        }
      }

      // References
      for (int ri = 0; ri < refCount; ri++) {
        int entryCount = buf.getInt();
        SerializedReferenceValue srv =
            new SerializedReferenceValue(metaPointers[te.referenceMPIs[ri]]);
        for (int vi = 0; vi < entryCount; vi++) {
          SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
          entry.setReference(strings[buf.getInt()]);
          entry.setResolveInfo(strings[buf.getInt()]);
          srv.addValue(entry);
        }
        if (serializeEmptyFeatures || !srv.getValue().isEmpty()) {
          sci.unsafeAppendReferenceValue(srv);
        }
      }

      // Annotations
      for (int ai = 0; ai < annCount; ai++) {
        sci.addAnnotation(strings[buf.getInt()]);
      }

      chunk.addClassifierInstance(sci);
    }

    return chunk;
  }

  private static String readUtf8(ByteBuffer buf) {
    int len = buf.getInt();
    byte[] bytes = new byte[len];
    buf.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static byte[] readAllBytes(InputStream in) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = in.read(buf)) != -1) {
      baos.write(buf, 0, n);
    }
    return baos.toByteArray();
  }

  // ===========================================================================
  // Node collection helper
  // ===========================================================================

  private static void collectNonProxyNoDup(
      ClassifierInstance<?> node, List<ClassifierInstance<?>> result, Set<String> seenIDs) {
    if (node instanceof ProxyNode) return;
    String id = node.getID();
    if (id != null && !seenIDs.add(id)) return;
    result.add(node);
    List<AnnotationInstance> anns = node.getAnnotations();
    for (int i = 0, n = anns.size(); i < n; i++) {
      collectNonProxyNoDup(anns.get(i), result, seenIDs);
    }
    Classifier<?> classifier = node.getClassifier();
    List<Containment> conts = classifier.allContainments();
    for (int i = 0, n = conts.size(); i < n; i++) {
      List<? extends Node> children = node.getChildren(conts.get(i));
      for (int j = 0, m = children.size(); j < m; j++) {
        collectNonProxyNoDup(children.get(j), result, seenIDs);
      }
    }
  }

  // ===========================================================================
  // Inner classes
  // ===========================================================================

  // Ordered type descriptor: captures classifier + feature metapointers for one classifier.
  private static final class TypeEntry {
    int classifierMPI;
    int[] propertyMPIs;
    int[] containmentMPIs;
    int[] referenceMPIs;
  }

  // Intern tables for strings, languages, and metapointers during serialization.
  private static final class Intern {
    final List<String> strings = new ArrayList<>();
    final Map<String, Integer> stringMap = new HashMap<>();
    final List<LanguageVersion> languages = new ArrayList<>();
    final Map<LanguageVersion, Integer> languageMap = new HashMap<>();
    final List<MetaPointer> metaPointers = new ArrayList<>();
    final Map<MetaPointer, Integer> metaPointerMap = new HashMap<>();

    Intern() {
      // Index 0 = null for strings and languages
      strings.add(null);
      stringMap.put(null, 0);
      languages.add(null);
      languageMap.put(null, 0);
    }

    int string(String s) {
      Integer idx = stringMap.get(s);
      if (idx != null) return idx;
      int i = strings.size();
      strings.add(s);
      stringMap.put(s, i);
      return i;
    }

    int language(LanguageVersion lv) {
      Integer idx = languageMap.get(lv);
      if (idx != null) return idx;
      string(lv.getKey());
      string(lv.getVersion());
      int i = languages.size();
      languages.add(lv);
      languageMap.put(lv, i);
      return i;
    }

    int metaPointer(MetaPointer mp) {
      Integer idx = metaPointerMap.get(mp);
      if (idx != null) return idx;
      language(mp.getLanguageVersion());
      string(mp.getKey());
      int i = metaPointers.size();
      metaPointers.add(mp);
      metaPointerMap.put(mp, i);
      return i;
    }
  }
}
