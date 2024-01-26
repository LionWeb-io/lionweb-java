package io.lionweb.api.bulk.wrapper;

import io.lionweb.api.bulk.lowlevel.*;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.SerializedContainmentValue;
import io.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IdMappingWrapper<C extends ILowlevelConfig> implements IBulkLowlevel<C> {
    private final IBulkLowlevel<C> delegate;
    private final IIdMapper idMapper;

    public IdMappingWrapper(IBulkLowlevel<C> delegate, IIdMapper idMapper){
        this.delegate = delegate;
        this.idMapper = idMapper;
    }

    @Override
    public IPartitionsResponse partitions() {
        IPartitionsResponse response = delegate.partitions();
        return new IPartitionsResponse() {
            @Override
            public SerializedChunk getResult() {
                return mapToExternal(response.getResult());
            }

            @Override
            public boolean isOk() {
                return response.isOk();
            }

            @Override
            public String getErrorMessage() {
                return response.getErrorMessage();
            }
        };
    }

    @Override
    public IRetrieveResponse retrieve(List<String> nodeIds, String depthLimit) {
        List<String> mappedNodeIds = mapToInternal(nodeIds);
        IRetrieveResponse response = delegate.retrieve(mappedNodeIds, depthLimit);
        return new IRetrieveResponse() {
            @Override
            public SerializedChunk getResult() {
                return mapToExternal(response.getResult()) ;
            }

            @Override
            public boolean isValidNodeIds() {
                return response.isValidNodeIds();
            }

            @Override
            public boolean isValidDepthLimit() {
                return response.isValidDepthLimit();
            }

            @Override
            public List<String> getUnknownNodeIds() {
                return mapToExternal(response.getUnknownNodeIds());
            }

            @Override
            public boolean isOk() {
                return response.isOk();
            }

            @Override
            public String getErrorMessage() {
                return response.getErrorMessage();
            }
        };
    }

    @Override
    public IStoreResponse store(SerializedChunk nodes, String mode) {
        SerializedChunk mappedNodes = mapToInternal(nodes);
        IStoreResponse response = delegate.store(mappedNodes, mode);
        return response;
    }

    @Override
    public IDeleteResponse delete(List<String> nodeIds) {
        List<String> mappedNodeIds = mapToInternal(nodeIds);
        IDeleteResponse response = delegate.delete(mappedNodeIds);
        return new IDeleteResponse() {
            @Override
            public boolean isValidNodeIds() {
                return response.isValidNodeIds();
            }

            @Override
            public List<String> getUnknownNodeIds() {
                return mapToExternal(response.getUnknownNodeIds());
            }

            @Override
            public boolean isOk() {
                return response.isOk();
            }

            @Override
            public String getErrorMessage() {
                return response.getErrorMessage();
            }
        };
    }

    @Override
    public C getConfig() {
        return delegate.getConfig();
    }

    @Override
    public void setConfig(C config) {
        delegate.setConfig(config);
    }

    @Override
    public IIdsResponse ids(String count) {
        IIdsResponse response = delegate.ids(count);
        return new IIdsResponse() {
            @Override
            public List<String> getIds() {
                return mapToExternal(response.getIds());
            }

            @Override
            public boolean isValidCount() {
                return response.isValidCount();
            }

            @Override
            public boolean isOk() {
                return response.isOk();
            }

            @Override
            public String getErrorMessage() {
                return response.getErrorMessage();
            }
        };
    }

    private SerializedChunk mapToExternal(SerializedChunk input) {
        return map(input, idMapper::internalToExternal);
    }

    private SerializedChunk mapToInternal(SerializedChunk input) {
        return map(input, idMapper::externalToInternal);
    }

    private List<String> mapToExternal(List<String> nodeIds) {
        return map(nodeIds, idMapper::internalToExternal);
    }

    private List<String> mapToInternal(List<String> nodeIds) {
        return map(nodeIds, idMapper::externalToInternal);
    }

    private SerializedChunk map(SerializedChunk input, Function<String, String> mapper) {
        SerializedChunk result = new SerializedChunk();

        result.setSerializationFormatVersion(input.getSerializationFormatVersion());
        input.getLanguages().forEach(result::addLanguage);

        input.getClassifierInstances().forEach(ci ->
        {
            SerializedClassifierInstance mci = new SerializedClassifierInstance(mapper.apply(ci.getID()), ci.getClassifier());
            mci.setParentNodeID(ci.getParentNodeID());
            mci.setAnnotations(map(ci.getAnnotations(), mapper));
            ci.getProperties().forEach(mci::addPropertyValue);
            ci.getContainments().forEach(c -> mci.addContainmentValue(new SerializedContainmentValue(c.getMetaPointer(), map(c.getValue(), mapper))));
            ci.getReferences().forEach(r -> mci.addReferenceValue(r.getMetaPointer(), r.getValue().stream().map(e -> new SerializedReferenceValue.Entry(mapper.apply(e.getReference()), e.getResolveInfo())).collect(Collectors.toList())));
            result.addClassifierInstance(mci);
        });

        return result;
    }

    private List<String> map(List<String> ids, Function<String, String> mapper) {
        return ids.stream().map(mapper).collect(Collectors.toList());
    }
}
