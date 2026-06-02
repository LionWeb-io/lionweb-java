package io.lionweb.server.bulk

import io.lionweb.LionWebVersion
import io.lionweb.client.api.HistorySupport
import io.lionweb.client.api.RepositoryConfiguration
import io.lionweb.client.inmemory.InMemoryServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractHTTPBulkServerFunctionalTest {
    protected lateinit var inMemoryServer: InMemoryServer
    protected lateinit var httpBulkServer: HTTPBulkServer
    protected var serverPort: Int = 0

    @BeforeAll
    fun setup() {
        inMemoryServer = InMemoryServer()
        inMemoryServer.createRepository(
            RepositoryConfiguration("default", LionWebVersion.v2023_1, HistorySupport.DISABLED),
        )

        httpBulkServer = HTTPBulkServer(inMemoryServer, 0)
        val server = httpBulkServer.start()
        serverPort = server.address.port
    }

    @AfterAll
    fun teardown() {
        httpBulkServer.stop()
    }
}
