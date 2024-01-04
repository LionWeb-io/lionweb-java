package io.lionweb.api.bulk.lowlevel;

public interface ILowlevelResponse {
    boolean isOk();

    //@Nonnull
    String getErrorMessage();
}
