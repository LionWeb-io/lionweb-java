package io.lionweb.api.bulk.test.store;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Leaves residual data in lionweb-repository, influencing other tests")
public class TestAddNewProperty extends ATestStore {
    @Test
    public void updateFullPartition() {
        test(
                "add-new-property-with-value/Disk-Property-add-property-partition.json",
                "add-new-property-with-value/Disk-Property-add-property-partition.json"
        );
    }

    @Test
    public void updateSingleNode() {
        test(
                "add-new-property-with-value/Disk-Property-add-property-partition.json",
                "add-new-property-with-value/Disk-Property-add-property-single-node.json"
        );
    }
}
