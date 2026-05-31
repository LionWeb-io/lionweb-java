package io.lionweb.server.integration

import io.lionweb.client.delta.DeltaClient
import io.lionweb.client.delta.messages.DeltaCommand
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.server.WebSocketDeltaChannel
import java.io.File
import java.net.InetSocketAddress
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

/**
 * Runs inside a [TestClientAppCommand] process and accepts a single debug WebSocket connection from the
 * integration test.  Text commands are processed synchronously and a one-line response is sent
 * back on the same connection.
 *
 * Protocol:
 *   SIGN_ON                      → OK:<participationId>
 *   SEND <deltaCommandJson>      → OK
 *   STORE <nodeId> <filePath>    → OK   (node JSON written to file)
 *   GET_PARTITION_IDS            → OK:<json-array>
 */
class DebugWebSocketServer(
    port: Int,
    private val deltaClient: DeltaClient,
    private val channel: WebSocketDeltaChannel,
    private val serialization: DeltaMessageSerialization,
) : WebSocketServer(InetSocketAddress(port)) {

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {}

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {}

    override fun onError(conn: WebSocket?, ex: Exception) {
        System.err.println("DebugWebSocketServer error: ${ex.message}")
    }

    override fun onStart() {}

    override fun onMessage(conn: WebSocket, message: String) {
        val response = try {
            dispatch(message.trim())
        } catch (e: Exception) {
            "ERROR:${e.message}"
        }
        conn.send(response)
    }

    private fun dispatch(message: String): String = when {
        message == "SIGN_ON" -> {
            deltaClient.sendSignOnRequest()
            "OK:${deltaClient.getParticipationId()}"
        }

        message.startsWith("SEND ") -> {
            val json = message.removePrefix("SEND ").trim()
            val command = serialization.deserialize(json) as? DeltaCommand
                ?: return "ERROR:could not deserialize as DeltaCommand"
            val participationId = deltaClient.getParticipationId()
                ?: return "ERROR:client is not signed on"
            channel.sendCommand(participationId) { _ -> command }
            "OK"
        }

        else -> "ERROR:unknown debug command: $message"
    }
}
