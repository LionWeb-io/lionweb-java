package io.lionweb.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.google.gson.Gson
import io.lionweb.LionWebVersion
import io.lionweb.client.delta.DeltaClient
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.client.delta.messages.events.partitions.PartitionAdded
import io.lionweb.client.delta.messages.events.partitions.PartitionDeleted
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList

class DemoClientCommand : CliktCommand(name = "demo-client") {
    private val serverWsUrl by option("--server-ws-url", help = "WebSocket URL of the delta server")
        .default("ws://localhost:9240")

    private val clientId by option("--client-id", help = "Client identifier")
        .default("demo-client")

    private val httpPort by option("--http-port", help = "HTTP port for the demo client web UI")
        .int()
        .default(9242)

    private val gson = Gson()

    override fun run() {
        val messageLog = MessageLog()
        val serialization = DeltaMessageSerialization()

        val channel = WebSocketDeltaChannel(URI(serverWsUrl))

        channel.registerEventReceiver { event ->
            messageLog.add(
                MessageLogEntry(
                    timestamp = System.currentTimeMillis(),
                    direction = "received",
                    category = "event",
                    messageKind = event.javaClass.simpleName,
                    json = gson.toJson(event),
                ),
            )
        }

        val loggingChannel = LoggingDeltaChannel(channel, messageLog, serialization)
        val deltaClient = DeltaClient(LionWebVersion.v2024_1, loggingChannel, clientId)

        channel.connectBlocking()

        deltaClient.sendSignOnRequest()

        val partitionIds = CopyOnWriteArrayList<String>()

        channel.registerEventReceiver { event ->
            when (event) {
                is PartitionAdded -> {
                    val instances = event.newPartition.getClassifierInstances()
                    val rootId = instances.firstOrNull { it.parentNodeID == null }?.id
                    if (rootId != null) {
                        partitionIds.addIfAbsent(rootId)
                    }
                }
                is PartitionDeleted -> {
                    partitionIds.remove(event.deletedPartition)
                }
                else -> {}
            }
        }

        val listResp = deltaClient.sendListAndSubscribePartitionsRequest()
        val initialIds =
            listResp.partitions
                .getClassifierInstances()
                .filter { it.parentNodeID == null }
                .map {
                    it.id
                }
        partitionIds.addAllAbsent(initialIds)

        DemoClientWebServer(httpPort, clientId, serverWsUrl, deltaClient, messageLog, partitionIds).start()

        echo("Demo client '$clientId' connected to $serverWsUrl, web UI at http://localhost:$httpPort")

        Thread.currentThread().join()
    }
}
