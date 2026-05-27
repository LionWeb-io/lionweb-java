package io.lionweb.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.CodedInputStream;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Testing various functionalities of ExtraProtoBufSerialization. */
public class ExtraProtoBufSerializationTest {

  // ---- Wire-format parser for test verification ----

  private static class ParsedBulkImport {
    final List<String> strings = new ArrayList<>();
    final List<int[]> metaPointers = new ArrayList<>(); // [liLanguage, siKey]
    final List<int[]> languages = new ArrayList<>(); // [siKey, siVersion]
    final List<int[]> attachPoints = new ArrayList<>(); // [siContainer, mpiMetaPointer, siRoot]
    final List<int[]> nodes = new ArrayList<>(); // [siId, siParent]

    String str(int si) {
      return si == 0 ? null : strings.get(si - 1);
    }

    static ParsedBulkImport parse(byte[] bytes) throws IOException {
      ParsedBulkImport r = new ParsedBulkImport();
      CodedInputStream cis = CodedInputStream.newInstance(bytes);
      int tag;
      while ((tag = cis.readTag()) != 0) {
        switch (tag >>> 3) {
          case 1:
            r.strings.add(cis.readString());
            break;
          case 2:
            r.metaPointers.add(readMetaPointer(cis));
            break;
          case 3:
            r.attachPoints.add(readAttachPoint(cis));
            break;
          case 4:
            r.nodes.add(readNode(cis));
            break;
          case 5:
            r.languages.add(readLanguage(cis));
            break;
          default:
            cis.skipField(tag);
        }
      }
      return r;
    }

    private static int[] readMetaPointer(CodedInputStream cis) throws IOException {
      int len = cis.readRawVarint32();
      int oldLimit = cis.pushLimit(len);
      int liLanguage = 0, siKey = 0;
      int t;
      while ((t = cis.readTag()) != 0) {
        switch (t >>> 3) {
          case 1:
            liLanguage = cis.readUInt32();
            break;
          case 2:
            siKey = cis.readUInt32();
            break;
          default:
            cis.skipField(t);
        }
      }
      cis.popLimit(oldLimit);
      return new int[] {liLanguage, siKey};
    }

    private static int[] readLanguage(CodedInputStream cis) throws IOException {
      int len = cis.readRawVarint32();
      int oldLimit = cis.pushLimit(len);
      int siKey = 0, siVersion = 0;
      int t;
      while ((t = cis.readTag()) != 0) {
        switch (t >>> 3) {
          case 1:
            siKey = cis.readUInt32();
            break;
          case 2:
            siVersion = cis.readUInt32();
            break;
          default:
            cis.skipField(t);
        }
      }
      cis.popLimit(oldLimit);
      return new int[] {siKey, siVersion};
    }

    private static int[] readAttachPoint(CodedInputStream cis) throws IOException {
      int len = cis.readRawVarint32();
      int oldLimit = cis.pushLimit(len);
      int siContainer = 0, mpiMetaPointer = 0, siRoot = 0;
      int t;
      while ((t = cis.readTag()) != 0) {
        switch (t >>> 3) {
          case 1:
            siContainer = cis.readUInt32();
            break;
          case 2:
            mpiMetaPointer = cis.readUInt32();
            break;
          case 3:
            siRoot = cis.readUInt32();
            break;
          default:
            cis.skipField(t);
        }
      }
      cis.popLimit(oldLimit);
      return new int[] {siContainer, mpiMetaPointer, siRoot};
    }

    private static int[] readNode(CodedInputStream cis) throws IOException {
      int len = cis.readRawVarint32();
      int oldLimit = cis.pushLimit(len);
      int siId = 0, siParent = 0;
      int t;
      while ((t = cis.readTag()) != 0) {
        switch (t >>> 3) {
          case 1:
            siId = cis.readUInt32();
            break;
          case 7:
            siParent = cis.readUInt32();
            break;
          default:
            cis.skipField(t);
        }
      }
      cis.popLimit(oldLimit);
      return new int[] {siId, siParent};
    }
  }

  // ---- Tests ----

  @Test
  public void bulkImportSerialization() throws IOException {
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

    ParsedBulkImport p = ParsedBulkImport.parse(bytes);
    assertEquals(1, p.attachPoints.size());
    assertEquals(1, p.nodes.size());

    int[] ap = p.attachPoints.get(0);
    assertEquals("n2", p.str(ap[0])); // siContainer
    assertEquals("n1", p.str(ap[2])); // siRoot

    int[] mp = p.metaPointers.get(ap[1]); // mpiMetaPointer (0-based)
    int[] lang = p.languages.get(mp[0] - 1); // liLanguage (1-based into languages)
    assertEquals("Foo", p.str(lang[0])); // language key
    assertEquals("1", p.str(lang[1])); // language version
    assertEquals("c-key", p.str(mp[1])); // metapointer key

    int[] node = p.nodes.get(0);
    assertEquals("n1", p.str(node[0])); // siId
    assertEquals(0, node[1]); // siParent (null → 0)
  }

  @Test
  public void bulkImportSerializationLW2023() throws IOException {
    Language l = new Language(LionWebVersion.v2023_1, "l");
    l.setID("l-id");
    l.setKey("l-key");
    l.setVersion("1");
    Concept c = new Concept(l, "c", "c-id", "c-key");
    Property property =
        Property.createRequired("foo", LionCoreBuiltins.getString(LionWebVersion.v2023_1));
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

    ParsedBulkImport p = ParsedBulkImport.parse(bytes);
    assertEquals(1, p.attachPoints.size());
    assertEquals(1, p.nodes.size());

    int[] ap = p.attachPoints.get(0);
    assertEquals("n2", p.str(ap[0])); // siContainer
    assertEquals("n1", p.str(ap[2])); // siRoot

    int[] mp = p.metaPointers.get(ap[1]); // mpiMetaPointer (0-based)
    int[] lang = p.languages.get(mp[0] - 1); // liLanguage (1-based)
    assertEquals("Foo", p.str(lang[0]));
    assertEquals("1", p.str(lang[1]));
    assertEquals("c-key", p.str(mp[1]));

    int[] node = p.nodes.get(0);
    assertEquals("n1", p.str(node[0]));
    assertEquals(0, node[1]);
  }

  /**
   * Serializes a root node (no attach point). Verifies that a null parentID serializes correctly.
   */
  @Test
  public void bulkImportSerializationOfPartitions() throws IOException {
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

    ParsedBulkImport p = ParsedBulkImport.parse(bytes);
    assertEquals(0, p.attachPoints.size());
    assertEquals(1, p.nodes.size());

    int[] node = p.nodes.get(0);
    assertEquals("n1", p.str(node[0]));
    assertEquals(0, node[1]);
  }
}
