package io.lionweb.api.bulk.lowlevel;

import java.util.List;

public interface IIdsResponse extends ILowlevelResponse {
    /**
     * Retrieved available (i.e. free, usable, reserved for this client) ids, if successful.
     */
    List<String> getIds();

    /**
     * Whether there was an issue with the count parameter.
     */
    boolean isValidCount();
}
