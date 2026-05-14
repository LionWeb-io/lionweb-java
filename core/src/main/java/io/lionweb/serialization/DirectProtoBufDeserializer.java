package io.lionweb.serialization;

import com.google.protobuf.CodedInputStream;
import io.lionweb.serialization.data.*;
import java.io.*;
import java.util.*;

/**
 * Deserializes protobuf binary data directly into a {@link SerializationChunk} without creating
 * intermediate protobuf message objects (PBChunk, PBNode, etc.).
 *
 * <p>The wire format is read in two logical passes:
 *
 * <ol>
 *   <li>Read all fields of the top-level PBChunk message: accumulate interned strings inline, store
 *       raw language/meta-pointer field pairs as {@code int[]}, and buffer each node's body as a
 *       {@code byte[]}.
 *   <li>Resolve the index tables (languages then meta-pointers) and decode each buffered node body
 *       directly into a {@link SerializedClassifierInstance}.
 * </ol>
 *
 * <p>The output is semantically equivalent to what {@code PBChunk.parseFrom(bytes)} followed by
 * {@code ProtoBufSerialization.deserializeSerializationChunk(pbChunk)} would produce, but avoids
 * creating any protobuf message objects.
 *
 * <p><b>Field ordering assumption:</b> this implementation buffers all node bodies and resolves
 * tables in a second pass, so fields may appear in any order in the input stream.
 */
final class DirectProtoBufDeserializer {

  private DirectProtoBufDeserializer() {}

  // ---- Public entry points ----

  static SerializationChunk deserialize(byte[] bytes, boolean serializeEmptyFeatures)
      throws IOException {
    return deserialize(CodedInputStream.newInstance(bytes), serializeEmptyFeatures);
  }

  static SerializationChunk deserialize(InputStream in, boolean serializeEmptyFeatures)
      throws IOException {
    return deserialize(CodedInputStream.newInstance(in), serializeEmptyFeatures);
  }

  // ---- Core deserialize logic ----

  private static SerializationChunk deserialize(
      CodedInputStream cis, boolean serializeEmptyFeatures) throws IOException {

    String version = "";
    // index 0 = null; subsequent entries are the actual interned strings
    ArrayList<String> strings = new ArrayList<>();
    strings.add(null);

    // Each int[2] = {siKey, siVersion} (raw string-table indices from the wire)
    ArrayList<int[]> rawLanguages = new ArrayList<>();
    // Each int[2] = {liLanguage, siKey} (raw table indices from the wire)
    ArrayList<int[]> rawMetaPointers = new ArrayList<>();
    // Raw bytes of each PBNode body (already stripped of the outer tag+length)
    ArrayList<byte[]> nodeBodyList = new ArrayList<>();

    // Pass 1: read top-level PBChunk fields
    int tag;
    while ((tag = cis.readTag()) != 0) {
      int fieldNumber = tag >>> 3;
      switch (fieldNumber) {
        case 1: // serialization_format_version (string)
          version = cis.readString();
          break;

        case 2: // interned_strings (repeated string)
          strings.add(cis.readString());
          break;

        case 3: // interned_meta_pointers (repeated PBMetaPointer embedded message)
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int liLanguage = 0, siKey = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int f = innerTag >>> 3;
              if (f == 1) liLanguage = cis.readRawVarint32();
              else if (f == 2) siKey = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            rawMetaPointers.add(new int[] {liLanguage, siKey});
            break;
          }

        case 4: // interned_languages (repeated PBLanguage embedded message)
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int siKeyL = 0, siVersionL = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int f = innerTag >>> 3;
              if (f == 1) siKeyL = cis.readRawVarint32();
              else if (f == 2) siVersionL = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            rawLanguages.add(new int[] {siKeyL, siVersionL});
            break;
          }

        case 5: // nodes (repeated PBNode embedded message) — buffer body bytes
          {
            int len = cis.readRawVarint32();
            nodeBodyList.add(cis.readRawBytes(len));
            break;
          }

        default:
          cis.skipField(tag);
      }
    }

    // Pass 2: resolve index tables
    String[] stringsArray = strings.toArray(new String[0]);

    LanguageVersion[] languagesArray = new LanguageVersion[rawLanguages.size() + 1];
    languagesArray[0] = null;
    for (int i = 0; i < rawLanguages.size(); i++) {
      int[] raw = rawLanguages.get(i);
      String key = safeGet(stringsArray, raw[0]);
      String ver = safeGet(stringsArray, raw[1]);
      languagesArray[i + 1] = LanguageVersion.of(key, ver);
    }

    MetaPointer[] metaPointersArray = new MetaPointer[rawMetaPointers.size()];
    for (int i = 0; i < rawMetaPointers.size(); i++) {
      int[] raw = rawMetaPointers.get(i);
      LanguageVersion lv = safeGet(languagesArray, raw[0]);
      String key = safeGet(stringsArray, raw[1]);
      if (lv != null) {
        metaPointersArray[i] = MetaPointer.get(lv.getKey(), lv.getVersion(), key);
      } else {
        metaPointersArray[i] = MetaPointer.get(null, null, key);
      }
    }

    // Pass 3: build SerializationChunk
    SerializationChunk chunk = new SerializationChunk(nodeBodyList.size());
    chunk.setSerializationFormatVersion(version);
    for (int i = 1; i < languagesArray.length; i++) {
      if (languagesArray[i] != null) chunk.addLanguage(languagesArray[i]);
    }

    for (byte[] nodeBody : nodeBodyList) {
      SerializedClassifierInstance sci =
          readNode(nodeBody, stringsArray, metaPointersArray, serializeEmptyFeatures);
      chunk.addClassifierInstance(sci);
    }

    return chunk;
  }

  // ---- Node decoding ----

  private static SerializedClassifierInstance readNode(
      byte[] body, String[] strings, MetaPointer[] metaPointers, boolean serializeEmptyFeatures)
      throws IOException {
    CodedInputStream cis = CodedInputStream.newInstance(body);

    SerializedClassifierInstance sci = new SerializedClassifierInstance();
    int siId = 0, mpiClassifier = 0, siParent = 0;

    int tag;
    while ((tag = cis.readTag()) != 0) {
      int fieldNumber = tag >>> 3;
      switch (fieldNumber) {
        case 1: // si_id
          siId = cis.readRawVarint32();
          break;

        case 2: // mpi_classifier
          mpiClassifier = cis.readRawVarint32();
          break;

        case 3: // PBProperty embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0, siValue = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int f = innerTag >>> 3;
              if (f == 1) mpiIndex = cis.readRawVarint32();
              else if (f == 2) siValue = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            if (serializeEmptyFeatures || siValue != 0) {
              MetaPointer mp = safeGet(metaPointers, mpiIndex);
              String value = safeGet(strings, siValue);
              sci.unsafeAppendPropertyValue(SerializedPropertyValue.get(mp, value));
            }
            break;
          }

        case 4: // PBContainment embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0;
            List<String> children = null;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int f = innerTag >>> 3;
              if (f == 1) {
                mpiIndex = cis.readRawVarint32();
              } else if (f == 2) {
                // packed repeated uint32 si_children
                int packedLen = cis.readRawVarint32();
                int packedOldLimit = cis.pushLimit(packedLen);
                children = new ArrayList<>();
                while (!cis.isAtEnd()) {
                  int childIdx = cis.readRawVarint32();
                  if (childIdx == 0) {
                    throw new DeserializationException(
                        "Unable to deserialize child identified by Null ID");
                  }
                  children.add(safeGet(strings, childIdx));
                }
                cis.popLimit(packedOldLimit);
              } else {
                cis.skipField(innerTag);
              }
            }
            cis.popLimit(oldLimit);
            if (children == null) children = Collections.emptyList();
            if (serializeEmptyFeatures || !children.isEmpty()) {
              MetaPointer mp = safeGet(metaPointers, mpiIndex);
              sci.unsafeAppendContainmentValue(new SerializedContainmentValue(mp, children));
            }
            break;
          }

        case 5: // PBReference embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0;
            List<SerializedReferenceValue.Entry> entries = new ArrayList<>();
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int f = innerTag >>> 3;
              if (f == 1) {
                mpiIndex = cis.readRawVarint32();
              } else if (f == 2) {
                // PBReferenceValue embedded message
                int rvLen = cis.readRawVarint32();
                int rvOldLimit = cis.pushLimit(rvLen);
                int siResolveInfo = 0, siReferred = 0;
                int rvTag;
                while ((rvTag = cis.readTag()) != 0) {
                  int rf = rvTag >>> 3;
                  if (rf == 1) siResolveInfo = cis.readRawVarint32();
                  else if (rf == 2) siReferred = cis.readRawVarint32();
                  else cis.skipField(rvTag);
                }
                cis.popLimit(rvOldLimit);
                SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
                entry.setReference(safeGet(strings, siReferred));
                entry.setResolveInfo(safeGet(strings, siResolveInfo));
                entries.add(entry);
              } else {
                cis.skipField(innerTag);
              }
            }
            cis.popLimit(oldLimit);
            if (serializeEmptyFeatures || !entries.isEmpty()) {
              MetaPointer mp = safeGet(metaPointers, mpiIndex);
              sci.unsafeAppendReferenceValue(new SerializedReferenceValue(mp, entries));
            }
            break;
          }

        case 6: // si_annotations (packed repeated uint32)
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            while (!cis.isAtEnd()) {
              int annIdx = cis.readRawVarint32();
              sci.addAnnotation(safeGet(strings, annIdx));
            }
            cis.popLimit(oldLimit);
            break;
          }

        case 7: // si_parent
          siParent = cis.readRawVarint32();
          break;

        default:
          cis.skipField(tag);
      }
    }

    sci.setID(safeGet(strings, siId));
    sci.setClassifier(safeGet(metaPointers, mpiClassifier));
    sci.setParentNodeID(safeGet(strings, siParent));

    return sci;
  }

  // ---- Helpers ----

  private static <T> T safeGet(T[] array, int index) {
    return (index >= 0 && index < array.length) ? array[index] : null;
  }
}
