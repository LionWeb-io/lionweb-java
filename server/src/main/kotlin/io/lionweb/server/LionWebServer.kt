package io.lionweb.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import io.lionweb.LionWebVersion
import io.lionweb.client.api.HistorySupport
import io.lionweb.client.api.RepositoryConfiguration
import io.lionweb.client.inmemory.InMemoryServer

class LionWebServerCommand : CliktCommand(name = "lionweb-server") {

    private val port by option("--port", help = "WebSocket port for the delta protocol")
        .int()
        .default(9240)

    private val repository by option("--repository", help = "Repository name to serve")
        .default("repo").validate { require(it.isNotBlank()) { "The repository name should not be blank" } }

    override fun run() {
        val inMemoryServer = InMemoryServer()
        inMemoryServer.createRepository(
            RepositoryConfiguration(repository, LionWebVersion.v2024_1, HistorySupport.DISABLED),
        )

        val wsServer = WebSocketDeltaServer(port, inMemoryServer, repository)
        wsServer.start()

        echo("LionWeb server listening on port $port (repository: $repository)")

        // Block until interrupted
        Thread.currentThread().join()
    }
}

fun main(args: Array<String>) = LionWebServerCommand().main(args)
