package io.lionweb.api.bulk.test;

import io.lionweb.api.bulk.IBulk;
import io.lionweb.api.bulk.lowlevel.IBulkLowlevel;

public abstract class ATestBulk {

  protected IBulk getBulk() {
    return BulkApiProvider.getInstance().getBulk();
  }

  protected IBulkLowlevel getBulkLowlevel() {
    return BulkApiProvider.getInstance().getBulkLowlevel();
  }
}
