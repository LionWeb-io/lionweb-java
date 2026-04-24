package io.lionweb.serialization.data;

import io.lionweb.language.Classifier;

public class Schema {
    public MetaPointer classifier;
    public MetaPointer[] properties;
    public MetaPointer[] containments;
    public MetaPointer[] references;

    public static Schema fromMetaPointer(MetaPointer classifier) {
        throw new UnsupportedOperationException();
    }

    public static Schema fromClassifier(Classifier<?> classifier) {
        throw new UnsupportedOperationException();
    }
}
