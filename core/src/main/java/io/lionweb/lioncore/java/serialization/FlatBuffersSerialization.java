package io.lionweb.lioncore.java.serialization;

import com.google.flatbuffers.FlatBufferBuilder;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.flatbuffers.*;
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

  public byte[] serializeBulkImport(BulkImport bulkImport) {
    FlatBufferBuilder builder = new FlatBufferBuilder(1024);

    FBHelper helper = new FBHelper(builder);
    Map<String, String> containerByAttached = new HashMap<>();

    int[] attachPointOffsets = new int[bulkImport.getAttachPoints().size()];
    int i = 0;
    for (BulkImport.AttachPoint attachPoint : bulkImport.getAttachPoints()) {
      int containment = helper.offsetForMetaPointer(attachPoint.containment);
      int container = builder.createSharedString(attachPoint.container);
      int root = builder.createSharedString(attachPoint.rootId);
      FBAttachPoint.startFBAttachPoint(builder);
      FBAttachPoint.addContainer(builder, container);
      FBAttachPoint.addContainment(builder, containment);
      FBAttachPoint.addRoot(builder, root);
      attachPointOffsets[i] = FBAttachPoint.endFBAttachPoint(builder);
      i++;
      containerByAttached.put(attachPoint.rootId, attachPoint.container);
    }

    int[] nodesOffsets = new int[bulkImport.getNodes().size()];
    i = 0;
    for (ClassifierInstance<?> node : bulkImport.getNodes()) {

      List<Feature<?>> features = node.getClassifier().allFeatures();
      List<Property> properties = new LinkedList<>();
      List<Containment> containments = new LinkedList<>();
      List<Reference> references = new LinkedList<>();
      for (Feature<?> feature : features) {
        if (feature instanceof Property) {
          properties.add((Property) feature);
        } else if (feature instanceof Containment) {
          containments.add((Containment) feature);
        } else if (feature instanceof Reference) {
          references.add((Reference) feature);
        } else {
          throw new IllegalStateException();
        }
      }
      int[] propOffsets;
      {
        propOffsets = new int[properties.size()];
        int fi = 0;
        for (Property property : properties) {
          int metaPointer = helper.offsetForMetaPointer(MetaPointer.from(property));
          Object propertyValue = node.getPropertyValue(property);
          int propertyValueOffset = -1;
          if (propertyValue != null) {
            String propertyValueStr =
                this.primitiveValuesSerialization.serialize(
                    property.getType().getID(), propertyValue);
            propertyValueOffset = builder.createSharedString(propertyValueStr);
          }
          FBProperty.startFBProperty(builder);
          FBProperty.addMetaPointer(builder, metaPointer);
          if (propertyValueOffset != -1) {
            FBProperty.addValue(builder, propertyValueOffset);
          }
          propOffsets[fi] = FBProperty.endFBProperty(builder);
          fi++;
        }
      }
      int[] contOffsets;
      {
        contOffsets = new int[containments.size()];
        int fi = 0;
        for (Containment containment : containments) {
          List<? extends Node> children = node.getChildren(containment);
          int[] childOffsets = new int[children.size()];
          int ci = 0;
          for (Node child : children) {
            childOffsets[ci] = builder.createSharedString(child.getID());
            ci++;
          }

          int metaPointer = helper.offsetForMetaPointer(MetaPointer.from(containment));
          int childrenVector = FBContainment.createChildrenVector(builder, childOffsets);
          FBContainment.startFBContainment(builder);
          FBContainment.addMetaPointer(builder, metaPointer);
          FBContainment.addChildren(builder, childrenVector);
          contOffsets[fi] = FBContainment.endFBContainment(builder);
          fi++;
        }
      }
      int[] refeOffsets;
      {
        refeOffsets = new int[references.size()];
        int fi = 0;
        for (Reference reference : references) {
          List<ReferenceValue> referenceValues = node.getReferenceValues(reference);
          int[] refValuesOffsets = new int[referenceValues.size()];
          int ci = 0;
          for (ReferenceValue referenceValue : referenceValues) {
            int resolveInfo = -1;
            if (referenceValue.getResolveInfo() != null) {
              resolveInfo = builder.createSharedString(referenceValue.getResolveInfo());
            }
            int referredID = -1;
            if (referenceValue.getReferredID() != null) {
              referredID = builder.createSharedString(referenceValue.getReferredID());
            }
            FBReferenceValue.startFBReferenceValue(builder);
            if (resolveInfo != -1) {
              FBReferenceValue.addResolveInfo(builder, resolveInfo);
            }
            if (referredID != -1) {
              FBReferenceValue.addReferred(builder, referredID);
            }
            refValuesOffsets[ci] = FBReferenceValue.endFBReferenceValue(builder);
            ci++;
          }

          int metaPointer = helper.offsetForMetaPointer(MetaPointer.from(reference));
          int valuesVector = FBReference.createValuesVector(builder, refValuesOffsets);
          FBReference.startFBReference(builder);
          FBReference.addMetaPointer(builder, metaPointer);
          FBReference.addValues(builder, valuesVector);
          refeOffsets[fi] = FBReference.endFBReference(builder);
          fi++;
        }
      }
      int[] annOffsets = new int[node.getAnnotations().size()];
      int ai = 0;
      for (AnnotationInstance annotation : node.getAnnotations()) {
        annOffsets[ai] = builder.createSharedString(annotation.getID());
        ai++;
      }

      int classifier = helper.offsetForMetaPointer(MetaPointer.from(node.getClassifier()));
      int id = builder.createSharedString(node.getID());
      int propsVector = FBNode.createPropertiesVector(builder, propOffsets);
      int consVector = FBNode.createContainmentsVector(builder, contOffsets);
      int refsVector = FBNode.createReferencesVector(builder, refeOffsets);
      int annsVector = FBNode.createAnnotationsVector(builder, annOffsets);
      String parentID =
          node.getParent() == null
              ? containerByAttached.get(node.getID())
              : node.getParent().getID();
      int parent = builder.createSharedString(parentID);
      FBNode.startFBNode(builder);
      FBNode.addId(builder, id);
      FBNode.addClassifier(builder, classifier);
      FBNode.addProperties(builder, propsVector);
      FBNode.addContainments(builder, consVector);
      FBNode.addReferences(builder, refsVector);
      FBNode.addAnnotations(builder, annsVector);
      FBNode.addParent(builder, parent);

      nodesOffsets[i] = FBNode.endFBNode(builder);
      i++;
    }
    int attachPointsVectorOffset =
        FBBulkImport.createAttachPointsVector(builder, attachPointOffsets);
    int nodesVectorOffset = FBBulkImport.createNodesVector(builder, nodesOffsets);
    FBBulkImport.startFBBulkImport(builder);
    FBBulkImport.addAttachPoints(builder, attachPointsVectorOffset);
    FBBulkImport.addNodes(builder, nodesVectorOffset);
    builder.finish(FBBulkImport.endFBBulkImport(builder));
    return builder.dataBuffer().compact().array();
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

    int[] languagesVector(List<UsedLanguage> usedLanguages) {
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

    int[] propsVector(List<SerializedPropertyValue> properties) {
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

    int[] containmentsVector(List<SerializedContainmentValue> containments) {
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

    int[] referencesVector(List<SerializedReferenceValue> references) {
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
