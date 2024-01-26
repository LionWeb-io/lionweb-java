package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestAddReference extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "add-reference/Disk_add-reference-partition.json",
                "add-reference/Disk_add-reference-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "add-reference/Disk_add-reference-partition.json",
                "add-reference/Disk_add-reference-single-node.json"
        );
    }
}
