package org.lionweb.lioncore.java.serialization;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.AnnotationInstance;
import org.lionweb.lioncore.java.model.Model;
import org.lionweb.lioncore.java.model.Node;
import org.lionweb.lioncore.java.model.impl.DynamicNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

abstract class DummyNode extends DynamicNode {

    public DummyNode(String id, Concept concept) {
        super(id, concept);
    }
}
