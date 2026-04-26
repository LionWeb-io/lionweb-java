package io.lionweb.client.diskbased;

import io.lionweb.serialization.data.SerializedClassifierInstance;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class HotPartitionManager {

    private final LinkedHashMap<String, PartitionData> hotPartitions =

        new LinkedHashMap<String, PartitionData>(16, 0.75f, true);

    private int maxHotPartitions;

    public HotPartitionManager(int maxHotPartitions, ColdPartitionManager coldPartitions) {

        this.maxHotPartitions = maxHotPartitions;

    }

    public PartitionData get(String key) {

        return hotPartitions.get(key); // updates access order

    }

    public void put(String key, PartitionData value) {

        hotPartitions.put(key, value);

        evictIfNeeded();

    }

    public void setMaxHotPartitions(int maxHotPartitions) {

        this.maxHotPartitions = maxHotPartitions;

        evictIfNeeded();

    }

    private void evictIfNeeded() {
        while (hotPartitions.size() > maxHotPartitions) {
            Map.Entry<String, PartitionData> eldest = hotPartitions.entrySet().iterator().next();
            String key = eldest.getKey();
            PartitionData value = eldest.getValue();
            hotPartitions.remove(key);
            moveToColdStorage(key, value);
        }
    }

    private void moveToColdStorage(String key, PartitionData value) {

        // your logic here
        throw new UnsupportedOperationException();
    }

    public Set<String> getNodesIDs() {
        Set<String> ids = new HashSet<>();
        for (PartitionData partitionData : hotPartitions.values()) {
            ids.addAll(partitionData.nodesByID.keySet());
        }
        return ids;
    }

    public boolean containsNodeID(String nodeId) {
        for (PartitionData partitionData : hotPartitions.values()) {
            if (partitionData.nodesByID.containsKey(nodeId)) {
                return true;
            }
        }
        return false;
    }

    public SerializedClassifierInstance getNodeByID(String nodeId) {
        throw new UnsupportedOperationException();
    }
}
