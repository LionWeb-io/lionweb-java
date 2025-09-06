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
      // Pre-size arrays for better performance
      int stringCount = chunk.getInternedStringsCount();
      int languageCount = chunk.getInternedLanguagesCount();
      int metaPointerCount = chunk.getInternedMetaPointersCount();


      String[] stringsArray = new String[stringCount];
      for (int i = 0; i < chunk.getInternedStringsCount(); i++) {
          stringsArray[i] =  chunk.getInternedStrings(i);
    }
    LanguageVersion[] languagesArray = new LanguageVersion[languageCount];
    for (int i = 0; i < chunk.getInternedLanguagesCount(); i++) {
      PBLanguage l = chunk.getInternedLanguages(i);
      String key = stringsArray[l.getKey()];
      String version = stringsArray[l.getVersion()];
      LanguageVersion lv = LanguageVersion.of(key, version);
        languagesArray[i] = lv;
    }
    MetaPointer[] metapointersArray = new MetaPointer[metaPointerCount];
    for (int i = 0; i < chunk.getInternedMetaPointersCount(); i++) {
      PBMetaPointer mp = chunk.getInternedMetaPointers(i);

      if (mp.getLanguage() >= languagesArray.length) {
        throw new DeserializationException(
            "Unable to deserialize meta pointer with language " + mp.getLanguage());
      }
        LanguageVersion languageVersion = languagesArray[mp.getLanguage()];
      MetaPointer metaPointer =
          MetaPointer.get(
              languageVersion.getKey(), languageVersion.getVersion(), stringsArray[mp.getKey()]);
      metapointersArray[i] = metaPointer;
    }

    SerializedChunk serializedChunk = new SerializedChunk();
    serializedChunk.setSerializationFormatVersion(chunk.getSerializationFormatVersion());
    for (LanguageVersion languageVersion : languagesArray) {
        serializedChunk.addLanguage(languageVersion);
    }

    chunk
        .getNodesList()
        .forEach(
            n -> {
              SerializedClassifierInstance sci = new SerializedClassifierInstance();
              sci.setID(n.getId() == -1 ? null : stringsArray[n.getId()]);
              sci.setParentNodeID(n.getParent() == -1 ? null : stringsArray[n.getParent()]);
              sci.setClassifier(metapointersArray[n.getClassifier()]);
              n.getPropertiesList()
                  .forEach(
                      p -> {
                        SerializedPropertyValue spv =
                            SerializedPropertyValue.get(
                                metapointersArray[p.getMetaPointer()],
                                    p.getValue() == -1 ? null : stringsArray[p.getValue()]);
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
                                .map(el -> stringsArray[el])
                                .collect(Collectors.toList());
                        if (!children.isEmpty()) {
                          SerializedContainmentValue scv =
                              new SerializedContainmentValue(
                                  metapointersArray[c.getMetaPointer()], children);
                          sci.addContainmentValue(scv);
                        }
                      });
              n.getReferencesList()
                  .forEach(
                      r -> {
                        SerializedReferenceValue srv =
                            new SerializedReferenceValue(metapointersArray[r.getMetaPointer()]);
                        r.getValuesList()
                            .forEach(
                                rv -> {
                                  SerializedReferenceValue.Entry entry =
                                      new SerializedReferenceValue.Entry();
                                  entry.setReference(rv.getReferred() == -1 ? null : stringsArray[rv.getReferred()]);
                                  entry.setResolveInfo(rv.getResolveInfo() == -1 ? null : stringsArray[rv.getResolveInfo()]);
                                  srv.addValue(entry);
                                });
                        if (!srv.getValue().isEmpty()) {
                          sci.addReferenceValue(srv);
                        }
                      });
              n.getAnnotationsList().forEach(a -> sci.addAnnotation(stringsArray[a]));
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
    // Replace HashMaps with ArrayList for better cache locality and faster access
    private final List<MetaPointer> metaPointers = new ArrayList<>();
    private final List<String> strings = new ArrayList<>();
    private final List<LanguageVersion> languages = new ArrayList<>();

    // Keep reverse lookup maps for indexing only
    private final Map<MetaPointer, Integer> metaPointerIndexMap = new HashMap<>();
    private final Map<String, Integer> stringIndexMap = new HashMap<>();
    private final Map<LanguageVersion, Integer> languageIndexMap = new HashMap<>();

    public List<MetaPointer> getMetaPointers() {
      return metaPointers;
    }

    public List<String> getStrings() {
      return strings;
    }

    public List<LanguageVersion> getLanguages() {
      return languages;
    }

    public SerializeHelper() {}

    public int stringIndexer(String string) {
      if (string == null) {
        return -1;
      }
      if (stringIndexMap.containsKey(string)) {
        return stringIndexMap.get(string);
      }
      int index = strings.size();
      strings.add(string);
      stringIndexMap.put(string, index);
      return index;
    }

    public int languageIndexer(LanguageVersion language) {
      if (language == null) {
        return -1;
      }
      if (languageIndexMap.containsKey(language)) {
        return languageIndexMap.get(language);
      }
      int index = languages.size();
      languages.add(language);
      languageIndexMap.put(language, index);
      return index;
    }

    public int metaPointerIndexer(MetaPointer metaPointer) {
      if (metaPointerIndexMap.containsKey(metaPointer)) {
        return metaPointerIndexMap.get(metaPointer);
      }
      int index = metaPointers.size();
      languageIndexer(metaPointer.getLanguageVersion());
      stringIndexer(metaPointer.getKey());
      metaPointers.add(metaPointer);
      metaPointerIndexMap.put(metaPointer, index);
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
                b.setMetaPointer(this.metaPointerIndexer(p.getMetaPointer()));
                nodeBuilder.addProperties(b.build());
              });
      n.getContainments()
          .forEach(
              p ->
                  nodeBuilder.addContainments(
                      PBContainment.newBuilder()
                          .addAllChildren(
                              p.getValue().stream()
                                  .map(this::stringIndexer)
                                  .collect(Collectors.toList()))
                          .setMetaPointer(this.metaPointerIndexer(p.getMetaPointer()))
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
                          .setMetaPointer(this.metaPointerIndexer(p.getMetaPointer()))
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
    for (LanguageVersion languageVersion : serializeHelper.languages) {
      chunkBuilder.addInternedLanguages(
          PBLanguage.newBuilder()
              .setKey(serializeHelper.stringIndexer(languageVersion.getKey()))
              .setVersion(serializeHelper.stringIndexer(languageVersion.getVersion()))
              .build());
    }

    for (String string : serializeHelper.strings) {
      chunkBuilder.addInternedStrings(string);
    }
    for (MetaPointer metaPointer : serializeHelper.metaPointers) {
      chunkBuilder.addInternedMetaPointers(
          PBMetaPointer.newBuilder()
              .setKey(serializeHelper.stringIndexer(metaPointer.getKey()))
              .setLanguage(serializeHelper.languageIndexer(metaPointer.getLanguageVersion()))
              .build());
    }

    return chunkBuilder.build();
  }
}
