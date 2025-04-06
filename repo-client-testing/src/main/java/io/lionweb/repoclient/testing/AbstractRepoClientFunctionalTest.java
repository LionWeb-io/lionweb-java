package io.lionweb.repoclient.testing;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.utils.ModelComparator;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class AbstractRepoClientFunctionalTest {
  private static final int DB_CONTAINER_PORT = 5432;

  protected boolean modelRepoDebug = true;

  protected PostgreSQLContainer<?> db;
  protected GenericContainer<?> modelRepository;

  public AbstractRepoClientFunctionalTest() {
    this(true);
  }

  public AbstractRepoClientFunctionalTest(boolean modelRepoDebug) {
    this.modelRepoDebug = modelRepoDebug;
  }

  @BeforeEach
  public void setup() {
    Network network = Network.newNetwork();

    db =
        new PostgreSQLContainer<>("postgres:16.1")
            .withNetwork(network)
            .withNetworkAliases("mypgdb")
            .withUsername("postgres")
            .withPassword("lionweb")
            .withExposedPorts(DB_CONTAINER_PORT);

    db.setLogConsumers(
        Collections.singletonList(
            frame -> System.out.println("DB: " + frame.getUtf8String().trim())));

    db.start();

    int dbPort = db.getFirstMappedPort();
    Testcontainers.exposeHostPorts(dbPort);

    modelRepository =
        new GenericContainer<>(
                new ImageFromDockerfile()
                    .withFileFromClasspath(
                        "Dockerfile", "repoclienttesting-lionweb-repository-Dockerfile")
                    .withFileFromClasspath("config-gen.py", "config-gen.py")
                    .withBuildArg(
                        "lionwebRepositoryCommitId", BuildConfig.LIONWEB_REPOSITORY_COMMIT_ID))
            .dependsOn(db)
            .withNetwork(network)
            .withEnv("PGHOST", "mypgdb")
            .withEnv("PGPORT", Integer.toString(DB_CONTAINER_PORT))
            .withEnv("PGUSER", "postgres")
            .withEnv("PGPASSWORD", "lionweb")
            .withEnv("PGDB", "lionweb_test")
            .withExposedPorts(3005);

    modelRepository.setLogConsumers(
        Collections.singletonList(
            frame -> {
              if (modelRepoDebug) {
                System.out.println("MODEL REPO: " + frame.getUtf8String().trim());
              }
            }));

    modelRepository.withCommand(); // empty command
    modelRepository.start();

    System.setProperty("MODEL_REPO_PORT", Integer.toString(getModelRepoPort()));
  }

  @AfterEach
  public void teardown() {
    if (modelRepository != null) {
      modelRepository.stop();
    }
  }

  public int getModelRepoPort() {
    return modelRepository.getMappedPort(3005);
  }

  public void assertLWTreesAreEqual(Node a, Node b) {
    ModelComparator.ComparisonResult comparison = new ModelComparator().compare(a, b);
    if (!comparison.areEquivalent()) {
      throw new AssertionError("Differences between " + a + " and " + b + ": " + comparison);
    }
  }
}
