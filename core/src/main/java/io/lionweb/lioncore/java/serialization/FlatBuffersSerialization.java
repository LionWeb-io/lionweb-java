package io.lionweb.lioncore.java.serialization;

import com.google.flatbuffers.FlatBufferBuilder;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.flatbuffers.*;
import io.lionweb.lioncore.protobuf.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class FlatBuffersSerialization extends AbstractSerialization {

    /**
     * This has specific support for LionCore or LionCoreBuiltins.
     */
    public static FlatBuffersSerialization getStandardSerialization() {
        FlatBuffersSerialization serialization = new FlatBuffersSerialization();
        serialization.classifierResolver.registerLanguage(LionCore.getInstance());
        serialization.instantiator.registerLionCoreCustomDeserializers();
        serialization.primitiveValuesSerialization
                .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
        serialization.instanceResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
        serialization.instanceResolver.addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
        return serialization;
    }

    /**
     * This has no specific support for LionCore or LionCoreBuiltins.
     */
    public static FlatBuffersSerialization getBasicSerialization() {
        FlatBuffersSerialization serialization = new FlatBuffersSerialization();
        return serialization;
    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(byte[] bytes) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return deserializeToNodes(FBChunk.getRootAsFBChunk(bb));
    }

//    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(File file) throws IOException {
//        return deserializeToNodes(new FileInputStream(file));
//    }
//
//    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(InputStream inputStream) throws IOException {
//
//        return deserializeToNodes(FBChunk..parseFrom(inputStream));
//    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(FBChunk chunk) {
        return deserializeToClassifierInstances(chunk).stream()
                .filter(ci -> ci instanceof io.lionweb.lioncore.java.model.Node)
                .map(ci -> (io.lionweb.lioncore.java.model.Node) ci)
                .collect(Collectors.toList());
    }

    public List<ClassifierInstance<?>> deserializeToClassifierInstances(FBChunk chunk) {
        SerializedChunk serializationBlock =
                deserializeSerializationChunk(chunk);
        validateSerializationBlock(serializationBlock);
        return deserializeSerializationBlock(serializationBlock);
    }

    private SerializedChunk deserializeSerializationChunk(FBChunk chunk) {
        Map<Integer, String> stringsMap = new HashMap<>();
        for (int i = 0; i < chunk.stringValuesLength(); i++) {
            stringsMap.put(i, chunk.stringValues(i));
        }
        Map<Integer, MetaPointer> metapointersMap = new HashMap<>();
        for (int i = 0; i < chunk.metapointersLength(); i++) {
            FBMetaPointer mp = chunk.metapointers(i);
            MetaPointer metaPointer = new MetaPointer();
            metaPointer.setKey(stringsMap.get(mp.key()));
            metaPointer.setLanguage(stringsMap.get(mp.language()));
            metaPointer.setVersion(stringsMap.get(mp.version()));
            metapointersMap.put(i, metaPointer);
        };

        SerializedChunk serializedChunk = new SerializedChunk();
        serializedChunk.setSerializationFormatVersion(chunk.serializationFormatVersion());
        for (int i = 0; i < chunk.languagesLength(); i++) {
            FBLanguage l = chunk.languages(i);
            UsedLanguage usedLanguage = new UsedLanguage();
            usedLanguage.setKey(stringsMap.get(l.key()));
            usedLanguage.setVersion(stringsMap.get(l.version()));
            serializedChunk.addLanguage(usedLanguage);
        };

        for (int i = 0; i < chunk.nodesLength(); i++) {
            FBNode n = chunk.nodes(i);
            SerializedClassifierInstance sci = new SerializedClassifierInstance();
            sci.setID(stringsMap.get(n.id()));
            sci.setParentNodeID(stringsMap.get(n.parent()));
            sci.setClassifier(metapointersMap.get(n.classifier()));
            for (int j = 0; j < n.propertiesLength(); j++) {
                FBProperty p = n.properties(j);
                SerializedPropertyValue spv = new SerializedPropertyValue();
                spv.setValue(stringsMap.get(p.value()));
                spv.setMetaPointer(metapointersMap.get(p.metaPointerIndex()));
                sci.addPropertyValue(spv);
            };
            for (int j = 0; j < n.containmentsLength(); j++) {
                FBContainment c = n.containments(j);
                SerializedContainmentValue scv = new SerializedContainmentValue();
                List<String> children = new ArrayList<>(c.childrenLength());
                for (int k = 0; k < c.childrenLength(); k++) {
                    String child = stringsMap.get(c.children(k));
                    children.set(k, child);
                }
                scv.setValue(children);
                scv.setMetaPointer(metapointersMap.get(c.metaPointerIndex()));
                sci.addContainmentValue(scv);
            };
            for (int j = 0; j < n.referencesLength(); j++) {
                FBReference r = n.references(j);
                SerializedReferenceValue srv = new SerializedReferenceValue();
                for (int k = 0; k < r.valuesLength(); k++) {
                    FBReferenceValue rv = r.values(k);
                    SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
                    entry.setReference(stringsMap.get(rv.referred()));
                    entry.setResolveInfo(stringsMap.get(rv.resolveInfo()));
                    srv.addValue(entry);
                };
                srv.setMetaPointer(metapointersMap.get(r.metaPointerIndex()));
                sci.addReferenceValue(srv);
            };
            // TODO
//          n.getAnnotationsList().forEach(a -> {
//              sci.getAnnotations().add(a);
//          });
            serializedChunk.addClassifierInstance(sci);
        };
        return serializedChunk;
    }

    public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
        Set<String> nodesIDs = new HashSet<>();
        List<ClassifierInstance<?>> allNodes = new ArrayList<>();
        for (ClassifierInstance<?> root : roots) {
            Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
            ClassifierInstance.collectSelfAndDescendants(root, true, classifierInstances);
            classifierInstances.forEach(
                    n -> {
                        // We support serialization of incorrect nodes, so we allow nodes without ID to be
                        // serialized
                        if (n.getID() != null) {
                            if (!nodesIDs.contains(n.getID())) {
                                allNodes.add(n);
                                nodesIDs.add(n.getID());
                            }
                        } else {
                            allNodes.add(n);
                        }
                    });
        }
        return serializeNodesToByteArray(
                allNodes.stream().filter(n -> !(n instanceof ProxyNode)).collect(Collectors.toList()));
    }

    public byte[] serializeNodesToByteArray(List<ClassifierInstance<?>> classifierInstances) {
        if (classifierInstances.stream().anyMatch(n -> n instanceof ProxyNode)) {
            throw new IllegalArgumentException("Proxy nodes cannot be serialized");
        }
        SerializedChunk serializationBlock = serializeNodesToSerializationBlock(classifierInstances);
        return serializeToByteArray(serializationBlock);
    }

    public byte[] serializeNodesToByteArray(ClassifierInstance<?>... classifierInstances) {
        return serializeNodesToByteArray(Arrays.asList(classifierInstances));
    }

    public byte[] serializeToByteArray(SerializedChunk serializedChunk) {
        return serialize(serializedChunk).getByteBuffer().array();
    }

    private class SerializeHelper {
        final Map<MetaPointer, Integer> metaPointers = new HashMap<>();
        final Map<String, Integer> strings = new HashMap<>();

        int stringIndexer(String string) {
            if (string == null) {
                return -1;
            }
            if (strings.containsKey(string)) {
                return strings.get(string);
            }
            int index = strings.size();
            strings.put(string, index);
            return index;
        }

        ;

        int metaPointerIndexer(MetaPointer metaPointer, FlatBufferBuilder flatBufferBuilder) {
            if (metaPointers.containsKey(metaPointer)) {
                return metaPointers.get(metaPointer);
            }
            FBMetaPointer.startFBMetaPointer(flatBufferBuilder);
            FBMetaPointer.addKey(flatBufferBuilder, stringIndexer(metaPointer.getKey()));
            FBMetaPointer.addVersion(flatBufferBuilder, stringIndexer(metaPointer.getVersion()));
            FBMetaPointer.addLanguage(flatBufferBuilder, stringIndexer(metaPointer.getLanguage()));
            FBMetaPointer.endFBMetaPointer(flatBufferBuilder);
            int index = metaPointers.size();
            metaPointers.put(metaPointer, index);
            return index;
        }

        Node serializeNode(SerializedClassifierInstance n) {
            Node.Builder nodeBuilder = Node.newBuilder();
            nodeBuilder.setId(this.stringIndexer(n.getID()));
            nodeBuilder.setClassifier(this.metaPointerIndexer((n.getClassifier())));
            nodeBuilder.setParent(this.stringIndexer(n.getParentNodeID()));
            // TODO n.getAnnotations()
            n.getProperties()
                    .forEach(
                            p -> {
                                Property.Builder b = Property.newBuilder();
                                b.setValue(this.stringIndexer(p.getValue()));
                                b.setMetaPointerIndex(this.metaPointerIndexer((p.getMetaPointer())));
                                nodeBuilder.addProperties(b.build());
                            });
            n.getContainments()
                    .forEach(
                            p ->
                                    nodeBuilder.addContainments(
                                            Containment.newBuilder()
                                                    .addAllChildren(p.getValue().stream().map(v -> this.stringIndexer(v)).collect(Collectors.toList()))
                                                    .setMetaPointerIndex(
                                                            this.metaPointerIndexer((p.getMetaPointer())))
                                                    .build()));
            n.getReferences()
                    .forEach(
                            p ->
                                    nodeBuilder.addReferences(
                                            Reference.newBuilder()
                                                    .addAllValues(
                                                            p.getValue().stream()
                                                                    .map(
                                                                            rf -> {
                                                                                ReferenceValue.Builder b =
                                                                                        ReferenceValue.newBuilder();
                                                                                b.setReferred(this.stringIndexer(rf.getReference()));
                                                                                b.setResolveInfo(this.stringIndexer(rf.getResolveInfo()));
                                                                                return b.build();
                                                                            })
                                                                    .collect(Collectors.toList()))
                                                    .setMetaPointerIndex(
                                                            this.metaPointerIndexer((p.getMetaPointer())))
                                                    .build()));
            return nodeBuilder.build();
        }
    }



//  public BulkImport serializeBulkImport(List<BulkImportElement> elements) {
//    BulkImport.Builder bulkImportBuilder = BulkImport.newBuilder();
//      FlatBuffersSerialization.SerializeHelper serializeHelper = new FlatBuffersSerialization.SerializeHelper();
//
//    elements.forEach(
//        bulkImportElement -> {
//          io.lionweb.lioncore.protobuf.BulkImportElement.Builder bulkImportElementBuilder =
//              io.lionweb.lioncore.protobuf.BulkImportElement.newBuilder();
//          bulkImportElementBuilder.setMetaPointerIndex(
//                  serializeHelper.metaPointerIndexer(bulkImportElement.containment));
//          SerializedChunk serializedChunk =
//              serializeTreeToSerializationBlock(bulkImportElement.tree);
//
//          serializedChunk
//              .getClassifierInstances()
//              .forEach(
//                  n -> {
//                    Node.Builder nodeBuilder = Node.newBuilder();
//                    nodeBuilder.setId(serializeHelper.stringIndexer(n.getID()));
//                    nodeBuilder.setClassifier(serializeHelper.metaPointerIndexer((n.getClassifier())));
//                    nodeBuilder.setParent(serializeHelper.stringIndexer(n.getParentNodeID()));
//                    // TODO n.getAnnotations()
//                    n.getProperties()
//                        .forEach(
//                            p -> {
//                              Property.Builder b = Property.newBuilder();
//                              b.setValue(serializeHelper.stringIndexer(p.getValue()));
//                              b.setMetaPointerIndex(serializeHelper.metaPointerIndexer((p.getMetaPointer())));
//                              nodeBuilder.addProperties(b.build());
//                            });
//                    n.getContainments()
//                        .forEach(
//                            p ->
//                                nodeBuilder.addContainments(
//                                    Containment.newBuilder()
//                                        .addAllChildren(p.getValue().stream().map(v -> serializeHelper.stringIndexer(v)).collect(Collectors.toList()))
//                                        .setMetaPointerIndex(
//                                            serializeHelper.metaPointerIndexer((p.getMetaPointer())))
//                                        .build()));
//                    n.getReferences()
//                        .forEach(
//                            p ->
//                                nodeBuilder.addReferences(
//                                    Reference.newBuilder()
//                                        .addAllValues(
//                                            p.getValue().stream()
//                                                .map(
//                                                    rf -> {
//                                                      ReferenceValue.Builder b =
//                                                          ReferenceValue.newBuilder();
//                                                      b.setReferred(serializeHelper.stringIndexer(rf.getReference()));
//                                                        b.setResolveInfo(serializeHelper.stringIndexer(rf.getResolveInfo()));
//                                                      return b.build();
//                                                    })
//                                                .collect(Collectors.toList()))
//                                        .setMetaPointerIndex(
//                                            serializeHelper.metaPointerIndexer((p.getMetaPointer())))
//                                        .build()));
//                    bulkImportElementBuilder.addTree(nodeBuilder.build());
//                  });
//
//          bulkImportBuilder.addElements(bulkImportElementBuilder.build());
//        });
//
//    serializeHelper.metaPointers
//        .entrySet()
//            .stream().sorted()
//        .forEach(
//            entry ->
//                bulkImportBuilder.addMetaPointerDefs(
//                    io.lionweb.lioncore.protobuf.MetaPointer.newBuilder()
//                        .setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()))
//                        .setKey(serializeHelper.stringIndexer(entry.getKey().getKey()))
//                        .setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()))
//                        .build()));
//    return bulkImportBuilder.build();
//  }

  public FBChunk serializeTree(ClassifierInstance<?> classifierInstance) {
    if (classifierInstance instanceof ProxyNode) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
    ClassifierInstance.collectSelfAndDescendants(classifierInstance, true, classifierInstances);

    SerializedChunk serializedChunk =
        serializeNodesToSerializationBlock(
            classifierInstances.stream()
                .filter(n -> !(n instanceof ProxyNode))
                .collect(Collectors.toList()));
    return serialize(serializedChunk);
  }

  public FBChunk serialize(SerializedChunk serializedChunk) {
      FlatBufferBuilder builder = new FlatBufferBuilder(1024);
      FBChunk.createFBChunk(builder,
        builder.createSharedString(serializedChunk.getSerializationFormatVersion());

    Chunk.Builder chunkBuilder = Chunk.newBuilder();
    chunkBuilder.setSerializationFormatVersion(serializedChunk.getSerializationFormatVersion());
    SerializeHelper serializeHelper = new SerializeHelper();
    serializedChunk
        .getLanguages()
        .forEach(
            ul -> {
              chunkBuilder.addLanguages(
                  Language.newBuilder()
                          .setKey(serializeHelper.stringIndexer(ul.getKey()))
                          .setVersion(serializeHelper.stringIndexer(ul.getVersion()))
                          .build());
            });

    serializedChunk
        .getClassifierInstances()
        .forEach(
            n -> chunkBuilder.addNodes(serializeHelper.serializeNode(n)));

    serializeHelper.strings.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
        chunkBuilder.addStringValues(entry.getKey());
    });
      serializeHelper.metaPointers.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
          io.lionweb.lioncore.protobuf.MetaPointer.Builder metaPointer = io.lionweb.lioncore.protobuf.MetaPointer.newBuilder();
          metaPointer.setKey(serializeHelper.stringIndexer(entry.getKey().getKey()));
          metaPointer.setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()));
          metaPointer.setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()));
          chunkBuilder.addMetaPointers(metaPointer.build());
      });
    return chunkBuilder.build();
  }
}
