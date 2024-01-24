package io.lionweb.api.bulk.test.store;

import io.lionweb.api.bulk.StoreMode;
import io.lionweb.api.bulk.test.ATestBulk;
import io.lionweb.json.sorted.SortedSerializedChunk;
import org.junit.Before;

import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class ATestStore extends ATestBulk {
    @Before
    public void initDb() {
        getBulk().store(loadResource("Disk_A.json"), StoreMode.REPLACE);
    }

    protected void test(String originalJsonResource, String changesResource) {
        getBulk().store(loadResource(changesResource), StoreMode.REPLACE);

        SortedSerializedChunk expected = new SortedSerializedChunk(loadResource(originalJsonResource));
        SortedSerializedChunk actual = new SortedSerializedChunk(getBulk().retrieve(List.of("ID-2"), null));
        assertEquals(expected, actual);
    }
}