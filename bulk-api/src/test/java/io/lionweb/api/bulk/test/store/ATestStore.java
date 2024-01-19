package io.lionweb.api.bulk.test.store;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.lionweb.api.bulk.StoreMode;
import io.lionweb.api.bulk.test.ATestBulk;
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.json.sorted.SortedSerializedChunk;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class ATestStore extends ATestBulk {
    @Before
    public void initDb() {
        getBulk().store(loadResource("Disk_A.json"), StoreMode.REPLACE);
    }

    protected SerializedChunk loadResource(String resourcePath) {
        SerializedChunk chunk;
        Class<? extends ATestStore> aClass = this.getClass();
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

    protected void test(String originalJsonResource, String changesResource) {
        getBulk().store(loadResource(changesResource), StoreMode.REPLACE);

        SortedSerializedChunk expected = new SortedSerializedChunk(loadResource(originalJsonResource));
        SortedSerializedChunk actual = new SortedSerializedChunk(getBulk().retrieve(List.of("ID-2"), null));
        assertEquals(expected, actual);
    }
}
