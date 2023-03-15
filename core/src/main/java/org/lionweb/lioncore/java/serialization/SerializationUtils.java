package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

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
}
