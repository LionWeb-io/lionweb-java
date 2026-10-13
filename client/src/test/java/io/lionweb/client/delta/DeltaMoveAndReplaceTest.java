package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.annotations.MoveAndReplaceAnnotationFromOtherParent;
import io.lionweb.client.delta.messages.commands.annotations.MoveAndReplaceAnnotationInSameParent;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildFromOtherContainment;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildInSameContainment;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedAndReplacedFromOtherParent;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedAndReplacedInSameParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedFromOtherContainment;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedInSameContainment;
import io.lionweb.client.delta.messages.queries.participations.SignOnRequest;
import io.lionweb.client.delta.messages.queries.participations.SignOnResponse;
import io.lionweb.client.delta.messages.queries.subscriptions.SubscribeToPartitionContentsRequest;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.language.Annotation;
import io.lionweb.language.Concept;
import io.lionweb.language.Containment;
import io.lionweb.language.Language;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.model.impl.DynamicNode;
import io.lionweb.serialization.JsonSerialization;
import io.lionweb.serialization.data.MetaPointer;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * Tests for the handling, by the in-memory server, of the commands moving a child or an annotation
 * while replacing another one: MoveAndReplaceChildInSameContainment,
 * MoveAndReplaceChildFromOtherContainment, MoveAndReplaceChildFromOtherContainmentInSameParent,
 * MoveAndReplaceAnnotationInSameParent and MoveAndReplaceAnnotationFromOtherParent.
 */
public class DeltaMoveAndReplaceTest extends AbstractDeltaProtocolTest {

  private static final Language LANG = new Language("BoxLang", "box-lang", "box-lang-key", "1");
  private static final Concept ITEM = new Concept(LANG, "Item", "item-id", "item-key");
  private static final Concept BOX = new Concept(LANG, "Box", "box-id", "box-key");
  private static final Annotation COMMENT =
      new Annotation(LANG, "Comment", "comment-id", "comment-key");
  private static final Containment LEFT = Containment.createMultiple("left", ITEM, "box-left-id");
  private static final Containment RIGHT =
      Containment.createMultiple("right", ITEM, "box-right-id");
  private static final Containment CHILDREN =
      Containment.createMultiple("children", ITEM, "item-children-id");

  static {
    LANG.addElement(ITEM);
    LANG.addElement(BOX);
    LANG.addElement(COMMENT);
    BOX.setPartition(true);
    LEFT.setKey("box-left-key");
    BOX.addFeature(LEFT);
    RIGHT.setKey("box-right-key");
    BOX.addFeature(RIGHT);
    CHILDREN.setKey("item-children-key");
    ITEM.addFeature(CHILDREN);
  }

  private static final MetaPointer LEFT_MP = MetaPointer.from(LEFT);
  private static final MetaPointer RIGHT_MP = MetaPointer.from(RIGHT);
  private static final MetaPointer CHILDREN_MP = MetaPointer.from(CHILDREN);

  private InMemoryServer server;
  private DeltaChannel channel;
  private String participationId;
  private final List<DeltaEvent> events = new ArrayList<>();

  /**
   * Prepares a server storing a Box partition with box.left = [i1, i2, i3], box.right = [i4],
   * i4.children = [i5], i5.children = [i6], box annotations = [a1, a2, a3] and i1 annotations =
   * [a4]. A participation subscribed to the partition records the events it receives.
   */
  private void prepare() {
    server = createServerWithRepository();
    JsonSerialization ser = serialization();
    ser.registerLanguage(LANG);
    ser.enableDynamicNodes();

    DynamicNode box = new DynamicNode("box", BOX);
    DynamicNode i1 = new DynamicNode("i1", ITEM);
    box.addChild(LEFT, i1);
    box.addChild(LEFT, new DynamicNode("i2", ITEM));
    box.addChild(LEFT, new DynamicNode("i3", ITEM));
    DynamicNode i4 = new DynamicNode("i4", ITEM);
    box.addChild(RIGHT, i4);
    DynamicNode i5 = new DynamicNode("i5", ITEM);
    i4.addChild(CHILDREN, i5);
    i5.addChild(CHILDREN, new DynamicNode("i6", ITEM));
    box.addAnnotation(new DynamicAnnotationInstance("a1", COMMENT));
    box.addAnnotation(new DynamicAnnotationInstance("a2", COMMENT));
    box.addAnnotation(new DynamicAnnotationInstance("a3", COMMENT));
    i1.addAnnotation(new DynamicAnnotationInstance("a4", COMMENT));
    server.createPartitionFromChunk(
        "MyRepo", ser.serializeTreeToSerializationChunk(box).getClassifierInstances());

    channel = prepareChannel(server);
    participationId =
        ((SignOnResponse)
                channel.sendQuery(
                    queryId ->
                        new SignOnRequest(queryId, DeltaProtocolVersion.v2026_1, "my-client")))
            .participationId;
    channel.sendQuery(queryId -> new SubscribeToPartitionContentsRequest(queryId, "box"));
    channel.registerEventReceiver(
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

    assertEquals(List.of("i1", "i2", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("a1", "a2", "a3"), stored("box").getAnnotations());
  }

  private SerializedClassifierInstance stored(String nodeId) {
    return server.retrieve("MyRepo", List.of(nodeId), 0).get(0);
  }

  private Set<String> storedIds() {
    return server.retrieve("MyRepo", List.of("box"), Integer.MAX_VALUE).stream()
        .map(SerializedClassifierInstance::getID)
        .collect(Collectors.toSet());
  }

  private <T extends DeltaEvent> T singleEvent(Class<T> eventClass) {
    assertEquals(1, events.size(), "Events received: " + events);
    return assertInstanceOf(eventClass, events.get(0));
  }

  @Test
  public void moveAndReplaceChildInSameContainment() {
    prepare();

    // Move i3 replacing i1: once i3 is removed, i1 is at index 0
    channel.sendCommand(
        participationId,
        commandId ->
            new MoveAndReplaceChildInSameContainment(commandId, "box", LEFT_MP, -2, 2, "i1", "i3"));

    assertEquals(List.of("i3", "i2"), stored("box").getContainmentValues(LEFT_MP));
    assertFalse(storedIds().contains("i1"));
    ChildMovedAndReplacedInSameContainment event =
        singleEvent(ChildMovedAndReplacedInSameContainment.class);
    assertEquals("i3", event.movedChild);
    assertEquals("i1", event.replacedChild);
    assertEquals(List.of(), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildFromOtherContainmentInSameParent() {
    prepare();

    // Move left[0] (i1) replacing right[0] (i4)
    channel.sendCommand(
        participationId,
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, RIGHT_MP, 0, "i4", "i1", "box", LEFT_MP, 0));

    assertEquals(List.of("i2", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("i1"), stored("box").getContainmentValues(RIGHT_MP));
    assertEquals("box", stored("i1").getParentNodeID());
    Set<String> ids = storedIds();
    assertFalse(ids.contains("i4"));
    assertFalse(ids.contains("i5"));
    assertFalse(ids.contains("i6"));
    ChildMovedAndReplacedFromOtherContainmentInSameParent event =
        singleEvent(ChildMovedAndReplacedFromOtherContainmentInSameParent.class);
    assertEquals("i1", event.movedChild);
    assertEquals("i4", event.replacedChild);
    assertEquals(List.of("i5", "i6"), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildFromOtherContainment() {
    prepare();

    // Move box.left[1] (i2) replacing i4.children[0] (i5)
    channel.sendCommand(
        participationId,
        commandId ->
            new MoveAndReplaceChildFromOtherContainment(
                commandId, "i4", CHILDREN_MP, 0, "box", LEFT_MP, 1, "i5", "i2"));

    assertEquals(List.of("i1", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("i2"), stored("i4").getContainmentValues(CHILDREN_MP));
    assertEquals("i4", stored("i2").getParentNodeID());
    Set<String> ids = storedIds();
    assertFalse(ids.contains("i5"));
    assertFalse(ids.contains("i6"));
    ChildMovedAndReplacedFromOtherContainment event =
        singleEvent(ChildMovedAndReplacedFromOtherContainment.class);
    assertEquals("i2", event.movedChild);
    assertEquals("i5", event.replacedChild);
    assertEquals(List.of("i6"), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildCannotReplaceAnAncestor() {
    prepare();

    // i6 is a descendant of i5, so it cannot replace it
    channel.sendCommand(
        participationId,
        commandId ->
            new MoveAndReplaceChildFromOtherContainment(
                commandId, "i4", CHILDREN_MP, 0, "i5", CHILDREN_MP, 0, "i5", "i6"));

    ErrorEvent error = singleEvent(ErrorEvent.class);
    assertEquals(StandardErrorCode.INVALID_MOVE.code, error.errorCode);
    assertEquals(List.of("i5"), stored("i4").getContainmentValues(CHILDREN_MP));
    assertEquals(List.of("i6"), stored("i5").getContainmentValues(CHILDREN_MP));
  }

  @Test
  public void moveAndReplaceAnnotationInSameParent() {
    prepare();

    // Move a3 replacing a1: once a3 is removed, a1 is at index 0
    channel.sendCommand(
        participationId,
        commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, -2, "a1", "a3", "box", 2));

    assertEquals(List.of("a3", "a2"), stored("box").getAnnotations());
    assertFalse(storedIds().contains("a1"));
    AnnotationMovedAndReplacedInSameParent event =
        singleEvent(AnnotationMovedAndReplacedInSameParent.class);
    assertEquals("a3", event.movedAnnotation);
    assertEquals("a1", event.replacedAnnotation);
  }

  @Test
  public void moveAndReplaceAnnotationFromOtherParent() {
    prepare();

    // Move a4 from i1 replacing a2 on box
    channel.sendCommand(
        participationId,
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 1, "a2", "a4", "i1", 0));

    assertEquals(List.of("a1", "a4", "a3"), stored("box").getAnnotations());
    assertEquals(List.of(), stored("i1").getAnnotations());
    assertEquals("box", stored("a4").getParentNodeID());
    assertFalse(storedIds().contains("a2"));
    AnnotationMovedAndReplacedFromOtherParent event =
        singleEvent(AnnotationMovedAndReplacedFromOtherParent.class);
    assertEquals("a4", event.movedAnnotation);
    assertEquals("a2", event.replacedAnnotation);
  }
}
