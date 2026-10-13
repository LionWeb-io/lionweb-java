package io.lionweb.client.delta;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.commands.annotations.MoveAndReplaceAnnotationFromOtherParent;
import io.lionweb.client.delta.messages.commands.annotations.MoveAndReplaceAnnotationInSameParent;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildFromContainmentInOtherParent;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.commands.children.MoveAndReplaceChildInSameContainmentInSameParent;
import io.lionweb.client.delta.messages.events.ErrorEvent;
import io.lionweb.client.delta.messages.events.StandardErrorCode;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedAndReplacedFromOtherParent;
import io.lionweb.client.delta.messages.events.annotations.AnnotationMovedAndReplacedInSameParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedFromContainmentInOtherParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedFromOtherContainmentInSameParent;
import io.lionweb.client.delta.messages.events.children.ChildMovedAndReplacedInSameContainmentInSameParent;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * Tests for the handling, by the in-memory server, of the commands moving a child or an annotation
 * while replacing another one: MoveAndReplaceChildFromContainmentInOtherParent,
 * MoveAndReplaceChildFromOtherContainmentInSameParent,
 * MoveAndReplaceChildInSameContainmentInSameParent, MoveAndReplaceAnnotationFromOtherParent and
 * MoveAndReplaceAnnotationInSameParent.
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
  private final List<DeltaEvent> observerEvents = new ArrayList<>();

  /**
   * Prepares a server storing a Box partition with box.left = [i1, i2, i3], box.right = [i4],
   * i4.children = [i5], i5.children = [i6], box annotations = [a1, a2, a3], a2 annotations = [a6],
   * i1 annotations = [a4] and i5 annotations = [a5].
   */
  private void prepare() {
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
    i5.addAnnotation(new DynamicAnnotationInstance("a5", COMMENT));
    box.addAnnotation(new DynamicAnnotationInstance("a1", COMMENT));
    DynamicAnnotationInstance a2 = new DynamicAnnotationInstance("a2", COMMENT);
    a2.addAnnotation(new DynamicAnnotationInstance("a6", COMMENT));
    box.addAnnotation(a2);
    box.addAnnotation(new DynamicAnnotationInstance("a3", COMMENT));
    i1.addAnnotation(new DynamicAnnotationInstance("a4", COMMENT));
    prepare(box);

    assertEquals(List.of("i1", "i2", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("a1", "a2", "a3"), stored("box").getAnnotations());
    assertEquals(List.of("a6"), stored("a2").getAnnotations());
    assertEquals(List.of("a5"), stored("i5").getAnnotations());
  }

  /**
   * Prepares a server storing a Box partition whose left containment holds items with the given IDs
   * and whose annotations have the given IDs.
   */
  private void prepareList(List<String> itemIds, List<String> annotationIds) {
    DynamicNode box = new DynamicNode("box", BOX);
    itemIds.forEach(id -> box.addChild(LEFT, new DynamicNode(id, ITEM)));
    annotationIds.forEach(id -> box.addAnnotation(new DynamicAnnotationInstance(id, COMMENT)));
    prepare(box);
  }

  /**
   * Stores the given partition and signs on two participations subscribed to it: the one sending
   * the commands, and an observer. Both record the events they receive.
   */
  private void prepare(DynamicNode box) {
    server = createServerWithRepository();
    JsonSerialization ser = serialization();
    ser.registerLanguage(LANG);
    ser.enableDynamicNodes();
    server.createPartitionFromChunk(
        "MyRepo", ser.serializeTreeToSerializationChunk(box).getClassifierInstances());

    channel = prepareChannel(server);
    participationId = signOnAndSubscribe(channel, "my-client", events);
    DeltaChannel observerChannel = prepareChannel(server);
    signOnAndSubscribe(observerChannel, "observer", observerEvents);
  }

  private String signOnAndSubscribe(
      DeltaChannel channel, String clientId, List<DeltaEvent> receivedEvents) {
    String participation =
        ((SignOnResponse)
                channel.sendQuery(
                    queryId -> new SignOnRequest(queryId, DeltaProtocolVersion.v2026_1, clientId)))
            .participationId;
    channel.sendQuery(queryId -> new SubscribeToPartitionContentsRequest(queryId, "box"));
    channel.registerEventReceiver(
        new DeltaEventReceiver() {
          @Override
          public void receiveEvent(@NotNull DeltaEvent event) {
            receivedEvents.add(event);
          }

          @Override
          public @NotNull String getParticipationId() {
            return participation;
          }
        });
    return participation;
  }

  private void send(Function<String, DeltaCommand> commandProducer) {
    channel.sendCommand(participationId, commandProducer);
  }

  private SerializedClassifierInstance stored(String nodeId) {
    return server.retrieve("MyRepo", List.of(nodeId), 0).get(0);
  }

  private Set<String> storedIds() {
    return server.retrieve("MyRepo", List.of("box"), Integer.MAX_VALUE).stream()
        .map(SerializedClassifierInstance::getID)
        .collect(Collectors.toSet());
  }

  /** A textual snapshot of the whole stored partition, used to verify it is unchanged. */
  private List<String> snapshot() {
    return server.retrieve("MyRepo", List.of("box"), Integer.MAX_VALUE).stream()
        .map(SerializedClassifierInstance::toString)
        .collect(Collectors.toList());
  }

  private void assertDeleted(String... nodeIds) {
    Set<String> ids = storedIds();
    for (String nodeId : nodeIds) {
      assertFalse(ids.contains(nodeId), "Node " + nodeId + " should have been deleted");
    }
  }

  /** Verifies that both participations received the same single event, and returns it. */
  private <T extends DeltaEvent> T singleEvent(Class<T> eventClass) {
    assertEquals(1, events.size(), "Events received: " + events);
    assertEquals(1, observerEvents.size(), "Events received by the observer: " + observerEvents);
    assertInstanceOf(eventClass, observerEvents.get(0));
    return assertInstanceOf(eventClass, events.get(0));
  }

  /**
   * Sends the command and verifies that it is rejected with the given error code, sent only to the
   * issuing participation, and that the stored partition is unchanged.
   */
  private void assertRejected(
      StandardErrorCode expectedCode, Function<String, DeltaCommand> commandProducer) {
    List<String> before = snapshot();
    events.clear();
    observerEvents.clear();
    List<String> commandIds = new ArrayList<>();
    send(
        commandId -> {
          commandIds.add(commandId);
          return commandProducer.apply(commandId);
        });
    assertEquals(1, events.size(), "Events received: " + events);
    ErrorEvent error = assertInstanceOf(ErrorEvent.class, events.get(0));
    assertEquals(expectedCode.code, error.errorCode, error.message);
    assertEquals(1, error.originCommands.size());
    assertEquals(participationId, error.originCommands.get(0).participationId);
    assertEquals(commandIds.get(0), error.originCommands.get(0).commandId);
    assertEquals(List.of(), observerEvents);
    assertEquals(before, snapshot());
  }

  //
  // Successful commands
  //

  @Test
  public void moveAndReplaceChildFromContainmentInOtherParent() {
    prepare();

    // Move box.left[1] (i2) replacing i4.children[0] (i5, with child i6 and annotation a5)
    send(
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i4", CHILDREN_MP, 0, "box", LEFT_MP, 1, "i5", "i2"));

    assertEquals(List.of("i1", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("i2"), stored("i4").getContainmentValues(CHILDREN_MP));
    assertEquals("i4", stored("i2").getParentNodeID());
    assertDeleted("i5", "i6", "a5");
    ChildMovedAndReplacedFromContainmentInOtherParent event =
        singleEvent(ChildMovedAndReplacedFromContainmentInOtherParent.class);
    assertEquals("i4", event.newParent);
    assertEquals(CHILDREN_MP, event.newContainment);
    assertEquals(0, event.newIndex);
    assertEquals("i2", event.movedChild);
    assertEquals("box", event.oldParent);
    assertEquals(LEFT_MP, event.oldContainment);
    assertEquals(1, event.oldIndex);
    assertEquals("i5", event.replacedChild);
    assertEquals(List.of("i6", "a5"), event.replacedDescendants);
    assertEquals(participationId, event.originCommands.get(0).participationId);
  }

  @Test
  public void moveAndReplaceChildFromOtherContainmentInSameParent() {
    prepare();

    // Move box.left[0] (i1, with annotation a4) replacing box.right[0] (i4)
    send(
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, RIGHT_MP, 0, "i4", "i1", "box", LEFT_MP, 0));

    assertEquals(List.of("i2", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("i1"), stored("box").getContainmentValues(RIGHT_MP));
    assertEquals("box", stored("i1").getParentNodeID());
    assertEquals(List.of("a4"), stored("i1").getAnnotations());
    assertDeleted("i4", "i5", "i6", "a5");
    ChildMovedAndReplacedFromOtherContainmentInSameParent event =
        singleEvent(ChildMovedAndReplacedFromOtherContainmentInSameParent.class);
    assertEquals("box", event.parent);
    assertEquals(RIGHT_MP, event.newContainment);
    assertEquals(0, event.newIndex);
    assertEquals(LEFT_MP, event.oldContainment);
    assertEquals(0, event.oldIndex);
    assertEquals("i1", event.movedChild);
    assertEquals("i4", event.replacedChild);
    assertEquals(List.of("i5", "i6", "a5"), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildInSameContainmentMovingUp() {
    // Example of the specification: moving X with indexOffset = 3, replacing F
    prepareList(Arrays.asList("A", "B", "C", "X", "D", "E", "F", "G"), List.of());

    send(
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, 3, 3, "F", "X"));

    assertEquals(
        Arrays.asList("A", "B", "C", "D", "E", "X", "G"),
        stored("box").getContainmentValues(LEFT_MP));
    assertDeleted("F");
    ChildMovedAndReplacedInSameContainmentInSameParent event =
        singleEvent(ChildMovedAndReplacedInSameContainmentInSameParent.class);
    assertEquals("box", event.parent);
    assertEquals(LEFT_MP, event.containment);
    assertEquals(3, event.oldIndex);
    assertEquals(3, event.indexOffset);
    assertEquals("X", event.movedChild);
    assertEquals("F", event.replacedChild);
    assertEquals(List.of(), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildInSameContainmentMovingDown() {
    // Example of the specification: moving X with indexOffset = -3, replacing B
    prepareList(Arrays.asList("A", "B", "C", "D", "X", "E", "F"), List.of());

    send(
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, -3, 4, "B", "X"));

    assertEquals(
        Arrays.asList("A", "X", "C", "D", "E", "F"), stored("box").getContainmentValues(LEFT_MP));
    assertDeleted("B");
    ChildMovedAndReplacedInSameContainmentInSameParent event =
        singleEvent(ChildMovedAndReplacedInSameContainmentInSameParent.class);
    assertEquals(4, event.oldIndex);
    assertEquals(-3, event.indexOffset);
    assertEquals("X", event.movedChild);
    assertEquals("B", event.replacedChild);
  }

  @Test
  public void moveAndReplaceChildInSameContainmentWithDescendants() {
    prepare();

    // Move box.left[0] (i1) replacing box.left[1] (i2): the same as deleting i2
    send(
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, 1, 0, "i2", "i1"));

    assertEquals(List.of("i1", "i3"), stored("box").getContainmentValues(LEFT_MP));
    assertEquals(List.of("a4"), stored("i1").getAnnotations());
    assertDeleted("i2");
    singleEvent(ChildMovedAndReplacedInSameContainmentInSameParent.class);
  }

  @Test
  public void moveAndReplaceAnnotationFromOtherParent() {
    prepare();

    // Move a4 from i1 replacing box.annotations[1] (a2, annotated with a6)
    send(
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 1, "a2", "a4", "i1", 0));

    assertEquals(List.of("a1", "a4", "a3"), stored("box").getAnnotations());
    assertEquals(List.of(), stored("i1").getAnnotations());
    assertEquals("box", stored("a4").getParentNodeID());
    assertDeleted("a2", "a6");
    AnnotationMovedAndReplacedFromOtherParent event =
        singleEvent(AnnotationMovedAndReplacedFromOtherParent.class);
    assertEquals("box", event.newParent);
    assertEquals(1, event.newIndex);
    assertEquals("a4", event.movedAnnotation);
    assertEquals("i1", event.oldParent);
    assertEquals(0, event.oldIndex);
    assertEquals("a2", event.replacedAnnotation);
    assertArrayEquals(new String[] {"a6"}, event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceAnnotationInSameParentMovingUp() {
    // Example of the specification: moving X with indexOffset = 3, replacing F
    prepareList(List.of(), Arrays.asList("A", "B", "C", "X", "D", "E", "F", "G"));

    send(commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, 3, "F", "X", "box", 3));

    assertEquals(Arrays.asList("A", "B", "C", "D", "E", "X", "G"), stored("box").getAnnotations());
    assertDeleted("F");
    AnnotationMovedAndReplacedInSameParent event =
        singleEvent(AnnotationMovedAndReplacedInSameParent.class);
    assertEquals("box", event.parent);
    assertEquals(3, event.oldIndex);
    assertEquals(3, event.indexOffset);
    assertEquals("X", event.movedAnnotation);
    assertEquals("F", event.replacedAnnotation);
    assertArrayEquals(new String[0], event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceAnnotationInSameParentMovingDown() {
    // Example of the specification: moving X with indexOffset = -3, replacing B
    prepareList(List.of(), Arrays.asList("A", "B", "C", "D", "X", "E", "F"));

    send(commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, -3, "B", "X", "box", 4));

    assertEquals(Arrays.asList("A", "X", "C", "D", "E", "F"), stored("box").getAnnotations());
    assertDeleted("B");
    AnnotationMovedAndReplacedInSameParent event =
        singleEvent(AnnotationMovedAndReplacedInSameParent.class);
    assertEquals(4, event.oldIndex);
    assertEquals(-3, event.indexOffset);
    assertEquals("X", event.movedAnnotation);
    assertEquals("B", event.replacedAnnotation);
  }

  @Test
  public void moveAndReplaceAnnotationInSameParentWithDescendants() {
    prepare();

    // Move box.annotations[0] (a1) replacing box.annotations[1] (a2, annotated with a6)
    send(commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, 1, "a2", "a1", "box", 0));

    assertEquals(List.of("a1", "a3"), stored("box").getAnnotations());
    assertDeleted("a2", "a6");
    AnnotationMovedAndReplacedInSameParent event =
        singleEvent(AnnotationMovedAndReplacedInSameParent.class);
    assertArrayEquals(new String[] {"a6"}, event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceChildReplacingAnAncestor() {
    prepare();

    // i6 (child of i5) replaces its own parent i5 in i4.children
    send(
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i4", CHILDREN_MP, 0, "i5", CHILDREN_MP, 0, "i5", "i6"));

    assertEquals(List.of("i6"), stored("i4").getContainmentValues(CHILDREN_MP));
    assertEquals("i4", stored("i6").getParentNodeID());
    assertTrue(storedIds().contains("i6"));
    assertDeleted("i5", "a5");
    ChildMovedAndReplacedFromContainmentInOtherParent event =
        singleEvent(ChildMovedAndReplacedFromContainmentInOtherParent.class);
    assertEquals("i5", event.replacedChild);
    assertEquals(List.of("a5"), event.replacedDescendants);
  }

  @Test
  public void moveAndReplaceAnnotationReplacingAnAncestor() {
    prepare();

    // a6 (annotation of a2) replaces its own parent a2 in the annotations of box
    send(
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 1, "a2", "a6", "a2", 0));

    assertEquals(List.of("a1", "a6", "a3"), stored("box").getAnnotations());
    assertEquals("box", stored("a6").getParentNodeID());
    assertDeleted("a2");
    AnnotationMovedAndReplacedFromOtherParent event =
        singleEvent(AnnotationMovedAndReplacedFromOtherParent.class);
    assertArrayEquals(new String[0], event.replacedDescendants);
  }

  //
  // Rejected commands
  //

  @Test
  public void movingANodeIntoItsOwnSubtreeIsRejected() {
    prepare();

    // i4 would be moved under i5, which is its child
    assertRejected(
        StandardErrorCode.CIRCULAR_CONTAINMENT,
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i5", CHILDREN_MP, 0, "box", RIGHT_MP, 0, "i6", "i4"));
    // i5 would be moved under itself
    assertRejected(
        StandardErrorCode.CIRCULAR_CONTAINMENT,
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i5", CHILDREN_MP, 0, "i4", CHILDREN_MP, 0, "i6", "i5"));
    // a2 would be moved under a6, which annotates it
    assertRejected(
        StandardErrorCode.CIRCULAR_CONTAINMENT,
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "a2", 0, "a6", "a2", "box", 1));
  }

  @Test
  public void unknownNodeIsRejected() {
    prepare();

    assertRejected(
        StandardErrorCode.UNKNOWN_NODE,
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i4", CHILDREN_MP, 0, "box", LEFT_MP, 1, "i5", "unknown"));
    assertRejected(
        StandardErrorCode.UNKNOWN_NODE,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "unknown", LEFT_MP, 1, 0, "i2", "i1"));
    assertRejected(
        StandardErrorCode.UNKNOWN_NODE,
        commandId ->
            new MoveAndReplaceAnnotationInSameParent(commandId, 1, "unknown", "a1", "box", 0));
  }

  @Test
  public void unknownContainmentIsRejected() {
    prepare();

    // box has no "children" containment
    assertRejected(
        StandardErrorCode.UNKNOWN_CONTAINMENT,
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, CHILDREN_MP, 0, "i4", "i1", "box", LEFT_MP, 0));
    assertRejected(
        StandardErrorCode.UNKNOWN_CONTAINMENT,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", CHILDREN_MP, 1, 0, "i2", "i1"));
  }

  @Test
  public void parentMismatchIsRejected() {
    prepare();

    // i5 is a child of i4, not of box
    assertRejected(
        StandardErrorCode.PARENT_MISMATCH,
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, RIGHT_MP, 0, "i4", "i5", "box", LEFT_MP, 0));
    // a4 annotates i1, not box
    assertRejected(
        StandardErrorCode.PARENT_MISMATCH,
        commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, 1, "a2", "a4", "box", 0));
  }

  @Test
  public void indexOutOfRangeIsRejected() {
    prepare();

    assertRejected(
        StandardErrorCode.INDEX_OUT_OF_RANGE,
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "i4", CHILDREN_MP, 1, "box", LEFT_MP, 1, "i5", "i2"));
    assertRejected(
        StandardErrorCode.INDEX_OUT_OF_RANGE,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, -1, 3, "i2", "i3"));
    assertRejected(
        StandardErrorCode.INDEX_OUT_OF_RANGE,
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 1, "a2", "a4", "i1", 1));
  }

  @Test
  public void indexChildMismatchIsRejected() {
    prepare();

    // i2 is at index 1, not 0
    assertRejected(
        StandardErrorCode.INDEX_CHILD_MISMATCH,
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, RIGHT_MP, 0, "i4", "i2", "box", LEFT_MP, 0));
    // the child at index 0 + 2 is i3, not i2
    assertRejected(
        StandardErrorCode.INDEX_CHILD_MISMATCH,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, 2, 0, "i2", "i1"));
  }

  @Test
  public void indexAnnotationMismatchIsRejected() {
    prepare();

    // the annotation at index 0 + 2 is a3, not a2
    assertRejected(
        StandardErrorCode.INDEX_ANNOTATION_MISMATCH,
        commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, 2, "a2", "a1", "box", 0));
    // a2 is at index 1, not 2
    assertRejected(
        StandardErrorCode.INDEX_ANNOTATION_MISMATCH,
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 2, "a2", "a4", "i1", 0));
  }

  @Test
  public void oldAndNewParentIdenticalIsRejected() {
    prepare();

    assertRejected(
        StandardErrorCode.OLD_AND_NEW_PARENT_IDENTICAL,
        commandId ->
            new MoveAndReplaceChildFromContainmentInOtherParent(
                commandId, "box", RIGHT_MP, 0, "box", LEFT_MP, 0, "i4", "i1"));
    assertRejected(
        StandardErrorCode.OLD_AND_NEW_PARENT_IDENTICAL,
        commandId ->
            new MoveAndReplaceAnnotationFromOtherParent(commandId, "box", 1, "a2", "a1", "box", 0));
  }

  @Test
  public void oldAndNewContainmentIdenticalIsRejected() {
    prepare();

    assertRejected(
        StandardErrorCode.OLD_AND_NEW_CONTAINMENT_IDENTICAL,
        commandId ->
            new MoveAndReplaceChildFromOtherContainmentInSameParent(
                commandId, LEFT_MP, 2, "i3", "i1", "box", LEFT_MP, 0));
  }

  @Test
  public void invalidIndexOffsetIsRejected() {
    prepare();

    assertRejected(
        StandardErrorCode.INVALID_INDEX_OFFSET,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, 0, 0, "i1", "i1"));
    assertRejected(
        StandardErrorCode.INVALID_INDEX_OFFSET,
        commandId ->
            new MoveAndReplaceChildInSameContainmentInSameParent(
                commandId, "box", LEFT_MP, 3, 0, "i2", "i1"));
    assertRejected(
        StandardErrorCode.INVALID_INDEX_OFFSET,
        commandId -> new MoveAndReplaceAnnotationInSameParent(commandId, -1, "a1", "a1", "box", 0));
  }
}
