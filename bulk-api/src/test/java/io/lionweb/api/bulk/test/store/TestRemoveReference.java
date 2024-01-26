package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestRemoveReference extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "remove-reference/Disk-remove-reference-partition.json",
                "remove-reference/Disk-remove-reference-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "remove-reference/Disk-remove-reference-partition.json",
                "remove-reference/Disk-remove-reference-single-node.json"
        );
    }
}
