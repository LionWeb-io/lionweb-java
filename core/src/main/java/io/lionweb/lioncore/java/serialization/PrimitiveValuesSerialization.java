package io.lionweb.lioncore.java.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.model.impl.DynamicEnumerationValue;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is responsible for serialization and deserializing primitive values, based on the type
 * of the primitive value.
 */
public class PrimitiveValuesSerialization {
  // We use the ID, and not the key, to classify the enumerations internally within
  // PrimitiveValuesSerialization
  // because that is unique
  private final Map<String, Enumeration> enumerationsByID = new HashMap<>();
  private boolean dynamicNodesEnabled = false;

  public void registerLanguage(Language language) {
    language.getElements().stream()
        .filter(e -> e instanceof Enumeration)
        .forEach(e -> enumerationsByID.put(e.getID(), (Enumeration) e));
  }

  public void enableDynamicNodes() {
    dynamicNodesEnabled = true;
  }

  public interface PrimitiveSerializer<V> {
    String serialize(V value);
  }

  public interface PrimitiveDeserializer<V> {
    V deserialize(String serializedValue);

    default V deserialize(String serializedValue, boolean isRequired) {
      return deserialize(serializedValue);
    }
  }

  public interface PrimitiveValueSerializerAndDeserializer<V>
      extends PrimitiveSerializer<V>, PrimitiveDeserializer<V> {}

  private final Map<String, PrimitiveDeserializer<?>> primitiveDeserializers = new HashMap<>();
  private final Map<String, PrimitiveSerializer<?>> primitiveSerializers = new HashMap<>();

  public PrimitiveValuesSerialization registerDeserializer(
      String dataTypeID, PrimitiveDeserializer<?> deserializer) {
    this.primitiveDeserializers.put(dataTypeID, deserializer);
    return this;
  }

  public PrimitiveValuesSerialization registerSerializer(
      String dataTypeID, PrimitiveSerializer<?> serializer) {
    this.primitiveSerializers.put(dataTypeID, serializer);
    return this;
  }

  public void registerLionBuiltinsPrimitiveSerializersAndDeserializers() {
    primitiveDeserializers.put(
        LionCoreBuiltins.getBoolean().getID(),
        new PrimitiveDeserializer<Boolean>() {

          @Override
          public Boolean deserialize(String serializedValue) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Boolean deserialize(String serializedValue, boolean isRequired) {
            if (!isRequired && serializedValue == null) {
              return null;
            }
            return Boolean.parseBoolean(serializedValue);
          }
        });
    primitiveDeserializers.put(LionCoreBuiltins.getString().getID(), s -> s);
    primitiveDeserializers.put(
        LionCoreBuiltins.getJSON().getID(),
        (PrimitiveDeserializer<JsonElement>)
            serializedValue -> {
              if (serializedValue == null) {
                return null;
              }
              return JsonParser.parseString(serializedValue);
            });
    primitiveDeserializers.put(
        LionCoreBuiltins.getInteger().getID(),
        (PrimitiveDeserializer<Integer>)
            serializedValue -> {
              if (serializedValue == null) {
                return null;
              }
              return Integer.parseInt(serializedValue);
            });

    primitiveSerializers.put(
        LionCoreBuiltins.getBoolean().getID(),
        (PrimitiveSerializer<Boolean>) value -> Boolean.toString(value));
    primitiveSerializers.put(
        LionCoreBuiltins.getJSON().getID(),
        (PrimitiveSerializer<JsonElement>) value -> new Gson().toJson(value));
    primitiveSerializers.put(
        LionCoreBuiltins.getString().getID(), (PrimitiveSerializer<String>) value -> value);
    primitiveSerializers.put(
        LionCoreBuiltins.getInteger().getID(),
        (PrimitiveSerializer<Integer>) value -> value.toString());
  }

  public Object deserialize(DataType dataType, String serializedValue, boolean isRequired) {
    String dataTypeID = dataType.getID();
    if (primitiveDeserializers.containsKey(dataTypeID)) {
      return primitiveDeserializers.get(dataTypeID).deserialize(serializedValue, isRequired);
    } else if (enumerationsByID.containsKey(dataTypeID)) {
      if (serializedValue == null) {
        return null;
      }
      // In this case, where we are dealing with primitive values, we want to use the literal _key_
      // (and not the ID)
      // This is at least the default behavior, but the user can register specialized
      // primitiveDeserializers,
      // if a different behavior is needed
      Optional<EnumerationLiteral> enumerationLiteral =
          enumerationsByID.get(dataTypeID).getLiterals().stream()
              .filter(l -> Objects.equals(l.getKey(), serializedValue))
              .findFirst();
      if (enumerationLiteral.isPresent()) {
        return enumerationLiteral.get();
      } else {
        throw new RuntimeException("Invalid enumeration literal value: " + serializedValue);
      }
    } else if (dynamicNodesEnabled && dataType instanceof Enumeration) {
      return new DynamicEnumerationValue((Enumeration) dataType, serializedValue);
    } else {
      throw new IllegalArgumentException(
          "Unable to deserialize primitive values of type " + dataTypeID);
    }
  }

  public String serialize(@Nonnull String primitiveTypeID, @Nullable Object value) {
    Objects.requireNonNull(primitiveTypeID, "The primitiveTypeID should not be null");
    if (primitiveSerializers.containsKey(primitiveTypeID)) {
      return ((PrimitiveSerializer<Object>) primitiveSerializers.get(primitiveTypeID))
          .serialize(value);
    } else if (isEnum(primitiveTypeID)) {
      if (value == null) {
        return null;
      }
      // In this case, where we are dealing with primitive values, we want to use the literal _key_
      // (and not the ID)
      // This is at least the default behavior, but the user can register specialized
      // primitiveSerializers,
      // if a different behavior is needed
      return ((EnumerationLiteral) value).getKey();
    } else {
      throw new IllegalArgumentException(
          "Unable to serialize primitive values of type "
              + primitiveTypeID
              + " (class: "
              + value.getClass()
              + ")");
    }
  }

  private boolean isEnum(String primitiveTypeID) {
    return enumerationsByID.containsKey(primitiveTypeID);
  }
}
