package org.lionweb.lioncore.java.api;

import org.lionweb.lioncore.java.model.Node;

import javax.annotation.Nullable;

public interface NodeResolver {
    @Nullable
    Node resolve(String nodeID);
}
