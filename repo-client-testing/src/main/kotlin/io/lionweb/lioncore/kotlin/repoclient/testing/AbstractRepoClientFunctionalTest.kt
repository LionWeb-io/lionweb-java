package io.lionweb.lioncore.kotlin.repoclient.testing

import java.util.function.Consumer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.junit.jupiter.Testcontainers

private const val DB_CONTAINER_PORT = 5432

@Testcontainers
abstract class AbstractRepoClientFunctionalTest {
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
                    .withFileFromClasspath("Dockerfile", "lionweb-repository-Dockerfile")
                    .withFileFromClasspath("config-gen.py", "config-gen.py")
                    .withBuildArg(
                        "lionwebRepositoryCommitId",
                        BuildConfig.LIONWEB_REPOSITORY_COMMIT_ID
                    ),
            )
                .dependsOn(db)
                .withNetwork(network)
                .withEnv("PGHOST", "mypgdb")
                .withEnv("PGPORT", DB_CONTAINER_PORT.toString())
                .withEnv("PGUSER", "postgres")
                .withEnv("PGPASSWORD", "")
                .withEnv("PGDB", "lionweb_test")
                .withExposedPorts(3005).apply {
                    this.logConsumers =
                        listOf(
                            object : Consumer<OutputFrame> {
                                override fun accept(t: OutputFrame) {
                                    println("MODEL REPO: ${t.utf8String.trimEnd()}")
                                }
                            },
                        )
                }
        modelRepository!!.withCommand()
        modelRepository!!.start()
    }

    @AfterTest
    fun teardown() {
        modelRepository!!.stop()
    }

}