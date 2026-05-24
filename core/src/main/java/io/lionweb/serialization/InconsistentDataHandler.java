package io.lionweb.serialization;

import io.lionweb.language.Classifier;
import io.lionweb.serialization.data.MetaPointer;

public interface InconsistentDataHandler {
    void handleMissingProperty(Classifier<?> classifier, MetaPointer metaPointer);
}
