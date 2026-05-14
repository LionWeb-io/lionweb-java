package io.lionweb.serialization;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;
import io.lionweb.serialization.data.*;
import java.io.IOException;
import java.util.*;

/**
 * Serializes a {@link SerializationChunk} directly to the protobuf binary format without creating
 * intermediate protobuf message objects (PBChunk, PBNode, etc.).
 *
 * <p>Strategy:
 *
 * <ol>
 *   <li>Traverse all nodes once to populate string / language / meta-pointer intern tables and
 *       build a {@link SerializationPlan} containing every integer index and pre-computed body size
 *       needed for writing.
 *   <li>Build a {@link CachedTables} containing UTF-8 lengths, meta-pointer body sizes, and
 *       language body sizes derived from the now-complete intern tables.
 *   <li>Compute the exact total byte count without touching domain objects.
 *   <li>Allocate one {@code byte[]} and write from the plan — zero HashMap lookups, zero domain-
 *       object traversal.
 * </ol>
 *
 * <p>The output is byte-for-byte identical to standard protobuf serialization of a PBChunk message.
 */
final class DirectProtoBufSerializer {

  private DirectProtoBufSerializer() {}

  // ---- Inner types ----

  /** Mutable intern tables built during the first traversal. */
  static final class SerializeState {
    final List<String> strings;
    final Map<String, Integer> stringIndex;
    final List<LanguageVersion> languages;
    final Map<LanguageVersion, Integer> languageIndex;
    final List<MetaPointer> metaPointers;
    final Map<MetaPointer, Integer> metaPointerIndex;

    SerializeState(int estimatedStrings, int estimatedLanguages, int estimatedMetaPointers) {
      strings = new ArrayList<>(estimatedStrings + 1);
      stringIndex = new HashMap<>(hashCapacity(estimatedStrings + 1));
      languages = new ArrayList<>(estimatedLanguages + 1);
      languageIndex = new HashMap<>(hashCapacity(estimatedLanguages + 1));
      metaPointers = new ArrayList<>(estimatedMetaPointers);
      metaPointerIndex = new HashMap<>(hashCapacity(estimatedMetaPointers));
      strings.add(null);
      stringIndex.put(null, 0);
      languages.add(null);
      languageIndex.put(null, 0);
    }

    int stringIndexer(String s) {
      if (s == null) return 0;
      Integer idx = stringIndex.get(s);
      if (idx != null) return idx;
      int index = strings.size();
      strings.add(s);
      stringIndex.put(s, index);
      return index;
    }

    int languageIndexer(LanguageVersion lv) {
      if (lv == null) return 0;
      Integer idx = languageIndex.get(lv);
      if (idx != null) return idx;
      stringIndexer(lv.getKey());
      stringIndexer(lv.getVersion());
      int index = languages.size();
      languages.add(lv);
      languageIndex.put(lv, index);
      return index;
    }

    int metaPointerIndexer(MetaPointer mp) {
      if (mp == null) return 0;
      Integer idx = metaPointerIndex.get(mp);
      if (idx != null) return idx;
      languageIndexer(mp.getLanguageVersion());
      stringIndexer(mp.getKey());
      int index = metaPointers.size();
      metaPointers.add(mp);
      metaPointerIndex.put(mp, index);
      return index;
    }
  }

  /**
   * Resolved integer indexes and pre-computed body sizes for a single node. Built once; used by
   * both the size-computation phase and the write phase without any map lookups.
   */
  static final class NodePlan {
    int siId, mpiClassifier, siParent, bodySize;

    // Properties: arrays of length propCount (null when propCount == 0)
    int propCount;
    int[] propMpi, propSiValue, propBodySize;

    // Containments: arrays of length contCount (null when contCount == 0)
    int contCount;
    int[] contMpi, contBodySize, contPackedRawSize;
    int[][] contChildIndexes; // [contCount][childCount per containment]

    // References: arrays of length refCount (null when refCount == 0)
    int refCount;
    int[] refMpi, refBodySize;
    int[][] refSiResolveInfo, refSiReferred, refEntryBodySize;

    // Annotations (null when there are no annotations)
    int[] annotationIndexes;
    int annotPackedRawSize; // sum of varintSize(annotIdx) — used as the packed-field length prefix
  }

  /**
   * Derived numeric arrays computed once from the completed intern tables. Eliminates HashMap
   * lookups in both the size-computation and write phases for the chunk header fields.
   */
  static final class CachedTables {
    // Parallel to state.strings: [i] = utf8ByteCount(strings[i]) for i >= 1
    final int[] stringUtf8Lengths;

    // Parallel to state.metaPointers (0-indexed)
    final int[] mpBodySizes;
    final int[] mpLiLanguage; // language index for each meta-pointer
    final int[] mpSiKey;      // key string index for each meta-pointer

    // Parallel to state.languages (1-indexed; [i] corresponds to languages[i+1])
    final int[] langBodySizes;
    final int[] langSiKey;
    final int[] langSiVersion;

    CachedTables(SerializeState state) {
      int stringCount = state.strings.size() - 1;
      stringUtf8Lengths = new int[stringCount + 1];
      for (int i = 1; i <= stringCount; i++) {
        stringUtf8Lengths[i] = utf8ByteCount(state.strings.get(i));
      }

      int mpCount = state.metaPointers.size();
      mpBodySizes = new int[mpCount];
      mpLiLanguage = new int[mpCount];
      mpSiKey = new int[mpCount];
      for (int i = 0; i < mpCount; i++) {
        MetaPointer mp = state.metaPointers.get(i);
        // At this point all entries are guaranteed to be in the maps; use get() not getOrDefault()
        int li = mp.getLanguageVersion() != null
            ? requireIndex(state.languageIndex, mp.getLanguageVersion(), "LanguageVersion") : 0;
        int sk = mp.getKey() != null
            ? requireIndex(state.stringIndex, mp.getKey(), "string") : 0;
        mpLiLanguage[i] = li;
        mpSiKey[i] = sk;
        mpBodySizes[i] = uint32FieldSize(li) + uint32FieldSize(sk);
      }

      int langCount = state.languages.size() - 1;
      langBodySizes = new int[langCount];
      langSiKey = new int[langCount];
      langSiVersion = new int[langCount];
      for (int i = 0; i < langCount; i++) {
        LanguageVersion lv = state.languages.get(i + 1);
        int sk = lv.getKey() != null
            ? requireIndex(state.stringIndex, lv.getKey(), "string") : 0;
        int sv = lv.getVersion() != null
            ? requireIndex(state.stringIndex, lv.getVersion(), "string") : 0;
        langSiKey[i] = sk;
        langSiVersion[i] = sv;
        langBodySizes[i] = uint32FieldSize(sk) + uint32FieldSize(sv);
      }
    }

    private static int requireIndex(Map<?, Integer> map, Object key, String kind) {
      Integer idx = map.get(key);
      if (idx == null) {
        throw new IllegalStateException("Intern table missing " + kind + ": " + key);
      }
      return idx;
    }
  }

  // ---- Public entry point ----

  static byte[] serialize(SerializationChunk chunk, boolean serializeEmptyFeatures) {
    List<SerializedClassifierInstance> instances = chunk.getClassifierInstances();
    int nodeCount = instances.size();

    // Heuristic pre-sizing to avoid resize overhead on typical models
    int estimatedStrings = Math.max(16, nodeCount * 4);
    int estimatedMetaPointers = Math.max(16, nodeCount / 2);

    SerializeState state = new SerializeState(estimatedStrings, 8, estimatedMetaPointers);

    // Single traversal: populate intern tables AND build per-node plans
    NodePlan[] plans = new NodePlan[nodeCount];
    for (int i = 0; i < nodeCount; i++) {
      plans[i] = buildNodePlan(state, instances.get(i), serializeEmptyFeatures);
    }

    CachedTables cached = new CachedTables(state);
    int totalSize = computeChunkSize(chunk, cached, plans);

    byte[] result = new byte[totalSize];
    CodedOutputStream cos = CodedOutputStream.newInstance(result);
    try {
      writeChunk(cos, chunk, state, cached, plans);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected IOException writing to byte array", e);
    }
    return result;
  }

  // ---- Plan building ----

  private static NodePlan buildNodePlan(
      SerializeState state, SerializedClassifierInstance n, boolean serializeEmptyFeatures) {
    NodePlan plan = new NodePlan();

    // field 1: si_id
    plan.siId = n.getID() != null ? state.stringIndexer(n.getID()) : 0;

    // field 7: si_parent — indexed here (between id and classifier) to match legacy table ordering
    plan.siParent = n.getParentNodeID() != null ? state.stringIndexer(n.getParentNodeID()) : 0;

    // field 2: mpi_classifier
    plan.mpiClassifier = n.getClassifier() != null ? state.metaPointerIndexer(n.getClassifier()) : 0;

    int bodySize = uint32FieldSize(plan.siId) + uint32FieldSize(plan.mpiClassifier);

    // field 3: properties
    List<SerializedPropertyValue> propList = n.getProperties();
    int propCount = 0;
    for (SerializedPropertyValue p : propList) {
      if (serializeEmptyFeatures || p.getValue() != null) propCount++;
    }
    plan.propCount = propCount;
    if (propCount > 0) {
      plan.propMpi = new int[propCount];
      plan.propSiValue = new int[propCount];
      plan.propBodySize = new int[propCount];
      int pi = 0;
      for (SerializedPropertyValue p : propList) {
        String value = p.getValue();
        if (serializeEmptyFeatures || value != null) {
          // Match legacy SerializeHelper: value string indexed BEFORE meta-pointer
          int siValue = state.stringIndexer(value);
          int mpi = p.getMetaPointer() != null ? state.metaPointerIndexer(p.getMetaPointer()) : 0;
          int pb = uint32FieldSize(mpi) + uint32FieldSize(siValue);
          plan.propMpi[pi] = mpi;
          plan.propSiValue[pi] = siValue;
          plan.propBodySize[pi] = pb;
          bodySize += 1 + varintSize(pb) + pb;
          pi++;
        }
      }
    }

    // field 4: containments
    List<SerializedContainmentValue> contList = n.getContainments();
    int contCount = 0;
    for (SerializedContainmentValue c : contList) {
      if (serializeEmptyFeatures || !c.getChildrenIds().isEmpty()) contCount++;
    }
    plan.contCount = contCount;
    if (contCount > 0) {
      plan.contMpi = new int[contCount];
      plan.contBodySize = new int[contCount];
      plan.contPackedRawSize = new int[contCount];
      plan.contChildIndexes = new int[contCount][];
      int ci = 0;
      for (SerializedContainmentValue c : contList) {
        List<String> children = c.getChildrenIds();
        if (serializeEmptyFeatures || !children.isEmpty()) {
          int mpi = c.getMetaPointer() != null ? state.metaPointerIndexer(c.getMetaPointer()) : 0;
          int nChildren = children.size();
          int[] childIdxs = new int[nChildren];
          int packed = 0;
          for (int k = 0; k < nChildren; k++) {
            childIdxs[k] = state.stringIndexer(children.get(k));
            packed += varintSize(childIdxs[k]);
          }
          int packedFieldSize = nChildren == 0 ? 0 : (1 + varintSize(packed) + packed);
          int cb = uint32FieldSize(mpi) + packedFieldSize;
          plan.contMpi[ci] = mpi;
          plan.contBodySize[ci] = cb;
          plan.contPackedRawSize[ci] = packed; // raw byte count of the packed content
          plan.contChildIndexes[ci] = childIdxs;
          bodySize += 1 + varintSize(cb) + cb;
          ci++;
        }
      }
    }

    // field 5: references
    List<SerializedReferenceValue> refList = n.getReferences();
    int refCount = 0;
    for (SerializedReferenceValue r : refList) {
      if (serializeEmptyFeatures || !r.getValue().isEmpty()) refCount++;
    }
    plan.refCount = refCount;
    if (refCount > 0) {
      plan.refMpi = new int[refCount];
      plan.refBodySize = new int[refCount];
      plan.refSiResolveInfo = new int[refCount][];
      plan.refSiReferred = new int[refCount][];
      plan.refEntryBodySize = new int[refCount][];
      int ri = 0;
      for (SerializedReferenceValue r : refList) {
        List<SerializedReferenceValue.Entry> entries = r.getValue();
        if (serializeEmptyFeatures || !entries.isEmpty()) {
          int mpi = r.getMetaPointer() != null ? state.metaPointerIndexer(r.getMetaPointer()) : 0;
          int nEntries = entries.size();
          int[] siResolveInfo = new int[nEntries];
          int[] siReferred = new int[nEntries];
          int[] entryBodies = new int[nEntries];
          int refBody = uint32FieldSize(mpi);
          for (int k = 0; k < nEntries; k++) {
            SerializedReferenceValue.Entry entry = entries.get(k);
            // Match legacy: referred indexed before resolveInfo
            int sr = entry.getReference() != null ? state.stringIndexer(entry.getReference()) : 0;
            int sri = entry.getResolveInfo() != null ? state.stringIndexer(entry.getResolveInfo()) : 0;
            int rvBody = uint32FieldSize(sri) + uint32FieldSize(sr);
            siResolveInfo[k] = sri;
            siReferred[k] = sr;
            entryBodies[k] = rvBody;
            refBody += 1 + varintSize(rvBody) + rvBody;
          }
          plan.refMpi[ri] = mpi;
          plan.refBodySize[ri] = refBody;
          plan.refSiResolveInfo[ri] = siResolveInfo;
          plan.refSiReferred[ri] = siReferred;
          plan.refEntryBodySize[ri] = entryBodies;
          bodySize += 1 + varintSize(refBody) + refBody;
          ri++;
        }
      }
    }

    // field 6: annotations (packed repeated uint32)
    List<String> annotations = n.getAnnotations();
    if (!annotations.isEmpty()) {
      int nAnn = annotations.size();
      int[] annIdxs = new int[nAnn];
      int packed = 0;
      for (int k = 0; k < nAnn; k++) {
        // null annotation IDs map to index 0 (preserved from legacy getOrDefault behavior)
        annIdxs[k] = state.stringIndexer(annotations.get(k));
        packed += varintSize(annIdxs[k]);
      }
      plan.annotationIndexes = annIdxs;
      plan.annotPackedRawSize = packed;
      bodySize += 1 + varintSize(packed) + packed;
    }

    // field 7: si_parent size (index already set above for table-ordering purposes)
    bodySize += uint32FieldSize(plan.siParent);

    plan.bodySize = bodySize;
    return plan;
  }

  // ---- Size computation ----

  /** Computes the total serialized byte count from cached tables and plans — no domain objects. */
  private static int computeChunkSize(
      SerializationChunk chunk, CachedTables cached, NodePlan[] plans) {
    int size = 0;

    // field 1: serialization_format_version (not in the intern table; computed ad-hoc)
    String version = chunk.getSerializationFormatVersion();
    if (version != null && !version.isEmpty()) {
      int utf8Len = utf8ByteCount(version);
      size += 1 + varintSize(utf8Len) + utf8Len;
    }

    // field 2: interned_strings
    for (int i = 1; i < cached.stringUtf8Lengths.length; i++) {
      int utf8Len = cached.stringUtf8Lengths[i];
      size += 1 + varintSize(utf8Len) + utf8Len;
    }

    // field 3: interned_meta_pointers
    for (int bodySize : cached.mpBodySizes) {
      size += 1 + varintSize(bodySize) + bodySize;
    }

    // field 4: interned_languages
    for (int bodySize : cached.langBodySizes) {
      size += 1 + varintSize(bodySize) + bodySize;
    }

    // field 5: nodes
    for (NodePlan plan : plans) {
      size += 1 + varintSize(plan.bodySize) + plan.bodySize;
    }

    return size;
  }

  // ---- Write phase ----

  /**
   * Writes the entire chunk. No HashMap lookups; no domain-object traversal. All data comes from
   * {@code cached} arrays and {@code plans}.
   */
  private static void writeChunk(
      CodedOutputStream cos,
      SerializationChunk chunk,
      SerializeState state,
      CachedTables cached,
      NodePlan[] plans)
      throws IOException {

    // field 1: serialization_format_version
    String version = chunk.getSerializationFormatVersion();
    if (version != null && !version.isEmpty()) {
      cos.writeString(1, version);
    }

    // field 2: interned_strings
    List<String> strings = state.strings;
    for (int i = 1; i < strings.size(); i++) {
      cos.writeString(2, strings.get(i));
    }

    // field 3: interned_meta_pointers — use cached integer arrays, no map lookups
    int mpCount = cached.mpBodySizes.length;
    for (int i = 0; i < mpCount; i++) {
      cos.writeTag(3, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(cached.mpBodySizes[i]);
      int li = cached.mpLiLanguage[i];
      int sk = cached.mpSiKey[i];
      if (li != 0) cos.writeUInt32(1, li);
      if (sk != 0) cos.writeUInt32(2, sk);
    }

    // field 4: interned_languages — use cached integer arrays, no map lookups
    int langCount = cached.langBodySizes.length;
    for (int i = 0; i < langCount; i++) {
      cos.writeTag(4, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(cached.langBodySizes[i]);
      int sk = cached.langSiKey[i];
      int sv = cached.langSiVersion[i];
      if (sk != 0) cos.writeUInt32(1, sk);
      if (sv != 0) cos.writeUInt32(2, sv);
    }

    // field 5: nodes — written entirely from plans, zero map lookups
    for (NodePlan plan : plans) {
      cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.bodySize);
      writeNodeBody(cos, plan);
    }
  }

  private static void writeNodeBody(CodedOutputStream cos, NodePlan plan) throws IOException {
    // field 1: si_id
    if (plan.siId != 0) cos.writeUInt32(1, plan.siId);

    // field 2: mpi_classifier
    if (plan.mpiClassifier != 0) cos.writeUInt32(2, plan.mpiClassifier);

    // field 3: properties
    for (int i = 0; i < plan.propCount; i++) {
      int bodySize = plan.propBodySize[i];
      cos.writeTag(3, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(bodySize);
      int mpi = plan.propMpi[i];
      int sv = plan.propSiValue[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      if (sv != 0) cos.writeUInt32(2, sv);
    }

    // field 4: containments
    for (int i = 0; i < plan.contCount; i++) {
      cos.writeTag(4, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.contBodySize[i]);
      int mpi = plan.contMpi[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      int[] childIdxs = plan.contChildIndexes[i];
      if (childIdxs.length > 0) {
        cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(plan.contPackedRawSize[i]);
        for (int childIdx : childIdxs) cos.writeUInt32NoTag(childIdx);
      }
    }

    // field 5: references
    for (int i = 0; i < plan.refCount; i++) {
      cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.refBodySize[i]);
      int mpi = plan.refMpi[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      int[] siResolveInfo = plan.refSiResolveInfo[i];
      int[] siReferred = plan.refSiReferred[i];
      int[] entryBodies = plan.refEntryBodySize[i];
      for (int k = 0; k < siReferred.length; k++) {
        int rvBody = entryBodies[k];
        cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(rvBody);
        int sri = siResolveInfo[k];
        int sr = siReferred[k];
        if (sri != 0) cos.writeUInt32(1, sri);
        if (sr != 0) cos.writeUInt32(2, sr);
      }
    }

    // field 6: si_annotations (packed repeated uint32)
    if (plan.annotationIndexes != null) {
      cos.writeTag(6, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.annotPackedRawSize);
      for (int annIdx : plan.annotationIndexes) cos.writeUInt32NoTag(annIdx);
    }

    // field 7: si_parent
    if (plan.siParent != 0) cos.writeUInt32(7, plan.siParent);
  }

  // ---- Helpers ----

  /** UTF-8 byte count for {@code s} without allocating a byte array. */
  static int utf8ByteCount(String s) {
    int count = 0;
    for (int i = 0, len = s.length(); i < len; i++) {
      char c = s.charAt(i);
      if (c < 0x80) {
        count += 1;
      } else if (c < 0x800) {
        count += 2;
      } else if (Character.isHighSurrogate(c)) {
        count += 4; // valid surrogate pair → 4 UTF-8 bytes; consume low surrogate
        i++;
      } else {
        count += 3;
      }
    }
    return count;
  }

  private static int varintSize(int v) {
    return CodedOutputStream.computeUInt32SizeNoTag(v);
  }

  private static int uint32FieldSize(int value) {
    return value == 0 ? 0 : 1 + varintSize(value);
  }

  private static int hashCapacity(int expectedSize) {
    return Math.max(16, (int) (expectedSize / 0.75f) + 1);
  }
}
