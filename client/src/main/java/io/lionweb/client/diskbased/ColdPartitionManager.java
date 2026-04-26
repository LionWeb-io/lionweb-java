package io.lionweb.client.diskbased;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ColdPartitionManager {
    private final Path tempDir;

    private Map<String, File> partitionFilesByPartitionID = new HashMap<>();

    public ColdPartitionManager() {
        try {
            tempDir = Files.createTempDirectory("lionweb-cold-partitions-");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (Stream<Path> walk = Files.walk(tempDir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }));
    }

    public Collection<String> getNodesIDs() {
        Set<String> ids = new HashSet<>();
        for (File partitionFile : partitionFilesByPartitionID.values()) {
            throw new UnsupportedOperationException();
        }
        return ids;
    }

    public boolean containsNodeID(String nodeId) {
        throw new UnsupportedOperationException();
    }
}
