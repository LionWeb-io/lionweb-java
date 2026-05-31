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
import io.lionweb.language.Language
import io.lionweb.language.LionCoreBuiltins
import io.lionweb.lioncore.LionCore
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

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

        val knownLanguages: List<Language> =
            listOf(
                LionCore.getInstance(LionWebVersion.v2024_1),
                LionCoreBuiltins.getInstance(LionWebVersion.v2024_1),
            )
        knownLanguages.forEach { deltaClient.registerLanguage(it) }

        val connected = channel.connectBlocking()
        if (!connected) {
            echo("Cannot connect to server at $serverWsUrl — is it running?", err = true)
            return
        }

        deltaClient.sendSignOnRequest()

        // Maps partition ID → {id, classifierKey, classifierLanguageKey}
        val partitions = ConcurrentHashMap<String, Map<String, String?>>()

        fun partitionEntryFrom(
            instances: Iterable<io.lionweb.serialization.data.SerializedClassifierInstance>,
        ): Pair<String, Map<String, String?>>? {
            val root = instances.firstOrNull { it.parentNodeID == null } ?: return null
            val id = root.id ?: return null
            return id to
                mapOf(
                    "id" to id,
                    "classifierKey" to root.classifier?.key,
                    "classifierLanguageKey" to root.classifier?.language,
                )
        }

        channel.registerEventReceiver { event ->
            when (event) {
                is PartitionAdded -> {
                    partitionEntryFrom(event.newPartition.getClassifierInstances())
                        ?.let { (id, entry) -> partitions.putIfAbsent(id, entry) }
                }
                is PartitionDeleted -> partitions.remove(event.deletedPartition)
                else -> {}
            }
        }

        val listResp = deltaClient.sendListAndSubscribePartitionsRequest()
        listResp.partitions
            .getClassifierInstances()
            .filter { it.parentNodeID == null }
            .forEach { root ->
                val id = root.id ?: return@forEach
                partitions.putIfAbsent(
                    id,
                    mapOf(
                        "id" to id,
                        "classifierKey" to root.classifier?.key,
                        "classifierLanguageKey" to root.classifier?.language,
                    ),
                )
            }

        DemoClientWebServer(httpPort, clientId, serverWsUrl, deltaClient, messageLog, partitions, knownLanguages).start()

        echo("Demo client '$clientId' connected to $serverWsUrl, web UI at http://localhost:$httpPort")

        Thread.currentThread().join()
    }
}
