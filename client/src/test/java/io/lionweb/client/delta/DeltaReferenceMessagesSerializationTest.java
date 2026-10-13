package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import io.lionweb.client.delta.messages.commands.references.*;
import io.lionweb.client.delta.messages.events.references.*;
import io.lionweb.serialization.data.MetaPointer;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that all the commands and events concerning references are registered in {@link
 * DeltaMessageSerialization}, so that they can be serialized and deserialized.
 */
public class DeltaReferenceMessagesSerializationTest {

  private static final DeltaMessageSerialization SERIALIZATION = new DeltaMessageSerialization();
  private static final MetaPointer REF_A = MetaPointer.get("MyLang", "1", "refA");
  private static final MetaPointer REF_B = MetaPointer.get("MyLang", "1", "refB");

  static Stream<Object> messages() {
    return Stream.of(
        // Commands
        new AddReference("c1", "p", REF_A, 0, "t", "ri"),
        new ChangeReference("c2", "p", REF_A, 1, "t1", "ri1", "t2", "ri2"),
        new DeleteReference("c3", "p", REF_A, 2, "t", "ri"),
        new AddReferenceTarget("c4", "p", REF_A, 0, "t"),
        new ChangeReferenceTarget("c5", "p", REF_A, 0, "t1", "t2"),
        new DeleteReferenceTarget("c6", "p", REF_A, 0, "t"),
        new AddReferenceResolveInfo("c7", "p", REF_A, 0, "ri"),
        new ChangeReferenceResolveInfo("c8", "p", REF_A, 0, "ri1", "ri2"),
        new DeleteReferenceResolveInfo("c9", "p", REF_A, 0, "ri"),
        new MoveEntryInSameReference("c10", "p", REF_A, 3, -2, "t", "ri"),
        new MoveEntryFromOtherReference("c11", "p2", REF_B, 1, "p1", REF_A, 0, "t", "ri"),
        new MoveEntryFromOtherReferenceInSameParent("c12", "p", REF_B, 1, REF_A, 0, "t", null),
        new MoveAndReplaceEntryInSameReference("c13", "p", REF_A, 0, "t1", "ri1", 2, "t2", "ri2"),
        new MoveAndReplaceEntryFromOtherReference(
            "c14", "p2", REF_B, 1, "t2", "ri2", "p1", REF_A, 0, "t1", "ri1"),
        new MoveAndReplaceEntryFromOtherReferenceInSameParent(
            "c15", "p", REF_B, 1, "t2", null, REF_A, 0, null, "ri1"),
        // Events
        new ReferenceAdded(1, "p", REF_A, 0, "t", "ri"),
        new ReferenceChanged(2, "p", REF_A, 0, "t2", "ri2", "t1", "ri1"),
        new ReferenceDeleted(3, "p", REF_A, 0, "t", "ri"),
        new ReferenceTargetAdded(4, "p", REF_A, 0, "t", "ri"),
        new ReferenceTargetChanged(5, "p", REF_A, 0, "t2", "ri", "t1"),
        new ReferenceTargetDeleted(6, "p", REF_A, 0, "ri", "t"),
        new ReferenceResolveInfoAdded(7, "p", REF_A, 0, "ri", "t"),
        new ReferenceResolveInfoChanged(8, "p", REF_A, 0, "ri2", "t", "ri1"),
        new ReferenceResolveInfoDeleted(9, "p", REF_A, 0, "t", "ri"),
        new EntryMovedInSameReference(10, "p", REF_A, 3, -2, "t", "ri"),
        new EntryMovedFromOtherReference(11, "p2", REF_B, 1, "p1", REF_A, 0, "t", "ri"),
        new EntryMovedFromOtherReferenceInSameParent(12, "p", REF_B, 1, REF_A, 0, null, "ri"),
        new EntryMovedAndReplacedInSameReference(13, "p", REF_A, 2, "t1", "ri1", 0, "t2", "ri2"),
        new EntryMovedAndReplacedFromOtherReference(
            14, "p2", REF_B, 1, "t1", "ri1", "p1", REF_A, 0, "t2", "ri2"),
        new EntryMovedAndReplacedFromOtherReferenceInSameParent(
            15, "p", REF_B, 1, "t1", null, REF_A, 0, null, "ri2"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("messages")
  public void roundTrip(Object message) {
    String kind = message.getClass().getSimpleName();
    assertTrue(SERIALIZATION.isKnownKind(kind), kind + " is not registered");
    assertEquals(message.getClass(), SERIALIZATION.getClassForKind(kind));

    String json = SERIALIZATION.serialize(message);
    assertEquals(
        kind, JsonParser.parseString(json).getAsJsonObject().get("messageKind").getAsString());

    Object deserialized = SERIALIZATION.deserialize(json);
    assertNotNull(deserialized, "Deserialization returned null for " + kind);
    assertEquals(message.getClass(), deserialized.getClass());
    assertEquals(json, SERIALIZATION.serialize(deserialized));
  }

  @Test
  public void deserializeMoveEntryFromOtherReference() {
    String json =
        "{\"messageKind\":\"MoveEntryFromOtherReference\","
            + "\"newParent\":\"newParentId\","
            + "\"newReference\":{\"language\":\"MyLang\",\"version\":\"1\",\"key\":\"refB\"},"
            + "\"newIndex\":2,"
            + "\"oldParent\":\"oldParentId\","
            + "\"oldReference\":{\"language\":\"MyLang\",\"version\":\"1\",\"key\":\"refA\"},"
            + "\"oldIndex\":0,"
            + "\"movedTarget\":\"targetId\","
            + "\"movedResolveInfo\":\"info\","
            + "\"commandId\":\"cmdId\","
            + "\"additionalInfos\":[]}";
    MoveEntryFromOtherReference command =
        assertInstanceOf(MoveEntryFromOtherReference.class, SERIALIZATION.deserialize(json));
    assertEquals("cmdId", command.commandId);
    assertEquals("newParentId", command.newParent);
    assertEquals(REF_B, command.newReference);
    assertEquals(2, command.newIndex);
    assertEquals("oldParentId", command.oldParent);
    assertEquals(REF_A, command.oldReference);
    assertEquals(0, command.oldIndex);
    assertEquals("targetId", command.movedTarget);
    assertEquals("info", command.movedResolveInfo);
    assertEquals(List.of(), command.additionalInfos);
  }
}
