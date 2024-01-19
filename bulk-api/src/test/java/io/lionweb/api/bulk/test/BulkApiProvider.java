package io.lionweb.api.bulk.test;

import io.lionweb.api.bulk.IBulk;
import io.lionweb.api.bulk.lowlevel.IBulkLowlevel;
import io.lionweb.api.bulk.test.impl.LionwebRepositoryBulkLowlevel;
import io.lionweb.api.bulk.test.impl.MpsBulkLowlevel;
import io.lionweb.api.bulk.wrapper.BulkLowlevelWrapper;

public class BulkApiProvider {
    public static BulkApiProvider getInstance() {
        return new BulkApiProvider();
    }

    public IBulk getBulk() {
        return new BulkLowlevelWrapper(getBulkLowlevel());
    }

    public IBulkLowlevel getBulkLowlevel() {
//        return new MpsBulkLowlevel();
        return new LionwebRepositoryBulkLowlevel();
    }
}
