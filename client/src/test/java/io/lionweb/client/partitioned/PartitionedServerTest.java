package io.lionweb.client.partitioned;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.library.*;
import io.lionweb.model.impl.DynamicAnnotationInstance;
import io.lionweb.serialization.AbstractSerialization;
import io.lionweb.serialization.SerializationProvider;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.data.SerializedClassifierInstance;
import io.lionweb.utils.ValidationResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Mirrors {@code InMemoryServerTest} for the partitioned implementation.
 */
public class PartitionedServerTest {

  @TempDir
  Path tempDir;

  @Test
  public void testModifyTreeAddingSubtreeWithAnnotations() throws IOException {
    try (PartitionedServer server = new PartitionedServer(tempDir)) {
      ChunkLevelPartitionedServerClient client = new ChunkLevelPartitionedServerClient(server);
      client.createRepository(
          new RepositoryConfiguration("MyRepo", LionWebVersion.v2023_1, HistorySupport.DISABLED));
      client.setRepositoryName("MyRepo");

      AbstractSerialization serialization =
          SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2023_1);

      // Create initial tree: Library with one Book
      Library library = new Library("lib1", "Main Library");
      Writer originalWriter = new Writer("writer1", "John Doe");
      Book originalBook = new Book("book1", "Original Book", originalWriter);
      originalBook.setPages(100);
      library.addBook(originalBook);

      SerializationChunk initialChunk = serialization.serializeTreeToSerializationChunk(library);
      client.createPartitionsFromChunk(initialChunk.getClassifierInstances());

      assertEquals(Collections.singletonList("lib1"), client.listPartitionsIDs());
      List<SerializedClassifierInstance> initialNodes =
          client.retrieveAsChunk(Collections.singletonList("lib1"));
      assertEquals(2, initialNodes.size());

      // Modify: add a new book with annotation
      Writer newWriter = new Writer("writer2", "Jane Smith");
      Book newBook = new Book("book2", "New Book with Annotation", newWriter);
      newBook.setPages(200);

      DynamicAnnotationInstance myAnnotation =
          new DynamicAnnotationInstance("my-ann", LibraryLanguage.PRIZE);
      newBook.addAnnotation(myAnnotation);
      library.addBook(newBook);

      SerializationChunk modifiedChunk = serialization.serializeTreeToSerializationChunk(library);
      client.storeChunk(modifiedChunk);

      List<SerializedClassifierInstance> finalNodes =
          client.retrieveAsChunk(Collections.singletonList("lib1"));
      assertEquals(4, finalNodes.size(), "Final tree should have Library + 2 Books + 1 annotation");

      Optional<SerializedClassifierInstance> book2Node =
          finalNodes.stream().filter(n -> "book2".equals(n.getID())).findFirst();
      assertTrue(book2Node.isPresent());

      List<String> annotations = book2Node.get().getAnnotations();
      assertEquals(1, annotations.size());
      assertEquals("my-ann", annotations.get(0));

      Optional<SerializedClassifierInstance> annotationNode =
          finalNodes.stream().filter(n -> "my-ann".equals(n.getID())).findFirst();
      assertTrue(annotationNode.isPresent());
      assertEquals("book2", annotationNode.get().getParentNodeID());

      ValidationResult consistencyCheck = server.checkConsistency();
      assertTrue(
          consistencyCheck.isSuccessful(),
          "Repository should be consistent. Issues: " + consistencyCheck.getIssues());

      List<SerializedClassifierInstance> book2Subtree =
          client.retrieveAsChunk(Collections.singletonList("book2"));
      assertEquals(2, book2Subtree.size());

      Set<String> subtreeIds =
          book2Subtree.stream().map(SerializedClassifierInstance::getID).collect(Collectors.toSet());
      assertTrue(subtreeIds.contains("book2"));
      assertTrue(subtreeIds.contains("my-ann"));
    }
  }
}
