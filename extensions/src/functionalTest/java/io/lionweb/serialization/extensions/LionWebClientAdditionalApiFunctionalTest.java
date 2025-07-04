package io.lionweb.serialization.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.ExtendedLionWebClient;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.testing.AbstractClientFunctionalTest;
import io.lionweb.language.Containment;
import io.lionweb.model.Node;
import io.lionweb.serialization.extensions.library.Book;
import io.lionweb.serialization.extensions.library.Library;
import io.lionweb.serialization.extensions.library.LibraryLanguage;
import io.lionweb.serialization.extensions.library.Writer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebClientAdditionalApiFunctionalTest extends AbstractClientFunctionalTest {

  public LionWebClientAdditionalApiFunctionalTest() {
    super(LionWebVersion.v2023_1, true);
  }

  @Test
  public void bulkImportUsingJsonAndNoCompression() throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingJsonAndNoCompression", TransferFormat.JSON, Compression.DISABLED);
  }

  @Test
  public void bulkImportUsingJsonAndCompression() throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingJsonAndCompression", TransferFormat.JSON, Compression.ENABLED);
  }

  @Test
  public void bulkImportUsingProtobufAndNoCompression() throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingProtobufAndNoCompression",
        TransferFormat.PROTOBUF,
        Compression.DISABLED);
  }

  @Test
  public void bulkImportUsingProtobufAndCompression() throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingProtobufAndCompression", TransferFormat.PROTOBUF, Compression.ENABLED);
  }

  @Test
  public void bulkImportUsingFlatbuffersAndNoCompression()
      throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingFlatbuffersAndNoCompression",
        TransferFormat.FLATBUFFERS,
        Compression.DISABLED);
  }

  @Test
  public void bulkImportUsingFlatbuffersAndCompression() throws IOException, InterruptedException {
    bulkImportTestingRoutine(
        "repo_bulkImportUsingFlatbuffersAndCompression",
        TransferFormat.FLATBUFFERS,
        Compression.ENABLED);
  }

  @Test
  public void nodeTree() throws IOException {
    String repositoryName = "repo_nodeTree";
    ExtendedLionWebClient client =
        new ExtendedLionWebClient(
            LionWebVersion.v2023_1, "localhost", getServerPort(), repositoryName);
    client.createRepository(
        new RepositoryConfiguration(
            repositoryName, LionWebVersion.v2023_1, HistorySupport.DISABLED));

    assertEquals(Collections.emptyList(), client.getNodeTree("lib1"));

    Library library = new Library("lib1", "The Alexandria's Library");
    client.createPartition(library);
    assertEquals(
        Collections.singletonList(new NodeInfo("lib1", null, 0)), client.getNodeTree("lib1"));

    Writer w1 = new Writer("w1", "Anonymous de Anonymis");
    Book b1 = new Book("b1", "The history of LionWeb, Volume I", w1);
    b1.setPages(100);
    library.addBook(b1);
    client.store(library);
    assertEquals(
        Arrays.asList(new NodeInfo("lib1", null, 0), new NodeInfo("b1", "lib1", 1)),
        client.getNodeTree("lib1"));

    Book b2 = new Book("b2", "The history of LionWeb, Volume II", w1);
    Book b3 = new Book("b3", "The history of LionWeb, Volume III", w1);
    library.addBook(b2);
    library.addBook(b3);
    client.store(library);
    assertEquals(
        Arrays.asList(
            new NodeInfo("lib1", null, 0),
            new NodeInfo("b1", "lib1", 1),
            new NodeInfo("b2", "lib1", 1),
            new NodeInfo("b3", "lib1", 1)),
        client.getNodeTree("lib1"));
  }

  private void bulkImportTestingRoutine(
      String repositoryName, TransferFormat transferFormat, Compression compression)
      throws IOException, InterruptedException {
    ExtendedLionWebClient client =
        new ExtendedLionWebClient(
            LionWebVersion.v2023_1, "localhost", getServerPort(), repositoryName);
    client.createRepository(
        new RepositoryConfiguration(
            repositoryName, LionWebVersion.v2023_1, HistorySupport.DISABLED));
    client.getJsonSerialization().registerLanguage(LibraryLanguage.LANGUAGE);

    // Let's try an empty bulk import
    BulkImport bulkImport0 = new BulkImport();
    client.bulkImport(bulkImport0, transferFormat, compression);

    // The repository should still be empty
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());

    // First, let's use bulk import to create a new partition, containing some data within it
    BulkImport bulkImport1 = new BulkImport();
    Library library = new Library("lib1", "The Alexandria's Library");
    Writer w1 = new Writer("w1", "Anonymous de Anonymis");
    Book b1 = new Book("b1", "The history of LionWeb, Volume I", w1);
    b1.setPages(100);
    library.addBook(b1);
    bulkImport1.addNode(library);
    client.bulkImport(bulkImport1, transferFormat, compression);

    // A delay seems necessary only when running on GitHub Actions and not locally.
    // We may want to check if the server can wait to provide an answer when the operation has been
    // finalized,
    // but that is not something we can change in this project
    Thread.sleep(150);

    // Verify the library has been recognized as partition, given it had no attach point
    assertEquals(new HashSet(Arrays.asList("lib1")), new HashSet(client.listPartitionsIDs()));

    // Check content
    List<Node> retrievedNodes1 = client.retrieve(Arrays.asList("lib1"));
    Node retrievedLibrary1 =
        retrievedNodes1.stream().filter(n -> n.getID().equals("lib1")).findFirst().get();
    assertEquals(library, retrievedLibrary1);

    // Then, let's attach more data to an existing partition
    Book b2 = new Book("b2", "The history of LionWeb, Volume II", w1);
    Book b3 = new Book("b3", "The history of LionWeb, Volume III", w1);
    BulkImport bulkImport2 = new BulkImport();
    Containment libraryBooks = LibraryLanguage.LIBRARY.getContainmentByName("books");
    bulkImport2.addNode(b2);
    bulkImport2.addAttachPoint(new BulkImport.AttachPoint("lib1", libraryBooks, "b2"));
    bulkImport2.addNode(b3);
    bulkImport2.addAttachPoint(new BulkImport.AttachPoint("lib1", libraryBooks, "b3"));
    client.bulkImport(bulkImport2, transferFormat, compression);
    Thread.sleep(150);

    // Verify the books has NOT been recognized as partitions, given they had attach points
    assertEquals(new HashSet(Arrays.asList("lib1")), new HashSet(client.listPartitionsIDs()));

    // Check content
    library.addBook(b2);
    library.addBook(b3);

    List<Node> retrievedNodes2 = client.retrieve(Arrays.asList("lib1"));
    Node retrievedLibrary2 =
        retrievedNodes2.stream().filter(n -> n.getID().equals("lib1")).findFirst().get();
    assertEquals(library, retrievedLibrary2);
  }
}
