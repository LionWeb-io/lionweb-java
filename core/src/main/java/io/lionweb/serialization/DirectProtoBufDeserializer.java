package io.lionweb.serialization;

import com.google.protobuf.CodedInputStream;
import io.lionweb.serialization.data.*;
import java.io.*;
import java.util.*;

/**
 * Deserializes protobuf binary data directly into a {@link SerializationChunk} without creating
 * intermediate protobuf message objects (PBChunk, PBNode, etc.).
 *
 * <p>For {@code byte[]} input the implementation uses two passes over the <em>same</em> backing
 * array (no copy):
 *
 * <ol>
 *   <li>Read all string, language, and meta-pointer fields; skip node bodies.
 *   <li>Resolve the index tables, then read only the node fields inline using {@code
 *       pushLimit}/{@code popLimit} — no per-node {@code byte[]} allocation.
 * </ol>
 *
 * <p>For {@link InputStream} input the stream is first drained into a single {@code byte[]}, then
 * the same two-pass strategy is applied.
 *
 * <p>The output is semantically equivalent to what {@code PBChunk.parseFrom(bytes)} followed by
 * {@code ProtoBufSerialization.deserializeSerializationChunk(pbChunk)} would produce, but avoids
 * creating any protobuf message objects.
 */
final class DirectProtoBufDeserializer {

  private DirectProtoBufDeserializer() {}

  // ---- Public entry points ----

  static SerializationChunk deserialize(byte[] bytes, boolean serializeEmptyFeatures)
      throws IOException {
    return deserializeTwoPass(bytes, serializeEmptyFeatures);
  }

  static SerializationChunk deserialize(InputStream in, boolean serializeEmptyFeatures)
      throws IOException {
    // Drain to a single byte[] so we can apply the same two-pass optimisation.
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = in.read(buf)) != -1) baos.write(buf, 0, n);
    return deserializeTwoPass(baos.toByteArray(), serializeEmptyFeatures);
  }

  // ---- Two-pass core ----

  private static SerializationChunk deserializeTwoPass(byte[] bytes, boolean serializeEmptyFeatures)
      throws IOException {

    String version = "";
    // index 0 = null; actual strings start at index 1
    ArrayList<String> strings = new ArrayList<>();
    strings.add(null);

    // Flat int arrays for compact storage: langData[2i]=siKey, langData[2i+1]=siVersion
    int[] langData = new int[16];
    int langCount = 0;

    // mpData[2i]=liLanguage, mpData[2i+1]=siKey
    int[] mpData = new int[32];
    int mpCount = 0;

    // Pass 1: read strings / languages / meta-pointers; skip node bodies
    CodedInputStream cis = CodedInputStream.newInstance(bytes);
    int tag;
    while ((tag = cis.readTag()) != 0) {
      int f = tag >>> 3;
      switch (f) {
        case 1: // serialization_format_version
          version = cis.readString();
          break;

        case 2: // interned_strings
          strings.add(cis.readString());
          break;

        case 3: // interned_meta_pointers
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int liLanguage = 0, siKey = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == 1) liLanguage = cis.readRawVarint32();
              else if (fi == 2) siKey = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            if (mpCount * 2 >= mpData.length) mpData = Arrays.copyOf(mpData, mpData.length * 2);
            mpData[mpCount * 2] = liLanguage;
            mpData[mpCount * 2 + 1] = siKey;
            mpCount++;
            break;
          }

        case 4: // interned_languages
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int siKeyL = 0, siVersionL = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == 1) siKeyL = cis.readRawVarint32();
              else if (fi == 2) siVersionL = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            if (langCount * 2 >= langData.length)
              langData = Arrays.copyOf(langData, langData.length * 2);
            langData[langCount * 2] = siKeyL;
            langData[langCount * 2 + 1] = siVersionL;
            langCount++;
            break;
          }

        case 5: // nodes — skip in pass 1
          cis.skipField(tag);
          break;

        default:
          cis.skipField(tag);
      }
    }

    // Resolve index tables
    String[] stringsArray = strings.toArray(new String[strings.size()]);

    LanguageVersion[] languagesArray = new LanguageVersion[langCount + 1];
    languagesArray[0] = null;
    for (int i = 0; i < langCount; i++) {
      String key = safeGet(stringsArray, langData[i * 2]);
      String ver = safeGet(stringsArray, langData[i * 2 + 1]);
      languagesArray[i + 1] = LanguageVersion.of(key, ver);
    }

    MetaPointer[] metaPointersArray = new MetaPointer[mpCount];
    for (int i = 0; i < mpCount; i++) {
      LanguageVersion lv = safeGet(languagesArray, mpData[i * 2]);
      String key = safeGet(stringsArray, mpData[i * 2 + 1]);
      if (lv != null) {
        metaPointersArray[i] = MetaPointer.get(lv.getKey(), lv.getVersion(), key);
      } else {
        metaPointersArray[i] = MetaPointer.get(null, null, key);
      }
    }

    // Build chunk header
    SerializationChunk chunk = new SerializationChunk();
    chunk.setSerializationFormatVersion(version);
    for (int i = 1; i < languagesArray.length; i++) {
      if (languagesArray[i] != null) chunk.addLanguage(languagesArray[i]);
    }

    // Pass 2: second pass over the same byte[] (no copy) — parse only node fields inline
    cis = CodedInputStream.newInstance(bytes);
    while ((tag = cis.readTag()) != 0) {
      int f = tag >>> 3;
      if (f == 5) {
        int len = cis.readRawVarint32();
        int oldLimit = cis.pushLimit(len);
        chunk.addClassifierInstance(
            readNodeInline(cis, stringsArray, metaPointersArray, serializeEmptyFeatures));
        cis.popLimit(oldLimit);
      } else {
        cis.skipField(tag);
      }
    }

    return chunk;
  }

  // ---- Node decoding ----

  /**
   * Reads a single PBNode body from {@code cis} (already limited by the caller via {@code
   * pushLimit}) and converts it directly to a {@link SerializedClassifierInstance}. No intermediate
   * byte[] or CodedInputStream is allocated.
   */
  private static SerializedClassifierInstance readNodeInline(
      CodedInputStream cis,
      String[] strings,
      MetaPointer[] metaPointers,
      boolean serializeEmptyFeatures)
      throws IOException {

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
              int fi = innerTag >>> 3;
              if (fi == 1) mpiIndex = cis.readRawVarint32();
              else if (fi == 2) siValue = cis.readRawVarint32();
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
              int fi = innerTag >>> 3;
              if (fi == 1) {
                mpiIndex = cis.readRawVarint32();
              } else if (fi == 2) {
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
              int fi = innerTag >>> 3;
              if (fi == 1) {
                mpiIndex = cis.readRawVarint32();
              } else if (fi == 2) {
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

  @SuppressWarnings("unchecked")
  private static <T> T safeGet(T[] array, int index) {
    return (index >= 0 && index < array.length) ? array[index] : null;
  }
}
