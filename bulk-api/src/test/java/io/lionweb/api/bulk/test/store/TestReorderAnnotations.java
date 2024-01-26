package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestReorderAnnotations extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "reorder-annotations/reorder-annotations-partition.json",
                "reorder-annotations/reorder-annotations-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "reorder-annotations/reorder-annotations-partition.json",
                "reorder-annotations/reorder-annotations-single-node.json"
        );
    }
}
