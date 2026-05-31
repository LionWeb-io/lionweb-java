package io.lionweb.server

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
import io.lionweb.client.inmemory.InMemoryServer
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

/**
 * Accepts WebSocket connections from delta-protocol clients and routes messages through a shared
 * broadcast channel wired to an [InMemoryServer].
 *
 * Wire format for commands includes an extra `participationId` field injected alongside the
 * standard `messageKind` fields, because [DeltaCommand] does not carry that field internally.
 */
class WebSocketDeltaServer(
    port: Int,
    inMemoryServer: InMemoryServer,
    repositoryName: String,
) : WebSocketServer(InetSocketAddress(port)) {
    private val serialization = DeltaMessageSerialization()
    val broadcastChannel = BroadcastChannel()

    init {
        inMemoryServer.monitorDeltaChannel(repositoryName, broadcastChannel)
    }

    override fun onOpen(
        conn: WebSocket,
        handshake: ClientHandshake,
    ) {
        broadcastChannel.addConnection(conn)
    }

    override fun onClose(
        conn: WebSocket,
        code: Int,
        reason: String,
        remote: Boolean,
    ) {
        broadcastChannel.removeConnection(conn)
    }

    override fun onMessage(
        conn: WebSocket,
        message: String,
    ) {
        val root = JsonParser.parseString(message).asJsonObject
        val kind = root.get("messageKind")?.asString ?: return
        val targetClass = serialization.getClassForKind(kind) ?: return

        when {
            DeltaMessageSerialization.isCommandClass(targetClass) -> {
                // Commands carry participationId as an extra wire field injected by
                // WebSocketDeltaChannel.sendCommand – strip it before deserializing.
                val participationId = root.get("participationId")?.asString ?: return
                root.remove("participationId")
                val command = serialization.deserialize(root.toString()) as? DeltaCommand ?: return
                broadcastChannel.commandReceiver?.receiveCommand(participationId, command)
            }
            DeltaMessageSerialization.isQueryClass(targetClass) -> {
                // Queries (including ReconnectRequest which has a participationId field
                // of its own) are routed purely by messageKind, not by participationId.
                val query = serialization.deserialize(message) as? DeltaQuery ?: return
                val response = broadcastChannel.queryReceiver?.receiveQuery(query)
                if (response != null) {
                    conn.send(serialization.serialize(response))
                }
            }
        }
    }

    override fun onError(
        conn: WebSocket?,
        ex: Exception,
    ) {
        System.err.println("WebSocket error on ${conn?.remoteSocketAddress}: ${ex.message}")
    }

    override fun onStart() {
        // nothing extra needed on startup
    }

    /**
     * A [DeltaChannel] that broadcasts events to all currently connected WebSocket clients.
     * Commands and queries received over WebSocket are dispatched to the registered receivers
     * (wired by [InMemoryServer.monitorDeltaChannel]).
     */
    inner class BroadcastChannel : DeltaChannel {
        private val connections = CopyOnWriteArrayList<WebSocket>()
        private val eventReceivers = CopyOnWriteArrayList<DeltaEventReceiver>()
        private val queryResponseReceivers = CopyOnWriteArrayList<DeltaQueryResponseReceiver>()
        private val nextEventId = AtomicInteger(1)

        var commandReceiver: DeltaCommandReceiver? = null
        var queryReceiver: DeltaQueryReceiver? = null

        fun addConnection(conn: WebSocket) = connections.add(conn)

        fun removeConnection(conn: WebSocket) = connections.remove(conn)

        override fun sendEvent(eventProducer: Function<Int, DeltaEvent>) {
            val event = eventProducer.apply(nextEventId.getAndIncrement())
            val json = serialization.serialize(event)
            connections.forEach { if (it.isOpen) it.send(json) }
            eventReceivers.forEach { it.receiveEvent(event) }
        }

        override fun sendQuery(queryProducer: Function<String, DeltaQuery>): DeltaQueryResponse? = null

        override fun sendCommand(
            participationId: String,
            commandProducer: Function<String, DeltaCommand>,
        ) {}

        override fun registerEventReceiver(r: DeltaEventReceiver) {
            eventReceivers.add(r)
        }

        override fun unregisterEventReceiver(r: DeltaEventReceiver) {
            eventReceivers.remove(r)
        }

        override fun registerCommandReceiver(r: DeltaCommandReceiver) {
            commandReceiver = r
        }

        override fun unregisterCommandReceiver(r: DeltaCommandReceiver) {
            if (commandReceiver === r) commandReceiver = null
        }

        override fun registerQueryReceiver(r: DeltaQueryReceiver) {
            queryReceiver = r
        }

        override fun unregisterQueryReceiver(r: DeltaQueryReceiver) {
            if (queryReceiver === r) queryReceiver = null
        }

        override fun registerQueryResponseReceiver(r: DeltaQueryResponseReceiver) {
            queryResponseReceivers.add(r)
        }

        override fun unregisterQueryResponseReceiver(r: DeltaQueryResponseReceiver) {
            queryResponseReceivers.remove(r)
        }
    }
}
