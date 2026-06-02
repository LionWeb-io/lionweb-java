@file:JvmName("TestClientApp")

package io.lionweb.server.integration

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.lionweb.client.delta.DeltaClient
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.server.delta.WebSocketDeltaChannel
import java.net.URI

/**
 * Client process used in multi-process integration tests.
 *
 * Connects to the delta server at [serverUrl] and exposes a secondary debug WebSocket server on
 * [debugPort].  The integration test connects to that debug port to issue control commands
 * (SIGN_ON, SEND, STORE, …) and read back responses without needing stdin/stdout redirection.
 */
class TestClientAppCommand : CliktCommand(name = "test-client") {
    private val serverUrl by option(
        "--server-url",
        help = "WebSocket URL of the LionWeb delta server",
    ).default("ws://localhost:9240")

    private val clientId by option(
        "--client-id",
        help = "Identifier sent to the server during sign-on",
    ).default("client-1")

    private val debugPort by option(
        "--debug-port",
        help = "Port on which the debug WebSocket server listens for test commands",
    ).int().default(4000)

    override fun run() {
        val serialization = DeltaMessageSerialization()

        val channel = WebSocketDeltaChannel(URI(serverUrl))
        channel.connectBlocking()

        val deltaClient = DeltaClient(channel, clientId)

        val debugServer = DebugWebSocketServer(debugPort, deltaClient, channel, serialization)
        debugServer.start()

        echo("Client '$clientId' connected to $serverUrl — debug WebSocket on port $debugPort")

        // Keep the process alive until killed by the test harness
        Thread.currentThread().join()
    }
}

fun main(args: Array<String>) = TestClientAppCommand().main(args)
