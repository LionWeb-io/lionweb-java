package io.lionweb.api.bulk.wrapper;

public interface IIdMapper {
    String externalToInternal(String externalId);

    String internalToExternal(String internalId);
}
