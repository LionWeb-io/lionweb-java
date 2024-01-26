package io.lionweb.api.bulk.test.store;

import org.junit.Test;

public class TestChangePropertyValue extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "change-property-value/Disk_Property_value_changed-partition.json",
                "change-property-value/Disk_Property_value_changed-partition.json"
        );
    }

    @Test
    public void updateNode3() {
        test(
                "change-property-value/Disk_Property_value_changed-partition.json",
                "change-property-value/Disk_Property_value_changed-single-node.json"
        );
    }
}
