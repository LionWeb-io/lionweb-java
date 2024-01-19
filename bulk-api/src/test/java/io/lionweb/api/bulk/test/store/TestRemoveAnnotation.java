package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestRemoveAnnotation extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "remove-annotation/Disk-remove-annotation-partition.json",
                "remove-annotation/Disk-remove-annotation-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "remove-annotation/Disk-remove-annotation-partition.json",
                "remove-annotation/Disk-remove-annotation-single-node.json"
        );
    }
}
