package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestAddNewAnnotation extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "add-new-annotation/Disk-add-new-annotation-partition.json",
                "add-new-annotation/Disk-add-new-annotation-partition.json"
        );
    }

    @Test
    public void updateTwoNodes() {
        test(
                "add-new-annotation/Disk-add-new-annotation-partition.json",
                "add-new-annotation/Disk-add-new-annotation-two-nodes.json"
        );
    }
}
