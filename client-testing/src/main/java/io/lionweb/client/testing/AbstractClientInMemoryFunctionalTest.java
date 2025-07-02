package io.lionweb.client.testing;

import io.lionweb.LionWebVersion;
import io.lionweb.client.api.HistorySupport;
import io.lionweb.client.api.RepositoryConfiguration;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.model.Node;
import io.lionweb.utils.ModelComparator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractClientInMemoryFunctionalTest {
  protected LionWebVersion lionWebVersion;
  private InMemoryServer server;

  public AbstractClientInMemoryFunctionalTest(@NotNull LionWebVersion lionWebVersion) {
    this.lionWebVersion = lionWebVersion;
  }

  @BeforeAll
  public void setup() {
    server = new InMemoryServer();
    server.createRepository(
        new RepositoryConfiguration("default", lionWebVersion, HistorySupport.DISABLED));
  }

  public InMemoryServer getServer() {
    return server;
  }

  public void assertLWTreesAreEqual(Node a, Node b) {
    ModelComparator.ComparisonResult comparison = new ModelComparator().compare(a, b);
    if (!comparison.areEquivalent()) {
      throw new AssertionError("Differences between " + a + " and " + b + ": " + comparison);
    }
  }
}
