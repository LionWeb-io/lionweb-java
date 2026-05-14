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
   * Flat serialization plan for all nodes. Replaces the per-node {@code NodePlan[]} object graph
   * with a single struct of parallel int[] arrays for improved cache locality and reduced GC
   * pressure.
   *
   * <p>Node-level data (one entry per node) is stored in parallel arrays indexed by node index.
   * Feature data (properties, containments, references, annotations) is stored in flat arrays; each
   * node has a start index and count that slice into the relevant flat arrays.
   */
  static final class SerializationPlan {
    int nodeCount;

    // Per-node arrays (length == nodeCount)
    int[] nodeSiId;
    int[] nodeMpiClassifier;
    int[] nodeSiParent;
    int[] nodeBodySize;

    int[] nodePropStart;
    int[] nodePropCount;

    int[] nodeContStart;
    int[] nodeContCount;

    int[] nodeRefStart;
    int[] nodeRefCount;

    int[] nodeAnnotStart;
    int[] nodeAnnotCount;
    int[] nodeAnnotPackedRawSize;

    // Flat property arrays
    int[] propMpi;
    int[] propSiValue;
    int[] propBodySize;

    // Flat containment arrays
    int[] contMpi;
    int[] contBodySize;
    int[] contPackedRawSize;
    int[] contChildStart;
    int[] contChildCount;

    // Flat children array
    int[] childIndexes;

    // Flat reference arrays
    int[] refMpi;
    int[] refBodySize;
    int[] refEntryStart;
    int[] refEntryCount;

    // Flat reference entry arrays
    int[] refEntrySiResolveInfo;
    int[] refEntrySiReferred;
    int[] refEntryBodySize;

    // Flat annotation array
    int[] annotIndexes;
  }

  /**
   * Simple growable int[] builder used to construct flat arrays in one pass.
   *
   * <p>Callers that own the appender and use explicit start+count bounds when reading can call
   * {@link #rawBuffer()} instead of {@link #toArray()} to avoid an {@code Arrays.copyOf} when
   * assigning to the plan — the extra trailing zeroes are never read.
   */
  private static final class IntAppender {
    int[] data;
    int size;

    IntAppender(int cap) {
      data = new int[Math.max(cap, 4)];
    }

    void add(int v) {
      if (size == data.length) data = Arrays.copyOf(data, data.length * 2);
      data[size++] = v;
    }

    int size() {
      return size;
    }

    /** Returns the internal backing array (may be larger than {@link #size()}). */
    int[] rawBuffer() {
      return data;
    }

    int[] toArray() {
      return Arrays.copyOf(data, size);
    }
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
    final int[] mpSiKey; // key string index for each meta-pointer

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
        int li =
            mp.getLanguageVersion() != null
                ? requireIndex(state.languageIndex, mp.getLanguageVersion(), "LanguageVersion")
                : 0;
        int sk = mp.getKey() != null ? requireIndex(state.stringIndex, mp.getKey(), "string") : 0;
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
        int sk = lv.getKey() != null ? requireIndex(state.stringIndex, lv.getKey(), "string") : 0;
        int sv =
            lv.getVersion() != null
                ? requireIndex(state.stringIndex, lv.getVersion(), "string")
                : 0;
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

    // Single traversal: populate intern tables AND build flat plan
    SerializationPlan plan = buildSerializationPlan(state, instances, serializeEmptyFeatures);

    CachedTables cached = new CachedTables(state);
    int totalSize = computeChunkSize(chunk, cached, plan);

    byte[] result = new byte[totalSize];
    CodedOutputStream cos = CodedOutputStream.newInstance(result);
    try {
      writeChunk(cos, chunk, state, cached, plan);
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected IOException writing to byte array", e);
    }
    return result;
  }

  // ---- Plan building ----

  private static SerializationPlan buildSerializationPlan(
      SerializeState state,
      List<SerializedClassifierInstance> instances,
      boolean serializeEmptyFeatures) {

    int nodeCount = instances.size();

    // Per-node arrays
    int[] nodeSiId = new int[nodeCount];
    int[] nodeMpiClassifier = new int[nodeCount];
    int[] nodeSiParent = new int[nodeCount];
    int[] nodeBodySize = new int[nodeCount];
    int[] nodePropStart = new int[nodeCount];
    int[] nodePropCount = new int[nodeCount];
    int[] nodeContStart = new int[nodeCount];
    int[] nodeContCount = new int[nodeCount];
    int[] nodeRefStart = new int[nodeCount];
    int[] nodeRefCount = new int[nodeCount];
    int[] nodeAnnotStart = new int[nodeCount];
    int[] nodeAnnotCount = new int[nodeCount];
    int[] nodeAnnotPackedRawSize = new int[nodeCount];

    // Flat feature appenders
    IntAppender propMpi = new IntAppender(nodeCount * 3);
    IntAppender propSiValue = new IntAppender(nodeCount * 3);
    IntAppender propBodySize = new IntAppender(nodeCount * 3);

    IntAppender contMpi = new IntAppender(nodeCount);
    IntAppender contBodySize = new IntAppender(nodeCount);
    IntAppender contPackedRawSize = new IntAppender(nodeCount);
    IntAppender contChildStart = new IntAppender(nodeCount);
    IntAppender contChildCount = new IntAppender(nodeCount);

    IntAppender childIndexes = new IntAppender(nodeCount * 2);

    IntAppender refMpi = new IntAppender(nodeCount);
    IntAppender refBodySize = new IntAppender(nodeCount);
    IntAppender refEntryStart = new IntAppender(nodeCount);
    IntAppender refEntryCount = new IntAppender(nodeCount);

    IntAppender refEntrySiResolveInfo = new IntAppender(nodeCount);
    IntAppender refEntrySiReferred = new IntAppender(nodeCount);
    IntAppender refEntryBodySize = new IntAppender(nodeCount);

    IntAppender annotIndexes = new IntAppender(nodeCount / 4 + 4);

    for (int ni = 0; ni < nodeCount; ni++) {
      SerializedClassifierInstance n = instances.get(ni);

      // field 1: si_id
      int siId = n.getID() != null ? state.stringIndexer(n.getID()) : 0;
      nodeSiId[ni] = siId;

      // field 7: si_parent — indexed here (between id and classifier) to match legacy table ordering
      int siParent = n.getParentNodeID() != null ? state.stringIndexer(n.getParentNodeID()) : 0;
      nodeSiParent[ni] = siParent;

      // field 2: mpi_classifier
      int mpiClassifier =
          n.getClassifier() != null ? state.metaPointerIndexer(n.getClassifier()) : 0;
      nodeMpiClassifier[ni] = mpiClassifier;

      int bodySize = uint32FieldSize(siId) + uint32FieldSize(mpiClassifier);

      // field 3: properties
      List<SerializedPropertyValue> propList = n.getProperties();
      int propStart = propMpi.size();
      int propCnt = 0;
      for (SerializedPropertyValue p : propList) {
        String value = p.getValue();
        if (serializeEmptyFeatures || value != null) {
          // Match legacy SerializeHelper: value string indexed BEFORE meta-pointer
          int siValue = state.stringIndexer(value);
          int mpi = p.getMetaPointer() != null ? state.metaPointerIndexer(p.getMetaPointer()) : 0;
          int pb = uint32FieldSize(mpi) + uint32FieldSize(siValue);
          propMpi.add(mpi);
          propSiValue.add(siValue);
          propBodySize.add(pb);
          bodySize += 1 + varintSize(pb) + pb;
          propCnt++;
        }
      }
      nodePropStart[ni] = propStart;
      nodePropCount[ni] = propCnt;

      // field 4: containments
      List<SerializedContainmentValue> contList = n.getContainments();
      int contStart = contMpi.size();
      int contCnt = 0;
      for (SerializedContainmentValue c : contList) {
        List<String> children = c.getChildrenIds();
        if (serializeEmptyFeatures || !children.isEmpty()) {
          int mpi = c.getMetaPointer() != null ? state.metaPointerIndexer(c.getMetaPointer()) : 0;
          int nChildren = children.size();
          int childStart = childIndexes.size();
          int packed = 0;
          for (int k = 0; k < nChildren; k++) {
            int childIdx = state.stringIndexer(children.get(k));
            childIndexes.add(childIdx);
            packed += varintSize(childIdx);
          }
          int packedFieldSize = nChildren == 0 ? 0 : (1 + varintSize(packed) + packed);
          int cb = uint32FieldSize(mpi) + packedFieldSize;
          contMpi.add(mpi);
          contBodySize.add(cb);
          contPackedRawSize.add(packed);
          contChildStart.add(childStart);
          contChildCount.add(nChildren);
          bodySize += 1 + varintSize(cb) + cb;
          contCnt++;
        }
      }
      nodeContStart[ni] = contStart;
      nodeContCount[ni] = contCnt;

      // field 5: references
      List<SerializedReferenceValue> refList = n.getReferences();
      int refStart = refMpi.size();
      int refCnt = 0;
      for (SerializedReferenceValue r : refList) {
        List<SerializedReferenceValue.Entry> entries = r.getValue();
        if (serializeEmptyFeatures || !entries.isEmpty()) {
          int mpi = r.getMetaPointer() != null ? state.metaPointerIndexer(r.getMetaPointer()) : 0;
          int nEntries = entries.size();
          int entryStart = refEntrySiResolveInfo.size();
          int refBody = uint32FieldSize(mpi);
          for (int k = 0; k < nEntries; k++) {
            SerializedReferenceValue.Entry entry = entries.get(k);
            // Match legacy: referred indexed before resolveInfo
            int sr =
                entry.getReference() != null ? state.stringIndexer(entry.getReference()) : 0;
            int sri =
                entry.getResolveInfo() != null ? state.stringIndexer(entry.getResolveInfo()) : 0;
            int rvBody = uint32FieldSize(sri) + uint32FieldSize(sr);
            refEntrySiResolveInfo.add(sri);
            refEntrySiReferred.add(sr);
            refEntryBodySize.add(rvBody);
            refBody += 1 + varintSize(rvBody) + rvBody;
          }
          refMpi.add(mpi);
          refBodySize.add(refBody);
          refEntryStart.add(entryStart);
          refEntryCount.add(nEntries);
          bodySize += 1 + varintSize(refBody) + refBody;
          refCnt++;
        }
      }
      nodeRefStart[ni] = refStart;
      nodeRefCount[ni] = refCnt;

      // field 6: annotations (packed repeated uint32)
      List<String> annotations = n.getAnnotations();
      int annotStart = annotIndexes.size();
      int annotCnt = annotations.size();
      int packed = 0;
      for (int k = 0; k < annotCnt; k++) {
        // null annotation IDs map to index 0 (preserved from legacy getOrDefault behavior)
        int annIdx = state.stringIndexer(annotations.get(k));
        annotIndexes.add(annIdx);
        packed += varintSize(annIdx);
      }
      nodeAnnotStart[ni] = annotStart;
      nodeAnnotCount[ni] = annotCnt;
      nodeAnnotPackedRawSize[ni] = packed;
      if (annotCnt > 0) {
        bodySize += 1 + varintSize(packed) + packed;
      }

      // field 7: si_parent size (index already set above for table-ordering purposes)
      bodySize += uint32FieldSize(siParent);

      nodeBodySize[ni] = bodySize;
    }

    SerializationPlan plan = new SerializationPlan();
    plan.nodeCount = nodeCount;
    plan.nodeSiId = nodeSiId;
    plan.nodeMpiClassifier = nodeMpiClassifier;
    plan.nodeSiParent = nodeSiParent;
    plan.nodeBodySize = nodeBodySize;
    plan.nodePropStart = nodePropStart;
    plan.nodePropCount = nodePropCount;
    plan.nodeContStart = nodeContStart;
    plan.nodeContCount = nodeContCount;
    plan.nodeRefStart = nodeRefStart;
    plan.nodeRefCount = nodeRefCount;
    plan.nodeAnnotStart = nodeAnnotStart;
    plan.nodeAnnotCount = nodeAnnotCount;
    plan.nodeAnnotPackedRawSize = nodeAnnotPackedRawSize;

    // Use rawBuffer() to skip 17 Arrays.copyOf calls — access is always bounded by start+count.
    plan.propMpi = propMpi.rawBuffer();
    plan.propSiValue = propSiValue.rawBuffer();
    plan.propBodySize = propBodySize.rawBuffer();

    plan.contMpi = contMpi.rawBuffer();
    plan.contBodySize = contBodySize.rawBuffer();
    plan.contPackedRawSize = contPackedRawSize.rawBuffer();
    plan.contChildStart = contChildStart.rawBuffer();
    plan.contChildCount = contChildCount.rawBuffer();
    plan.childIndexes = childIndexes.rawBuffer();

    plan.refMpi = refMpi.rawBuffer();
    plan.refBodySize = refBodySize.rawBuffer();
    plan.refEntryStart = refEntryStart.rawBuffer();
    plan.refEntryCount = refEntryCount.rawBuffer();
    plan.refEntrySiResolveInfo = refEntrySiResolveInfo.rawBuffer();
    plan.refEntrySiReferred = refEntrySiReferred.rawBuffer();
    plan.refEntryBodySize = refEntryBodySize.rawBuffer();

    plan.annotIndexes = annotIndexes.rawBuffer();

    return plan;
  }

  // ---- Size computation ----

  /** Computes the total serialized byte count from cached tables and plans — no domain objects. */
  private static int computeChunkSize(
      SerializationChunk chunk, CachedTables cached, SerializationPlan plan) {
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
    for (int ni = 0; ni < plan.nodeCount; ni++) {
      int bodySize = plan.nodeBodySize[ni];
      size += 1 + varintSize(bodySize) + bodySize;
    }

    return size;
  }

  // ---- Write phase ----

  /**
   * Writes the entire chunk. No HashMap lookups; no domain-object traversal. All data comes from
   * {@code cached} arrays and {@code plan}.
   */
  private static void writeChunk(
      CodedOutputStream cos,
      SerializationChunk chunk,
      SerializeState state,
      CachedTables cached,
      SerializationPlan plan)
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

    // field 5: nodes — written entirely from plan, zero map lookups
    for (int ni = 0; ni < plan.nodeCount; ni++) {
      cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.nodeBodySize[ni]);
      writeNodeBody(cos, plan, ni);
    }
  }

  private static void writeNodeBody(CodedOutputStream cos, SerializationPlan plan, int ni)
      throws IOException {
    // field 1: si_id
    int siId = plan.nodeSiId[ni];
    if (siId != 0) cos.writeUInt32(1, siId);

    // field 2: mpi_classifier
    int mpiClassifier = plan.nodeMpiClassifier[ni];
    if (mpiClassifier != 0) cos.writeUInt32(2, mpiClassifier);

    // field 3: properties
    int propEnd = plan.nodePropStart[ni] + plan.nodePropCount[ni];
    for (int i = plan.nodePropStart[ni]; i < propEnd; i++) {
      int bodySize = plan.propBodySize[i];
      cos.writeTag(3, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(bodySize);
      int mpi = plan.propMpi[i];
      int sv = plan.propSiValue[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      if (sv != 0) cos.writeUInt32(2, sv);
    }

    // field 4: containments
    int contEnd = plan.nodeContStart[ni] + plan.nodeContCount[ni];
    for (int i = plan.nodeContStart[ni]; i < contEnd; i++) {
      cos.writeTag(4, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.contBodySize[i]);
      int mpi = plan.contMpi[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      int nChildren = plan.contChildCount[i];
      if (nChildren > 0) {
        cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(plan.contPackedRawSize[i]);
        int childEnd = plan.contChildStart[i] + nChildren;
        for (int k = plan.contChildStart[i]; k < childEnd; k++) {
          cos.writeUInt32NoTag(plan.childIndexes[k]);
        }
      }
    }

    // field 5: references
    int refEnd = plan.nodeRefStart[ni] + plan.nodeRefCount[ni];
    for (int i = plan.nodeRefStart[ni]; i < refEnd; i++) {
      cos.writeTag(5, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.refBodySize[i]);
      int mpi = plan.refMpi[i];
      if (mpi != 0) cos.writeUInt32(1, mpi);
      int entryEnd = plan.refEntryStart[i] + plan.refEntryCount[i];
      for (int k = plan.refEntryStart[i]; k < entryEnd; k++) {
        int rvBody = plan.refEntryBodySize[k];
        cos.writeTag(2, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(rvBody);
        int sri = plan.refEntrySiResolveInfo[k];
        int sr = plan.refEntrySiReferred[k];
        if (sri != 0) cos.writeUInt32(1, sri);
        if (sr != 0) cos.writeUInt32(2, sr);
      }
    }

    // field 6: si_annotations (packed repeated uint32)
    int annotCnt = plan.nodeAnnotCount[ni];
    if (annotCnt > 0) {
      cos.writeTag(6, WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(plan.nodeAnnotPackedRawSize[ni]);
      int annotEnd = plan.nodeAnnotStart[ni] + annotCnt;
      for (int k = plan.nodeAnnotStart[ni]; k < annotEnd; k++) {
        cos.writeUInt32NoTag(plan.annotIndexes[k]);
      }
    }

    // field 7: si_parent
    int siParent = plan.nodeSiParent[ni];
    if (siParent != 0) cos.writeUInt32(7, siParent);
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
