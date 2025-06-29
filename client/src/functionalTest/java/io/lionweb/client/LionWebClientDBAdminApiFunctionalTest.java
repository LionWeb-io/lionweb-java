package io.lionweb.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.testing.AbstractClientFunctionalTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class LionWebClientDBAdminApiFunctionalTest extends AbstractClientFunctionalTest {

  public LionWebClientDBAdminApiFunctionalTest() {
    super(LionWebVersion.v2024_1, false);
  }

  @Test
  public void repositoriesCRUD() throws IOException {
    LionWebClient client =
        new LionWebClient(LionWebVersion.v2024_1, "localhost", getServerPort(), "default");

    // Initially we should have one repository
    RepositoryConfiguration defaultRepository =
        new RepositoryConfiguration("default", LionWebVersion.v2024_1, HistorySupport.DISABLED);
    assertEquals(
        new HashSet<>(Collections.singletonList(defaultRepository)), client.listRepositories());

    // Create repository
    RepositoryConfiguration repo1 =
        new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.ENABLED);
    client.createRepository(repo1);

    // Check list
    assertEquals(new HashSet<>(Arrays.asList(defaultRepository, repo1)), client.listRepositories());

    // Create repositories
    RepositoryConfiguration repo2 =
        new RepositoryConfiguration("repo2", LionWebVersion.v2023_1, HistorySupport.ENABLED);
    RepositoryConfiguration repo3 =
        new RepositoryConfiguration("repo3", LionWebVersion.v2024_1, HistorySupport.DISABLED);
    client.createRepository(repo2);
    client.createRepository(repo3);

    // Check list
    assertEquals(
        new HashSet<>(Arrays.asList(defaultRepository, repo1, repo2, repo3)),
        client.listRepositories());

    // Delete repositories
    client.deleteRepository(defaultRepository.getName());
    client.deleteRepository(repo2.getName());

    // Check list
    assertEquals(new HashSet<>(Arrays.asList(repo1, repo3)), client.listRepositories());

    // Delete repository
    client.deleteRepository(repo3.getName());

    // Check list
    assertEquals(new HashSet<>(Collections.singletonList(repo1)), client.listRepositories());

    // Delete repository
    client.deleteRepository(repo1.getName());

    // Check list
    assertEquals(Collections.emptySet(), client.listRepositories());
  }
}
