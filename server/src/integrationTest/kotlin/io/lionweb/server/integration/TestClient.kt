package io.lionweb.server.integration

import io.lionweb.server.WebSocketDeltaChannel
import java.net.URI

/**
 * A thin wrapper around [WebSocketDeltaChannel] used by integration tests.
 *
 * Connects to the server synchronously and exposes the channel for sending commands and queries.
 * Call [close] in a finally block or use Kotlin's `use` extension.
 */
class TestClient(serverUrl: String, val clientId: String) : AutoCloseable {

    val channel: WebSocketDeltaChannel = WebSocketDeltaChannel(URI(serverUrl))

    init {
        channel.connectBlocking()
    }

    override fun close() {
        channel.closeBlocking()
    }
}
