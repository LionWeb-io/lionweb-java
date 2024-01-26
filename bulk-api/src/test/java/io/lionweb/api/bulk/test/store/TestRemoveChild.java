package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestRemoveChild extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "remove-child/Disk-remove-child-partition.json",
                "remove-child/Disk-remove-child-partition.json"
        );
    }

    @Test
    public void updateNode3() {
        test(
                "remove-child/Disk-remove-child-partition.json",
                "remove-child/Disk-remove-child-single-node.json"
        );
    }
}
