package io.lionweb.client.testing;

import io.lionweb.LionWebVersion;
import io.lionweb.model.Node;
import io.lionweb.utils.ModelComparator;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
/**
 * Base class for functional tests that start an actual LionWeb HTTP server inside a Docker
 * container and exercise the client API against it.
 *
 * <p>Containers are cached per {@link LionWebVersion} and shared across all test classes that use
 * the same version, so Docker containers are started at most once per version per JVM run.
 */
public class AbstractClientFunctionalTest {
  private static final int DB_CONTAINER_PORT = 5432;

  private static final ConcurrentHashMap<String, Network> networkCache = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, PostgreSQLContainer<?>> dbCache =
      new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, GenericContainer<?>> serverCache =
      new ConcurrentHashMap<>();

  protected boolean serverDebug = true;
  protected LionWebVersion lionWebVersion;

  protected PostgreSQLContainer<?> db;
  protected GenericContainer<?> server;

  public AbstractClientFunctionalTest() {
    this(LionWebVersion.currentVersion, true);
  }

  public AbstractClientFunctionalTest(@NotNull LionWebVersion lionWebVersion, boolean serverDebug) {
    this.lionWebVersion = lionWebVersion;
    this.serverDebug = serverDebug;
  }

  @BeforeAll
  public void setup() {
    String versionKey = lionWebVersion.getVersionString();

    Network network =
        networkCache.computeIfAbsent(versionKey, k -> Network.newNetwork());

    db =
        dbCache.computeIfAbsent(
            versionKey,
            k -> {
              PostgreSQLContainer<?> newDb =
                  new PostgreSQLContainer<>("postgres:16.1")
                      .withNetwork(network)
                      .withNetworkAliases("mypgdb")
                      .withUsername("postgres")
                      .withPassword("lionweb")
                      .withExposedPorts(DB_CONTAINER_PORT);
              newDb.setLogConsumers(
                  Collections.singletonList(
                      frame -> System.out.println("DB: " + frame.getUtf8String().trim())));
              newDb.start();
              Testcontainers.exposeHostPorts(newDb.getFirstMappedPort());
              return newDb;
            });

    server =
        serverCache.computeIfAbsent(
            versionKey,
            k -> {
              GenericContainer<?> newServer =
                  new GenericContainer<>(
                          new ImageFromDockerfile()
                              .withFileFromClasspath(
                                  "Dockerfile", "clienttesting-lionweb-server-Dockerfile")
                              .withFileFromClasspath(
                                  "server-config.template.json", "server-config.template.json")
                              .withBuildArg(
                                  "lionwebServerCommitID", BuildConfig.LIONWEB_SERVER_COMMIT_ID))
                      .dependsOn(db)
                      .withNetwork(network)
                      .withEnv("PGHOST", "mypgdb")
                      .withEnv("PGPORT", Integer.toString(DB_CONTAINER_PORT))
                      .withEnv("PGUSER", "postgres")
                      .withEnv("PGPASSWORD", "lionweb")
                      .withEnv("PGDB", "lionweb_test")
                      .withEnv("LIONWEB_VERSION", lionWebVersion.getVersionString())
                      .withExposedPorts(3005);
              newServer.setLogConsumers(
                  Collections.singletonList(
                      frame -> {
                        if (serverDebug) {
                          System.out.println("MODEL REPO: " + frame.getUtf8String().trim());
                        }
                      }));
              newServer.withCommand();
              newServer.start();
              return newServer;
            });

    System.setProperty("MODEL_REPO_PORT", Integer.toString(getServerPort()));
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
