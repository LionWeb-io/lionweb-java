package io.lionweb.server.integration

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * WebSocket client used by JUnit integration tests to talk to a [DebugWebSocketServer] running
 * inside a [TestClientAppCommand] process.
 *
 * Call [sendCommand] to send a single-line command and wait synchronously for the one-line
 * response.  Commands and responses must be interleaved one-at-a-time.
 */
class DebugClient(
    uri: URI,
) : WebSocketClient(uri),
    AutoCloseable {
    private val responses = LinkedBlockingQueue<String>()

    override fun onOpen(handshake: ServerHandshake) {}

    override fun onClose(
        code: Int,
        reason: String,
        remote: Boolean,
    ) {
        // Unblock any waiting sendCommand so the test fails fast instead of timing out
        responses.offer("ERROR:connection closed (code=$code reason=$reason)")
    }

    override fun onError(ex: Exception) {
        responses.offer("ERROR:${ex.message}")
    }

    override fun onMessage(message: String) {
        responses.offer(message)
    }

    /**
     * Sends [command] to the debug server and blocks until a response arrives or [timeoutMs]
     * elapses.  Throws [IllegalStateException] on timeout.
     */
    fun sendCommand(
        command: String,
        timeoutMs: Long = 10_000,
    ): String {
        send(command)
        return responses.poll(timeoutMs, TimeUnit.MILLISECONDS)
            ?: error("Timed out (${timeoutMs}ms) waiting for response to: $command")
    }

    // AutoCloseable.close() — delegate to WebSocketClient.close() to avoid the
    // infinite loop that would occur if we called closeBlocking() here, because
    // WebSocketClient.closeBlocking() itself calls close().
    override fun close() {
        super.close()
    }
}
