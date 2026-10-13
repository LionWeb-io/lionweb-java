package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.references.DeleteReferenceResolveInfo;
import io.lionweb.client.delta.messages.commands.references.MoveEntryInSameReference;
import io.lionweb.client.delta.messages.events.CustomErrorCode;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.queries.participations.SignOnRequest;
import io.lionweb.client.delta.messages.queries.participations.SignOnResponse;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.language.Language;
import io.lionweb.language.Reference;
import io.lionweb.model.ClassifierInstance;
import io.lionweb.model.Node;
import io.lionweb.model.ReferenceValue;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedReferenceValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * Tests for Delta protocol implementation: operations on the entries of references.
 *
 * <p>Covers: AddReferenceTarget, ChangeReferenceTarget, DeleteReferenceTarget,
 * AddReferenceResolveInfo, ChangeReferenceResolveInfo, DeleteReferenceResolveInfo,
 * MoveEntryInSameReference, MoveEntryFromOtherReference, MoveEntryFromOtherReferenceInSameParent,
 * MoveAndReplaceEntryInSameReference, MoveAndReplaceEntryFromOtherReference,
 * MoveAndReplaceEntryFromOtherReferenceInSameParent, and the corresponding events.
 *
 * <p>Each test checks both the state stored by the server and the local model of a second client,
 * which applies the events it receives.
 */
public class DeltaReferenceEntriesTest extends AbstractDeltaProtocolTest {

  private static final Language LANG = new Language("RefLang", "ref-lang", "ref-lang-key", "1");
  private static final Concept ITEM = new Concept(LANG, "Item", "item-id", "item-key");
  private static final Concept BOX = new Concept(LANG, "Box", "box-id", "box-key");
  private static final Containment ITEMS =
      Containment.createMultiple("items", ITEM, "box-items-id");
  private static final Reference REFS_A = Reference.createMultiple("refsA", ITEM, "box-refs-a-id");
  private static final Reference REFS_B = Reference.createMultiple("refsB", ITEM, "box-refs-b-id");
  private static final Reference LINKS = Reference.createMultiple("links", ITEM, "item-links-id");

  static {
    LANG.addElement(ITEM);
    LANG.addElement(BOX);
    BOX.setPartition(true);
    ITEMS.setKey("box-items-key");
    BOX.addFeature(ITEMS);
    REFS_A.setKey("box-refs-a-key");
    BOX.addFeature(REFS_A);
    REFS_B.setKey("box-refs-b-key");
    BOX.addFeature(REFS_B);
    LINKS.setKey("item-links-key");
    ITEM.addFeature(LINKS);
  }

  private static final MetaPointer REFS_A_MP = MetaPointer.from(REFS_A);
  private static final MetaPointer REFS_B_MP = MetaPointer.from(REFS_B);
  private static final MetaPointer LINKS_MP = MetaPointer.from(LINKS);

  /**
   * A server storing a Box partition with items i1..i4, where box.refsA = [i1, i2, i3] (with
   * resolveInfo n1, n2, n3), box.refsB = [i4] (with resolveInfo n4) and i1.links = [i2] (with
   * resolveInfo n2). Client 1 is used to send commands, while client 2 monitors its own copy of the
   * partition.
   */
  private static class Fixture {
    final InMemoryServer server;
    final DeltaChannel channel;
    final DeltaClient client1;
    final ClassifierInstance<?> box2;

    Fixture(InMemoryServer server, DeltaChannel channel, DeltaClient client1, Node box2) {
      this.server = server;
      this.channel = channel;
      this.client1 = client1;
      this.box2 = box2;
    }

    List<SerializedReferenceValue.Entry> stored(String nodeId, MetaPointer reference) {
      return server.retrieve("MyRepo", List.of(nodeId), 0).get(0).getReferenceValues(reference);
    }

    ClassifierInstance<?> onClient2(String nodeId) {
      if ("box".equals(nodeId)) return box2;
      return box2.getChildren(ITEMS).stream()
          .filter(n -> nodeId.equals(n.getID()))
          .findFirst()
          .orElseThrow();
    }

    List<SerializedReferenceValue.Entry> local(String nodeId, Reference reference) {
      return onClient2(nodeId).getReferenceValues(reference).stream()
          .map(rv -> new SerializedReferenceValue.Entry(rv.getReferredID(), rv.getResolveInfo()))
          .collect(Collectors.toList());
    }

    /** Verifies that both the server and client 2 have the expected entries. */
    void assertEntries(
        String nodeId, Reference reference, SerializedReferenceValue.Entry... expected) {
      List<SerializedReferenceValue.Entry> expectedList = Arrays.asList(expected);
      assertEquals(expectedList, stored(nodeId, MetaPointer.from(reference)), "server state");
      assertEquals(expectedList, local(nodeId, reference), "client 2 state");
    }
  }

  private static SerializedReferenceValue.Entry entry(String target, String resolveInfo) {
    return new SerializedReferenceValue.Entry(target, resolveInfo);
  }

  private Fixture prepare() {
    InMemoryServer server = createServerWithRepository();
    JsonSerialization ser = serialization();
    ser.registerLanguage(LANG);
    ser.enableDynamicNodes();

    DynamicNode box = new DynamicNode("box", BOX);
    List<DynamicNode> items = new ArrayList<>();
    for (int i = 1; i <= 4; i++) {
      DynamicNode item = new DynamicNode("i" + i, ITEM);
      box.addChild(ITEMS, item);
      items.add(item);
    }
    for (int i = 0; i < 3; i++) {
      box.addReferenceValue(REFS_A, new ReferenceValue(items.get(i), "n" + (i + 1)));
    }
    box.addReferenceValue(REFS_B, new ReferenceValue(items.get(3), "n4"));
    items.get(0).addReferenceValue(LINKS, new ReferenceValue(items.get(1), "n2"));
    server.createPartitionFromChunk(
        "MyRepo", ser.serializeTreeToSerializationChunk(box).getClassifierInstances());

    Node box2 = (Node) server.retrieveAsClassifierInstance("MyRepo", "box", ser);
    assertNotNull(box2);

    DeltaChannel channel = prepareChannel(server);

    DeltaClient client1 = new DeltaClient(channel, "my-client-1");
    client1.registerLanguage(LANG);
    client1.sendSignOnRequest();

    DeltaClient client2 = new DeltaClient(channel, "my-client-2");
    client2.registerLanguage(LANG);
    client2.sendSignOnRequest();
    client2.monitorPartition(box2);

    Fixture fixture = new Fixture(server, channel, client1, box2);
    fixture.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", "n3"));
    fixture.assertEntries("box", REFS_B, entry("i4", "n4"));
    fixture.assertEntries("i1", LINKS, entry("i2", "n2"));
    return fixture;
  }

  // ---------------------------------------------------------------------------
  // Targets and resolveInfos
  // ---------------------------------------------------------------------------

  @Test
  public void deleteAddAndChangeReferenceTarget() {
    Fixture f = prepare();

    f.client1.sendDeleteReferenceTargetCommand("box", REFS_A_MP, 1, "i2");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry(null, "n2"), entry("i3", "n3"));

    f.client1.sendAddReferenceTargetCommand("box", REFS_A_MP, 1, "i4");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i4", "n2"), entry("i3", "n3"));

    f.client1.sendChangeReferenceTargetCommand("box", REFS_A_MP, 1, "i4", "i2");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", "n3"));
  }

  @Test
  public void deleteAddAndChangeReferenceResolveInfo() {
    Fixture f = prepare();

    f.client1.sendDeleteReferenceResolveInfoCommand("box", REFS_A_MP, 2, "n3");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", null));

    f.client1.sendAddReferenceResolveInfoCommand("box", REFS_A_MP, 2, "third");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", "third"));

    f.client1.sendChangeReferenceResolveInfoCommand("box", REFS_A_MP, 2, "third", "n3");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", "n3"));
  }

  // ---------------------------------------------------------------------------
  // Moves
  // ---------------------------------------------------------------------------

  @Test
  public void moveEntryInSameReference() {
    Fixture f = prepare();

    // Move refsA[2] to index 0
    f.client1.sendMoveEntryInSameReferenceCommand("box", REFS_A_MP, 2, 0, "i3", "n3");
    f.assertEntries("box", REFS_A, entry("i3", "n3"), entry("i1", "n1"), entry("i2", "n2"));

    // Move refsA[0] to index 2
    f.client1.sendMoveEntryInSameReferenceCommand("box", REFS_A_MP, 0, 2, "i3", "n3");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"), entry("i3", "n3"));
  }

  @Test
  public void moveEntryFromOtherReferenceInSameParent() {
    Fixture f = prepare();

    // Move refsA[0] to refsB[1]
    f.client1.sendMoveEntryFromOtherReferenceInSameParentCommand(
        "box", REFS_A_MP, 0, REFS_B_MP, 1, "i1", "n1");
    f.assertEntries("box", REFS_A, entry("i2", "n2"), entry("i3", "n3"));
    f.assertEntries("box", REFS_B, entry("i4", "n4"), entry("i1", "n1"));
  }

  @Test
  public void moveEntryFromOtherReference() {
    Fixture f = prepare();

    // Move box.refsA[1] to i1.links[0]
    f.client1.sendMoveEntryFromOtherReferenceCommand(
        "box", REFS_A_MP, 1, "i1", LINKS_MP, 0, "i2", "n2");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i3", "n3"));
    f.assertEntries("i1", LINKS, entry("i2", "n2"), entry("i2", "n2"));

    // Move i1.links[1] back to box.refsB[0]
    f.client1.sendMoveEntryFromOtherReferenceCommand(
        "i1", LINKS_MP, 1, "box", REFS_B_MP, 0, "i2", "n2");
    f.assertEntries("i1", LINKS, entry("i2", "n2"));
    f.assertEntries("box", REFS_B, entry("i2", "n2"), entry("i4", "n4"));
  }

  @Test
  public void moveAndReplaceEntryInSameReference() {
    Fixture f = prepare();

    // Move refsA[0] replacing i3: once i1 is removed, i3 is at index 1
    f.client1.sendMoveAndReplaceEntryInSameReferenceCommand(
        "box", REFS_A_MP, 0, 1, "i1", "n1", "i3", "n3");
    f.assertEntries("box", REFS_A, entry("i2", "n2"), entry("i1", "n1"));

    // Move refsA[1] replacing refsA[0]
    f.client1.sendMoveAndReplaceEntryInSameReferenceCommand(
        "box", REFS_A_MP, 1, 0, "i1", "n1", "i2", "n2");
    f.assertEntries("box", REFS_A, entry("i1", "n1"));
  }

  @Test
  public void moveAndReplaceEntryFromOtherReferenceInSameParent() {
    Fixture f = prepare();

    // Move refsA[2] replacing refsB[0]
    f.client1.sendMoveAndReplaceEntryFromOtherReferenceInSameParentCommand(
        "box", REFS_A_MP, 2, REFS_B_MP, 0, "i3", "n3", "i4", "n4");
    f.assertEntries("box", REFS_A, entry("i1", "n1"), entry("i2", "n2"));
    f.assertEntries("box", REFS_B, entry("i3", "n3"));
  }

  @Test
  public void moveAndReplaceEntryFromOtherReference() {
    Fixture f = prepare();

    // Move box.refsA[0] replacing i1.links[0]
    f.client1.sendMoveAndReplaceEntryFromOtherReferenceCommand(
        "box", REFS_A_MP, 0, "i1", LINKS_MP, 0, "i1", "n1", "i2", "n2");
    f.assertEntries("box", REFS_A, entry("i2", "n2"), entry("i3", "n3"));
    f.assertEntries("i1", LINKS, entry("i1", "n1"));
  }

  // ---------------------------------------------------------------------------
  // Invalid commands
  // ---------------------------------------------------------------------------

  @Test
  public void invalidCommandsAreAnsweredWithErrorEvents() {
    Fixture f = prepare();

    String participationId =
        ((SignOnResponse)
                f.channel.sendQuery(
                    queryId ->
                        new SignOnRequest(queryId, DeltaProtocolVersion.v2026_1, "raw-client")))
            .participationId;
    List<DeltaEvent> events = new ArrayList<>();
    f.channel.registerEventReceiver(
        new DeltaEventReceiver() {
          @Override
          public void receiveEvent(@NotNull DeltaEvent event) {
            events.add(event);
          }

          @Override
          public @NotNull String getParticipationId() {
            return participationId;
          }
        });

    // Index out of range
    f.channel.sendCommand(
        participationId,
        commandId -> new MoveEntryInSameReference(commandId, "box", REFS_A_MP, 5, -1, null, null));
    assertEquals(1, events.size());
    assertInstanceOf(ErrorEvent.class, events.get(0));
    assertEquals(StandardErrorCode.UNKNOWN_INDEX.code, ((ErrorEvent) events.get(0)).errorCode);

    // Deleting a resolveInfo which is not there
    f.client1.sendDeleteReferenceResolveInfoCommand("box", REFS_A_MP, 0, "n1");
    f.channel.sendCommand(
        participationId,
        commandId -> new DeleteReferenceResolveInfo(commandId, "box", REFS_A_MP, 0, "n1"));
    assertEquals(2, events.size());
    assertInstanceOf(ErrorEvent.class, events.get(1));
    assertEquals(CustomErrorCode.UNDEFINED_RESOLVE_INFO, ((ErrorEvent) events.get(1)).errorCode);

    // The stored state is not affected by the invalid commands
    f.assertEntries("box", REFS_A, entry("i1", null), entry("i2", "n2"), entry("i3", "n3"));
  }
}
