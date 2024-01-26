package io.lionweb.api.bulk;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.util.List;

public interface IBulk {
    //    @Nonnull
    SerializedChunk partitions() throws BulkException;

    //    @Nonnull
    SerializedChunk retrieve(List<String> nodeIds,   /*@Nullable*/    Integer depthLimit) throws BulkException;

    // @Nonnull
    void store(/*@Nonnull*/ SerializedChunk nodes, /*@Nullable*/ StoreMode mode) throws BulkException;

    // @Nonnull
    void delete(List<String> nodeIds) throws BulkException;

    // @Nonnull
    List<String> ids(/*@Nonnull*/ Integer count) throws BulkException;
}
