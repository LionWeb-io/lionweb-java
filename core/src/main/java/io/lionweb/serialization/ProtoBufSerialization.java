package io.lionweb.serialization;

import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.model.AnnotationInstance;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.ProxyNode;
import io.lionweb.protobuf.*;
import io.lionweb.serialization.data.*;
import io.lionweb.serialization.data.MetaPointer;
import java.io.*;
import java.util.*;
import javax.annotation.Nonnull;

public class ProtoBufSerialization extends AbstractSerialization {

  public ProtoBufSerialization() {
    super();
  }

  public ProtoBufSerialization(@Nonnull LionWebVersion lionWebVersion) {
    super(lionWebVersion);
  }

  public List<io.lionweb.model.Node> deserializeToNodes(byte[] bytes) throws IOException {
    return deserializeToNodes(PBChunk.parseFrom(bytes));
  }

  public SerializationChunk deserializeToChunk(byte[] bytes) throws IOException {
    return deserializeSerializationChunk(PBChunk.parseFrom(bytes));
  }

  public SerializationChunk deserializeToChunk(InputStream inputStream) throws IOException {
    PBChunk pbChunk = PBChunk.parseFrom(inputStream);
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
    List<ClassifierInstance<?>> all = deserializeToClassifierInstances(chunk);
    List<io.lionweb.model.Node> nodes = new ArrayList<>(all.size());
    for (ClassifierInstance<?> ci : all) {
      if (ci instanceof io.lionweb.model.Node) nodes.add((io.lionweb.model.Node) ci);
    }
    return nodes;
  }

  public List<ClassifierInstance<?>> deserializeToClassifierInstances(PBChunk chunk) {
    SerializationChunk serializationBlock = deserializeSerializationChunk(chunk);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationChunk(serializationBlock);
  }

  private SerializationChunk deserializeSerializationChunk(PBChunk chunk) {
    // Pre-size arrays for better performance
    int stringCount = chunk.getInternedStringsCount();
    int languageCount = chunk.getInternedLanguagesCount();
    int metaPointerCount = chunk.getInternedMetaPointersCount();

    String[] stringsArray = new String[stringCount + 1];
    stringsArray[0] = null;
    for (int i = 0; i < chunk.getInternedStringsCount(); i++) {
      stringsArray[i + 1] = chunk.getInternedStrings(i);
    }
    LanguageVersion[] languagesArray = new LanguageVersion[languageCount + 1];
    languagesArray[0] = null;
    for (int i = 0; i < chunk.getInternedLanguagesCount(); i++) {
      PBLanguage l = chunk.getInternedLanguages(i);
      String key = stringsArray[l.getSiKey()];
      String version = stringsArray[l.getSiVersion()];
      LanguageVersion lv = LanguageVersion.of(key, version);
      languagesArray[i + 1] = lv;
    }
    MetaPointer[] metapointersArray = new MetaPointer[metaPointerCount];
    for (int i = 0; i < chunk.getInternedMetaPointersCount(); i++) {
      PBMetaPointer mp = chunk.getInternedMetaPointers(i);

      if (mp.getLiLanguage() >= languagesArray.length) {
        throw new DeserializationException(
            "Unable to deserialize meta pointer with language " + mp.getLiLanguage());
      }
      LanguageVersion languageVersion = languagesArray[mp.getLiLanguage()];
      MetaPointer metaPointer =
          MetaPointer.get(
              languageVersion.getKey(), languageVersion.getVersion(), stringsArray[mp.getSiKey()]);
      metapointersArray[i] = metaPointer;
    }

    SerializationChunk serializationChunk = new SerializationChunk();
    serializationChunk.setSerializationFormatVersion(chunk.getSerializationFormatVersion());
    for (LanguageVersion languageVersion : languagesArray) {
      if (languageVersion != null) {
        serializationChunk.addLanguage(languageVersion);
      }
    }

    int nodeCount = chunk.getNodesCount();
    for (int ni = 0; ni < nodeCount; ni++) {
      PBNode n = chunk.getNodes(ni);
      int propCount = n.getPropertiesCount();
      int contCount = n.getContainmentsCount();
      int refCount = n.getReferencesCount();
      int annCount = n.getSiAnnotationsCount();
      SerializedClassifierInstance sci =
          new SerializedClassifierInstance(propCount, contCount, refCount, annCount);
      sci.setID(stringsArray[n.getSiId()]);
      sci.setParentNodeID(stringsArray[n.getSiParent()]);
      sci.setClassifier(metapointersArray[n.getMpiClassifier()]);
      for (int pi = 0; pi < propCount; pi++) {
        PBProperty p = n.getProperties(pi);
        int siValue = p.getSiValue();
        if (serializeEmptyFeatures || siValue != 0) {
          SerializedPropertyValue spv =
              SerializedPropertyValue.get(
                  metapointersArray[p.getMpiMetaPointer()], stringsArray[siValue]);
          sci.unsafeAppendPropertyValue(spv);
        }
      }
      for (int ci = 0; ci < contCount; ci++) {
        PBContainment c = n.getContainments(ci);
        int childrenCount = c.getSiChildrenCount();
        List<String> children = new ArrayList<>(childrenCount);
        for (int chi = 0; chi < childrenCount; chi++) {
          int childIndex = c.getSiChildren(chi);
          if (childIndex == 0) {
            throw new DeserializationException("Unable to deserialize child identified by Null ID");
          }
          children.add(stringsArray[childIndex]);
        }
        if (serializeEmptyFeatures || !children.isEmpty()) {
          SerializedContainmentValue scv =
              new SerializedContainmentValue(metapointersArray[c.getMpiMetaPointer()], children);
          sci.unsafeAppendContainmentValue(scv);
        }
      }
      for (int ri = 0; ri < refCount; ri++) {
        PBReference r = n.getReferences(ri);
        SerializedReferenceValue srv =
            new SerializedReferenceValue(metapointersArray[r.getMpiMetaPointer()]);
        int valCount = r.getValuesCount();
        for (int vi = 0; vi < valCount; vi++) {
          PBReferenceValue rv = r.getValues(vi);
          SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
          entry.setReference(stringsArray[rv.getSiReferred()]);
          entry.setResolveInfo(stringsArray[rv.getSiResolveInfo()]);
          srv.addValue(entry);
        }
        if (serializeEmptyFeatures || !srv.getValue().isEmpty()) {
          sci.unsafeAppendReferenceValue(srv);
        }
      }
      for (int ai = 0; ai < annCount; ai++) {
        sci.addAnnotation(stringsArray[n.getSiAnnotations(ai)]);
      }
      serializationChunk.addClassifierInstanceWithoutLanguageScan(sci);
    }
    return serializationChunk;
  }

  public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(1024);
    Set<String> seenIDs = new HashSet<>(1024);
    for (ClassifierInstance<?> root : roots) {
      collectNonProxyNoDup(root, allNodes, seenIDs);
    }
    return serializeNodesToByteArray(allNodes);
  }

  private static void collectNonProxyNoDup(
      ClassifierInstance<?> node, List<ClassifierInstance<?>> result, Set<String> seenIDs) {
    if (node instanceof ProxyNode) return;
    String id = node.getID();
    if (id != null && !seenIDs.add(id)) return;
    result.add(node);
    List<AnnotationInstance> anns = node.getAnnotations();
    for (int i = 0, n = anns.size(); i < n; i++) {
      collectNonProxyNoDup(anns.get(i), result, seenIDs);
    }
    Classifier<?> classifier = node.getClassifier();
    List<Containment> conts = classifier.allContainments();
    for (int i = 0, n = conts.size(); i < n; i++) {
      List<? extends Node> children = node.getChildren(conts.get(i));
      for (int j = 0, m = children.size(); j < m; j++) {
        collectNonProxyNoDup(children.get(j), result, seenIDs);
      }
    }
  }

  public byte[] serializeNodesToByteArray(List<ClassifierInstance<?>> classifierInstances) {
    for (int i = 0, sz = classifierInstances.size(); i < sz; i++) {
      if (classifierInstances.get(i) instanceof ProxyNode) {
        throw new IllegalArgumentException("Proxy nodes cannot be serialized");
      }
    }
    return buildPBChunk(classifierInstances).toByteArray();
  }

  private PBChunk buildPBChunk(List<ClassifierInstance<?>> classifierInstances) {
    PBChunk.Builder chunkBuilder = PBChunk.newBuilder();
    chunkBuilder.setSerializationFormatVersion(getLionWebVersion().getVersionString());
    SerializeHelper helper = new SerializeHelper();
    for (int i = 0, sz = classifierInstances.size(); i < sz; i++) {
      chunkBuilder.addNodes(serializeNodeDirect(classifierInstances.get(i), helper));
    }
    List<LanguageVersion> langs = helper.getLanguages();
    for (int i = 1, n = langs.size(); i < n; i++) {
      LanguageVersion lv = langs.get(i);
      PBLanguage.Builder lb = PBLanguage.newBuilder();
      if (lv.getKey() != null) lb.setSiKey(helper.stringIndexer(lv.getKey()));
      if (lv.getVersion() != null) lb.setSiVersion(helper.stringIndexer(lv.getVersion()));
      chunkBuilder.addInternedLanguages(lb.build());
    }
    List<String> strs = helper.getStrings();
    for (int i = 1, n = strs.size(); i < n; i++) {
      chunkBuilder.addInternedStrings(strs.get(i));
    }
    List<MetaPointer> mps = helper.getMetaPointers();
    for (int i = 0, n = mps.size(); i < n; i++) {
      MetaPointer mp = mps.get(i);
      PBMetaPointer.Builder mpb =
          PBMetaPointer.newBuilder().setLiLanguage(helper.languageIndexer(mp.getLanguageVersion()));
      if (mp.getKey() != null) mpb.setSiKey(helper.stringIndexer(mp.getKey()));
      chunkBuilder.addInternedMetaPointers(mpb.build());
    }
    return chunkBuilder.build();
  }

  private PBNode serializeNodeDirect(ClassifierInstance<?> node, SerializeHelper helper) {
    PBNode.Builder nodeBuilder = PBNode.newBuilder();
    if (node.getID() != null) nodeBuilder.setSiId(helper.stringIndexer(node.getID()));
    ClassifierInstance<?> parent = node.getParent();
    if (parent != null) nodeBuilder.setSiParent(helper.stringIndexer(parent.getID()));
    Classifier<?> classifier = node.getClassifier();
    nodeBuilder.setMpiClassifier(helper.metaPointerIndexer(MetaPointer.from(classifier)));

    List<Property> props = classifier.allProperties();
    for (int i = 0, n = props.size(); i < n; i++) {
      Property property = props.get(i);
      Object val = node.getPropertyValue(property);
      if (serializeEmptyFeatures || val != null) {
        String strVal = null;
        if (val != null) {
          strVal = dataTypesValuesSerialization.serialize(property.getType().getID(), val);
        }
        PBProperty.Builder b = PBProperty.newBuilder();
        b.setSiValue(helper.stringIndexer(strVal));
        b.setMpiMetaPointer(helper.metaPointerIndexer(MetaPointer.from(property)));
        nodeBuilder.addProperties(b.build());
      }
    }

    List<Containment> conts = classifier.allContainments();
    for (int i = 0, n = conts.size(); i < n; i++) {
      Containment containment = conts.get(i);
      List<? extends Node> children = node.getChildren(containment);
      if (serializeEmptyFeatures || !children.isEmpty()) {
        PBContainment.Builder cb = PBContainment.newBuilder();
        cb.setMpiMetaPointer(helper.metaPointerIndexer(MetaPointer.from(containment)));
        for (int j = 0, m = children.size(); j < m; j++) {
          cb.addSiChildren(helper.stringIndexer(children.get(j).getID()));
        }
        nodeBuilder.addContainments(cb.build());
      }
    }

    List<Reference> refs = classifier.allReferences();
    for (int i = 0, n = refs.size(); i < n; i++) {
      Reference reference = refs.get(i);
      List<ReferenceValue> rvs = node.getReferenceValues(reference);
      if (serializeEmptyFeatures || !rvs.isEmpty()) {
        PBReference.Builder rb = PBReference.newBuilder();
        rb.setMpiMetaPointer(helper.metaPointerIndexer(MetaPointer.from(reference)));
        for (int j = 0, m = rvs.size(); j < m; j++) {
          ReferenceValue rv = rvs.get(j);
          PBReferenceValue.Builder b = PBReferenceValue.newBuilder();
          String refID = rv.getReferred() == null ? null : rv.getReferred().getID();
          if (builtinsReferenceDangling
              && ClassifierInstanceUtils.isBuiltinElement(rv.getReferred())) refID = null;
          if (refID != null) b.setSiReferred(helper.stringIndexer(refID));
          if (rv.getResolveInfo() != null)
            b.setSiResolveInfo(helper.stringIndexer(rv.getResolveInfo()));
          rb.addValues(b.build());
        }
        nodeBuilder.addReferences(rb.build());
      }
    }

    List<AnnotationInstance> anns = node.getAnnotations();
    for (int i = 0, n = anns.size(); i < n; i++) {
      nodeBuilder.addSiAnnotations(helper.stringIndexer(anns.get(i).getID()));
    }
    return nodeBuilder.build();
  }

  public byte[] serializeNodesToByteArray(ClassifierInstance<?>... classifierInstances) {
    return serializeNodesToByteArray(Arrays.asList(classifierInstances));
  }

  public byte[] serializeToByteArray(SerializationChunk serializationChunk) {
    return serialize(serializationChunk).toByteArray();
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

    public SerializeHelper() {
      stringIndexMap.put(null, 0);
      languageIndexMap.put(null, 0);
      strings.add(null);
      languages.add(null);
    }

    public int stringIndexer(String string) {
      if (string == null) {
        return 0;
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
        return 0;
      }
      if (languageIndexMap.containsKey(language)) {
        return languageIndexMap.get(language);
      }
      int index = languages.size();
      stringIndexer(language.getKey());
      stringIndexer(language.getVersion());
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
      // If it is zero we should not set the field at all, so the present bit will not be set
      if (n.getID() != null) {
        nodeBuilder.setSiId(this.stringIndexer(n.getID()));
      }
      if (n.getParentNodeID() != null) {
        nodeBuilder.setSiParent(this.stringIndexer(n.getParentNodeID()));
      }
      nodeBuilder.setMpiClassifier(this.metaPointerIndexer((n.getClassifier())));
      n.getProperties()
          .forEach(
              p -> {
                String propertyValue = p.getValue();
                if (serializeEmptyFeatures || propertyValue != null) {
                  PBProperty.Builder b = PBProperty.newBuilder();
                  b.setSiValue(this.stringIndexer(propertyValue));
                  b.setMpiMetaPointer(this.metaPointerIndexer(p.getMetaPointer()));
                  nodeBuilder.addProperties(b.build());
                }
              });
      for (SerializedContainmentValue p : n.getContainments()) {
        List<String> childrenIds = p.getChildrenIds();
        if (serializeEmptyFeatures || !childrenIds.isEmpty()) {
          PBContainment.Builder cb = PBContainment.newBuilder();
          cb.setMpiMetaPointer(this.metaPointerIndexer(p.getMetaPointer()));
          for (String childId : childrenIds) {
            cb.addSiChildren(this.stringIndexer(childId));
          }
          nodeBuilder.addContainments(cb.build());
        }
      }
      for (SerializedReferenceValue p : n.getReferences()) {
        List<SerializedReferenceValue.Entry> referenceEntries = p.getValue();
        if (serializeEmptyFeatures || !referenceEntries.isEmpty()) {
          PBReference.Builder rb = PBReference.newBuilder();
          rb.setMpiMetaPointer(this.metaPointerIndexer(p.getMetaPointer()));
          for (SerializedReferenceValue.Entry rf : referenceEntries) {
            PBReferenceValue.Builder b = PBReferenceValue.newBuilder();
            if (rf.getReference() != null) {
              b.setSiReferred(this.stringIndexer(rf.getReference()));
            }
            if (rf.getResolveInfo() != null) {
              b.setSiResolveInfo(this.stringIndexer(rf.getResolveInfo()));
            }
            rb.addValues(b.build());
          }
          nodeBuilder.addReferences(rb.build());
        }
      }
      n.getAnnotations().forEach(a -> nodeBuilder.addSiAnnotations(this.stringIndexer(a)));
      return nodeBuilder.build();
    }
  }

  public PBChunk serializeTree(ClassifierInstance<?> classifierInstance) {
    List<ClassifierInstance<?>> allNodes = new ArrayList<>(1024);
    Set<String> seenIDs = new HashSet<>(1024);
    collectNonProxyNoDup(classifierInstance, allNodes, seenIDs);
    return buildPBChunk(allNodes);
  }

  public PBChunk serialize(SerializationChunk serializationChunk) {
    PBChunk.Builder chunkBuilder = PBChunk.newBuilder();
    chunkBuilder.setSerializationFormatVersion(serializationChunk.getSerializationFormatVersion());
    SerializeHelper serializeHelper = new SerializeHelper();

    // Process all nodes first to build indices
    List<SerializedClassifierInstance> instances = serializationChunk.getClassifierInstances();
    for (SerializedClassifierInstance instance : instances) {
      chunkBuilder.addNodes(serializeHelper.serializeNode(instance));
    }

    // We need to process languages before strings, otherwise we might end up with null pointers
    for (LanguageVersion languageVersion : serializeHelper.languages) {
      if (languageVersion != null) {
        PBLanguage.Builder languageBuilder = PBLanguage.newBuilder();
        if (languageVersion.getKey() != null) {
          languageBuilder.setSiKey(serializeHelper.stringIndexer(languageVersion.getKey()));
        }
        if (languageVersion.getVersion() != null) {
          languageBuilder.setSiVersion(serializeHelper.stringIndexer(languageVersion.getVersion()));
        }
        chunkBuilder.addInternedLanguages(languageBuilder.build());
      }
    }

    for (String string : serializeHelper.strings) {
      if (string != null) {
        chunkBuilder.addInternedStrings(string);
      }
    }
    for (MetaPointer metaPointer : serializeHelper.metaPointers) {
      PBMetaPointer.Builder metaPointerBuilder =
          PBMetaPointer.newBuilder()
              .setLiLanguage(serializeHelper.languageIndexer(metaPointer.getLanguageVersion()));
      if (metaPointer.getKey() != null) {
        metaPointerBuilder.setSiKey(serializeHelper.stringIndexer(metaPointer.getKey()));
      }
      chunkBuilder.addInternedMetaPointers(metaPointerBuilder.build());
    }

    return chunkBuilder.build();
  }
}
