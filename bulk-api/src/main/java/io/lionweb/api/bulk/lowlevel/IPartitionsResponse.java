package io.lionweb.api.bulk.lowlevel;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

public interface IPartitionsResponse extends ILowlevelResponse {
    /**
     * Retrieved partition nodes, if successful.
     */
    // @Nonnull
    SerializedChunk getResult();
}
