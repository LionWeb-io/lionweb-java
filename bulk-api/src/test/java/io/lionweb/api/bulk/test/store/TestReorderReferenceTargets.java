package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestReorderReferenceTargets extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "reorder-reference-targets/reorder-reference-targets-partition.json",
                "reorder-reference-targets/reorder-reference-targets-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "reorder-reference-targets/reorder-reference-targets-partition.json",
                "reorder-reference-targets/reorder-reference-targets-single-node.json"
        );
    }
}
