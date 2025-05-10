package io.lionweb.serialization.extensions;

import com.google.flatbuffers.FlatBufferBuilder;
import io.lionweb.lioncore.java.serialization.FlatBuffersSerialization;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.serialization.flatbuffers.gen.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** It contains the logic to serialize non-standard messages. */
public class ExtraFlatBuffersSerialization extends FlatBuffersSerialization {

  public byte[] serializeBulkImport(BulkImport bulkImport) {
    FlatBufferBuilder builder = new FlatBufferBuilder(1024);

    FBHelper helper = new FBHelper(builder);
    Map<String, String> containerByAttached = new HashMap<>();

    int[] attachPointOffsets =
        serializeAttachPoints(bulkImport, helper, builder, containerByAttached);
    int attachPointsVectorOffset =
        FBBulkImport.createAttachPointsVector(builder, attachPointOffsets);

    int[] nodesOffsets = serializeNodes(bulkImport, helper, builder, containerByAttached);
    int nodesVectorOffset = FBBulkImport.createNodesVector(builder, nodesOffsets);

    FBBulkImport.startFBBulkImport(builder);
    FBBulkImport.addAttachPoints(builder, attachPointsVectorOffset);
    FBBulkImport.addNodes(builder, nodesVectorOffset);
    builder.finish(FBBulkImport.endFBBulkImport(builder));
    return builder.dataBuffer().compact().array();
  }

  private int[] serializeNodes(
      BulkImport bulkImport,
      FBHelper helper,
      FlatBufferBuilder builder,
      Map<String, String> containerByAttached) {
    int i;

    int[] nodesOffsets = new int[bulkImport.getNodes().size()];
    i = 0;
    for (SerializedClassifierInstance node : bulkImport.getNodes()) {

      List<SerializedPropertyValue> properties = node.getProperties();
      List<SerializedContainmentValue> containments = node.getContainments();
      List<SerializedReferenceValue> references = node.getReferences();
      int[] propOffsets;
      {
        propOffsets = new int[properties.size()];
        int fi = 0;
        for (SerializedPropertyValue property : properties) {
          int metaPointer = helper.offsetForMetaPointer(property.getMetaPointer());
          String propertyValue = property.getValue();
          int propertyValueOffset = -1;
          if (propertyValue != null) {
            propertyValueOffset = builder.createSharedString(propertyValue);
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
        for (SerializedContainmentValue containment : containments) {
          List<String> children = containment.getValue();
          int[] childOffsets = new int[children.size()];
          int ci = 0;
          for (String child : children) {
            childOffsets[ci] = builder.createSharedString(child);
            ci++;
          }

          int metaPointer = helper.offsetForMetaPointer(containment.getMetaPointer());
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
        for (SerializedReferenceValue reference : references) {
          List<SerializedReferenceValue.Entry> referenceValues = reference.getValue();
          int[] refValuesOffsets = new int[referenceValues.size()];
          int ci = 0;
          for (SerializedReferenceValue.Entry referenceValue : referenceValues) {
            int resolveInfo = -1;
            if (referenceValue.getResolveInfo() != null) {
              resolveInfo = builder.createSharedString(referenceValue.getResolveInfo());
            }
            int referredID = -1;
            if (referenceValue.getReference() != null) {
              referredID = builder.createSharedString(referenceValue.getReference());
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

          int metaPointer = helper.offsetForMetaPointer(reference.getMetaPointer());
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
      for (String annotation : node.getAnnotations()) {
        annOffsets[ai] = builder.createSharedString(annotation);
        ai++;
      }

      int classifier = helper.offsetForMetaPointer(node.getClassifier());
      int id = builder.createSharedString(node.getID());
      int propsVector = FBNode.createPropertiesVector(builder, propOffsets);
      int consVector = FBNode.createContainmentsVector(builder, contOffsets);
      int refsVector = FBNode.createReferencesVector(builder, refeOffsets);
      int annsVector = FBNode.createAnnotationsVector(builder, annOffsets);
      String parentID =
          node.getParentNodeID() == null
              ? containerByAttached.get(node.getID())
              : node.getParentNodeID();
      int parent = -1;
      if (parentID != null) {
        parent = builder.createSharedString(parentID);
      }
      FBNode.startFBNode(builder);
      FBNode.addId(builder, id);
      FBNode.addClassifier(builder, classifier);
      FBNode.addProperties(builder, propsVector);
      FBNode.addContainments(builder, consVector);
      FBNode.addReferences(builder, refsVector);
      FBNode.addAnnotations(builder, annsVector);
      if (parentID != null) {
        FBNode.addParent(builder, parent);
      }

      nodesOffsets[i] = FBNode.endFBNode(builder);
      i++;
    }
    return nodesOffsets;
  }

  private static int[] serializeAttachPoints(
      BulkImport bulkImport,
      FBHelper helper,
      FlatBufferBuilder builder,
      Map<String, String> containerByAttached) {
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
    return attachPointOffsets;
  }
}
