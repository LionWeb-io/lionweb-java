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
import java.util.function.Function;
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

    private class DeserializationHelper {

        public MetaPointer deserialize(FBMetaPointer classifier) {
            throw new UnsupportedOperationException();
        }

        public SerializedContainmentValue deserialize(FBContainment containment) {
            SerializedContainmentValue scv = new SerializedContainmentValue();
            List<String> children = new ArrayList<>(containment.childrenLength());
            for (int k = 0; k < containment.childrenLength(); k++) {
                String child = containment.children(k);
                children.set(k, child);
            }
            scv.setValue(children);
            scv.setMetaPointer(deserialize(containment.metaPointer()));
            return scv;
        }
    }

    private SerializedChunk deserializeSerializationChunk(FBChunk chunk) {
        DeserializationHelper helper = new DeserializationHelper();

        SerializedChunk serializedChunk = new SerializedChunk();
        serializedChunk.setSerializationFormatVersion(chunk.serializationFormatVersion());
        for (int i = 0; i < chunk.languagesLength(); i++) {
            FBLanguage l = chunk.languages(i);
            UsedLanguage usedLanguage = new UsedLanguage();
            usedLanguage.setKey(l.key());
            usedLanguage.setVersion(l.version());
            serializedChunk.addLanguage(usedLanguage);
        };

        for (int i = 0; i < chunk.nodesLength(); i++) {
            FBNode n = chunk.nodes(i);
            SerializedClassifierInstance sci = new SerializedClassifierInstance();
            sci.setID(n.id());
            sci.setParentNodeID(n.parent());
            sci.setClassifier(helper.deserialize(n.classifier()));
            for (int j = 0; j < n.propertiesLength(); j++) {
                FBProperty p = n.properties(j);
                SerializedPropertyValue spv = new SerializedPropertyValue();
                spv.setValue(p.value());
                spv.setMetaPointer(helper.deserialize(p.metaPointer()));
                sci.addPropertyValue(spv);
            };
            for (int j = 0; j < n.containmentsLength(); j++) {
                FBContainment c = n.containments(j);
                sci.addContainmentValue(helper.deserialize(c));
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
        return serialize(serializationBlock);
    }

    public byte[] serializeNodesToByteArray(ClassifierInstance<?>... classifierInstances) {
        return serializeNodesToByteArray(Arrays.asList(classifierInstances));
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

  public byte[] serializeTree(ClassifierInstance<?> classifierInstance) {
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

  private class FBHelper {
        FlatBufferBuilder builder;
        Map<MetaPointer, Integer> serializedMetapointers = new HashMap<>();
        FBHelper(FlatBufferBuilder builder) {
            this.builder = builder;
        }

        /**
         * This method can create objects, so it should not be nested inside another object creation.
         */
        int offsetForMetaPointer(MetaPointer metaPointer) {
            if (!serializedMetapointers.containsKey(metaPointer)) {
                int fbMetapointer = FBMetaPointer.createFBMetaPointer(builder,
                        builder.createSharedString(metaPointer.getLanguage()),
                        builder.createSharedString(metaPointer.getKey()),
                        builder.createSharedString(metaPointer.getVersion()));
                serializedMetapointers.put(metaPointer, fbMetapointer);
            }
            return serializedMetapointers.get(metaPointer);
        }

        int offsetForLanguage(UsedLanguage usedLanguage) {
            return FBLanguage.createFBLanguage(builder,
                    builder.createSharedString(usedLanguage.getKey()), builder.createSharedString(usedLanguage.getVersion()));
        }

        int[] languagesVector(List<UsedLanguage> usedLanguages) {
            int[] languagesOffsets = new int[usedLanguages.size()];
            for (int i = 0; i < usedLanguages.size(); i++) {
                UsedLanguage ul = usedLanguages.get(i);
                languagesOffsets[i] = FBLanguage.createFBLanguage(builder,
                        builder.createSharedString(ul.getKey()), builder.createSharedString(ul.getVersion()));
            }
            return languagesOffsets;
        }

        int[] propsVector(List<SerializedPropertyValue> properties) {
            int[] props = new int[properties.size()];
            for (int j = 0; j < properties.size(); j++) {
                SerializedPropertyValue el = properties.get(j);
                props[j] = FBProperty.createFBProperty(builder, offsetForMetaPointer(el.getMetaPointer()),
                        builder.createSharedString(el.getValue()));
            }
            return props;
        }

        int[] containmentsVector(List<SerializedContainmentValue> containments) {
            int[] cons = new int[containments.size()];
            for (int j = 0; j < containments.size(); j++) {
                SerializedContainmentValue el =containments.get(j);
                int[] children = new int[el.getValue().size()];
                for (int k = 0; k < el.getValue().size(); k++) {
                    children[k] = builder.createSharedString(el.getValue().get(k));
                }
                cons[j] = FBContainment.createFBContainment(builder, offsetForMetaPointer(el.getMetaPointer()),
                        FBContainment.createChildrenVector(builder, children));
            }
            return cons;
        }

        int[] referencesVector(List<SerializedReferenceValue> references) {
            int[] refs = new int[references.size()];
            for (int j = 0; j < references.size(); j++) {
                SerializedReferenceValue el = references.get(j);
                int[] values = new int[el.getValue().size()];
                for (int k = 0; k < el.getValue().size(); k++) {
                    values[k] = FBReferenceValue.createFBReferenceValue(builder,
                            builder.createSharedString(el.getValue().get(k).getResolveInfo()),
                            builder.createSharedString(el.getValue().get(k).getReference()));
                }
                refs[j] = FBReference.createFBReference(builder, offsetForMetaPointer(el.getMetaPointer()),
                        FBReference.createValuesVector(builder, values));
            }
            return refs;
        }

        int[] annotationsVector(List<String> annotations) {
            int[] anns = new int[annotations.size()];
            if (anns.length > 0) {
                throw new UnsupportedOperationException();
            }
            return anns;
        }
    }

  public byte[] serialize(SerializedChunk serializedChunk) {
      FlatBufferBuilder builder = new FlatBufferBuilder(1024);

      FBHelper helper = new FBHelper(builder);

      int[] languagesOffsets = helper.languagesVector(serializedChunk.getLanguages());

      int[] nodesOffsets = new int[serializedChunk.getClassifierInstances().size()];
      for (int i = 0; i < serializedChunk.getClassifierInstances().size(); i++) {
          SerializedClassifierInstance sci = serializedChunk.getClassifierInstances().get(i);

          nodesOffsets[i] = builder.offset();
          int idOffset = builder.createSharedString(sci.getID());
          int parentOffset = sci.getParentNodeID() == null ? -1 : builder.createSharedString(sci.getParentNodeID());
          int propsVector = FBNode.createPropertiesVector(builder, helper.propsVector(sci.getProperties()));
          int consVector = FBNode.createContainmentsVector(builder, helper.containmentsVector(sci.getContainments()));
          int refsVector = FBNode.createReferencesVector(builder, helper.referencesVector(sci.getReferences()));
          int annsVector = FBNode.createAnnotationsVector(builder, helper.annotationsVector(sci.getAnnotations()));
          FBNode.startFBNode(builder);
          FBNode.addId(builder, idOffset);
          FBNode.addProperties(builder, propsVector);
          FBNode.addContainments(builder, consVector);
          FBNode.addReferences(builder, refsVector);
          FBNode.addAnnotations(builder, annsVector);

          if (parentOffset != -1) {
              FBNode.addParent(builder, parentOffset);
          }
          FBNode.endFBNode(builder);
      }

      int chunk = FBChunk.createFBChunk(builder,
        builder.createSharedString(serializedChunk.getSerializationFormatVersion()),
              FBChunk.createLanguagesVector(builder, languagesOffsets),
              FBChunk.createNodesVector(builder, nodesOffsets)
              );

    builder.finish(chunk);
    return builder.dataBuffer().array();
  }
}
