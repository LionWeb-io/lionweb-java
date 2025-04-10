package io.lionweb.repoclient;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.utils.CommonChecks;
import io.lionweb.repoclient.api.HistorySupport;
import io.lionweb.repoclient.api.RepositoryConfiguration;
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class LionWebRepoClientDBAdminApiFunctionalTest extends AbstractRepoClientFunctionalTest {

  @Test
  public void repositoriesCRUD() throws IOException {
    LionWebRepoClient client =
        new LionWebRepoClient(LionWebVersion.v2024_1, "localhost", getModelRepoPort(), "default");

    // Initially we should have one repository
    RepositoryConfiguration defaultRepository = new RepositoryConfiguration("default", LionWebVersion.v2024_1, HistorySupport.Disabled);
    assertEquals(new HashSet<>(Collections.singletonList(defaultRepository)),
            client.listRepositories());

    // Create repository
    RepositoryConfiguration repo1 = new RepositoryConfiguration("repo1", LionWebVersion.v2023_1, HistorySupport.Enabled);
    client.createRepository(repo1);

    // Check list
    assertEquals(new HashSet<>(Arrays.asList(defaultRepository, repo1)),
            client.listRepositories());

    // Create repositories
    RepositoryConfiguration repo2 = new RepositoryConfiguration("repo2", LionWebVersion.v2023_1, HistorySupport.Enabled);
    RepositoryConfiguration repo3 = new RepositoryConfiguration("repo3", LionWebVersion.v2024_1, HistorySupport.Disabled);
    client.createRepository(repo2);
    client.createRepository(repo3);

    // Check list
    assertEquals(new HashSet<>(Arrays.asList(defaultRepository, repo1, repo2, repo3)),
            client.listRepositories());

    // Delete repositories
    client.deleteRepository(defaultRepository.getName());
    client.deleteRepository(repo2.getName());

    // Check list
    assertEquals(new HashSet<>(Arrays.asList(repo1, repo3)),
            client.listRepositories());

    // Delete repository
    client.deleteRepository(repo3.getName());

    // Check list
    assertEquals(new HashSet<>(Collections.singletonList(repo1)),
            client.listRepositories());

    // Delete repository
    client.deleteRepository(repo1.getName());

    // Check list
    assertEquals(Collections.emptySet(), client.listRepositories());
  }

}
