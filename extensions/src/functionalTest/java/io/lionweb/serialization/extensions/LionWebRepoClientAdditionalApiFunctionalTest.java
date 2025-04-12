package io.lionweb.serialization.extensions;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.Containment;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import io.lionweb.serialization.extensions.library.Book;
import io.lionweb.serialization.extensions.library.Library;
import io.lionweb.serialization.extensions.library.LibraryLanguage;
import io.lionweb.serialization.extensions.library.Writer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class LionWebRepoClientAdditionalApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  public LionWebRepoClientAdditionalApiFunctionalTest() {
    super(LionWebVersion.v2023_1, true);
  }

  @Test
  public void bulkImportUsingJsonAndNoCompression() throws IOException {
    ExtendedLionWebRepoClient client =
        new ExtendedLionWebRepoClient(LionWebVersion.v2023_1, "localhost", getModelRepoPort(), "repo_bulkImportUsingJsonAndNoCompression");
    client.createRepository(new RepositoryConfiguration("repo_bulkImportUsingJsonAndNoCompression", LionWebVersion.v2023_1, HistorySupport.Disabled));
    client.getJsonSerialization().registerLanguage(LibraryLanguage.LANGUAGE);

    // Let's try an empty bulk import
    BulkImport bulkImport0 = new BulkImport();
    client.bulkImport(bulkImport0, TransferFormat.JSON, Compression.DISABLED);

    // The repository should still be empty
    assertEquals(Collections.emptyList(), client.listPartitionsIDs());

    // First, let's use bulk import to create a new partition, containing some data within it
    BulkImport bulkImport1 = new BulkImport();
    Library library = new Library("lib1", "The Alexandria's Library");
    Writer w1 = new Writer("w1", "Anonymous de Anonymis");
    Book b1 = new Book("b1", "The history of LionWeb, Volume I", w1);
    library.addBook(b1);
    bulkImport1.addNode(library);
    client.bulkImport(bulkImport1, TransferFormat.JSON, Compression.DISABLED);

    // Check content
    List<Node> retrievedNodes1 = client.retrieve(Arrays.asList("lib1"));
    Node retrievedLibrary1 = retrievedNodes1.stream().filter(n -> n.getID().equals("lib1")).findFirst().get();
    assertEquals(library, retrievedLibrary1);

    // Then, let's attach more data to an existing partition
    Book b2 = new Book("b2", "The history of LionWeb, Volume II", w1);
    Book b3 = new Book("b3", "The history of LionWeb, Volume III", w1);
    BulkImport bulkImport2 = new BulkImport();
    Containment libraryBooks = LibraryLanguage.LIBRARY.getContainmentByName("books");
    bulkImport2.addNode(b2);
    bulkImport1.addAttachPoint(new BulkImport.AttachPoint("lib1", libraryBooks, "b2"));
    bulkImport2.addNode(b3);
    bulkImport1.addAttachPoint(new BulkImport.AttachPoint("lib1", libraryBooks, "b3"));
    client.bulkImport(bulkImport2, TransferFormat.JSON, Compression.DISABLED);

    // Check content
    library.addBook(b2);
    library.addBook(b3);

    List<Node> retrievedNodes2 = client.retrieve(Arrays.asList("lib1"));
    Node retrievedLibrary2 = retrievedNodes2.stream().filter(n -> n.getID().equals("lib1")).findFirst().get();
    assertEquals(library, retrievedLibrary2);
  }
}
