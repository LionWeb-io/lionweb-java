package io.lionweb.serialization.extensions;

import io.lionweb.LionWebVersion;
import io.lionweb.protobuf.PBAttachPoint;
import io.lionweb.protobuf.PBBulkImport;
import io.lionweb.protobuf.PBLanguage;
import io.lionweb.protobuf.PBMetaPointer;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.data.LanguageVersion;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializationChunk;
import java.util.Objects;
import javax.annotation.Nonnull;

/** It contains the logic to serialize non-standard messages. */
public class ExtraProtoBufSerialization extends ProtoBufSerialization {

  public ExtraProtoBufSerialization() {
    super();
  }

  public ExtraProtoBufSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public PBBulkImport serializeBulkImport(@Nonnull BulkImport bulkImport) {
    Objects.requireNonNull(bulkImport);
    PBBulkImport.Builder bulkImportBuilder = PBBulkImport.newBuilder();
    SerializeHelper serializeHelper = new SerializeHelper();

    bulkImport
        .getAttachPoints()
        .forEach(
            attachPoint -> {
              PBAttachPoint.Builder attachPointBuilder = PBAttachPoint.newBuilder();
              attachPointBuilder.setSiContainer(
                  serializeHelper.stringIndexer(attachPoint.container));
              attachPointBuilder.setSiRoot(serializeHelper.stringIndexer(attachPoint.rootId));
              attachPointBuilder.setMpiMetaPointer(
                  serializeHelper.metaPointerIndexer(attachPoint.containment));
              bulkImportBuilder.addAttachPoints(attachPointBuilder.build());
            });

    SerializationChunk serializationChunk =
        LowLevelJsonSerialization.groupNodesIntoSerializationBlock(
            bulkImport.getNodes(), getLionWebVersion());

    serializationChunk
        .getClassifierInstances()
        .forEach(
            serializedNode -> {
              bulkImportBuilder.addNodes(serializeHelper.serializeNode(serializedNode));
            });

    // We need to process languages before strings, otherwise we might end up with null pointers
    for (LanguageVersion languageVersion : serializationChunk.getLanguages()) {
      serializeHelper.languageIndexer(languageVersion);
    }
    for (LanguageVersion languageVersion : serializeHelper.getLanguages()) {
      if (languageVersion != null) {
        bulkImportBuilder.addInternedLanguages(
            PBLanguage.newBuilder()
                .setSiKey(serializeHelper.stringIndexer(languageVersion.getKey()))
                .setSiVersion(serializeHelper.stringIndexer(languageVersion.getVersion()))
                .build());
      }
    }

    for (String string : serializeHelper.getStrings()) {
      if (string != null) {
        bulkImportBuilder.addInternedStrings(string);
      }
    }

    for (MetaPointer metaPointer : serializeHelper.getMetaPointers()) {
      PBMetaPointer.Builder pbMetaPointer = PBMetaPointer.newBuilder();
      pbMetaPointer.setSiKey(serializeHelper.stringIndexer(metaPointer.getKey()));
      pbMetaPointer.setLiLanguage(
          serializeHelper.languageIndexer(metaPointer.getLanguageVersion()));
      bulkImportBuilder.addInternedMetaPointers(pbMetaPointer.build());
    }
    return bulkImportBuilder.build();
  }

  public byte[] serializeBulkImportToBytes(@Nonnull BulkImport bulkImport) {
    PBBulkImport pbBulkImport = serializeBulkImport(bulkImport);
    return pbBulkImport.toByteArray();
  }
}
