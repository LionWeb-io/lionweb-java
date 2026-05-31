package io.lionweb.server

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.lionweb.client.delta.DeltaChannel
import io.lionweb.client.delta.DeltaCommandReceiver
import io.lionweb.client.delta.DeltaEventReceiver
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.client.delta.DeltaQueryReceiver
import io.lionweb.client.delta.DeltaQueryResponseReceiver
import io.lionweb.client.delta.messages.DeltaCommand
import io.lionweb.client.delta.messages.DeltaEvent
import io.lionweb.client.delta.messages.DeltaQuery
import io.lionweb.client.delta.messages.DeltaQueryResponse
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

/**
 * A [DeltaChannel] backed by a WebSocket connection to a [WebSocketDeltaServer].
 *
 * - Queries are sent as plain serialized [DeltaQuery] JSON; the response is matched by `queryId`.
 * - Commands are sent with an extra `participationId` field merged into the JSON object.
 * - Incoming messages are dispatched to registered [DeltaEventReceiver]s or to a waiting query
 *   future.
 */
class WebSocketDeltaChannel(serverUri: URI) : WebSocketClient(serverUri), DeltaChannel {

    private val serialization = DeltaMessageSerialization()
    private val gson = Gson()

    private val eventReceivers = CopyOnWriteArrayList<DeltaEventReceiver>()
    private val queryResponseReceivers = CopyOnWriteArrayList<DeltaQueryResponseReceiver>()
    private val pendingQueries = ConcurrentHashMap<String, CompletableFuture<DeltaQueryResponse>>()
    private val nextQueryId = AtomicInteger(1)
    private val nextCommandId = AtomicInteger(1)

    override fun onOpen(handshake: ServerHandshake) {
        // connection ready
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        // cancel any pending query futures
        pendingQueries.values.forEach { it.cancel(true) }
        pendingQueries.clear()
    }

    override fun onError(ex: Exception) {
        throw RuntimeException("WebSocketDeltaChannel error: ${ex.message}")
    }

    override fun onMessage(message: String) {
        val msg = serialization.deserialize(message) ?: return
        when (msg) {
            is DeltaQueryResponse -> {
                // Notify receivers BEFORE completing the future so that by the time
                // sendQuery() unblocks the client state (e.g. participationId) is set.
                // Use try/finally so the future is always completed even if a receiver
                // throws (e.g. DeltaClient throws UnsupportedOperationException for
                // ErrorResponse), which would otherwise cause sendQuery() to hang.
                try {
                    queryResponseReceivers.forEach { it.receiveQueryResponse(msg) }
                } catch (_: Exception) {
                } finally {
                    pendingQueries.remove(msg.queryId)?.complete(msg)
                }
            }
            is DeltaEvent -> {
                eventReceivers.forEach { it.receiveEvent(msg) }
            }
        }
    }

    // ── DeltaChannel ────────────────────────────────────────────────────────

    override fun sendQuery(queryProducer: Function<String, DeltaQuery>): DeltaQueryResponse? {
        val queryId = "q-${nextQueryId.getAndIncrement()}"
        val query = queryProducer.apply(queryId)
        val future = CompletableFuture<DeltaQueryResponse>()
        pendingQueries[queryId] = future
        send(serialization.serialize(query))
        return future.get(30, TimeUnit.SECONDS)
    }

    override fun sendCommand(participationId: String, commandProducer: Function<String, DeltaCommand>) {
        val commandId = "cmd-${nextCommandId.getAndIncrement()}"
        val command = commandProducer.apply(commandId)
        // Merge participationId into the serialized JSON so the server can extract it
        val obj = JsonParser.parseString(serialization.serialize(command)).asJsonObject
        obj.addProperty("participationId", participationId)
        send(gson.toJson(obj))
    }

    override fun sendEvent(eventProducer: Function<Int, DeltaEvent>) {
        // clients do not send events
    }

    override fun registerEventReceiver(r: DeltaEventReceiver) { eventReceivers.add(r) }

    override fun unregisterEventReceiver(r: DeltaEventReceiver) { eventReceivers.remove(r) }

    override fun registerCommandReceiver(r: DeltaCommandReceiver) {}

    override fun unregisterCommandReceiver(r: DeltaCommandReceiver) {}

    override fun registerQueryReceiver(r: DeltaQueryReceiver) {}

    override fun unregisterQueryReceiver(r: DeltaQueryReceiver) {}

    override fun registerQueryResponseReceiver(r: DeltaQueryResponseReceiver) { queryResponseReceivers.add(r) }

    override fun unregisterQueryResponseReceiver(r: DeltaQueryResponseReceiver) { queryResponseReceivers.remove(r) }
}
