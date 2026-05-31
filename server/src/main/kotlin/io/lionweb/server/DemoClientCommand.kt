package io.lionweb.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.google.gson.Gson
import io.lionweb.LionWebVersion
import io.lionweb.client.delta.DeltaClient
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.client.delta.messages.events.children.ChildAdded
import io.lionweb.client.delta.messages.events.children.ChildDeleted
import io.lionweb.client.delta.messages.events.partitions.PartitionAdded
import io.lionweb.client.delta.messages.events.partitions.PartitionDeleted
import io.lionweb.client.delta.messages.events.properties.PropertyAdded
import io.lionweb.client.delta.messages.events.properties.PropertyChanged
import io.lionweb.client.delta.messages.events.properties.PropertyDeleted
import io.lionweb.language.Language
import io.lionweb.language.LionCoreBuiltins
import io.lionweb.lioncore.LionCore
import io.lionweb.serialization.data.SerializedClassifierInstance
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

        val maxAttempts = 10
        var connected = false
        for (attempt in 1..maxAttempts) {
            connected = if (attempt == 1) channel.connectBlocking() else channel.reconnectBlocking()
            if (connected) break
            if (attempt < maxAttempts) {
                echo("Connection attempt $attempt failed, retrying in 1s…")
                Thread.sleep(1000)
            }
        }
        if (!connected) {
            echo("Cannot connect to server at $serverWsUrl after $maxAttempts attempts — is it running?", err = true)
            return
        }

        deltaClient.sendSignOnRequest()

        // Maps partition ID → partition info
        val partitions = ConcurrentHashMap<String, Map<String, String?>>()
        // Maps node ID → node info (all nodes, including partition roots)
        val nodes = ConcurrentHashMap<String, NodeInfo>()

        fun addNodeFromSerialized(
            instance: SerializedClassifierInstance,
            parentId: String?,
            containmentKey: String?,
            containmentLanguageKey: String?,
            containmentLanguageVersion: String?,
        ) {
            val id = instance.id ?: return
            val props = ConcurrentHashMap<String, PropertyValue>()
            instance.properties.forEach { pv ->
                val key = pv.metaPointer?.key ?: return@forEach
                props[key] =
                    PropertyValue(
                        key = key,
                        languageKey = pv.metaPointer?.language,
                        languageVersion = pv.metaPointer?.version,
                        value = pv.value,
                    )
            }
            val childrenByContainment = ConcurrentHashMap<String, MutableList<String>>()
            instance.containments?.forEach { cv ->
                val cKey = cv.metaPointer?.key ?: return@forEach
                childrenByContainment.getOrPut(cKey) { mutableListOf() }.addAll(cv.childrenIds)
            }
            nodes[id] =
                NodeInfo(
                    id = id,
                    classifierKey = instance.classifier?.key,
                    classifierLanguageKey = instance.classifier?.language,
                    classifierLanguageVersion = instance.classifier?.version,
                    parentId = parentId,
                    containmentKey = containmentKey,
                    containmentLanguageKey = containmentLanguageKey,
                    containmentLanguageVersion = containmentLanguageVersion,
                    properties = props,
                    children = childrenByContainment,
                )
        }

        fun addAllNodesFromChunk(
            instances: Iterable<SerializedClassifierInstance>,
            rootParentId: String?,
            rootContainmentKey: String?,
            rootContainmentLanguageKey: String?,
            rootContainmentLanguageVersion: String?,
        ) {
            val byId = instances.associateBy { it.id }
            instances.forEach { inst ->
                // A node is the root of this chunk if its parent is not inside the chunk itself:
                // either parentNodeID is null (detached node) or points outside the chunk.
                val isChunkRoot = inst.parentNodeID == null || !byId.containsKey(inst.parentNodeID)
                val parentId = if (isChunkRoot) (inst.parentNodeID ?: rootParentId) else inst.parentNodeID
                val (cKey, cLangKey, cLangVer) =
                    if (isChunkRoot) {
                        Triple(rootContainmentKey, rootContainmentLanguageKey, rootContainmentLanguageVersion)
                    } else {
                        val parent = byId[inst.parentNodeID]
                        val cv = parent?.containments?.firstOrNull { it.childrenIds.contains(inst.id) }
                        Triple(cv?.metaPointer?.key, cv?.metaPointer?.language, cv?.metaPointer?.version)
                    }
                addNodeFromSerialized(inst, parentId, cKey, cLangKey, cLangVer)
            }
        }

        channel.registerEventReceiver { event ->
            when (event) {
                is PartitionAdded -> {
                    val instances = event.newPartition.getClassifierInstances()
                    val root = instances.firstOrNull { it.parentNodeID == null } ?: return@registerEventReceiver
                    val id = root.id ?: return@registerEventReceiver
                    partitions.putIfAbsent(
                        id,
                        mapOf(
                            "id" to id,
                            "classifierKey" to root.classifier?.key,
                            "classifierLanguageKey" to root.classifier?.language,
                        ),
                    )
                    addAllNodesFromChunk(instances, null, null, null, null)
                }
                is PartitionDeleted -> {
                    partitions.remove(event.deletedPartition)
                    nodes.keys
                        .filter { nodeId ->
                            var n = nodes[nodeId]
                            while (n != null && n.parentId != null) n = nodes[n.parentId]
                            n?.id == event.deletedPartition
                        }.forEach { nodes.remove(it) }
                    nodes.remove(event.deletedPartition)
                }
                is ChildAdded -> {
                    val instances = event.newChild.getClassifierInstances()
                    addAllNodesFromChunk(
                        instances,
                        event.parent,
                        event.containment.key,
                        event.containment.language,
                        event.containment.version,
                    )
                    // Update parent's children map
                    nodes[event.parent]?.let { parent ->
                        val cKey = event.containment.key ?: return@let
                        val list = parent.children.getOrPut(cKey) { mutableListOf() }
                        val byId = instances.associateBy { it.id }
                        val rootInst = instances.firstOrNull { it.parentNodeID == null || !byId.containsKey(it.parentNodeID) }
                        val rootId = rootInst?.id ?: return@let
                        if (!list.contains(rootId)) {
                            val idx = minOf(event.index, list.size)
                            list.add(idx, rootId)
                        }
                    }
                }
                is ChildDeleted -> {
                    val allDeleted = listOf(event.deletedChild) + event.deletedDescendants
                    allDeleted.forEach { nodes.remove(it) }
                    nodes[event.parent]?.let { parent ->
                        val cKey = event.containment.key ?: return@let
                        parent.children[cKey]?.remove(event.deletedChild)
                    }
                }
                is PropertyAdded -> {
                    nodes[event.node]?.let { node ->
                        val key = event.property.key ?: return@let
                        node.properties[key] =
                            PropertyValue(
                                key = key,
                                languageKey = event.property.language,
                                languageVersion = event.property.version,
                                value = event.newValue,
                            )
                    }
                }
                is PropertyChanged -> {
                    nodes[event.node]?.let { node ->
                        val key = event.property.key ?: return@let
                        node.properties[key] =
                            PropertyValue(
                                key = key,
                                languageKey = event.property.language,
                                languageVersion = event.property.version,
                                value = event.newValue,
                            )
                    }
                }
                is PropertyDeleted -> {
                    nodes[event.node]?.let { node ->
                        val key = event.property.key ?: return@let
                        node.properties.remove(key)
                    }
                }
                else -> {}
            }
        }

        // Subscribe to partition lifecycle events (PartitionAdded/Deleted) and get initial list.
        // Content events (ChildAdded, PropertyChanged, etc.) only arrive after explicitly
        // calling sendSubscribeToPartitionContentsRequest for each partition.
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

        // Tracks which partitions this client has subscribed to for content events
        val subscribedPartitions = ConcurrentHashMap.newKeySet<String>()

        DemoClientWebServer(
            httpPort,
            clientId,
            serverWsUrl,
            deltaClient,
            messageLog,
            partitions,
            nodes,
            subscribedPartitions,
            knownLanguages,
        ).start()

        echo("Demo client '$clientId' connected to $serverWsUrl, web UI at http://localhost:$httpPort")

        Thread.currentThread().join()
    }
}
