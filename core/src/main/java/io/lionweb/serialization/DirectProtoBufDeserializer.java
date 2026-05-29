package io.lionweb.serialization;

import static io.lionweb.serialization.ProtoBufFieldNumbers.*;

import com.google.protobuf.CodedInputStream;
import io.lionweb.serialization.data.*;
import java.io.*;
import java.util.*;

/**
 * Deserializes LionWeb protobuf binary data directly into a {@link SerializationChunk}.
 *
 * <p>For {@code byte[]} input the implementation uses two passes over the same backing array (no
 * copy): first to read strings/languages/meta-pointers, second to read node bodies inline using
 * {@code pushLimit}/{@code popLimit}. For {@link java.io.InputStream} input the stream is drained
 * into a {@code byte[]} first.
 */
final class DirectProtoBufDeserializer {

  private DirectProtoBufDeserializer() {}

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
        case CHUNK_SERIALIZATION_FORMAT_VERSION:
          version = cis.readString();
          break;

        case CHUNK_INTERNED_STRINGS:
          strings.add(cis.readString());
          break;

        case CHUNK_INTERNED_META_POINTERS:
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int liLanguage = 0, siKey = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == META_POINTER_LI_LANGUAGE) liLanguage = cis.readRawVarint32();
              else if (fi == META_POINTER_SI_KEY) siKey = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            if (mpCount * 2 >= mpData.length) mpData = Arrays.copyOf(mpData, mpData.length * 2);
            mpData[mpCount * 2] = liLanguage;
            mpData[mpCount * 2 + 1] = siKey;
            mpCount++;
            break;
          }

        case CHUNK_INTERNED_LANGUAGES:
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int siKeyL = 0, siVersionL = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == LANGUAGE_SI_KEY) siKeyL = cis.readRawVarint32();
              else if (fi == LANGUAGE_SI_VERSION) siVersionL = cis.readRawVarint32();
              else cis.skipField(innerTag);
            }
            cis.popLimit(oldLimit);
            if (langCount * 2 >= langData.length) {
              langData = Arrays.copyOf(langData, langData.length * 2);
            }
            langData[langCount * 2] = siKeyL;
            langData[langCount * 2 + 1] = siVersionL;
            langCount++;
            break;
          }

        case CHUNK_NODES: // skip in pass 1
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
      int liLanguage = mpData[i * 2];
      if (liLanguage != 0 && liLanguage >= languagesArray.length) {
        throw new DeserializationException(
            "Unable to deserialize meta pointer with language " + liLanguage);
      }
      LanguageVersion lv = safeGet(languagesArray, liLanguage);
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
      if (f == CHUNK_NODES) {
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
        case NODE_SI_ID:
          siId = cis.readRawVarint32();
          break;

        case NODE_MPI_CLASSIFIER:
          mpiClassifier = cis.readRawVarint32();
          break;

        case NODE_PROPERTIES: // PBProperty embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0, siValue = 0;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == PROPERTY_MPI) mpiIndex = cis.readRawVarint32();
              else if (fi == PROPERTY_SI_VALUE) siValue = cis.readRawVarint32();
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

        case NODE_CONTAINMENTS: // PBContainment embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0;
            List<String> children = null;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == CONTAINMENT_MPI) {
                mpiIndex = cis.readRawVarint32();
              } else if (fi == CONTAINMENT_SI_CHILDREN) {
                // packed repeated uint32 si_children
                int packedLen = cis.readRawVarint32();
                int packedOldLimit = cis.pushLimit(packedLen);
                children = new ArrayList<>(Math.max(4, packedLen));
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

        case NODE_REFERENCES: // PBReference embedded message
          {
            int len = cis.readRawVarint32();
            int oldLimit = cis.pushLimit(len);
            int mpiIndex = 0;
            List<SerializedReferenceValue.Entry> entries = null;
            int innerTag;
            while ((innerTag = cis.readTag()) != 0) {
              int fi = innerTag >>> 3;
              if (fi == REFERENCE_MPI) {
                mpiIndex = cis.readRawVarint32();
              } else if (fi == REFERENCE_ENTRIES) {
                // PBReferenceValue embedded message
                int rvLen = cis.readRawVarint32();
                int rvOldLimit = cis.pushLimit(rvLen);
                int siResolveInfo = 0, siReferred = 0;
                int rvTag;
                while ((rvTag = cis.readTag()) != 0) {
                  int rf = rvTag >>> 3;
                  if (rf == REFERENCE_VALUE_SI_RESOLVE_INFO) siResolveInfo = cis.readRawVarint32();
                  else if (rf == REFERENCE_VALUE_SI_REFERRED) siReferred = cis.readRawVarint32();
                  else cis.skipField(rvTag);
                }
                cis.popLimit(rvOldLimit);
                if (entries == null) entries = new ArrayList<>();
                SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
                entry.setReference(safeGet(strings, siReferred));
                entry.setResolveInfo(safeGet(strings, siResolveInfo));
                entries.add(entry);
              } else {
                cis.skipField(innerTag);
              }
            }
            cis.popLimit(oldLimit);
            if (entries == null) entries = Collections.emptyList();
            if (serializeEmptyFeatures || !entries.isEmpty()) {
              MetaPointer mp = safeGet(metaPointers, mpiIndex);
              sci.unsafeAppendReferenceValue(new SerializedReferenceValue(mp, entries));
            }
            break;
          }

        case NODE_SI_ANNOTATIONS: // packed repeated uint32
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

        case NODE_SI_PARENT:
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
