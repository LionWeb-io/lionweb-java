package io.lionweb.api.bulk.test.impl;

import io.lionweb.api.bulk.lowlevel.ILowlevelConfig;

import java.net.URI;

public class LionwebRepositoryConfig implements ILowlevelConfig {
    private URI baseUri = URI.create("http://127.0.0.1:3005/bulk/");

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
