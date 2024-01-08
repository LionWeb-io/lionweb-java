package io.lionweb.api.bulk.test.impl;

import io.lionweb.api.bulk.wrapper.DefaultIdMapper;
import io.lionweb.api.bulk.wrapper.IdMappingWrapper;

import java.util.Map;

public class MpsBulkLowlevel extends IdMappingWrapper {
    public MpsBulkLowlevel() {
        super(new MpsBulkLowlevelImpl(), new DefaultIdMapper(Map.of(
                "leaf-id", "3783922071925855302",
                "other-leaf-id", "3783922071925855326",
                "third-leaf-id", "3783922071925855319",
                "partition-id", "3783922071925855294",
                "other-partition-id", "3783922071925855305",
                "midnode-id", "3783922071925855296",
                "other-midnode-id", "3783922071925855307",
                "third-midnode-id", "3783922071925855313"
        )));
    }
}
