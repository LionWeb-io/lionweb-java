package io.lionweb.api.bulk.lowlevel;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.util.List;

public interface IRetrieveResponse extends ILowlevelResponse {
    /**
     * Retrieved nodes, if successful.
     */
    // @Nonnull
    SerializedChunk getResult();

    /**
     * Whether there was an issue with the nodeIds parameter.
     */
    boolean isValidNodeIds();

    /**
     * Whether there was an issue with the depthLimit parameter.
     */
    boolean isValidDepthLimit();

    /**
     * Requested node ids unknown to the repository.
     */
    List<String> getUnknownNodeIds();
}
