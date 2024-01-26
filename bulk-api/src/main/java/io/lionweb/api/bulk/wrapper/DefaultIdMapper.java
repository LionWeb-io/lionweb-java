package io.lionweb.api.bulk.wrapper;

import java.util.Map;
import java.util.stream.Collectors;

public class DefaultIdMapper implements IIdMapper {
    private final Map<String, String> externalToInternal;
    private final Map<String, String> internalToExternal;

    public DefaultIdMapper(Map<String, String> externalToInternal) {
        this.externalToInternal = externalToInternal;
        this.internalToExternal = externalToInternal.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public String externalToInternal(String externalId) {
        return externalToInternal.getOrDefault(externalId, externalId);
    }

    @Override
    public String internalToExternal(String internalId) {
        return internalToExternal.getOrDefault(internalId, internalId);
    }
}
