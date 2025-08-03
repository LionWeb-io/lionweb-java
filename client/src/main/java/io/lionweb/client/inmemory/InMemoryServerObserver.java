package io.lionweb.client.inmemory;

public interface InMemoryServerObserver {
    void partitionAdded(String partitionId);
    void partitionRemoved(String partitionId);
    void nodeDeleted(String node);
}
