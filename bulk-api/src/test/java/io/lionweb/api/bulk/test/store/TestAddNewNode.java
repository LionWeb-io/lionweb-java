package io.lionweb.api.bulk.test.store;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Crashes Lionweb-repository on second run")
public class TestAddNewNode extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "add-new-nodes/Disk-add-new-nodes-partition.json",
                "add-new-nodes/Disk-add-new-nodes-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "add-new-nodes/Disk-add-new-nodes-partition.json",
                "add-new-nodes/Disk-add-new-nodes-single-node.json"
        );
    }
}
