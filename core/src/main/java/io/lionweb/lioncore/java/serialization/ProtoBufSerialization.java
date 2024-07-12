package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.protobuf.*;

import java.util.HashMap;
import java.util.Map;

public class ProtoBufSerialization {
    public Chunk serialize(SerializedChunk serializedChunk) {
        Chunk.Builder chunkBuilder = Chunk.newBuilder();
        chunkBuilder.setSerializationFormatVersion(serializedChunk.getSerializationFormatVersion());
        serializedChunk.getLanguages().forEach(ul ->{
            chunkBuilder.addLanguages(Language.newBuilder()
                            .setKey(ul.getKey())
                            .setVersion(ul.getVersion())
                    .build());
        });
        Map<MetaPointer, MetaPointerDef> metaPointerDefs = new HashMap<>();
        serializedChunk.getClassifierInstances().forEach(n ->{
            Node.Builder nodeBuilder = Node.newBuilder();
            nodeBuilder.setId(n.getID());
            nodeBuilder.setClassifier(Classifier.newBuilder()
                        .setKey(n.getClassifier().getKey())
                        .setVersion(n.getClassifier().getVersion())
                        .setLanguage(n.getClassifier().getLanguage())
                    .build());
            nodeBuilder.setParent(n.getParentNodeID());
            chunkBuilder.addNodes(nodeBuilder.build());
        });
        return chunkBuilder.build();
    }
}
