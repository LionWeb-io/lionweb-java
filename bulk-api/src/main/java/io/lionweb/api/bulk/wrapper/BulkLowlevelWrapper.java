package io.lionweb.api.bulk.wrapper;

import io.lionweb.api.bulk.BulkException;
import io.lionweb.api.bulk.IBulk;
import io.lionweb.api.bulk.StoreMode;
import io.lionweb.api.bulk.lowlevel.*;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.util.List;
import java.util.Objects;

public class BulkLowlevelWrapper implements IBulk {
    private final IBulkLowlevel lowlevel;

    public BulkLowlevelWrapper(/*@Nonnull*/ IBulkLowlevel lowlevel) {
        this.lowlevel = lowlevel;
    }

    @Override
    public SerializedChunk partitions() throws BulkException {
        IPartitionsResponse response = lowlevel.partitions();

        if(response.isOk()) {
            Objects.requireNonNull(response.getResult());
            return response.getResult();
        }

        throw new BulkException(response.getErrorMessage());
    }

    @Override
    public SerializedChunk retrieve(List<String> nodeIds, Integer depthLimit) throws BulkException {
        Objects.requireNonNull(nodeIds);
        nodeIds.forEach(Objects::requireNonNull);

        IRetrieveResponse response = lowlevel.retrieve(nodeIds, Objects.toString(depthLimit, null));

        if(response.isOk()) {
            Objects.requireNonNull(response.getResult());
            return response.getResult();
        }

        throw new BulkException(response.getErrorMessage());
    }

    @Override
    public void store(SerializedChunk nodes, StoreMode mode) throws BulkException {
        Objects.requireNonNull(nodes);
        String modeString = mode !=null ? mode.name().toLowerCase() : null;
        IStoreResponse response = lowlevel.store(nodes, modeString);

        if(response.isOk()) {
            return;
        }

        throw new BulkException(response.getErrorMessage());
    }

    @Override
    public void delete(List<String> nodeIds) throws BulkException {
        Objects.requireNonNull(nodeIds);
        nodeIds.forEach(Objects::requireNonNull);

        IDeleteResponse response = lowlevel.delete(nodeIds);

        if(response.isOk()) {
            return;
        }

        throw new BulkException(response.getErrorMessage());
    }

    @Override
    public List<String> ids(Integer count) throws BulkException {
        Objects.requireNonNull(count);

        IIdsResponse response = lowlevel.ids(count.toString());

        if(response.isOk()) {
            Objects.requireNonNull(response.getIds());
            response.getIds().forEach(Objects::requireNonNull);
            return response.getIds();
        }

        throw new BulkException(response.getErrorMessage());
    }
}