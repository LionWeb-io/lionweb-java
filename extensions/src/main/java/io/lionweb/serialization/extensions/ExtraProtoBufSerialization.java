package io.lionweb.serialization.extensions;

import io.lionweb.LionWebVersion;
import io.lionweb.serialization.ProtoBufSerialization;
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

  public byte[] serializeBulkImportToBytes(@Nonnull BulkImport bulkImport) {
    Objects.requireNonNull(bulkImport, "bulkImport should not be null");
    return DirectBulkImportSerializer.serialize(
        bulkImport, getLionWebVersion(), shouldSerializeEmptyFeatures());
  }
}
