package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.protobuf.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProtoBufSerialization extends AbstractSerialization {

  public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(byte[] bytes)
      throws IOException {
    return deserializeToNodes(new ByteArrayInputStream(bytes));
  }

  public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(File file)
      throws IOException {
    return deserializeToNodes(new FileInputStream(file));
  }

  public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(InputStream inputStream)
      throws IOException {
    return deserializeToNodes(PBChunk.parseFrom(inputStream));
  }

  public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(PBChunk chunk) {
    return deserializeToClassifierInstances(chunk).stream()
        .filter(ci -> ci instanceof io.lionweb.lioncore.java.model.Node)
        .map(ci -> (io.lionweb.lioncore.java.model.Node) ci)
        .collect(Collectors.toList());
  }

  public List<ClassifierInstance<?>> deserializeToClassifierInstances(PBChunk chunk) {
    SerializedChunk serializationBlock = deserializeSerializationChunk(chunk);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationBlock(serializationBlock);
  }

  private SerializedChunk deserializeSerializationChunk(PBChunk chunk) {
    Map<Integer, String> stringsMap = new HashMap<>();
    for (int i = 0; i < chunk.getStringValuesCount(); i++) {
      stringsMap.put(i, chunk.getStringValues(i));
    }
    Map<Integer, MetaPointer> metapointersMap = new HashMap<>();
    for (int i = 0; i < chunk.getMetaPointersCount(); i++) {
      PBMetaPointer mp = chunk.getMetaPointers(i);
      MetaPointer metaPointer = new MetaPointer();
      metaPointer.setKey(stringsMap.get(mp.getKey()));
      metaPointer.setLanguage(stringsMap.get(mp.getLanguage()));
      metaPointer.setVersion(stringsMap.get(mp.getVersion()));
      metapointersMap.put(i, metaPointer);
    }
    ;

    SerializedChunk serializedChunk = new SerializedChunk();
    serializedChunk.setSerializationFormatVersion(chunk.getSerializationFormatVersion());
    chunk
        .getLanguagesList()
        .forEach(
            l -> {
              UsedLanguage usedLanguage = new UsedLanguage();
              usedLanguage.setKey(stringsMap.get(l.getKey()));
              usedLanguage.setVersion(stringsMap.get(l.getVersion()));
              serializedChunk.addLanguage(usedLanguage);
            });

    chunk
        .getNodesList()
        .forEach(
            n -> {
              SerializedClassifierInstance sci = new SerializedClassifierInstance();
              sci.setID(stringsMap.get(n.getId()));
              sci.setParentNodeID(stringsMap.get(n.getParent()));
              sci.setClassifier(metapointersMap.get(n.getClassifier()));
              n.getPropertiesList()
                  .forEach(
                      p -> {
                        SerializedPropertyValue spv = new SerializedPropertyValue();
                        spv.setValue(stringsMap.get(p.getValue()));
                        spv.setMetaPointer(metapointersMap.get(p.getMetaPointerIndex()));
                        sci.addPropertyValue(spv);
                      });
              n.getContainmentsList()
                  .forEach(
                      c -> {
                        SerializedContainmentValue scv = new SerializedContainmentValue();
                        if (c.getChildrenList().stream().anyMatch(el -> el < 0)) {
                          throw new DeserializationException(
                              "Unable to deserialize child identified by Null ID");
                        }
                        scv.setValue(
                            c.getChildrenList().stream()
                                .map(el -> stringsMap.get(el))
                                .collect(Collectors.toList()));
                        scv.setMetaPointer(metapointersMap.get(c.getMetaPointerIndex()));
                        sci.addContainmentValue(scv);
                      });
              n.getReferencesList()
                  .forEach(
                      r -> {
                        SerializedReferenceValue srv = new SerializedReferenceValue();
                        r.getValuesList()
                            .forEach(
                                rv -> {
                                  SerializedReferenceValue.Entry entry =
                                      new SerializedReferenceValue.Entry();
                                  entry.setReference(stringsMap.get(rv.getReferred()));
                                  entry.setResolveInfo(stringsMap.get(rv.getResolveInfo()));
                                  srv.addValue(entry);
                                });
                        srv.setMetaPointer(metapointersMap.get(r.getMetaPointerIndex()));
                        sci.addReferenceValue(srv);
                      });
              n.getAnnotationsList().forEach(a -> sci.addAnnotation(stringsMap.get(a)));
              serializedChunk.addClassifierInstance(sci);
            });
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
    return serialize(serializedChunk).toByteArray();
  }

  protected class SerializeHelper {
    private final Map<MetaPointer, Integer> metaPointers = new HashMap<>();
    private final Map<String, Integer> strings = new HashMap<>();

    public Map<MetaPointer, Integer> getMetaPointers() {
      return metaPointers;
    }

    public Map<String, Integer> getStrings() {
      return strings;
    }

    public SerializeHelper() {}

    public int stringIndexer(String string) {
      if (string == null) {
        return -1;
      }
      if (strings.containsKey(string)) {
        return strings.get(string);
      }
      int index = strings.size();
      strings.put(string, index);
      return index;
    };

    public int metaPointerIndexer(MetaPointer metaPointer) {
      if (metaPointers.containsKey(metaPointer)) {
        return metaPointers.get(metaPointer);
      }
      PBMetaPointer metaPointerDef =
          PBMetaPointer.newBuilder()
              .setKey(stringIndexer(metaPointer.getKey()))
              .setVersion(stringIndexer(metaPointer.getVersion()))
              .setLanguage(stringIndexer(metaPointer.getLanguage()))
              .build();
      int index = metaPointers.size();
      metaPointers.put(metaPointer, index);
      return index;
    }

    public PBNode serializeNode(SerializedClassifierInstance n) {
      PBNode.Builder nodeBuilder = PBNode.newBuilder();
      nodeBuilder.setId(this.stringIndexer(n.getID()));
      nodeBuilder.setClassifier(this.metaPointerIndexer((n.getClassifier())));
      nodeBuilder.setParent(this.stringIndexer(n.getParentNodeID()));
      n.getProperties()
          .forEach(
              p -> {
                PBProperty.Builder b = PBProperty.newBuilder();
                b.setValue(this.stringIndexer(p.getValue()));
                b.setMetaPointerIndex(this.metaPointerIndexer((p.getMetaPointer())));
                nodeBuilder.addProperties(b.build());
              });
      n.getContainments()
          .forEach(
              p ->
                  nodeBuilder.addContainments(
                      PBContainment.newBuilder()
                          .addAllChildren(
                              p.getValue().stream()
                                  .map(v -> this.stringIndexer(v))
                                  .collect(Collectors.toList()))
                          .setMetaPointerIndex(this.metaPointerIndexer((p.getMetaPointer())))
                          .build()));
      n.getReferences()
          .forEach(
              p ->
                  nodeBuilder.addReferences(
                      PBReference.newBuilder()
                          .addAllValues(
                              p.getValue().stream()
                                  .map(
                                      rf -> {
                                        PBReferenceValue.Builder b = PBReferenceValue.newBuilder();
                                        b.setReferred(this.stringIndexer(rf.getReference()));
                                        b.setResolveInfo(this.stringIndexer(rf.getResolveInfo()));
                                        return b.build();
                                      })
                                  .collect(Collectors.toList()))
                          .setMetaPointerIndex(this.metaPointerIndexer((p.getMetaPointer())))
                          .build()));
      n.getAnnotations().forEach(a -> nodeBuilder.addAnnotations(this.stringIndexer(a)));
      return nodeBuilder.build();
    }
  }

  public PBChunk serializeTree(ClassifierInstance<?> classifierInstance) {
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

  public PBChunk serialize(SerializedChunk serializedChunk) {
    PBChunk.Builder chunkBuilder = PBChunk.newBuilder();
    chunkBuilder.setSerializationFormatVersion(serializedChunk.getSerializationFormatVersion());
    SerializeHelper serializeHelper = new SerializeHelper();
    serializedChunk
        .getLanguages()
        .forEach(
            ul -> {
              chunkBuilder.addLanguages(
                  PBLanguage.newBuilder()
                      .setKey(serializeHelper.stringIndexer(ul.getKey()))
                      .setVersion(serializeHelper.stringIndexer(ul.getVersion()))
                      .build());
            });

    serializedChunk
        .getClassifierInstances()
        .forEach(n -> chunkBuilder.addNodes(serializeHelper.serializeNode(n)));

    serializeHelper.strings.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry -> {
              chunkBuilder.addStringValues(entry.getKey());
            });
    serializeHelper.metaPointers.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry -> {
              PBMetaPointer.Builder metaPointer = PBMetaPointer.newBuilder();
              metaPointer.setKey(serializeHelper.stringIndexer(entry.getKey().getKey()));
              metaPointer.setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()));
              metaPointer.setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()));
              chunkBuilder.addMetaPointers(metaPointer.build());
            });
    return chunkBuilder.build();
  }
}
