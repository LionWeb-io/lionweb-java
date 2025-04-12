package io.lionweb.serialization.extensions;

import com.sun.org.apache.xalan.internal.lib.NodeInfo;

import javax.annotation.Nullable;

import java.io.IOException;
import java.util.List;

public interface AdditionalAPIClient {
    void bulkImport(BulkImport bulkImport, TransferFormat transferFormat, Compression compression) throws IOException;
    List<NodeInfo> getNodeTree(List<String> nodeIDs, @Nullable Integer depthLimit) throws IOException;
}
