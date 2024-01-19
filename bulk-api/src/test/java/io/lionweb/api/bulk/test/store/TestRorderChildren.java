package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestRorderChildren extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "reorder-children/reorder-children-partition.json",
                "reorder-children/reorder-children-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "reorder-children/reorder-children-partition.json",
                "reorder-children/reorder-children-single-node.json"
        );
    }
}
