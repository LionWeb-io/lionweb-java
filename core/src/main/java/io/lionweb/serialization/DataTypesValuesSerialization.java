package io.lionweb.serialization;

import com.google.gson.*;
import io.lionweb.LionWebVersion;
import io.lionweb.language.*;
import io.lionweb.language.Enumeration;
import io.lionweb.model.StructuredDataTypeInstance;
import io.lionweb.model.impl.DynamicStructuredDataTypeInstance;
import io.lionweb.model.impl.EnumerationValue;
import io.lionweb.model.impl.EnumerationValueImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is responsible for serialization and deserializing data type instances, based on the
 * type of the data type.
 */
public class DataTypesValuesSerialization {
  // We use the ID, and not the key, to classify the enumerations internally within
  // PrimitiveValuesSerialization because that is unique. In two versions of the language we may
  // have two PrimitiveTypes with the same key, that are different.
  private final Map<String, Enumeration> enumerationsByID = new HashMap<>();
  private final Map<String, StructuredDataType> structuredDataTypesByID = new HashMap<>();
  private boolean dynamicNodesEnabled = false;

  public void registerLanguage(@Nonnull Language language) {
    Objects.requireNonNull(language, "language should not be null");
    language.getElements().stream()
        .filter(e -> e instanceof Enumeration)
        .forEach(e -> enumerationsByID.put(e.getID(), (Enumeration) e));
    language.getElements().stream()
        .filter(e -> e instanceof StructuredDataType)
        .forEach(e -> structuredDataTypesByID.put(e.getID(), (StructuredDataType) e));
  }

  public void enableDynamicNodes() {
    dynamicNodesEnabled = true;
  }

  public interface DataTypeSerializer<V> {
    @Nullable
    String serialize(@Nullable V value);
  }

  public interface DataTypeDeserializer<V> {
    @Nullable
    V deserialize(@Nullable String serializedValue);

    default @Nullable V deserialize(@Nullable String serializedValue, boolean isRequired) {
      return deserialize(serializedValue);
    }
  }

  public interface DataTypeValueSerializerAndDeserializer<V>
      extends DataTypeSerializer<V>, DataTypeDeserializer<V> {}

  /** Indexed by ID */
  private final Map<String, DataTypeDeserializer<?>> dataTypeDeserializers = new HashMap<>();

  /** Indexed by ID */
  private final Map<String, DataTypeSerializer<?>> dataTypeSerializers = new HashMap<>();

  public @Nonnull DataTypesValuesSerialization registerDeserializer(
      @Nonnull String dataTypeID, @Nonnull DataTypeDeserializer<?> deserializer) {
    Objects.requireNonNull(dataTypeID, "dataTypeID should not be null");
    Objects.requireNonNull(deserializer, "deserializer should not be null");
    this.dataTypeDeserializers.put(dataTypeID, deserializer);
    return this;
  }

  public @Nonnull DataTypesValuesSerialization registerDeserializer(
      @Nonnull DataType<?> dataType, @Nonnull DataTypeDeserializer<?> deserializer) {
    return registerDeserializer(dataType.getID(), deserializer);
  }

  public @Nonnull DataTypesValuesSerialization registerSerializer(
      @Nonnull String dataTypeID, @Nonnull DataTypeSerializer<?> serializer) {
    Objects.requireNonNull(dataTypeID, "dataTypeID should not be null");
    Objects.requireNonNull(serializer, "serializer should not be null");
    this.dataTypeSerializers.put(dataTypeID, serializer);
    return this;
  }

  public @Nonnull DataTypesValuesSerialization registerSerializer(
      @Nonnull DataType<?> dataType, @Nonnull DataTypeSerializer<?> serializer) {
    return registerSerializer(dataType.getID(), serializer);
  }

  private @Nullable StructuredDataTypeInstance deserializeSDT(
      @Nonnull String dataTypeID, @Nullable JsonObject jo) {
    Objects.requireNonNull(dataTypeID, "dataTypeID should not be null");
    if (jo == null) {
      return null;
    }
    StructuredDataType sdt = structuredDataTypesByID.get(dataTypeID);
    DynamicStructuredDataTypeInstance sdtInstance = new DynamicStructuredDataTypeInstance(sdt);
    for (Field field : sdt.getFields()) {
      if (jo.has(field.getKey())) {
        DataType<?> fieldDataType = field.getType();
        JsonElement jFieldValue = jo.get(field.getKey());
        if (jFieldValue instanceof JsonNull) {
          sdtInstance.setFieldValue(field, null);
        } else {
          if (isStructuredDataType(fieldDataType.getID())) {
            sdtInstance.setFieldValue(
                field, deserializeSDT(fieldDataType.getID(), jFieldValue.getAsJsonObject()));
          } else {
            Object fieldValue = this.deserialize(fieldDataType, jFieldValue.getAsString(), false);
            sdtInstance.setFieldValue(field, fieldValue);
          }
        }
      }
    }
    return sdtInstance;
  }

  public void registerLionBuiltinsPrimitiveSerializersAndDeserializers(
      @Nonnull LionWebVersion lionWebVersion) {
    Objects.requireNonNull(lionWebVersion, "lionWebVersion should not be null");
    dataTypeDeserializers.put(
        LionCoreBuiltins.getBoolean(lionWebVersion).getID(),
        new DataTypeDeserializer<Boolean>() {

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
    dataTypeDeserializers.put(LionCoreBuiltins.getString(lionWebVersion).getID(), s -> s);
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      dataTypeDeserializers.put(
          LionCoreBuiltins.getJSON(lionWebVersion).getID(),
          (DataTypeDeserializer<JsonElement>)
              serializedValue -> {
                if (serializedValue == null) {
                  return null;
                }
                return JsonParser.parseString(serializedValue);
              });
    }
    dataTypeDeserializers.put(
        LionCoreBuiltins.getInteger(lionWebVersion).getID(),
        (DataTypeDeserializer<Integer>)
            serializedValue -> {
              if (serializedValue == null) {
                return null;
              }
              return Integer.parseInt(serializedValue);
            });

    dataTypeSerializers.put(
        LionCoreBuiltins.getBoolean(lionWebVersion).getID(),
        (DataTypeSerializer<Boolean>) value -> Boolean.toString(value));
    if (lionWebVersion.equals(LionWebVersion.v2023_1)) {
      dataTypeSerializers.put(
          LionCoreBuiltins.getJSON(lionWebVersion).getID(),
          (DataTypeSerializer<JsonElement>) value -> new Gson().toJson(value));
    }
    dataTypeSerializers.put(
        LionCoreBuiltins.getString(lionWebVersion).getID(),
        (DataTypeSerializer<String>) value -> value);
    dataTypeSerializers.put(
        LionCoreBuiltins.getInteger(lionWebVersion).getID(),
        (DataTypeSerializer<Integer>) value -> value.toString());
  }

  public Object deserialize(
      @Nonnull DataType dataType, String serializedValue, boolean isRequired) {
    Objects.requireNonNull(dataType, "dataType should not be null");
    String dataTypeID = dataType.getID();
    if (dataTypeDeserializers.containsKey(dataTypeID)) {
      return dataTypeDeserializers.get(dataTypeID).deserialize(serializedValue, isRequired);
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
    } else if (structuredDataTypesByID.containsKey(dataTypeID) && dynamicNodesEnabled) {
      if (serializedValue == null) {
        return null;
      }
      JsonObject jo = JsonParser.parseString(serializedValue).getAsJsonObject();
      return deserializeSDT(dataTypeID, jo);
    } else {
      throw new IllegalArgumentException(
          "Unable to deserialize primitive values of type " + dataType);
    }
  }

  private @Nonnull JsonObject serializeSDT(
      @Nonnull StructuredDataTypeInstance structuredDataTypeInstance) {
    JsonObject jo = new JsonObject();
    for (Field field : structuredDataTypeInstance.getStructuredDataType().getFields()) {
      Objects.requireNonNull(field.getKey(), "Field " + field + " has a null key");
      Objects.requireNonNull(field.getType(), "Field type should be set");
      Objects.requireNonNull(field.getType().getID(), "Field type ID should be set");
      Object fieldValue = structuredDataTypeInstance.getFieldValue(field);
      if (fieldValue == null) {
        jo.add(field.getKey(), JsonNull.INSTANCE);
      } else if (isStructuredDataType(field.getType().getID())) {
        // We need to handle those differently to avoid having nested strings
        StructuredDataTypeInstance fieldValueAsSDT = (StructuredDataTypeInstance) fieldValue;
        jo.add(field.getKey(), serializeSDT(fieldValueAsSDT));
      } else {
        String serializedFieldValue = this.serialize(field.getType().getID(), fieldValue);
        jo.addProperty(field.getKey(), serializedFieldValue);
      }
    }
    return jo;
  }

  public @Nullable String serialize(@Nonnull String primitiveTypeID, @Nullable Object value) {
    Objects.requireNonNull(primitiveTypeID, "The primitiveTypeID should not be null");
    if (dataTypeSerializers.containsKey(primitiveTypeID)) {
      return ((DataTypeSerializer<Object>) dataTypeSerializers.get(primitiveTypeID))
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
        if (enumerationLiteral.getKey() == null) {
          throw new IllegalStateException("Cannot serialize enumaration literal with null key");
        }
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
        return DataTypesValuesSerialization.<Enum>serializerFor(
                (Class<Enum>) value.getClass(), enumeration)
            .serialize((Enum) value);
      } else {
        throw new IllegalStateException(
            "The primitive value with primitiveTypeID "
                + primitiveTypeID
                + " was expected to be an EnumerationValue or an instance of Enum. Instead it is: "
                + value);
      }
    } else if (isStructuredDataType(primitiveTypeID)) {
      if (value == null) {
        return null;
      }
      if (value instanceof StructuredDataTypeInstance) {
        StructuredDataTypeInstance structuredDataTypeInstance = (StructuredDataTypeInstance) value;
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(serializeSDT(structuredDataTypeInstance));
      } else {
        throw new IllegalStateException(
            "The primitive value with primitiveTypeID "
                + primitiveTypeID
                + " was expected to be a StructuredDataTypeInstance. Instead it is: "
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
  public <E extends Enum<E>> void registerEnumClass(
      @Nonnull Class<E> enumClass, @Nonnull Enumeration enumeration) {
    Objects.requireNonNull(enumClass, "enumClass should not be null");
    Objects.requireNonNull(enumeration, "enumeration should not be null");
    dataTypeSerializers.put(enumeration.getID(), serializerFor(enumClass, enumeration));
    dataTypeDeserializers.put(enumeration.getID(), deserializerFor(enumClass, enumeration));
  }

  private boolean isEnum(@Nonnull String primitiveTypeID) {
    Objects.requireNonNull(primitiveTypeID, "primitiveTypeID should not be null");
    return enumerationsByID.containsKey(primitiveTypeID);
  }

  private boolean isStructuredDataType(@Nonnull String primitiveTypeID) {
    Objects.requireNonNull(primitiveTypeID, "primitiveTypeID should not be null");
    return structuredDataTypesByID.containsKey(primitiveTypeID);
  }

  public static <E extends Enum<E>> DataTypeSerializer<E> serializerFor(
      @Nonnull Class<E> enumClass, @Nonnull Enumeration enumeration) {
    Objects.requireNonNull(enumClass, "enumClass should not be null");
    Objects.requireNonNull(enumeration, "enumeration should not be null");
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
  public static <E extends Enum<E>> DataTypeDeserializer<E> deserializerFor(
      @Nonnull Class<E> enumClass, @Nonnull Enumeration enumeration) {
    Objects.requireNonNull(enumClass, "enumClass should not be null");
    Objects.requireNonNull(enumeration, "enumeration should not be null");
    return serializedValue -> {
      if (serializedValue == null) {
        return null;
      }
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
