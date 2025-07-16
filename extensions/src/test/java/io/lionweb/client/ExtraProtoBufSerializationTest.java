package io.lionweb.client;

import static org.junit.Assert.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.ExtraProtoBufSerialization;
import io.lionweb.serialization.extensions.ExtraSerializationProvider;
import io.lionweb.serialization.flatbuffers.gen.FBAttachPoint;
import io.lionweb.serialization.flatbuffers.gen.FBBulkImport;
import io.lionweb.serialization.flatbuffers.gen.FBMetaPointer;
import io.lionweb.serialization.flatbuffers.gen.FBNode;
import java.nio.ByteBuffer;
import org.junit.Test;

/** Testing various functionalities of ProtoBufSerialization. */
public class ExtraProtoBufSerializationTest {

  @Test
  public void bulkImportSerialization() {
    Language l = new Language("l", "l-id", "l-key", "1");
    Concept c = new Concept(l, "c", "c-id", "c-key");
    Property property = Property.createRequired("foo", LionCoreBuiltins.getString());
    property.setID("p-id");
    property.setKey("p-key");
    c.addFeature(property);

    DynamicNode n1 = new DynamicNode("n1", c);
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", "abc");

    BulkImport bulkImport = new BulkImport();
    bulkImport.addNode(n1);
    bulkImport.addAttachPoint(
        new BulkImport.AttachPoint("n2", MetaPointer.get("Foo", "1", "c-key"), "n1"));

    ExtraProtoBufSerialization serialization =
        ExtraSerializationProvider.getExtraStandardProtoBufSerialization();
    byte[] bytes = serialization.serializeBulkImportToBytes(bulkImport);

    ByteBuffer bb = ByteBuffer.wrap(bytes);
    FBBulkImport fbBulkImport = FBBulkImport.getRootAsFBBulkImport(bb);
    assertEquals(1, fbBulkImport.attachPointsLength());
    assertEquals(1, fbBulkImport.nodesLength());

    FBAttachPoint fbAttachPoint = fbBulkImport.attachPoints(0);

    assertEquals("n2", fbAttachPoint.container());
    assertEquals("n1", fbAttachPoint.root());

    FBMetaPointer fbAttachPointContainment = fbAttachPoint.containment();
    assertEquals("Foo", fbAttachPointContainment.language());
    assertEquals("1", fbAttachPointContainment.version());
    assertEquals("c-key", fbAttachPointContainment.key());

    FBNode fbNode = fbBulkImport.nodes(0);
    assertEquals("n1", fbNode.id());
    // Here, even if we serialized a node that had no parent, the parent is obtained from the
    // attach point
    assertEquals("n2", fbNode.parent());
  }

  @Test
  public void bulkImportSerializationLW2023() {
    Language l = new Language(LionWebVersion.v2023_1, "l");
    l.setID("l-id");
    l.setKey("l-key");
    l.setVersion("1");
    Concept c = new Concept(l, "c", "c-id", "c-key");
    Property property = Property.createRequired("foo", LionCoreBuiltins.getString());
    property.setID("p-id");
    property.setKey("p-key");
    c.addFeature(property);

    DynamicNode n1 = new DynamicNode("n1", c);
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", "abc");

    BulkImport bulkImport = new BulkImport();
    bulkImport.addNode(n1);
    bulkImport.addAttachPoint(
        new BulkImport.AttachPoint("n2", MetaPointer.get("Foo", "1", "c-key"), "n1"));

    ExtraProtoBufSerialization serialization =
        ExtraSerializationProvider.getExtraStandardProtoBufSerialization(LionWebVersion.v2023_1);
    byte[] bytes = serialization.serializeBulkImportToBytes(bulkImport);

    ByteBuffer bb = ByteBuffer.wrap(bytes);
    FBBulkImport fbBulkImport = FBBulkImport.getRootAsFBBulkImport(bb);
    assertEquals(1, fbBulkImport.attachPointsLength());
    assertEquals(1, fbBulkImport.nodesLength());

    FBAttachPoint fbAttachPoint = fbBulkImport.attachPoints(0);

    assertEquals("n2", fbAttachPoint.container());
    assertEquals("n1", fbAttachPoint.root());

    FBMetaPointer fbAttachPointContainment = fbAttachPoint.containment();
    assertEquals("Foo", fbAttachPointContainment.language());
    assertEquals("1", fbAttachPointContainment.version());
    assertEquals("c-key", fbAttachPointContainment.key());

    FBNode fbNode = fbBulkImport.nodes(0);
    assertEquals("n1", fbNode.id());
    // Here, even if we serialized a node that had no parent, the parent is obtained from the
    // attach point
    assertEquals("n2", fbNode.parent());
  }

  /**
   * In this example we serialize a proper root node as it has no parent and no attach point. We
   * verify that with an effectively null parentID the serialization can still be completed.
   */
  @Test
  public void bulkImportSerializationOfPartitions() {
    Language l = new Language("l", "l-id", "l-key", "1");
    Concept c = new Concept(l, "c", "c-id", "c-key");
    Property property = Property.createRequired("foo", LionCoreBuiltins.getString());
    property.setID("p-id");
    property.setKey("p-key");
    c.addFeature(property);

    DynamicNode n1 = new DynamicNode("n1", c);
    ClassifierInstanceUtils.setPropertyValueByName(n1, "foo", "abc");

    BulkImport bulkImport = new BulkImport();
    bulkImport.addNode(n1);

    ExtraProtoBufSerialization serialization =
        ExtraSerializationProvider.getExtraStandardProtoBufSerialization();
    byte[] bytes = serialization.serializeBulkImportToBytes(bulkImport);

    ByteBuffer bb = ByteBuffer.wrap(bytes);
    FBBulkImport fbBulkImport = FBBulkImport.getRootAsFBBulkImport(bb);
    assertEquals(0, fbBulkImport.attachPointsLength());
    assertEquals(1, fbBulkImport.nodesLength());

    FBNode fbNode = fbBulkImport.nodes(0);
    assertEquals("n1", fbNode.id());
    assertEquals(null, fbNode.parent());
  }
}
