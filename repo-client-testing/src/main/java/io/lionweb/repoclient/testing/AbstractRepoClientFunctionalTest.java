package io.lionweb.repoclient.testing;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.utils.ModelComparator;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractRepoClientFunctionalTest {
  private static final int DB_CONTAINER_PORT = 5432;

  protected boolean modelRepoDebug = true;
  protected LionWebVersion lionWebVersion;

  protected PostgreSQLContainer<?> db;
  protected GenericContainer<?> modelRepository;

  protected static final Network network = Network.newNetwork();

  public AbstractRepoClientFunctionalTest() {
    this(LionWebVersion.currentVersion, true);
  }

  public AbstractRepoClientFunctionalTest(
      @NotNull LionWebVersion lionWebVersion, boolean modelRepoDebug) {
    this.lionWebVersion = lionWebVersion;
    this.modelRepoDebug = modelRepoDebug;
  }

  @BeforeAll
  public void setup() {
    db =
        new PostgreSQLContainer<>("postgres:16.1")
                .withReuse(true)
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
                    .withFileFromClasspath(
                        "server-config.template.json", "server-config.template.json")
                    .withBuildArg(
                        "lionwebRepositoryCommitId", BuildConfig.LIONWEB_REPOSITORY_COMMIT_ID))
                .withReuse(true)
            .dependsOn(db)
            .withNetwork(network)
            .withEnv("PGHOST", "mypgdb")
            .withEnv("PGPORT", Integer.toString(DB_CONTAINER_PORT))
            .withEnv("PGUSER", "postgres")
            .withEnv("PGPASSWORD", "lionweb")
            .withEnv("PGDB", "lionweb_test")
            .withEnv("LIONWEB_VERSION", lionWebVersion.getVersionString())
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

  @AfterAll
  public void teardownContainers() {
    if (modelRepository != null) {
      modelRepository.stop();
    }
    if (db != null) {
      db.stop();
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
