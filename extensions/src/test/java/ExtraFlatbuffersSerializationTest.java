import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.serialization.extensions.BulkImport;
import io.lionweb.serialization.extensions.ExtraFlatBuffersSerialization;
import io.lionweb.serialization.extensions.ExtraSerializationProvider;
import io.lionweb.serialization.flatbuffers.gen.FBAttachPoint;
import io.lionweb.serialization.flatbuffers.gen.FBBulkImport;
import io.lionweb.serialization.flatbuffers.gen.FBMetaPointer;
import io.lionweb.serialization.flatbuffers.gen.FBNode;
import java.nio.ByteBuffer;
import org.junit.Test;

/** Testing various functionalities of FlatBuffersSerialization. */
public class ExtraFlatbuffersSerializationTest {

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
        new BulkImport.AttachPoint("n2", new MetaPointer("Foo", "1", "c-key"), "n1"));

    ExtraFlatBuffersSerialization flatBuffersSerialization =
        ExtraSerializationProvider.getExtraStandardFlatBuffersSerialization();
    byte[] bytes = flatBuffersSerialization.serializeBulkImport(bulkImport);

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
  }
}
