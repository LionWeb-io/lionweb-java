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
 * <p>The two-pass strategy:
 *
 * <ol>
 *   <li>Visit every node to populate the interned string / language / meta-pointer index tables
 *       <em>and</em> compute each node's serialized body size in the same traversal.
 *   <li>Compute the exact total byte count from the index tables plus the pre-computed node sizes.
 *   <li>Allocate a single {@code byte[]} and write all fields in one shot via {@link
 *       CodedOutputStream}.
 * </ol>
 *
 * <p>The output is byte-compatible with the standard protobuf serialization of a PBChunk message.
 */
final class DirectProtoBufSerializer {

  private DirectProtoBufSerializer() {}

  // ---- Public entry point ----

  /**
   * Serialize a {@link SerializationChunk} directly to protobuf bytes.
   *
   * @param chunk the chunk to serialize
   * @param serializeEmptyFeatures when {@code false}, properties with null values and
   *     containments/references with empty value lists are omitted
   * @return the protobuf-encoded bytes
   */
  static byte[] serialize(SerializationChunk chunk, boolean serializeEmptyFeatures) {
    SerializeState state = new SerializeState();
    List<SerializedClassifierInstance> instances = chunk.getClassifierInstances();

    // Single pass: build index tables AND compute each node's body size simultaneously.
    int[] nodeBodySizes = new int[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      nodeBodySizes[i] = visitAndComputeBodySize(state, instances.get(i), serializeEmptyFeatures);
    }

    int totalSize = computeChunkSize(chunk, state, nodeBodySizes);
    byte[] result = new byte[totalSize];
    CodedOutputStream cos = CodedOutputStream.newInstance(result);
    try {
      writeChunk(cos, chunk, state, instances, nodeBodySizes, serializeEmptyFeatures);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected IOException writing to byte array", e);
    }
    return result;
  }

  // ---- Index tables ----

  /** Mutable state built during Phase 1 (visiting nodes). */
  static final class SerializeState {
    final List<String> strings = new ArrayList<>();
    final Map<String, Integer> stringIndex = new HashMap<>();
    final List<LanguageVersion> languages = new ArrayList<>();
    final Map<LanguageVersion, Integer> languageIndex = new HashMap<>();
    final List<MetaPointer> metaPointers = new ArrayList<>();
    final Map<MetaPointer, Integer> metaPointerIndex = new HashMap<>();

    SerializeState() {
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
   * Visits {@code n} to populate index tables AND returns the node's serialized body size in a
   * single traversal, eliminating a separate size-computation pass.
   *
   * <p>Field indexing order matches the legacy {@code SerializeHelper.serializeNode} exactly so
   * that string/language/meta-pointer table indices — and therefore the output bytes — are
   * identical.
   */
  private static int visitAndComputeBodySize(
      SerializeState state, SerializedClassifierInstance n, boolean serializeEmptyFeatures) {
    int size = 0;

    // field 1: si_id
    if (n.getID() != null) size += uint32FieldSize(state.stringIndexer(n.getID()));

    // field 7: si_parent — indexed here to match legacy ordering; size contributed below
    int siParent = n.getParentNodeID() != null ? state.stringIndexer(n.getParentNodeID()) : 0;

    // field 2: mpi_classifier
    if (n.getClassifier() != null)
      size += uint32FieldSize(state.metaPointerIndexer(n.getClassifier()));

    // field 3: properties
    for (SerializedPropertyValue p : n.getProperties()) {
      String value = p.getValue();
      if (serializeEmptyFeatures || value != null) {
        // Match legacy SerializeHelper: value string indexed BEFORE meta-pointer
        int siValue = state.stringIndexer(value);
        int mpiIndex =
            p.getMetaPointer() != null ? state.metaPointerIndexer(p.getMetaPointer()) : 0;
        int bodySize = uint32FieldSize(mpiIndex) + uint32FieldSize(siValue);
        size += 1 + varintSize(bodySize) + bodySize;
      }
    }

    // field 4: containments
    for (SerializedContainmentValue c : n.getContainments()) {
      List<String> children = c.getChildrenIds();
      if (serializeEmptyFeatures || !children.isEmpty()) {
        int mpiIndex =
            c.getMetaPointer() != null ? state.metaPointerIndexer(c.getMetaPointer()) : 0;
        int packed = 0;
        for (String childId : children) packed += varintSize(state.stringIndexer(childId));
        int packedFieldSize = children.isEmpty() ? 0 : (1 + varintSize(packed) + packed);
        int bodySize = uint32FieldSize(mpiIndex) + packedFieldSize;
        size += 1 + varintSize(bodySize) + bodySize;
      }
    }

    // field 5: references
    for (SerializedReferenceValue r : n.getReferences()) {
      List<SerializedReferenceValue.Entry> entries = r.getValue();
      if (serializeEmptyFeatures || !entries.isEmpty()) {
        int mpiIndex =
            r.getMetaPointer() != null ? state.metaPointerIndexer(r.getMetaPointer()) : 0;
        int refBodySize = uint32FieldSize(mpiIndex);
        for (SerializedReferenceValue.Entry entry : entries) {
          // Match legacy: referred indexed before resolveInfo
          int siReferred =
              entry.getReference() != null ? state.stringIndexer(entry.getReference()) : 0;
          int siResolveInfo =
              entry.getResolveInfo() != null ? state.stringIndexer(entry.getResolveInfo()) : 0;
          int rvBody = referenceValueBodySize(siResolveInfo, siReferred);
          refBodySize += 1 + varintSize(rvBody) + rvBody;
        }
        size += 1 + varintSize(refBodySize) + refBodySize;
      }
    }

    // field 6: si_annotations (packed)
    List<String> annotations = n.getAnnotations();
    if (!annotations.isEmpty()) {
      int packed = 0;
      for (String annId : annotations) packed += varintSize(state.stringIndexer(annId));
      size += 1 + varintSize(packed) + packed;
    }

    // field 7: si_parent size (index already computed above to preserve table-population order)
    size += uint32FieldSize(siParent);

    return size;
  }

  // ---- Size helpers ----

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
        count += 4; // surrogate pair → 4 UTF-8 bytes
        i++; // consume low surrogate
      } else {
        count += 3;
      }
    }
    return count;
  }

  /** Varint encoding size of an unsigned 32-bit value. */
  private static int varintSize(int v) {
    return CodedOutputStream.computeUInt32SizeNoTag(v);
  }

  /**
   * Wire size of a uint32 field (tag=1 byte + varint). Returns 0 when {@code value==0} because
   * proto3 singular fields at their default value are not emitted.
   */
  private static int uint32FieldSize(int value) {
    return value == 0 ? 0 : 1 + varintSize(value);
  }

  /** Wire size of a string LEN field: tag(1) + varint(utf8Len) + utf8Len. */
  private static int stringFieldSize(String s) {
    int utf8Len = utf8ByteCount(s);
    return 1 + varintSize(utf8Len) + utf8Len;
  }

  private static int metaPointerBodySize(SerializeState state, MetaPointer mp) {
    int liLanguage = state.languageIndex.getOrDefault(mp.getLanguageVersion(), 0);
    int siKey = mp.getKey() != null ? state.stringIndex.getOrDefault(mp.getKey(), 0) : 0;
    return uint32FieldSize(liLanguage) + uint32FieldSize(siKey);
  }

  private static int languageBodySize(SerializeState state, LanguageVersion lv) {
    int siKey = lv.getKey() != null ? state.stringIndex.getOrDefault(lv.getKey(), 0) : 0;
    int siVersion =
        lv.getVersion() != null ? state.stringIndex.getOrDefault(lv.getVersion(), 0) : 0;
    return uint32FieldSize(siKey) + uint32FieldSize(siVersion);
  }

  private static int referenceValueBodySize(int siResolveInfo, int siReferred) {
    return uint32FieldSize(siResolveInfo) + uint32FieldSize(siReferred);
  }

  /** Computes the total chunk byte size from pre-computed node body sizes. */
  private static int computeChunkSize(
      SerializationChunk chunk, SerializeState state, int[] nodeBodySizes) {
    int size = 0;

    // field 1: serialization_format_version
    String version = chunk.getSerializationFormatVersion();
    if (version != null && !version.isEmpty()) {
      size += stringFieldSize(version);
    }

    // field 2: interned_strings (index 0 = null, skip it)
    for (int i = 1, n = state.strings.size(); i < n; i++) {
      size += stringFieldSize(state.strings.get(i));
    }

    // field 3: interned_meta_pointers
    for (MetaPointer mp : state.metaPointers) {
      int bodySize = metaPointerBodySize(state, mp);
      size += 1 + varintSize(bodySize) + bodySize;
    }

    // field 4: interned_languages (index 0 = null, skip it)
    for (int i = 1, n = state.languages.size(); i < n; i++) {
      LanguageVersion lv = state.languages.get(i);
      int bodySize = languageBodySize(state, lv);
      size += 1 + varintSize(bodySize) + bodySize;
    }

    // field 5: nodes — sizes already computed by visitAndComputeBodySize
    for (int bodySize : nodeBodySizes) {
      size += 1 + varintSize(bodySize) + bodySize;
    }

    return size;
  }

  // ---- Write phase (Phase 3) ----

  private static void writeChunk(
      CodedOutputStream cos,
      SerializationChunk chunk,
      SerializeState state,
      List<SerializedClassifierInstance> instances,
      int[] nodeBodySizes,
      boolean serializeEmptyFeatures)
      throws IOException {

    // field 1: serialization_format_version
    String version = chunk.getSerializationFormatVersion();
    if (version != null && !version.isEmpty()) {
      cos.writeString(1, version);
    }

    // field 2: interned_strings
    for (int i = 1, n = state.strings.size(); i < n; i++) {
      cos.writeString(2, state.strings.get(i));
    }

    // field 3: interned_meta_pointers
    for (MetaPointer mp : state.metaPointers) {
      int bodySize = metaPointerBodySize(state, mp);
      cos.writeTag(3, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(bodySize);
      int liLanguage = state.languageIndex.getOrDefault(mp.getLanguageVersion(), 0);
      int siKey = mp.getKey() != null ? state.stringIndex.getOrDefault(mp.getKey(), 0) : 0;
      if (liLanguage != 0) cos.writeUInt32(1, liLanguage);
      if (siKey != 0) cos.writeUInt32(2, siKey);
    }

    // field 4: interned_languages
    for (int i = 1, n = state.languages.size(); i < n; i++) {
      LanguageVersion lv = state.languages.get(i);
      int bodySize = languageBodySize(state, lv);
      cos.writeTag(4, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(bodySize);
      int siKey = lv.getKey() != null ? state.stringIndex.getOrDefault(lv.getKey(), 0) : 0;
      int siVersion =
          lv.getVersion() != null ? state.stringIndex.getOrDefault(lv.getVersion(), 0) : 0;
      if (siKey != 0) cos.writeUInt32(1, siKey);
      if (siVersion != 0) cos.writeUInt32(2, siVersion);
    }

    // field 5: nodes — use pre-computed body sizes
    for (int i = 0, n = instances.size(); i < n; i++) {
      cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(nodeBodySizes[i]);
      writeNodeBody(cos, instances.get(i), state, serializeEmptyFeatures);
    }
  }

  private static void writeNodeBody(
      CodedOutputStream cos,
      SerializedClassifierInstance n,
      SerializeState state,
      boolean serializeEmptyFeatures)
      throws IOException {

    // field 1: si_id
    int siId = n.getID() != null ? state.stringIndex.getOrDefault(n.getID(), 0) : 0;
    if (siId != 0) cos.writeUInt32(1, siId);

    // field 2: mpi_classifier
    int mpiClassifier =
        n.getClassifier() != null ? state.metaPointerIndex.getOrDefault(n.getClassifier(), 0) : 0;
    if (mpiClassifier != 0) cos.writeUInt32(2, mpiClassifier);

    // field 3: properties
    for (SerializedPropertyValue p : n.getProperties()) {
      String value = p.getValue();
      if (serializeEmptyFeatures || value != null) {
        int mpiIndex =
            p.getMetaPointer() != null
                ? state.metaPointerIndex.getOrDefault(p.getMetaPointer(), 0)
                : 0;
        int siValue = value != null ? state.stringIndex.getOrDefault(value, 0) : 0;
        int bodySize = uint32FieldSize(mpiIndex) + uint32FieldSize(siValue);
        cos.writeTag(3, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(bodySize);
        if (mpiIndex != 0) cos.writeUInt32(1, mpiIndex);
        if (siValue != 0) cos.writeUInt32(2, siValue);
      }
    }

    // field 4: containments — compute 'packed' once for both the length prefix and the write loop
    for (SerializedContainmentValue c : n.getContainments()) {
      List<String> children = c.getChildrenIds();
      if (serializeEmptyFeatures || !children.isEmpty()) {
        int mpiIndex =
            c.getMetaPointer() != null
                ? state.metaPointerIndex.getOrDefault(c.getMetaPointer(), 0)
                : 0;
        int packed = 0;
        for (String childId : children) packed += varintSize(state.stringIndex.get(childId));
        int packedFieldSize = children.isEmpty() ? 0 : (1 + varintSize(packed) + packed);
        int bodySize = uint32FieldSize(mpiIndex) + packedFieldSize;
        cos.writeTag(4, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(bodySize);
        if (mpiIndex != 0) cos.writeUInt32(1, mpiIndex);
        if (!children.isEmpty()) {
          cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
          cos.writeUInt32NoTag(packed);
          for (String childId : children) cos.writeUInt32NoTag(state.stringIndex.get(childId));
        }
      }
    }

    // field 5: references
    for (SerializedReferenceValue r : n.getReferences()) {
      List<SerializedReferenceValue.Entry> entries = r.getValue();
      if (serializeEmptyFeatures || !entries.isEmpty()) {
        int mpiIndex =
            r.getMetaPointer() != null
                ? state.metaPointerIndex.getOrDefault(r.getMetaPointer(), 0)
                : 0;
        int refBodySize = uint32FieldSize(mpiIndex);
        for (SerializedReferenceValue.Entry entry : entries) {
          int siReferred =
              entry.getReference() != null
                  ? state.stringIndex.getOrDefault(entry.getReference(), 0)
                  : 0;
          int siResolveInfo =
              entry.getResolveInfo() != null
                  ? state.stringIndex.getOrDefault(entry.getResolveInfo(), 0)
                  : 0;
          int rvBody = referenceValueBodySize(siResolveInfo, siReferred);
          refBodySize += 1 + varintSize(rvBody) + rvBody;
        }
        cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(refBodySize);
        if (mpiIndex != 0) cos.writeUInt32(1, mpiIndex);
        for (SerializedReferenceValue.Entry entry : entries) {
          int siReferred =
              entry.getReference() != null
                  ? state.stringIndex.getOrDefault(entry.getReference(), 0)
                  : 0;
          int siResolveInfo =
              entry.getResolveInfo() != null
                  ? state.stringIndex.getOrDefault(entry.getResolveInfo(), 0)
                  : 0;
          int rvBody = referenceValueBodySize(siResolveInfo, siReferred);
          cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
          cos.writeUInt32NoTag(rvBody);
          if (siResolveInfo != 0) cos.writeUInt32(1, siResolveInfo);
          if (siReferred != 0) cos.writeUInt32(2, siReferred);
        }
      }
    }

    // field 6: si_annotations (packed repeated uint32)
    List<String> annotations = n.getAnnotations();
    if (!annotations.isEmpty()) {
      int packed = 0;
      for (String annId : annotations)
        packed += varintSize(state.stringIndex.getOrDefault(annId, 0));
      cos.writeTag(6, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(packed);
      for (String annId : annotations)
        cos.writeUInt32NoTag(state.stringIndex.getOrDefault(annId, 0));
    }

    // field 7: si_parent
    int siParent =
        n.getParentNodeID() != null ? state.stringIndex.getOrDefault(n.getParentNodeID(), 0) : 0;
    if (siParent != 0) cos.writeUInt32(7, siParent);
  }
}
