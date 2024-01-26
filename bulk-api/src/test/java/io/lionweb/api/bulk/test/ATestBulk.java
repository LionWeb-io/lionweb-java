package io.lionweb.api.bulk.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.lionweb.api.bulk.IBulk;
import io.lionweb.api.bulk.lowlevel.IBulkLowlevel;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class ATestBulk {

    protected IBulk getBulk() {
        return BulkApiProvider.getInstance().getBulk();
    }

    protected IBulkLowlevel getBulkLowlevel() {
        return BulkApiProvider.getInstance().getBulkLowlevel();
    }

    protected SerializedChunk loadResource(String resourcePath) {
        SerializedChunk chunk;
        Class<?> aClass = this.getClass();
        try (InputStream is = aClass.getResourceAsStream("/" + resourcePath)) {
            LowLevelJsonSerialization jsonSerialization = new LowLevelJsonSerialization();
            InputStreamReader reader = new InputStreamReader(is);
            JsonReader json = new JsonReader(reader);
            json.setLenient(true);
            JsonElement jsonElement = JsonParser.parseReader(json);
            chunk = jsonSerialization.deserializeSerializationBlock(jsonElement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return chunk;
    }
}
