package io.lionweb.serialization.extensions;

import com.google.flatbuffers.FlatBufferBuilder;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.language.Feature;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.language.Reference;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.serialization.FlatBuffersSerialization;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.serialization.flatbuffers.FBBulkImport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** It contains the logic to serialize non-standard messages. */
public class ExtraFlatBuffersSerialization extends FlatBuffersSerialization {

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
      io.lionweb.serialization.flatbuffers.FBAttachPoint.startFBAttachPoint(builder);
      io.lionweb.serialization.flatbuffers.FBAttachPoint.addContainer(builder, container);
      io.lionweb.serialization.flatbuffers.FBAttachPoint.addContainment(builder, containment);
      io.lionweb.serialization.flatbuffers.FBAttachPoint.addRoot(builder, root);
      attachPointOffsets[i] =
          io.lionweb.serialization.flatbuffers.FBAttachPoint.endFBAttachPoint(builder);
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
          io.lionweb.serialization.flatbuffers.FBProperty.startFBProperty(builder);
          io.lionweb.serialization.flatbuffers.FBProperty.addMetaPointer(builder, metaPointer);
          if (propertyValueOffset != -1) {
            io.lionweb.serialization.flatbuffers.FBProperty.addValue(builder, propertyValueOffset);
          }
          propOffsets[fi] = io.lionweb.serialization.flatbuffers.FBProperty.endFBProperty(builder);
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
          int childrenVector =
              io.lionweb.serialization.flatbuffers.FBContainment.createChildrenVector(
                  builder, childOffsets);
          io.lionweb.serialization.flatbuffers.FBContainment.startFBContainment(builder);
          io.lionweb.serialization.flatbuffers.FBContainment.addMetaPointer(builder, metaPointer);
          io.lionweb.serialization.flatbuffers.FBContainment.addChildren(builder, childrenVector);
          contOffsets[fi] =
              io.lionweb.serialization.flatbuffers.FBContainment.endFBContainment(builder);
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
            io.lionweb.serialization.flatbuffers.FBReferenceValue.startFBReferenceValue(builder);
            if (resolveInfo != -1) {
              io.lionweb.serialization.flatbuffers.FBReferenceValue.addResolveInfo(
                  builder, resolveInfo);
            }
            if (referredID != -1) {
              io.lionweb.serialization.flatbuffers.FBReferenceValue.addReferred(
                  builder, referredID);
            }
            refValuesOffsets[ci] =
                io.lionweb.serialization.flatbuffers.FBReferenceValue.endFBReferenceValue(builder);
            ci++;
          }

          int metaPointer = helper.offsetForMetaPointer(MetaPointer.from(reference));
          int valuesVector =
              io.lionweb.serialization.flatbuffers.FBReference.createValuesVector(
                  builder, refValuesOffsets);
          io.lionweb.serialization.flatbuffers.FBReference.startFBReference(builder);
          io.lionweb.serialization.flatbuffers.FBReference.addMetaPointer(builder, metaPointer);
          io.lionweb.serialization.flatbuffers.FBReference.addValues(builder, valuesVector);
          refeOffsets[fi] =
              io.lionweb.serialization.flatbuffers.FBReference.endFBReference(builder);
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
      int propsVector =
          io.lionweb.serialization.flatbuffers.FBNode.createPropertiesVector(builder, propOffsets);
      int consVector =
          io.lionweb.serialization.flatbuffers.FBNode.createContainmentsVector(
              builder, contOffsets);
      int refsVector =
          io.lionweb.serialization.flatbuffers.FBNode.createReferencesVector(builder, refeOffsets);
      int annsVector =
          io.lionweb.serialization.flatbuffers.FBNode.createAnnotationsVector(builder, annOffsets);
      String parentID =
          node.getParent() == null
              ? containerByAttached.get(node.getID())
              : node.getParent().getID();
      int parent = builder.createSharedString(parentID);
      io.lionweb.serialization.flatbuffers.FBNode.startFBNode(builder);
      io.lionweb.serialization.flatbuffers.FBNode.addId(builder, id);
      io.lionweb.serialization.flatbuffers.FBNode.addClassifier(builder, classifier);
      io.lionweb.serialization.flatbuffers.FBNode.addProperties(builder, propsVector);
      io.lionweb.serialization.flatbuffers.FBNode.addContainments(builder, consVector);
      io.lionweb.serialization.flatbuffers.FBNode.addReferences(builder, refsVector);
      io.lionweb.serialization.flatbuffers.FBNode.addAnnotations(builder, annsVector);
      io.lionweb.serialization.flatbuffers.FBNode.addParent(builder, parent);

      nodesOffsets[i] = io.lionweb.serialization.flatbuffers.FBNode.endFBNode(builder);
      i++;
    }
    int attachPointsVectorOffset =
        io.lionweb.serialization.flatbuffers.FBBulkImport.createAttachPointsVector(
            builder, attachPointOffsets);
    int nodesVectorOffset =
        io.lionweb.serialization.flatbuffers.FBBulkImport.createNodesVector(builder, nodesOffsets);
    io.lionweb.serialization.flatbuffers.FBBulkImport.startFBBulkImport(builder);
    io.lionweb.serialization.flatbuffers.FBBulkImport.addAttachPoints(
        builder, attachPointsVectorOffset);
    io.lionweb.serialization.flatbuffers.FBBulkImport.addNodes(builder, nodesVectorOffset);
    builder.finish(FBBulkImport.endFBBulkImport(builder));
    return builder.dataBuffer().compact().array();
  }
}
