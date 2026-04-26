package io.lionweb.client.diskbased;

import io.lionweb.serialization.data.SerializedClassifierInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class PartitionData {
    final Map<String, SerializedClassifierInstance> nodesByID = new ConcurrentHashMap<>();
}
