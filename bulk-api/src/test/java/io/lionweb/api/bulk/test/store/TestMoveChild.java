package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestMoveChild extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "move-child/Disk-move-child-partition.json",
                "move-child/Disk-move-child-partition.json"
        );
    }

    @Test
    public void updateNode5() {
        test(
                "move-child/Disk-move-child-partition.json",
                "move-child/Disk-move-child-single-node.json"
        );
    }

    @Test
    public void updateNodes5And4() {
        test(
                "move-child/Disk-move-child-partition.json",
                "move-child/Disk-move-child-two-nodes.json"
        );
    }

    @Test
    public void updateNodes5And9() {
        test(
                "move-child/Disk-move-child-partition.json",
                "move-child/Disk-move-child-two-nodes-2.json"
        );
    }
}
