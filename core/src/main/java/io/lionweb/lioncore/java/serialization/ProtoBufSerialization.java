package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.protobuf.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtoBufSerialization {
    public JsonSerialization serialization = JsonSerialization.getStandardSerialization();

    public Chunk serializeTree(ClassifierInstance<?> classifierInstance) {
        if (classifierInstance instanceof ProxyNode) {
            throw new IllegalArgumentException("Proxy nodes cannot be serialized");
        }
        Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
        ClassifierInstance.collectSelfAndDescendants(classifierInstance, true, classifierInstances);

        SerializedChunk serializedChunk = serialization.serializeNodesToSerializationBlock(
                classifierInstances.stream()
                        .filter(n -> !(n instanceof ProxyNode)).collect(Collectors.toList()));
        return serialize(serializedChunk);
    }

    public Chunk serialize(SerializedChunk serializedChunk) {
        Chunk.Builder chunkBuilder = Chunk.newBuilder();
        chunkBuilder.setSerializationFormatVersion(serializedChunk.getSerializationFormatVersion());
        serializedChunk.getLanguages().forEach(ul ->{
            chunkBuilder.addLanguages(Language.newBuilder()
                            .setKey(ul.getKey())
                            .setVersion(ul.getVersion())
                    .build());
        });
        final Map<MetaPointer, MetaPointerDef> metaPointerDefs = new HashMap<>();

        Function<MetaPointer, Integer> metaPointerIndexer = metaPointer -> {
            if (metaPointerDefs.containsKey(metaPointer)) {
                return metaPointerDefs.get(metaPointer).getIndex();
            }
            MetaPointerDef metaPointerDef = MetaPointerDef.newBuilder()
                    .setIndex(metaPointerDefs.size() + 1)
                    .setKey(metaPointer.getKey())
                    .setVersion(metaPointer.getVersion())
                    .setLanguage(metaPointer.getLanguage())
                    .build();
            metaPointerDefs.put(metaPointer, metaPointerDef);
            return metaPointerDef.getIndex();
        };

        serializedChunk.getClassifierInstances().forEach(n ->{
            Node.Builder nodeBuilder = Node.newBuilder();
            nodeBuilder.setId(n.getID());
            nodeBuilder.setClassifier(Classifier.newBuilder()
                        .setKey(n.getClassifier().getKey())
                        .setVersion(n.getClassifier().getVersion())
                        .setLanguage(n.getClassifier().getLanguage())
                    .build());
            if (n.getParentNodeID() != null) {
                nodeBuilder.setParent(n.getParentNodeID());
            }
            //TODO n.getAnnotations()
            n.getProperties().forEach(p -> {
                        Property.Builder b = Property.newBuilder();
                        if (p.getValue() != null) {
                            b.setValue(p.getValue());
                        }
                        b.setMetaPointerIndex(metaPointerIndexer.apply(p.getMetaPointer()));
                        nodeBuilder.addProperties(b.build());
                    }
                    );
            n.getContainments().forEach(p ->
                    nodeBuilder.addContainments(Containment.newBuilder()
                            .addAllChildren(p.getValue())
                            .setMetaPointerIndex(metaPointerIndexer.apply(p.getMetaPointer()))
                            .build())
            );
            n.getReferences().forEach(p ->
                    nodeBuilder.addReferences(Reference.newBuilder()
                                    .addAllValues(p.getValue().stream().map(rf ->{ ReferenceValue.Builder b = ReferenceValue.newBuilder();
                                        if (rf.getReference() != null) {
                                            b.setReferred(rf.getReference());
                                        }
                                        if (rf.getResolveInfo() != null) {
                                            b.setResolveInfo(rf.getResolveInfo());
                                        }
                                            return b.build();
                                    }).collect(Collectors.toList()))
                            .setMetaPointerIndex(metaPointerIndexer.apply(p.getMetaPointer()))
                            .build())
            );
            chunkBuilder.addNodes(nodeBuilder.build());
        });
        return chunkBuilder.build();
    }
}
