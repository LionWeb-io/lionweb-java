package io.lionweb.client.inmemory;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class InMemoryServerTest {

  @Test
  public void testModifyTreeAddingSubtreeWithAnnotations() throws IOException {
    InMemoryServer server = new InMemoryServer();
    ChunkLevelInMemoryServerClient client = new ChunkLevelInMemoryServerClient(server);
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

    // Store the initial tree
    SerializationChunk initialChunk = serialization.serializeTreeToSerializationChunk(library);
    client.createPartitionsFromChunk(initialChunk.getClassifierInstances());

    // Verify initial state
    assertEquals(Collections.singletonList("lib1"), client.listPartitionsIDs());
    List<SerializedClassifierInstance> initialNodes =
        client.retrieveAsChunk(Collections.singletonList("lib1"));
    assertEquals(2, initialNodes.size()); // Library + Book

    // Now modify the tree by adding a new subtree with annotations
    Writer newWriter = new Writer("writer2", "Jane Smith");
    Book newBook = new Book("book2", "New Book with Annotation", newWriter);
    newBook.setPages(200);

    // Create an annotation
    DynamicAnnotationInstance myAnnotationInstance =
        new DynamicAnnotationInstance("my-ann", LibraryLanguage.PRIZE);

    // Add the annotation to the new book
    newBook.addAnnotation(myAnnotationInstance);

    // Add the new book to the library
    library.addBook(newBook);

    // Serialize and store the modified tree
    SerializationChunk modifiedChunk = serialization.serializeTreeToSerializationChunk(library);
    client.storeChunk(modifiedChunk);

    // Verify the tree was updated correctly
    List<SerializedClassifierInstance> finalNodes =
        client.retrieveAsChunk(Collections.singletonList("lib1"));

    // Should now have: Library + 2 Books + 1 annotation
    assertEquals(4, finalNodes.size(), "Final tree should have more nodes than initial tree");

    // Verify the annotation relationship is preserved
    Optional<SerializedClassifierInstance> book2Node =
        finalNodes.stream().filter(node -> "book2".equals(node.getID())).findFirst();
    assertTrue(book2Node.isPresent(), "New book should be present in the tree");

    List<String> annotations = book2Node.get().getAnnotations();
    assertEquals(1, annotations.size(), "New book should have one annotation");
    assertEquals("my-ann", annotations.get(0), "Annotation ID should match");

    // Verify annotation parent relationship
    Optional<SerializedClassifierInstance> annotationNode =
        finalNodes.stream().filter(node -> "my-ann".equals(node.getID())).findFirst();
    assertTrue(annotationNode.isPresent(), "Annotation should be present in the tree");
    assertEquals(
        "book2", annotationNode.get().getParentNodeID(), "Annotation should have book2 as parent");

    // Verify consistency of the repository
    ValidationResult consistencyCheck = server.checkConsistency();
    assertTrue(
        consistencyCheck.isSuccessful(),
        "Repository should be consistent after modification. Issues: "
            + consistencyCheck.getIssues());

    // Verify we can retrieve the specific subtree including annotations
    List<SerializedClassifierInstance> book2Subtree =
        client.retrieveAsChunk(Collections.singletonList("book2"));

    // Should include book2, and its annotation
    assertTrue(book2Subtree.size() == 2, "Book2 subtree should include the book and annotation");

    Set<String> subtreeIds =
        book2Subtree.stream()
            .map(SerializedClassifierInstance::getID)
            .collect(java.util.stream.Collectors.toSet());

    assertTrue(subtreeIds.contains("book2"), "Subtree should contain the book");
    assertTrue(subtreeIds.contains("my-ann"), "Subtree should contain the annotation");
  }
}
