package io.lionweb.client.testing;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import io.lionweb.utils.ModelComparator;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AbstractClientFunctionalTest {
  private static final int DB_CONTAINER_PORT = 5432;

  protected boolean serverDebug = true;
  protected LionWebVersion lionWebVersion;

  protected PostgreSQLContainer<?> db;
  protected GenericContainer<?> server;

  protected static final Network network = Network.newNetwork();

  public AbstractClientFunctionalTest() {
    this(LionWebVersion.currentVersion, true);
  }

  public AbstractClientFunctionalTest(@NotNull LionWebVersion lionWebVersion, boolean serverDebug) {
    this.lionWebVersion = lionWebVersion;
    this.serverDebug = serverDebug;
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

    server =
        new GenericContainer<>(
                new ImageFromDockerfile()
                    .withFileFromClasspath("Dockerfile", "clienttesting-lionweb-server-Dockerfile")
                    .withFileFromClasspath(
                        "server-config.template.json", "server-config.template.json")
                    .withBuildArg("lionwebServerCommitID", BuildConfig.LIONWEB_SERVER_COMMIT_ID))
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

    server.setLogConsumers(
        Collections.singletonList(
            frame -> {
              if (serverDebug) {
                System.out.println("MODEL REPO: " + frame.getUtf8String().trim());
              }
            }));

    server.withCommand(); // empty command
    server.start();

    System.setProperty("MODEL_REPO_PORT", Integer.toString(getServerPort()));
  }

  @AfterAll
  public void teardownContainers() {
    if (server != null) {
      server.stop();
    }
    if (db != null) {
      db.stop();
    }
  }

  public int getServerPort() {
    return server.getMappedPort(3005);
  }

  public void assertLWTreesAreEqual(Node a, Node b) {
    ModelComparator.ComparisonResult comparison = new ModelComparator().compare(a, b);
    if (!comparison.areEquivalent()) {
      throw new AssertionError("Differences between " + a + " and " + b + ": " + comparison);
    }
  }
}
