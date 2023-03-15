package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.lionweb.lioncore.java.serialization.data.SerializedReferenceValue;

import javax.annotation.Nullable;
import java.util.List;

class SerializationUtils {

    static String getAsStringOrNull(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else {
            return element.getAsString();
        }
    }

    @Nullable
    static String tryToGetStringProperty(JsonObject jsonObject, String propertyName) {
        if (!jsonObject.has(propertyName)) {
            return null;
        }
        JsonElement value = jsonObject.get(propertyName);
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            return value.getAsJsonPrimitive().getAsString();
        } else {
            return null;
        }
    }

    static JsonArray toJsonArray(List<String> stringList) {
        JsonArray jsonArray = new JsonArray();
        stringList.forEach(s -> jsonArray.add(s));
        return jsonArray;
    }

    static JsonArray toJsonArrayOfReferenceValues(List<SerializedReferenceValue.Entry> entries) {
        JsonArray jsonArray = new JsonArray();
        entries.forEach(e -> {
            JsonObject entryJson = new JsonObject();
            entryJson.addProperty("resolveInfo", e.getResolveInfo());
            entryJson.addProperty("reference", e.getReference());
            jsonArray.add(entryJson);
        });
        return jsonArray;
    }
}
