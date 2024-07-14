package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.protobuf.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtoBufSerialization {
    public JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();

    public BulkImport serializeBulkImport(List<BulkImportElement> elements) {
        BulkImport.Builder bulkImportBuilder = BulkImport.newBuilder();
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

        elements.forEach(bulkImportElement -> {
            io.lionweb.lioncore.protobuf.BulkImportElement.Builder bulkImportElementBuilder = io.lionweb.lioncore.protobuf.BulkImportElement.newBuilder();
            bulkImportElementBuilder.setMetaPointerIndex(metaPointerIndexer.apply(bulkImportElement.containment));
            SerializedChunk serializedChunk = jsonSerialization.serializeTreeToSerializationBlock(bulkImportElement.tree);

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
                } else {
                    nodeBuilder.clearParent();
                }
                //TODO n.getAnnotations()
                n.getProperties().forEach(p -> {
                            Property.Builder b = Property.newBuilder();
                            if (p.getValue() != null) {
                                b.setValue(p.getValue());
                            } else {
                                b.clearValue();
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
                                    } else {
                                        b.clearReferred();
                                    }
                                    if (rf.getResolveInfo() != null) {
                                        b.setResolveInfo(rf.getResolveInfo());
                                    } else {
                                        b.clearResolveInfo();
                                    }
                                    return b.build();
                                }).collect(Collectors.toList()))
                                .setMetaPointerIndex(metaPointerIndexer.apply(p.getMetaPointer()))
                                .build())
                );
                bulkImportElementBuilder.addTree(nodeBuilder.build());
            });

            bulkImportBuilder.addElements(bulkImportElementBuilder.build());
        });

        metaPointerDefs.values().forEach(mp -> bulkImportBuilder.addMetaPointerDefs(MetaPointerDef.newBuilder()
                        .setIndex(mp.getIndex())
                        .setLanguage(mp.getLanguage())
                        .setKey(mp.getKey())
                        .setVersion(mp.getVersion())
                .build()));
        return bulkImportBuilder.build();
    }

    public Chunk serializeTree(ClassifierInstance<?> classifierInstance) {
        if (classifierInstance instanceof ProxyNode) {
            throw new IllegalArgumentException("Proxy nodes cannot be serialized");
        }
        Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
        ClassifierInstance.collectSelfAndDescendants(classifierInstance, true, classifierInstances);

        SerializedChunk serializedChunk = jsonSerialization.serializeNodesToSerializationBlock(
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
                        } else {
                            b.clearValue();
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
