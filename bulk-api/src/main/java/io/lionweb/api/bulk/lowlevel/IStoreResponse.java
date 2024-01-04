package io.lionweb.api.bulk.lowlevel;

public interface IStoreResponse extends ILowlevelResponse {
    /**
     * Whether there was an issue with the nodes parameter.
     */
    boolean isValidNodes();

    /**
     * Whether there was an issue with the mode parameter.
     */
    boolean isValidMode();
}
