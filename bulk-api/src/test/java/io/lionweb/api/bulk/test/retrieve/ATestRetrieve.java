package io.lionweb.api.bulk.test.retrieve;

import io.lionweb.api.bulk.StoreMode;
import io.lionweb.api.bulk.test.ATestBulk;
import org.junit.Before;

public abstract class ATestRetrieve extends ATestBulk {
    @Before
    public void initDb() {
        getBulk().store(loadResource("retrieve-baseline.json"), StoreMode.REPLACE);
    }
}
