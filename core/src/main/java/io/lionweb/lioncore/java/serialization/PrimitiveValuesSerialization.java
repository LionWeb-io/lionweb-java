package io.lionweb.lioncore.java.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.language.EnumerationLiteral;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import java.util.*;

/**
 * This class is responsible for serialization and unserializing primitive values, based on the type
 * of the primitive value.
 */
public class PrimitiveValuesSerialization {
  // We use the ID, and not the key, to classify the enumerations internally within
  // PrimitiveValuesSerialization
  // because that is unique
  private Map<String, Enumeration> enumerationsByID = new HashMap<>();

  public void registerLanguage(Language language) {
    language.getElements().stream()
        .filter(e -> e instanceof Enumeration)
        .forEach(e -> enumerationsByID.put(e.getID(), (Enumeration) e));
  }

  public interface PrimitiveSerializer<V> {
    String serialize(V value);
  }

  public interface PrimitiveUnserializer<V> {
    V unserialize(String serializedValue);
  }

  public interface PrimitiveValueSerializerAndUnserializer<V>
      extends PrimitiveSerializer<V>, PrimitiveUnserializer<V> {}

  private Map<String, PrimitiveUnserializer<?>> primitiveUnserializers = new HashMap<>();
  private Map<String, PrimitiveSerializer<?>> primitiveSerializers = new HashMap<>();

  public PrimitiveValuesSerialization registerUnserializer(
      String dataTypeID, PrimitiveUnserializer<?> unserializer) {
    this.primitiveUnserializers.put(dataTypeID, unserializer);
    return this;
  }

  public PrimitiveValuesSerialization registerSerializer(
      String dataTypeID, PrimitiveSerializer<?> serializer) {
    this.primitiveSerializers.put(dataTypeID, serializer);
    return this;
  }

  public void registerLionBuiltinsPrimitiveSerializersAndUnserializers() {
    primitiveUnserializers.put(LionCoreBuiltins.getBoolean().getID(), Boolean::parseBoolean);
    primitiveUnserializers.put(LionCoreBuiltins.getString().getID(), s -> s);
    primitiveUnserializers.put(
        LionCoreBuiltins.getJSON().getID(),
        (PrimitiveUnserializer<JsonElement>)
            serializedValue -> {
              if (serializedValue == null) {
                return null;
              }
              return JsonParser.parseString(serializedValue);
            });
    primitiveUnserializers.put(
        LionCoreBuiltins.getInteger().getID(),
        (PrimitiveUnserializer<Integer>)
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

  public Object unserialize(String primitiveTypeID, String serializedValue) {
    if (primitiveUnserializers.containsKey(primitiveTypeID)) {
      return primitiveUnserializers.get(primitiveTypeID).unserialize(serializedValue);
    } else if (enumerationsByID.containsKey(primitiveTypeID)) {
      if (serializedValue == null) {
        return null;
      }
      // In this case, where we are dealing with primitive values, we want to use the literal _key_
      // (and not the ID)
      // This is at least the default behavior, but the user can register specialized
      // primitiveUnserializers,
      // if a different behavior is needed
      Optional<EnumerationLiteral> enumerationLiteral =
          enumerationsByID.get(primitiveTypeID).getLiterals().stream()
              .filter(l -> Objects.equals(l.getKey(), serializedValue))
              .findFirst();
      if (enumerationLiteral.isPresent()) {
        return enumerationLiteral.get();
      } else {
        throw new RuntimeException("Invalid enumeration literal value: " + serializedValue);
      }
    } else {
      throw new IllegalArgumentException(
          "Unable to unserialize primitive values of type " + primitiveTypeID);
    }
  }

  public String serialize(String primitiveTypeID, Object value) {
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
