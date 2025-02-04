package io.lionweb.lioncore.kotlin.repoclient.testing

import io.lionweb.lioncore.java.LionWebVersion
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.utils.ModelComparator
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.function.Consumer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val DB_CONTAINER_PORT = 5432

@Testcontainers
abstract class AbstractRepoClientFunctionalTest(
    val modelRepoDebug: Boolean = true,
    val lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
) {
    @JvmField
    var db: PostgreSQLContainer<*>? = null

    @JvmField
    var modelRepository: GenericContainer<*>? = null

    @BeforeTest
    fun setup() {
        val network = Network.newNetwork()
        db =
            PostgreSQLContainer("postgres:16.1")
                .withNetwork(network)
                .withNetworkAliases("mypgdb")
                .withUsername("postgres")
                .withPassword("lionweb")
                .withExposedPorts(DB_CONTAINER_PORT).apply {
                    this.logConsumers =
                        listOf(
                            object : Consumer<OutputFrame> {
                                override fun accept(t: OutputFrame) {
                                    println("DB: ${t.utf8String.trimEnd()}")
                                }
                            },
                        )
                }
        db!!.start()
        val dbPort = db!!.firstMappedPort
        org.testcontainers.Testcontainers.exposeHostPorts(dbPort)
        modelRepository =
            GenericContainer(
                ImageFromDockerfile()
                    .withFileFromClasspath("Dockerfile", "repoclienttesting-lionweb-repository-Dockerfile")
                    .withFileFromClasspath("config-gen.py", "config-gen.py")
                    .withBuildArg(
                        "lionwebRepositoryCommitId",
                        BuildConfig.LIONWEB_REPOSITORY_COMMIT_ID,
                    ),
            )
                .dependsOn(db)
                .withNetwork(network)
                .withEnv("PGHOST", "mypgdb")
                .withEnv("PGPORT", DB_CONTAINER_PORT.toString())
                .withEnv("PGUSER", "postgres")
                .withEnv("PGPASSWORD", "lionweb")
                .withEnv("PGDB", "lionweb_test")
                .withEnv("LIONWEB_VERSION", lionWebVersion.versionString)
                .withExposedPorts(3005).apply {
                    this.logConsumers =
                        listOf(
                            object : Consumer<OutputFrame> {
                                override fun accept(t: OutputFrame) {
                                    if (modelRepoDebug) {
                                        println("MODEL REPO: ${t.utf8String.trimEnd()}")
                                    }
                                }
                            },
                        )
                }
        modelRepository!!.withCommand()
        modelRepository!!.start()
        System.setProperty("MODEL_REPO_PORT", modelRepoPort.toString())
    }

    @AfterTest
    fun teardown() {
        modelRepository!!.stop()
    }

    val modelRepoPort: Int
        get() = modelRepository!!.getMappedPort(3005)

    fun assertLWTreesAreEqual(
        a: Node,
        b: Node,
    ) {
        val comparison = ModelComparator().compare(a, b)
        assert(comparison.areEquivalent()) {
            "Differences between $a and $b: $comparison"
        }
    }
}
