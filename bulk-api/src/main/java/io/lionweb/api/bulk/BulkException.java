package io.lionweb.api.bulk;

public class BulkException extends RuntimeException {
    public BulkException() {
    }

    public BulkException(String message) {
        super(message);
    }

    public BulkException(String message, Throwable cause) {
        super(message, cause);
    }

    public BulkException(Throwable cause) {
        super(cause);
    }
}
