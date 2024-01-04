package io.lionweb.api.bulk.lowlevel;

import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.util.List;
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;

public interface IBulkLowlevel {
    //    @Nonnull
    IPartitionsResponse partitions();

//    @Nonnull
    IRetrieveResponse retrieve(List<String> nodeIds,   /*@Nullable*/    String depthLimit);

    // @Nonnull
    IStoreResponse store(/*@Nonnull*/ SerializedChunk nodes, /*@Nullable*/ String mode);

    // @Nonnull
    IDeleteResponse delete(List<String> nodeIds);

    // @Nonnull
    IIdsResponse ids(/*@Nonnull*/ String count);
}
