@file:JvmName("LionWebServer")

package io.lionweb.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
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
        .default("repo")
        .validate { require(it.isNotBlank()) { "The repository name should not be blank" } }

    private val webUi by option("--web-ui", help = "Enable the web UI (HTTP dashboard)")
        .flag(default = false)

    private val httpPort by option("--http-port", help = "HTTP port for the web UI (requires --web-ui)")
        .int()
        .default(9241)

    override fun run() {
        val inMemoryServer = InMemoryServer()
        inMemoryServer.createRepository(
            RepositoryConfiguration(repository, LionWebVersion.v2024_1, HistorySupport.DISABLED),
        )

        val messageLog = MessageLog()
        val wsServer = WebSocketDeltaServer(port, inMemoryServer, repository, messageLog)
        wsServer.start()

        echo("LionWeb server listening on port $port (repository: $repository)")

        if (webUi) {
            val uiServer = WebUIServer(httpPort, inMemoryServer, messageLog)
            uiServer.start()
            echo("Web UI available at http://localhost:$httpPort")
        }

        // Block until interrupted
        Thread.currentThread().join()
    }
}

fun main(args: Array<String>) = LionWebServerCommand().main(args)
