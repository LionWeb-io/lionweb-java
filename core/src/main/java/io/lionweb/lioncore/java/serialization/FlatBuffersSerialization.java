package io.lionweb.lioncore.java.serialization;

import com.google.flatbuffers.FlatBufferBuilder;
import io.lionweb.lioncore.java.model.*;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.serialization.flatbuffers.gen.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class FlatBuffersSerialization extends AbstractSerialization {

  private static final String NULL_CONSTANT = "NULL";

  public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(byte[] bytes)
      throws IOException {
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
    SerializedChunk serializationBlock = deserializeSerializationChunk(chunk);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationBlock(serializationBlock);
  }

  private class DeserializationHelper {

    private IdentityHashMap<FBMetaPointer, MetaPointer> metaPointersCache = new IdentityHashMap<>();

    public MetaPointer deserialize(FBMetaPointer classifier) {
      if (classifier == null) {
        throw new IllegalStateException("Classifier should not be null");
      }
      return metaPointersCache.computeIfAbsent(
          classifier,
          fbMetaPointer -> {
            MetaPointer metaPointer = new MetaPointer();
            metaPointer.setKey(classifier.key());
            metaPointer.setLanguage(classifier.language());
            metaPointer.setVersion(classifier.version());
            return metaPointer;
          });
    }

    public SerializedContainmentValue deserialize(FBContainment containment) {
      SerializedContainmentValue scv = new SerializedContainmentValue();
      List<String> children = new ArrayList<>(containment.childrenLength());
      for (int k = 0; k < containment.childrenLength(); k++) {
        String child = containment.children(k);
        if (child.equals(NULL_CONSTANT)) {
          throw new DeserializationException("Unable to deserialize child identified by Null ID");
        } else {
          children.add(child);
        }
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
    }

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
      }

      for (int j = 0; j < n.containmentsLength(); j++) {
        FBContainment c = n.containments(j);
        sci.addContainmentValue(helper.deserialize(c));
      }

      for (int j = 0; j < n.referencesLength(); j++) {
        FBReference r = n.references(j);
        SerializedReferenceValue srv = new SerializedReferenceValue();
        for (int k = 0; k < r.valuesLength(); k++) {
          FBReferenceValue rv = r.values(k);
          SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
          entry.setReference(rv.referred());
          entry.setResolveInfo(rv.resolveInfo());
          srv.addValue(entry);
        }

        srv.setMetaPointer(helper.deserialize(r.metaPointer()));
        sci.addReferenceValue(srv);
      }
      for (int j = 0; j < n.annotationsLength(); j++) {
        String annotationID = n.annotations(j);
        sci.addAnnotation(annotationID);
      }
      serializedChunk.addClassifierInstance(sci);
    }
    ;
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

  protected class FBHelper {
    FlatBufferBuilder builder;
    Map<MetaPointer, Integer> serializedMetapointers = new HashMap<>();

    public FBHelper(FlatBufferBuilder builder) {
      this.builder = builder;
    }

    /**
     * This method can create objects, so it should not be nested inside another object creation.
     */
    public int offsetForMetaPointer(MetaPointer metaPointer) {
      if (!serializedMetapointers.containsKey(metaPointer)) {
        int fbMetapointer =
            FBMetaPointer.createFBMetaPointer(
                builder,
                builder.createSharedString(metaPointer.getLanguage()),
                builder.createSharedString(metaPointer.getKey()),
                builder.createSharedString(metaPointer.getVersion()));
        serializedMetapointers.put(metaPointer, fbMetapointer);
      }
      return serializedMetapointers.get(metaPointer);
    }

    public int[] languagesVector(List<UsedLanguage> usedLanguages) {
      int[] languagesOffsets = new int[usedLanguages.size()];
      for (int i = 0; i < usedLanguages.size(); i++) {
        UsedLanguage ul = usedLanguages.get(i);
        languagesOffsets[i] =
            FBLanguage.createFBLanguage(
                builder,
                builder.createSharedString(ul.getKey()),
                builder.createSharedString(ul.getVersion()));
      }
      return languagesOffsets;
    }

    public int[] propsVector(List<SerializedPropertyValue> properties) {
      int[] props = new int[properties.size()];
      for (int j = 0; j < properties.size(); j++) {
        SerializedPropertyValue el = properties.get(j);
        props[j] =
            FBProperty.createFBProperty(
                builder,
                offsetForMetaPointer(el.getMetaPointer()),
                builder.createSharedString(el.getValue()));
      }
      return props;
    }

    public int[] containmentsVector(List<SerializedContainmentValue> containments) {
      int[] cons = new int[containments.size()];
      for (int j = 0; j < containments.size(); j++) {
        SerializedContainmentValue el = containments.get(j);
        int[] children = new int[el.getValue().size()];
        for (int k = 0; k < el.getValue().size(); k++) {
          if (el.getValue().get(k) == null) {
            children[k] = builder.createSharedString(NULL_CONSTANT);
          } else {
            children[k] = builder.createSharedString(el.getValue().get(k));
          }
        }
        cons[j] =
            FBContainment.createFBContainment(
                builder,
                offsetForMetaPointer(el.getMetaPointer()),
                FBContainment.createChildrenVector(builder, children));
      }
      return cons;
    }

    public int[] referencesVector(List<SerializedReferenceValue> references) {
      int[] refs = new int[references.size()];
      for (int j = 0; j < references.size(); j++) {
        SerializedReferenceValue el = references.get(j);
        int[] values = new int[el.getValue().size()];
        for (int k = 0; k < el.getValue().size(); k++) {
          values[k] =
              FBReferenceValue.createFBReferenceValue(
                  builder,
                  builder.createSharedString(el.getValue().get(k).getResolveInfo()),
                  builder.createSharedString(el.getValue().get(k).getReference()));
        }
        refs[j] =
            FBReference.createFBReference(
                builder,
                offsetForMetaPointer(el.getMetaPointer()),
                FBReference.createValuesVector(builder, values));
      }
      return refs;
    }

    public int[] annotationsVector(List<String> annotations) {
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

      int idOffset = sci.getID() == null ? -1 : builder.createSharedString(sci.getID());
      int classifierOffset = helper.offsetForMetaPointer(sci.getClassifier());
      int parentOffset =
          sci.getParentNodeID() == null ? -1 : builder.createSharedString(sci.getParentNodeID());
      int propsVector =
          FBNode.createPropertiesVector(builder, helper.propsVector(sci.getProperties()));
      int consVector =
          FBNode.createContainmentsVector(
              builder, helper.containmentsVector(sci.getContainments()));
      int refsVector =
          FBNode.createReferencesVector(builder, helper.referencesVector(sci.getReferences()));
      int annsVector =
          FBNode.createAnnotationsVector(builder, helper.annotationsVector(sci.getAnnotations()));
      FBNode.startFBNode(builder);
      if (idOffset != -1) {
        FBNode.addId(builder, idOffset);
      }

      FBNode.addClassifier(builder, classifierOffset);
      FBNode.addProperties(builder, propsVector);
      FBNode.addContainments(builder, consVector);
      FBNode.addReferences(builder, refsVector);
      FBNode.addAnnotations(builder, annsVector);

      if (parentOffset != -1) {
        FBNode.addParent(builder, parentOffset);
      }
      nodesOffsets[i] = FBNode.endFBNode(builder);
    }

    int chunk =
        FBChunk.createFBChunk(
            builder,
            builder.createSharedString(serializedChunk.getSerializationFormatVersion()),
            FBChunk.createLanguagesVector(builder, languagesOffsets),
            FBChunk.createNodesVector(builder, nodesOffsets));

    builder.finish(chunk);
    return builder.dataBuffer().compact().array();
  }
}
