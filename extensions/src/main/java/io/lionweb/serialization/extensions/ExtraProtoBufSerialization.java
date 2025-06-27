package io.lionweb.serialization.extensions;

import io.lionweb.LionWebVersion;
import io.lionweb.protobuf.PBAttachPoint;
import io.lionweb.protobuf.PBBulkImport;
import io.lionweb.protobuf.PBMetaPointer;
import io.lionweb.serialization.LowLevelJsonSerialization;
import io.lionweb.serialization.ProtoBufSerialization;
import io.lionweb.serialization.data.SerializedChunk;
import java.util.Map;
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
              attachPointBuilder.setContainer(serializeHelper.stringIndexer(attachPoint.container));
              attachPointBuilder.setRootId(serializeHelper.stringIndexer(attachPoint.rootId));
              attachPointBuilder.setMetaPointerIndex(
                  serializeHelper.metaPointerIndexer(attachPoint.containment));
              bulkImportBuilder.addAttachPoints(attachPointBuilder.build());
            });

    SerializedChunk serializedChunk =
        LowLevelJsonSerialization.groupNodesIntoSerializationBlock(
            bulkImport.getNodes(), getLionWebVersion());

    serializedChunk
        .getClassifierInstances()
        .forEach(
            serializedNode -> {
              bulkImportBuilder.addNodes(serializeHelper.serializeNode(serializedNode));
            });

    serializeHelper.getStrings().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(entry -> bulkImportBuilder.addStringValues(entry.getKey()));

    serializeHelper.getMetaPointers().entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry ->
                bulkImportBuilder.addMetaPointers(
                    PBMetaPointer.newBuilder()
                        .setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()))
                        .setKey(serializeHelper.stringIndexer(entry.getKey().getKey()))
                        .setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()))
                        .build()));
    return bulkImportBuilder.build();
  }

  public byte[] serializeBulkImportToBytes(@Nonnull BulkImport bulkImport) {
    PBBulkImport pbBulkImport = serializeBulkImport(bulkImport);
    return pbBulkImport.toByteArray();
  }
}
