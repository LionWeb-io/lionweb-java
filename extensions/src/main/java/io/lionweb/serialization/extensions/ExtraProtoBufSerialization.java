package io.lionweb.serialization.extensions;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.ProtoBufSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.protobuf.PBAttachPoint;
import io.lionweb.lioncore.protobuf.PBBulkImport;
import io.lionweb.lioncore.protobuf.PBMetaPointer;
import java.util.Map;
import javax.annotation.Nonnull;

/** It contains the logic to serialize non-standard messages. */
public class ExtraProtoBufSerialization extends ProtoBufSerialization {

  public ExtraProtoBufSerialization() {
    super();
  }

  public ExtraProtoBufSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public PBBulkImport serializeBulkImport(BulkImport bulkImport) {
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
}
