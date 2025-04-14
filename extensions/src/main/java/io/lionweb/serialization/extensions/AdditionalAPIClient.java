package io.lionweb.serialization.extensions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AdditionalAPIClient {
  void bulkImport(BulkImport bulkImport, TransferFormat transferFormat, Compression compression)
      throws IOException;

  List<NodeInfo> getNodeTree(List<String> nodeIDs, @Nullable Integer depthLimit) throws IOException;

  default List<NodeInfo> getNodeTree(List<String> nodeIDs) throws IOException {
    return getNodeTree(nodeIDs, null);
  }

  default List<NodeInfo> getNodeTree(@Nonnull String nodeID) throws IOException {
    return getNodeTree(Collections.singletonList(nodeID));
  }
}
