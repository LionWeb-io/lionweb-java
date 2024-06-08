package io.lionweb.lioncore.java.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.model.impl.EnumerationValue;
import io.lionweb.lioncore.java.model.impl.EnumerationValueImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is responsible for serialization and deserializing primitive values, based on the type
 * of the primitive value.
 */
public class PrimitiveValuesSerialization {
  // We use the ID, and not the key, to classify the enumerations internally within
  // PrimitiveValuesSerialization because that is unique. In two versions of the language we may
  // have two PrimitiveTypes with the same key, that are different.
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

  /** Indexed by ID */
  private final Map<String, PrimitiveDeserializer<?>> primitiveDeserializers = new HashMap<>();

  /** Indexed by ID */
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
    } else if (enumerationsByID.containsKey(dataTypeID) && dynamicNodesEnabled) {
      if (serializedValue == null) {
        return null;
      }
      // While we map types by IDs, we map literals by key.
      // This is at least the default behavior, but the user can register specialized
      // primitiveDeserializers, if a different behavior is needed
      Optional<EnumerationLiteral> enumerationLiteral =
          enumerationsByID.get(dataTypeID).getLiterals().stream()
              .filter(l -> Objects.equals(l.getKey(), serializedValue))
              .findFirst();
      if (enumerationLiteral.isPresent()) {
        return new EnumerationValueImpl(enumerationLiteral.get());
      } else {
        throw new RuntimeException("Invalid enumeration literal value: " + serializedValue);
      }
    } else {
      throw new IllegalArgumentException(
          "Unable to deserialize primitive values of type " + dataType);
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
      // (and not the ID).
      // This is at least the default behavior, but the user can register specialized
      // primitiveSerializers, if a different behavior is needed
      if (value instanceof EnumerationValue) {
        EnumerationLiteral enumerationLiteral = ((EnumerationValue) value).getEnumerationLiteral();
        return enumerationLiteral.getKey();
      } else if (value instanceof Enum<?>) {
        Enumeration enumeration = enumerationsByID.get(primitiveTypeID);
        if (enumeration == null) {
          throw new RuntimeException(
              "Cannot find enumeration with id "
                  + primitiveTypeID
                  + " while serializing primitive value "
                  + value);
        }
        return PrimitiveValuesSerialization.<Enum>serializerFor(
                (Class<Enum>) value.getClass(), enumeration)
            .serialize((Enum) value);
      } else {
        throw new IllegalStateException(
            "The primitive value with primitiveTypeID "
                + primitiveTypeID
                + " was expected to be an EnumerationValue or an instance of Enum. Instead it is: "
                + value);
      }
    } else {
      throw new IllegalArgumentException(
          "Unable to serialize primitive values of type "
              + primitiveTypeID
              + " (class: "
              + value.getClass()
              + ")");
    }
  }

  /** Please note that this will require support for reflection. */
  public <E extends Enum<E>> void registerEnumClass(Class<E> enumClass, Enumeration enumeration) {
    primitiveSerializers.put(enumeration.getID(), serializerFor(enumClass, enumeration));
    primitiveDeserializers.put(enumeration.getID(), deserializerFor(enumClass, enumeration));
  }

  private boolean isEnum(String primitiveTypeID) {
    return enumerationsByID.containsKey(primitiveTypeID);
  }

  public static <E extends Enum<E>> PrimitiveSerializer<E> serializerFor(
      Class<E> enumClass, Enumeration enumeration) {
    return value -> {
      String enumerationLiteralName = value.name();
      Optional<EnumerationLiteral> enumerationLiteral =
          enumeration.getLiterals().stream()
              .filter(l -> l.getName().equals(enumerationLiteralName))
              .findFirst();
      if (!enumerationLiteral.isPresent()) {
        throw new IllegalArgumentException(
            "Cannot serialize enum instance with name "
                + enumerationLiteralName
                + " as we cannot find an enumeration literal with the same name when considering enumeration "
                + enumeration
                + ". Literals available are: "
                + enumeration.getLiterals().stream()
                    .map(l -> l.getName())
                    .collect(Collectors.joining(", ")));
      }
      return enumerationLiteral.get().getKey();
    };
  }

  /** Please note that this will require support for reflection. */
  public static <E extends Enum<E>> PrimitiveDeserializer<E> deserializerFor(
      Class<E> enumClass, Enumeration enumeration) {
    return serializedValue -> {
      Optional<EnumerationLiteral> matchingEnumerationLiteral =
          enumeration.getLiterals().stream()
              .filter(l -> l.getKey().equals(serializedValue))
              .findFirst();
      if (!matchingEnumerationLiteral.isPresent()) {
        throw new IllegalArgumentException(
            "Cannot deserialize value "
                + serializedValue
                + " as we cannot find an enumeration literal with the same key when considering enumeration "
                + enumeration
                + ". Literals available are: "
                + enumeration.getLiterals().stream()
                    .map(l -> l.getKey())
                    .collect(Collectors.joining(", ")));
      }
      Optional<Method> valueOf =
          Arrays.stream(enumClass.getDeclaredMethods())
              .filter(m -> m.getName().equals("valueOf") && m.getParameterCount() == 1)
              .findFirst();
      if (!valueOf.isPresent()) {
        throw new IllegalStateException(
            "Cannot find method valueOf(String) for class " + enumClass);
      }
      String literalName = matchingEnumerationLiteral.get().getName();
      try {
        E instance = (E) valueOf.get().invoke(null, literalName);
        return instance;
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(
            "Issue while invoking valueOf on class " + enumClass + " with value " + literalName, e);
      }
    };
  }
}
