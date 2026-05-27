package io.lionweb.serialization.extensions;

import com.google.protobuf.CodedOutputStream;
import io.lionweb.LionWebVersion;
import io.lionweb.serialization.data.*;
import java.io.IOException;
import java.util.*;

/** Serializes a {@link BulkImport} directly to the LionWeb protobuf binary format. */
final class DirectBulkImportSerializer {

  // BulkImport message field numbers
  private static final int BI_INTERNED_STRINGS = 1;
  private static final int BI_INTERNED_META_POINTERS = 2;
  private static final int BI_ATTACH_POINTS = 3;
  private static final int BI_NODES = 4;
  private static final int BI_INTERNED_LANGUAGES = 5;

  // AttachPoint field numbers
  private static final int AP_SI_CONTAINER = 1;
  private static final int AP_MPI_META_POINTER = 2;
  private static final int AP_SI_ROOT = 3;

  // MetaPointer field numbers
  private static final int MP_LI_LANGUAGE = 1;
  private static final int MP_SI_KEY = 2;

  // Language field numbers
  private static final int LANG_SI_KEY = 1;
  private static final int LANG_SI_VERSION = 2;

  // Node field numbers
  private static final int N_SI_ID = 1;
  private static final int N_MPI_CLASSIFIER = 2;
  private static final int N_PROPERTIES = 3;
  private static final int N_CONTAINMENTS = 4;
  private static final int N_REFERENCES = 5;
  private static final int N_SI_ANNOTATIONS = 6;
  private static final int N_SI_PARENT = 7;

  // Property field numbers
  private static final int PROP_MPI = 1;
  private static final int PROP_SI_VALUE = 2;

  // Containment field numbers
  private static final int CONT_MPI = 1;
  private static final int CONT_SI_CHILDREN = 2;

  // Reference field numbers
  private static final int REF_MPI = 1;
  private static final int REF_ENTRIES = 2;

  // ReferenceValue field numbers
  private static final int RV_SI_RESOLVE_INFO = 1;
  private static final int RV_SI_REFERRED = 2;

  private DirectBulkImportSerializer() {}

  static final class State {
    final List<String> strings = new ArrayList<>();
    final Map<String, Integer> stringIndex = new HashMap<>();
    final List<LanguageVersion> languages = new ArrayList<>();
    final Map<LanguageVersion, Integer> languageIndex = new HashMap<>();
    final List<MetaPointer> metaPointers = new ArrayList<>();
    final Map<MetaPointer, Integer> metaPointerIndex = new HashMap<>();

    State() {
      strings.add(null);
      stringIndex.put(null, 0);
      languages.add(null);
      languageIndex.put(null, 0);
    }

    int str(String s) {
      if (s == null) return 0;
      Integer idx = stringIndex.get(s);
      if (idx != null) return idx;
      int i = strings.size();
      strings.add(s);
      stringIndex.put(s, i);
      return i;
    }

    int lang(LanguageVersion lv) {
      if (lv == null) return 0;
      Integer idx = languageIndex.get(lv);
      if (idx != null) return idx;
      str(lv.getKey());
      str(lv.getVersion());
      int i = languages.size();
      languages.add(lv);
      languageIndex.put(lv, i);
      return i;
    }

    int mp(MetaPointer mp) {
      if (mp == null) return 0;
      Integer idx = metaPointerIndex.get(mp);
      if (idx != null) return idx;
      lang(mp.getLanguageVersion());
      str(mp.getKey());
      int i = metaPointers.size();
      metaPointers.add(mp);
      metaPointerIndex.put(mp, i);
      return i;
    }
  }

  static byte[] serialize(
      BulkImport bulkImport, LionWebVersion lionWebVersion, boolean serializeEmptyFeatures) {
    State state = new State();

    // -- First pass: build intern tables and pre-compute indexes --

    List<BulkImport.AttachPoint> attachPoints = bulkImport.getAttachPoints();
    int apCount = attachPoints.size();
    int[] apSiContainer = new int[apCount];
    int[] apMpiMetaPointer = new int[apCount];
    int[] apSiRoot = new int[apCount];
    int[] apBodySize = new int[apCount];
    for (int i = 0; i < apCount; i++) {
      BulkImport.AttachPoint ap = attachPoints.get(i);
      int sc = state.str(ap.container);
      int mmp = state.mp(ap.containment);
      int sr = state.str(ap.rootId);
      apSiContainer[i] = sc;
      apMpiMetaPointer[i] = mmp;
      apSiRoot[i] = sr;
      apBodySize[i] = u32fs(sc) + u32fs(mmp) + u32fs(sr);
    }

    List<SerializedClassifierInstance> nodes = bulkImport.getNodes();
    int nodeCount = nodes.size();
    int[] nodeSiId = new int[nodeCount];
    int[] nodeMpiClassifier = new int[nodeCount];
    int[] nodeSiParent = new int[nodeCount];
    int[] nodeBodySize = new int[nodeCount];
    for (int i = 0; i < nodeCount; i++) {
      SerializedClassifierInstance n = nodes.get(i);
      nodeSiId[i] = state.str(n.getID());
      nodeMpiClassifier[i] = state.mp(n.getClassifier());
      nodeSiParent[i] = state.str(n.getParentNodeID());
      nodeBodySize[i] =
          computeNodeBodySize(
              state, n, nodeSiId[i], nodeMpiClassifier[i], nodeSiParent[i], serializeEmptyFeatures);
    }

    // -- Compute total message size --

    int totalSize = 0;

    // interned_strings (field 1, repeated string)
    for (int i = 1; i < state.strings.size(); i++) {
      int utf8Len = utf8ByteCount(state.strings.get(i));
      totalSize += 1 + vs(utf8Len) + utf8Len;
    }

    // interned_meta_pointers (field 2, repeated message)
    for (MetaPointer mp : state.metaPointers) {
      int li = state.lang(mp.getLanguageVersion());
      int sk = state.str(mp.getKey());
      int body = u32fs(li) + u32fs(sk);
      totalSize += 1 + vs(body) + body;
    }

    // interned_languages (field 5, repeated message)
    for (int i = 1; i < state.languages.size(); i++) {
      LanguageVersion lv = state.languages.get(i);
      int sk = state.str(lv.getKey());
      int sv = state.str(lv.getVersion());
      int body = u32fs(sk) + u32fs(sv);
      totalSize += 1 + vs(body) + body;
    }

    // attach_points (field 3, repeated message)
    for (int i = 0; i < apCount; i++) {
      totalSize += 1 + vs(apBodySize[i]) + apBodySize[i];
    }

    // nodes (field 4, repeated message)
    for (int i = 0; i < nodeCount; i++) {
      totalSize += 1 + vs(nodeBodySize[i]) + nodeBodySize[i];
    }

    // -- Write --

    byte[] result = new byte[totalSize];
    CodedOutputStream cos = CodedOutputStream.newInstance(result);
    try {
      // interned_strings
      for (int i = 1; i < state.strings.size(); i++) {
        cos.writeString(BI_INTERNED_STRINGS, state.strings.get(i));
      }

      // interned_meta_pointers
      for (MetaPointer mp : state.metaPointers) {
        int li = state.lang(mp.getLanguageVersion());
        int sk = state.str(mp.getKey());
        int body = u32fs(li) + u32fs(sk);
        cos.writeTag(
            BI_INTERNED_META_POINTERS, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(body);
        if (li != 0) cos.writeUInt32(MP_LI_LANGUAGE, li);
        if (sk != 0) cos.writeUInt32(MP_SI_KEY, sk);
      }

      // interned_languages
      for (int i = 1; i < state.languages.size(); i++) {
        LanguageVersion lv = state.languages.get(i);
        int sk = state.str(lv.getKey());
        int sv = state.str(lv.getVersion());
        int body = u32fs(sk) + u32fs(sv);
        cos.writeTag(
            BI_INTERNED_LANGUAGES, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(body);
        if (sk != 0) cos.writeUInt32(LANG_SI_KEY, sk);
        if (sv != 0) cos.writeUInt32(LANG_SI_VERSION, sv);
      }

      // attach_points
      for (int i = 0; i < apCount; i++) {
        cos.writeTag(BI_ATTACH_POINTS, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(apBodySize[i]);
        if (apSiContainer[i] != 0) cos.writeUInt32(AP_SI_CONTAINER, apSiContainer[i]);
        if (apMpiMetaPointer[i] != 0) cos.writeUInt32(AP_MPI_META_POINTER, apMpiMetaPointer[i]);
        if (apSiRoot[i] != 0) cos.writeUInt32(AP_SI_ROOT, apSiRoot[i]);
      }

      // nodes
      for (int i = 0; i < nodeCount; i++) {
        cos.writeTag(BI_NODES, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(nodeBodySize[i]);
        writeNodeBody(
            cos,
            state,
            nodes.get(i),
            nodeSiId[i],
            nodeMpiClassifier[i],
            nodeSiParent[i],
            serializeEmptyFeatures);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected IOException writing to byte array", e);
    }
    return result;
  }

  private static int computeNodeBodySize(
      State state,
      SerializedClassifierInstance n,
      int siId,
      int mpiClassifier,
      int siParent,
      boolean serializeEmptyFeatures) {
    int size = u32fs(siId) + u32fs(mpiClassifier) + u32fs(siParent);

    for (SerializedPropertyValue p : n.getProperties()) {
      String value = p.getValue();
      if (serializeEmptyFeatures || value != null) {
        int mpi = state.mp(p.getMetaPointer());
        int sv = state.str(value);
        int body = u32fs(mpi) + u32fs(sv);
        size += 1 + vs(body) + body;
      }
    }

    for (SerializedContainmentValue c : n.getContainments()) {
      List<String> children = c.getChildrenIds();
      if (serializeEmptyFeatures || !children.isEmpty()) {
        int mpi = state.mp(c.getMetaPointer());
        int packedRaw = 0;
        for (String childId : children) {
          packedRaw += vs(state.str(childId));
        }
        int packedField = children.isEmpty() ? 0 : 1 + vs(packedRaw) + packedRaw;
        int body = u32fs(mpi) + packedField;
        size += 1 + vs(body) + body;
      }
    }

    for (SerializedReferenceValue r : n.getReferences()) {
      List<SerializedReferenceValue.Entry> entries = r.getValue();
      if (serializeEmptyFeatures || !entries.isEmpty()) {
        int mpi = state.mp(r.getMetaPointer());
        int body = u32fs(mpi);
        for (SerializedReferenceValue.Entry e : entries) {
          int sr = state.str(e.getReference());
          int sri = state.str(e.getResolveInfo());
          int rvBody = u32fs(sr) + u32fs(sri);
          body += 1 + vs(rvBody) + rvBody;
        }
        size += 1 + vs(body) + body;
      }
    }

    List<String> annotations = n.getAnnotations();
    if (!annotations.isEmpty()) {
      int packedRaw = 0;
      for (String ann : annotations) {
        packedRaw += vs(state.str(ann));
      }
      size += 1 + vs(packedRaw) + packedRaw;
    }

    return size;
  }

  private static void writeNodeBody(
      CodedOutputStream cos,
      State state,
      SerializedClassifierInstance n,
      int siId,
      int mpiClassifier,
      int siParent,
      boolean serializeEmptyFeatures)
      throws IOException {
    if (siId != 0) cos.writeUInt32(N_SI_ID, siId);
    if (mpiClassifier != 0) cos.writeUInt32(N_MPI_CLASSIFIER, mpiClassifier);

    for (SerializedPropertyValue p : n.getProperties()) {
      String value = p.getValue();
      if (serializeEmptyFeatures || value != null) {
        int mpi = state.mp(p.getMetaPointer());
        int sv = state.str(value);
        int body = u32fs(mpi) + u32fs(sv);
        cos.writeTag(N_PROPERTIES, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(body);
        if (mpi != 0) cos.writeUInt32(PROP_MPI, mpi);
        if (sv != 0) cos.writeUInt32(PROP_SI_VALUE, sv);
      }
    }

    for (SerializedContainmentValue c : n.getContainments()) {
      List<String> children = c.getChildrenIds();
      if (serializeEmptyFeatures || !children.isEmpty()) {
        int mpi = state.mp(c.getMetaPointer());
        int packedRaw = 0;
        int[] childIdxs = new int[children.size()];
        for (int k = 0; k < children.size(); k++) {
          childIdxs[k] = state.str(children.get(k));
          packedRaw += vs(childIdxs[k]);
        }
        int packedField = children.isEmpty() ? 0 : 1 + vs(packedRaw) + packedRaw;
        int body = u32fs(mpi) + packedField;
        cos.writeTag(N_CONTAINMENTS, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(body);
        if (mpi != 0) cos.writeUInt32(CONT_MPI, mpi);
        if (!children.isEmpty()) {
          cos.writeTag(CONT_SI_CHILDREN, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
          cos.writeUInt32NoTag(packedRaw);
          for (int idx : childIdxs) cos.writeUInt32NoTag(idx);
        }
      }
    }

    for (SerializedReferenceValue r : n.getReferences()) {
      List<SerializedReferenceValue.Entry> entries = r.getValue();
      if (serializeEmptyFeatures || !entries.isEmpty()) {
        int mpi = state.mp(r.getMetaPointer());
        int body = u32fs(mpi);
        int[][] entryData = new int[entries.size()][3]; // [sr, sri, rvBody]
        for (int k = 0; k < entries.size(); k++) {
          SerializedReferenceValue.Entry e = entries.get(k);
          int sr = state.str(e.getReference());
          int sri = state.str(e.getResolveInfo());
          int rvBody = u32fs(sr) + u32fs(sri);
          entryData[k][0] = sr;
          entryData[k][1] = sri;
          entryData[k][2] = rvBody;
          body += 1 + vs(rvBody) + rvBody;
        }
        cos.writeTag(N_REFERENCES, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
        cos.writeUInt32NoTag(body);
        if (mpi != 0) cos.writeUInt32(REF_MPI, mpi);
        for (int[] ed : entryData) {
          cos.writeTag(REF_ENTRIES, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
          cos.writeUInt32NoTag(ed[2]);
          if (ed[1] != 0) cos.writeUInt32(RV_SI_RESOLVE_INFO, ed[1]);
          if (ed[0] != 0) cos.writeUInt32(RV_SI_REFERRED, ed[0]);
        }
      }
    }

    List<String> annotations = n.getAnnotations();
    if (!annotations.isEmpty()) {
      int packedRaw = 0;
      int[] annotIdxs = new int[annotations.size()];
      for (int k = 0; k < annotations.size(); k++) {
        annotIdxs[k] = state.str(annotations.get(k));
        packedRaw += vs(annotIdxs[k]);
      }
      cos.writeTag(N_SI_ANNOTATIONS, com.google.protobuf.WireFormat.WIRETYPE_LENGTH_DELIMITED);
      cos.writeUInt32NoTag(packedRaw);
      for (int idx : annotIdxs) cos.writeUInt32NoTag(idx);
    }

    if (siParent != 0) cos.writeUInt32(N_SI_PARENT, siParent);
  }

  /** Varint size for a uint32 value. */
  private static int vs(int v) {
    return CodedOutputStream.computeUInt32SizeNoTag(v);
  }

  /**
   * Size contribution of a uint32 field (tag + value). Returns 0 if value is 0 (proto3 default).
   */
  private static int u32fs(int value) {
    return value == 0 ? 0 : 1 + vs(value);
  }

  static int utf8ByteCount(String s) {
    int count = 0;
    for (int i = 0, len = s.length(); i < len; i++) {
      char c = s.charAt(i);
      if (c < 0x80) count += 1;
      else if (c < 0x800) count += 2;
      else if (Character.isHighSurrogate(c)) {
        count += 4;
        i++;
      } else count += 3;
    }
    return count;
  }
}
