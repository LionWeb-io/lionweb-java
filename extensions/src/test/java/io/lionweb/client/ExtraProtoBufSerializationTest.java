package io.lionweb.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import io.lionweb.LionWebVersion;
import io.lionweb.language.Concept;
import io.lionweb.language.Language;
import io.lionweb.language.LionCoreBuiltins;
import io.lionweb.language.Property;
import io.lionweb.model.ClassifierInstanceUtils;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.protobuf.*;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.ExtraProtoBufSerialization;
import io.lionweb.serialization.extensions.ExtraSerializationProvider;
import org.junit.jupiter.api.Test;

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

    PBBulkImport pbBulkImport = null;
    try {
      pbBulkImport = PBBulkImport.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
    assertEquals(1, pbBulkImport.getAttachPointsCount());
    assertEquals(1, pbBulkImport.getNodesCount());

    PBAttachPoint pbAttachPoint = pbBulkImport.getAttachPoints(0);

    assertEquals("n2", pbBulkImport.getInternedStrings(pbAttachPoint.getSiContainer()));
    assertEquals("n1", pbBulkImport.getInternedStrings(pbAttachPoint.getSiRoot()));

    PBMetaPointer pbAttachPointContainment =
        pbBulkImport.getInternedMetaPointers(pbAttachPoint.getMpiMetaPointer());
    assertEquals(
        "Foo",
        pbBulkImport.getInternedStrings(
            pbBulkImport
                .getInternedLanguages(pbAttachPointContainment.getLiLanguage())
                .getSiKey()));
    assertEquals(
        "1",
        pbBulkImport.getInternedStrings(
            pbBulkImport
                .getInternedLanguages(pbAttachPointContainment.getLiLanguage())
                .getSiVersion()));
    assertEquals("c-key", pbBulkImport.getInternedStrings(pbAttachPointContainment.getSiKey()));

    PBNode fbNode = pbBulkImport.getNodes(0);
    assertEquals("n1", pbBulkImport.getInternedStrings(fbNode.getSiId()));
    // Here, even if we serialized a node that had no parent, the parent is obtained from the
    // attach point
    assertEquals("n2", pbBulkImport.getInternedStrings(fbNode.getSiParent()));
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

    PBBulkImport fbBulkImport = null;
    try {
      fbBulkImport = PBBulkImport.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
    assertEquals(1, fbBulkImport.getAttachPointsCount());
    assertEquals(1, fbBulkImport.getNodesCount());

    PBAttachPoint fbAttachPoint = fbBulkImport.getAttachPoints(0);

    assertEquals("n2", fbBulkImport.getInternedStrings(fbAttachPoint.getSiContainer()));
    assertEquals("n1", fbBulkImport.getInternedStrings(fbAttachPoint.getSiRoot()));

    PBMetaPointer fbAttachPointContainment =
        fbBulkImport.getInternedMetaPointers(fbAttachPoint.getMpiMetaPointer());
    PBLanguage fbLanguage =
        fbBulkImport.getInternedLanguages(fbAttachPointContainment.getLiLanguage());
    assertEquals("Foo", fbBulkImport.getInternedStrings(fbLanguage.getSiKey()));
    assertEquals("1", fbBulkImport.getInternedStrings(fbLanguage.getSiVersion()));
    assertEquals("c-key", fbBulkImport.getInternedStrings(fbAttachPointContainment.getSiKey()));

    PBNode fbNode = fbBulkImport.getNodes(0);
    assertEquals("n1", fbBulkImport.getInternedStrings(fbNode.getSiId()));
    // Here, even if we serialized a node that had no parent, the parent is obtained from the
    // attach point
    assertEquals("n2", fbBulkImport.getInternedStrings(fbNode.getSiParent()));
  }

  /**
   * In this example we serialize a proper root node as it has no parent and no attach point. We
   * verify that with an effectively null parentID the serialization can still be completed.
   */
  @Test
  public void bulkImportSerializationOfPartitions() throws InvalidProtocolBufferException {
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

    PBBulkImport fbBulkImport = PBBulkImport.parseFrom(bytes);
    assertEquals(0, fbBulkImport.getAttachPointsCount());
    assertEquals(1, fbBulkImport.getNodesCount());

    PBNode fbNode = fbBulkImport.getNodes(0);
    assertEquals("n1", fbBulkImport.getInternedStrings(fbNode.getSiId()));
    assertEquals(null, fbBulkImport.getInternedStrings(fbNode.getSiParent()));
  }
}
