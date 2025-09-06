package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.protobuf.*;
import io.lionweb.serialization.data.*;
import io.lionweb.serialization.data.MetaPointer;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class ProtoBufSerialization extends AbstractSerialization {

  public ProtoBufSerialization() {
    super();
  }

  public ProtoBufSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public List<io.lionweb.model.Node> deserializeToNodes(byte[] bytes) throws IOException {
    return deserializeToNodes(new ByteArrayInputStream(bytes));
  }

  public SerializedChunk deserializeToChunk(byte[] bytes) throws IOException {
    PBChunk pbChunk = PBChunk.parseFrom(new ByteArrayInputStream(bytes));
    return deserializeSerializationChunk(pbChunk);
  }

  public List<io.lionweb.model.Node> deserializeToNodes(File file) throws IOException {
    return deserializeToNodes(new FileInputStream(file));
  }

  public List<io.lionweb.model.Node> deserializeToNodes(InputStream inputStream)
      throws IOException {
    return deserializeToNodes(PBChunk.parseFrom(inputStream));
  }

  public List<io.lionweb.model.Node> deserializeToNodes(PBChunk chunk) {
    return deserializeToClassifierInstances(chunk).stream()
        .filter(ci -> ci instanceof io.lionweb.model.Node)
        .map(ci -> (io.lionweb.model.Node) ci)
        .collect(Collectors.toList());
  }

  public List<ClassifierInstance<?>> deserializeToClassifierInstances(PBChunk chunk) {
    SerializedChunk serializationBlock = deserializeSerializationChunk(chunk);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationChunk(serializationBlock);
  }

  private SerializedChunk deserializeSerializationChunk(PBChunk chunk) {
    Map<Integer, String> stringsMap = new HashMap<>();
    for (int i = 0; i < chunk.getInternedStringsCount(); i++) {
      stringsMap.put(i, chunk.getInternedStrings(i));
    }
    Map<Integer, LanguageVersion> languagesMap = new HashMap<>();
    for (int i = 0; i < chunk.getInternedLanguagesCount(); i++) {
      PBLanguage l = chunk.getInternedLanguages(i);
      String key = stringsMap.get(l.getKey());
      String version = stringsMap.get(l.getVersion());
      LanguageVersion lv = LanguageVersion.of(key, version);
      languagesMap.put(i, lv);
    }
    Map<Integer, MetaPointer> metapointersMap = new HashMap<>();
    for (int i = 0; i < chunk.getInternedMetaPointersCount(); i++) {
      PBMetaPointer mp = chunk.getInternedMetaPointers(i);
      LanguageVersion languageVersion = languagesMap.get(mp.getLanguage());
      if (languageVersion == null) {
        throw new DeserializationException(
            "Unable to deserialize meta pointer with language " + mp.getLanguage());
      }
      MetaPointer metaPointer =
          MetaPointer.get(
              languageVersion.getKey(), languageVersion.getVersion(), stringsMap.get(mp.getKey()));
      metapointersMap.put(i, metaPointer);
    }

    SerializedChunk serializedChunk = new SerializedChunk();
    serializedChunk.setSerializationFormatVersion(chunk.getSerializationFormatVersion());
    languagesMap.values().forEach(serializedChunk::addLanguage);

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
                        SerializedPropertyValue spv =
                            SerializedPropertyValue.get(
                                metapointersMap.get(p.getMetaPointer()),
                                stringsMap.get(p.getValue()));
                        sci.addPropertyValue(spv);
                      });
              n.getContainmentsList()
                  .forEach(
                      c -> {
                        if (c.getChildrenList().stream().anyMatch(el -> el < 0)) {
                          throw new DeserializationException(
                              "Unable to deserialize child identified by Null ID");
                        }
                        List<String> children =
                            c.getChildrenList().stream()
                                .map(el -> stringsMap.get(el))
                                .collect(Collectors.toList());
                        if (!children.isEmpty()) {
                          SerializedContainmentValue scv =
                              new SerializedContainmentValue(
                                  metapointersMap.get(c.getMetaPointer()), children);
                          sci.addContainmentValue(scv);
                        }
                      });
              n.getReferencesList()
                  .forEach(
                      r -> {
                        SerializedReferenceValue srv =
                            new SerializedReferenceValue(metapointersMap.get(r.getMetaPointer()));
                        r.getValuesList()
                            .forEach(
                                rv -> {
                                  SerializedReferenceValue.Entry entry =
                                      new SerializedReferenceValue.Entry();
                                  entry.setReference(stringsMap.get(rv.getReferred()));
                                  entry.setResolveInfo(stringsMap.get(rv.getResolveInfo()));
                                  srv.addValue(entry);
                                });
                        if (!srv.getValue().isEmpty()) {
                          sci.addReferenceValue(srv);
                        }
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
    SerializedChunk serializationBlock = serializeNodesToSerializationChunk(classifierInstances);
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
    private final Map<LanguageVersion, Integer> languages = new HashMap<>();

    public Map<MetaPointer, Integer> getMetaPointers() {
      return metaPointers;
    }

    public Map<String, Integer> getStrings() {
      return strings;
    }

    public Map<LanguageVersion, Integer> getLanguages() {
      return languages;
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
    }

    public int languageIndexer(LanguageVersion language) {
      if (language == null) {
        return -1;
      }
      if (languages.containsKey(language)) {
        return languages.get(language);
      }
      int index = languages.size();
      languages.put(language, index);
      return index;
    }

    public int metaPointerIndexer(MetaPointer metaPointer) {
      if (metaPointers.containsKey(metaPointer)) {
        return metaPointers.get(metaPointer);
      }
      PBMetaPointer metaPointerDef =
          PBMetaPointer.newBuilder()
              .setLanguage(languageIndexer(metaPointer.getLanguageVersion()))
              .setKey(stringIndexer(metaPointer.getKey()))
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
                b.setMetaPointer(this.metaPointerIndexer((p.getMetaPointer())));
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
                          .setMetaPointer(this.metaPointerIndexer((p.getMetaPointer())))
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
                          .setMetaPointer(this.metaPointerIndexer((p.getMetaPointer())))
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
        serializeNodesToSerializationChunk(
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
        .getClassifierInstances()
        .forEach(n -> chunkBuilder.addNodes(serializeHelper.serializeNode(n)));

    // We need to process languages before strings, otherwise we might end up with null pointers
    serializeHelper.languages.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry -> {
              chunkBuilder.addInternedLanguages(
                  PBLanguage.newBuilder()
                      .setKey(serializeHelper.stringIndexer(entry.getKey().getKey()))
                      .setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()))
                      .build());
            });
    serializeHelper.strings.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry -> {
              chunkBuilder.addInternedStrings(entry.getKey());
            });
    serializeHelper.metaPointers.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(
            entry -> {
              PBMetaPointer.Builder metaPointer = PBMetaPointer.newBuilder();
              metaPointer.setKey(serializeHelper.stringIndexer(entry.getKey().getKey()));
              metaPointer.setLanguage(
                  serializeHelper.languageIndexer(entry.getKey().getLanguageVersion()));
              chunkBuilder.addInternedMetaPointers(metaPointer.build());
            });
    return chunkBuilder.build();
  }
}
