package io.lionweb.api.bulk.test.impl;

import io.lionweb.api.bulk.lowlevel.ILowlevelConfig;

import java.net.URI;

public class MpsConfig implements ILowlevelConfig {
    private URI baseUri = URI.create("http://127.0.0.1:63320/lionweb/api/bulk/");
    private boolean onlyPartitions = true;
    private boolean includeLanguages = false;
    private boolean includePackaged = false;

    public boolean isOnlyPartitions() {
        return onlyPartitions;
    }

    public void setOnlyPartitions(boolean onlyPartitions) {
        this.onlyPartitions = onlyPartitions;
    }

    public boolean isIncludeLanguages() {
        return includeLanguages;
    }

    public void setIncludeLanguages(boolean includeLanguages) {
        this.includeLanguages = includeLanguages;
    }

    public boolean isIncludePackaged() {
        return includePackaged;
    }

    public void setIncludePackaged(boolean includePackaged) {
        this.includePackaged = includePackaged;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(URI baseUri) {
        this.baseUri = baseUri;
    }

    String getUriBase() {
        String result = getBaseUri().toString();
        if (!result.endsWith("/")) {
            result += "/";
        }
        return result;
    }
}
