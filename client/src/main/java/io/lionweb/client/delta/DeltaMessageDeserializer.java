package io.lionweb.client.delta;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.lionweb.client.delta.messages.DeltaCommand;
import io.lionweb.client.delta.messages.DeltaEvent;
import io.lionweb.client.delta.messages.DeltaQuery;
import io.lionweb.client.delta.messages.DeltaQueryResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Deserializes delta protocol JSON messages to their corresponding Java classes.
 *
 * <p>Uses the {@code messageKind} discriminator field to determine the target type. Returns null
 * for unknown or unimplemented message kinds (custom_*, Chunked*, etc.).
 *
 * <p>The map from messageKind to class is built once at class-load time by scanning the {@code
 * io.lionweb.client.delta.messages} package. For every concrete subclass of {@link DeltaCommand},
 * {@link DeltaEvent}, {@link DeltaQuery}, or {@link DeltaQueryResponse} the simple class name is
 * used as the messageKind key. The single exception is {@code NoOp}, whose messageKind in the
 * protocol is {@code "NoOpEvent"}.
 */
public class DeltaMessageDeserializer {

  private static final String PKG = "io.lionweb.client.delta.messages.";

  // Relative class names within PKG. Simple name == messageKind for all except NoOp → NoOpEvent.
  private static final String[] CLASS_NAMES = {
    "commands.partitions.AddPartition",
    "commands.partitions.DeletePartition",
    "commands.ChangeClassifier",
    "commands.CompositeCommand",
    "commands.children.AddChild",
    "commands.children.DeleteChild",
    "commands.children.MoveChildInSameContainment",
    "commands.children.MoveChildFromOtherContainment",
    "commands.children.MoveChildFromOtherContainmentInSameParent",
    "commands.children.MoveAndReplaceChildInSameContainment",
    "commands.children.MoveAndReplaceChildFromOtherContainment",
    "commands.children.MoveAndReplaceChildFromOtherContainmentInSameParent",
    "commands.children.ReplaceChild",
    "commands.properties.AddProperty",
    "commands.properties.ChangeProperty",
    "commands.properties.DeleteProperty",
    "commands.references.AddReference",
    "commands.references.ChangeReference",
    "commands.references.DeleteReference",
    "commands.annotations.AddAnnotation",
    "commands.annotations.DeleteAnnotation",
    "commands.annotations.ReplaceAnnotation",
    "commands.annotations.MoveAnnotationInSameParent",
    "commands.annotations.MoveAnnotationFromOtherParent",
    "commands.annotations.MoveAndReplaceAnnotationInSameParent",
    "commands.annotations.MoveAndReplaceAnnotationFromOtherParent",
    "events.partitions.PartitionAdded",
    "events.partitions.PartitionDeleted",
    "events.ClassifierChanged",
    "events.CompositeEvent",
    "events.ErrorEvent",
    "events.NoOp",
    "events.children.ChildAdded",
    "events.children.ChildDeleted",
    "events.children.ChildMovedInSameContainment",
    "events.children.ChildMovedFromOtherContainment",
    "events.children.ChildMovedFromOtherContainmentInSameParent",
    "events.children.ChildMovedAndReplacedInSameContainment",
    "events.children.ChildMovedAndReplacedFromOtherContainment",
    "events.children.ChildMovedAndReplacedFromOtherContainmentInSameParent",
    "events.children.ChildReplaced",
    "events.properties.PropertyAdded",
    "events.properties.PropertyChanged",
    "events.properties.PropertyDeleted",
    "events.references.ReferenceAdded",
    "events.references.ReferenceChanged",
    "events.references.ReferenceDeleted",
    "events.annotations.AnnotationAdded",
    "events.annotations.AnnotationDeleted",
    "events.annotations.AnnotationReplaced",
    "events.annotations.AnnotationMovedInSameParent",
    "events.annotations.AnnotationMovedFromOtherParent",
    "events.annotations.AnnotationMovedAndReplacedInSameParent",
    "events.annotations.AnnotationMovedAndReplacedFromOtherParent",
    "queries.partitcipations.SignOnRequest",
    "queries.partitcipations.SignOnResponse",
    "queries.partitcipations.SignOffRequest",
    "queries.partitcipations.SignOffResponse",
    "queries.partitcipations.ReconnectRequest",
    "queries.partitcipations.ReconnectResponse",
    "queries.ListPartitionsRequest",
    "queries.ListPartitionsResponse",
    "queries.GetAvailableIdsRequest",
    "queries.GetAvailableIdsResponse",
    "queries.subscriptions.SubscribeToPartitionContentsRequest",
    "queries.subscriptions.SubscribeToPartitionContentsResponse",
    "queries.subscriptions.SubscribeToChangingPartitionsRequest",
    "queries.subscriptions.SubscribeToChangingPartitionsResponse",
    "queries.subscriptions.UnsubscribeFromPartitionContentsRequest",
  };

  private static final Map<String, Class<?>> KIND_TO_CLASS = new HashMap<>();

  static {
    for (String name : CLASS_NAMES) {
      try {
        Class<?> clazz = Class.forName(PKG + name);
        KIND_TO_CLASS.put(clazz.getSimpleName(), clazz);
      } catch (ClassNotFoundException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
    // NoOp is the one class whose simple name differs from its messageKind
    KIND_TO_CLASS.put("NoOpEvent", KIND_TO_CLASS.remove("NoOp"));
  }

  private final Gson gson;

  public DeltaMessageDeserializer() {
    this.gson = new GsonBuilder().serializeNulls().create();
  }

  /**
   * Deserializes a delta message from the given JSON string.
   *
   * @return the deserialized object, or null if the messageKind is unknown/unimplemented
   */
  @Nullable
  public Object deserialize(String json) {
    return deserialize(JsonParser.parseString(json));
  }

  /**
   * Deserializes a delta message from the given Reader.
   *
   * @return the deserialized object, or null if the messageKind is unknown/unimplemented
   */
  @Nullable
  public Object deserialize(Reader reader) {
    return deserialize(JsonParser.parseReader(reader));
  }

  /**
   * Deserializes a delta message from the given File.
   *
   * @return the deserialized object, or null if the messageKind is unknown/unimplemented
   */
  @Nullable
  public Object deserialize(File file) throws IOException {
    try (FileReader reader = new FileReader(file)) {
      return deserialize(reader);
    }
  }

  /** Returns true if the given messageKind has a registered Java class. */
  public boolean isKnownKind(String messageKind) {
    return KIND_TO_CLASS.containsKey(messageKind);
  }

  /** Returns the Java class registered for a given messageKind, or null if unknown. */
  @Nullable
  public Class<?> getClassForKind(String messageKind) {
    return KIND_TO_CLASS.get(messageKind);
  }

  /** Returns true if the target class is a DeltaCommand subclass. */
  public static boolean isCommandClass(Class<?> clazz) {
    return DeltaCommand.class.isAssignableFrom(clazz);
  }

  /** Returns true if the target class is a DeltaEvent subclass. */
  public static boolean isEventClass(Class<?> clazz) {
    return DeltaEvent.class.isAssignableFrom(clazz);
  }

  /** Returns true if the target class is a DeltaQuery or DeltaQueryResponse subclass. */
  public static boolean isQueryClass(Class<?> clazz) {
    return DeltaQuery.class.isAssignableFrom(clazz)
        || DeltaQueryResponse.class.isAssignableFrom(clazz);
  }

  @Nullable
  private Object deserialize(JsonElement element) {
    if (!element.isJsonObject()) {
      throw new JsonParseException("Delta message must be a JSON object");
    }
    JsonObject obj = element.getAsJsonObject();
    JsonElement kindElement = obj.get("messageKind");
    if (kindElement == null || kindElement.isJsonNull()) {
      throw new JsonParseException("Delta message missing 'messageKind' field");
    }
    String kind = kindElement.getAsString();
    Class<?> targetClass = KIND_TO_CLASS.get(kind);
    if (targetClass == null) {
      return null;
    }
    return gson.fromJson(element, targetClass);
  }

}
