package io.lionweb.api.bulk.lowlevel;

import java.util.List;

public interface IDeleteResponse extends ILowlevelResponse {
    /**
     * Whether there was an issue with the nodeIds parameter.
     */
    boolean isValidNodeIds();

    /**
     * Requested node ids unknown to the repository.
     */
    List<String> getUnknownNodeIds();
}
