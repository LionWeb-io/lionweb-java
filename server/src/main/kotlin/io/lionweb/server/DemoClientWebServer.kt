package io.lionweb.server

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import io.lionweb.client.delta.DeltaClient
import io.lionweb.language.Concept
import io.lionweb.language.Language
import io.lionweb.model.impl.DynamicNode
import io.lionweb.serialization.data.MetaPointer
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DemoClientWebServer(
    private val port: Int,
    private val clientId: String,
    private val serverUrl: String,
    private val deltaClient: DeltaClient,
    private val messageLog: MessageLog,
    private val partitions: ConcurrentHashMap<String, Map<String, String?>>,
    private val nodes: ConcurrentHashMap<String, NodeInfo>,
    private val subscribedPartitions: MutableSet<String>,
    private val knownLanguages: List<Language> = emptyList(),
) {
    private val gson = Gson()

    fun start(): HttpServer {
        val server = HttpServer.create(InetSocketAddress(port), 0)

        server.createContext("/api/state") { exchange ->
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            if (exchange.requestMethod == "GET") {
                val concepts =
                    knownLanguages.flatMap { lang ->
                        lang.elements
                            .filterIsInstance<Concept>()
                            .filter { !it.isAbstract }
                            .map { c ->
                                val containments =
                                    c.allContainments().map { cont ->
                                        mapOf(
                                            "key" to cont.key,
                                            "name" to cont.name,
                                            "languageKey" to cont.declaringLanguage?.key,
                                            "languageVersion" to cont.declaringLanguage?.version,
                                            "multiple" to cont.isMultiple,
                                            "optional" to cont.isOptional,
                                            "typeKey" to cont.type?.key,
                                            "typeLanguageKey" to cont.type?.language?.key,
                                        )
                                    }
                                val properties =
                                    c.allProperties().map { prop ->
                                        mapOf(
                                            "key" to prop.key,
                                            "name" to prop.name,
                                            "languageKey" to prop.declaringLanguage?.key,
                                            "languageVersion" to prop.declaringLanguage?.version,
                                            "optional" to prop.isOptional,
                                            "typeKey" to prop.type?.key,
                                            "typeName" to prop.type?.name,
                                        )
                                    }
                                mapOf(
                                    "key" to c.key,
                                    "name" to c.name,
                                    "languageName" to lang.name,
                                    "languageKey" to lang.key,
                                    "languageVersion" to lang.version,
                                    "isPartition" to c.isPartition,
                                    "containments" to containments,
                                    "properties" to properties,
                                )
                            }
                    }
                val serializedNodes =
                    nodes.values.map { node ->
                        mapOf(
                            "id" to node.id,
                            "classifierKey" to node.classifierKey,
                            "classifierLanguageKey" to node.classifierLanguageKey,
                            "classifierLanguageVersion" to node.classifierLanguageVersion,
                            "parentId" to node.parentId,
                            "containmentKey" to node.containmentKey,
                            "containmentLanguageKey" to node.containmentLanguageKey,
                            "containmentLanguageVersion" to node.containmentLanguageVersion,
                            "properties" to
                                node.properties.values.map { pv ->
                                    mapOf(
                                        "key" to pv.key,
                                        "languageKey" to pv.languageKey,
                                        "languageVersion" to pv.languageVersion,
                                        "value" to pv.value,
                                    )
                                },
                            "children" to node.children,
                        )
                    }
                val state =
                    mapOf(
                        "clientId" to clientId,
                        "serverUrl" to serverUrl,
                        "partitions" to partitions.values.toList(),
                        "subscribedPartitions" to subscribedPartitions.toList(),
                        "messages" to messageLog.getAll(),
                        "concepts" to concepts,
                        "nodes" to serializedNodes,
                    )
                val bytes = gson.toJson(state).toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }

        server.createContext("/api/action") { exchange ->
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            if (exchange.requestMethod == "POST") {
                val body = exchange.requestBody.use { it.readBytes() }.toString(Charsets.UTF_8)
                val json = JsonParser.parseString(body).asJsonObject
                val type = json.get("type")?.asString
                when (type) {
                    "create" -> {
                        val conceptKey = json.get("conceptKey")?.asString
                        val languageKey = json.get("languageKey")?.asString
                        val concept: Concept? =
                            if (conceptKey != null && languageKey != null) {
                                knownLanguages
                                    .firstOrNull { it.key == languageKey }
                                    ?.elements
                                    ?.filterIsInstance<Concept>()
                                    ?.firstOrNull { it.key == conceptKey }
                            } else {
                                null
                            }
                        if (concept == null) {
                            exchange.sendResponseHeaders(400, -1)
                            return@createContext
                        }
                        val id = UUID.randomUUID().toString()
                        val node = DynamicNode(id, concept)
                        deltaClient.sendAddPartitionCommand(node)
                        deltaClient.sendSubscribeToPartitionContentsRequest(id)
                        partitions[id] =
                            mapOf(
                                "id" to id,
                                "classifierKey" to concept.key,
                                "classifierLanguageKey" to concept.language?.key,
                            )
                        nodes[id] =
                            NodeInfo(
                                id = id,
                                classifierKey = concept.key,
                                classifierLanguageKey = concept.language?.key,
                                classifierLanguageVersion = concept.language?.version,
                                parentId = null,
                                containmentKey = null,
                                containmentLanguageKey = null,
                                containmentLanguageVersion = null,
                            )
                        subscribedPartitions.add(id)
                        val resp = gson.toJson(mapOf("id" to id)).toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "delete" -> {
                        val partitionId =
                            json.get("partitionId")?.asString ?: run {
                                exchange.sendResponseHeaders(400, -1)
                                return@createContext
                            }
                        deltaClient.sendDeletePartitionCommand(partitionId)
                        partitions.remove(partitionId)
                        nodes.remove(partitionId)
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "addChild" -> {
                        val parentId = json.get("parentId")?.asString
                        val containmentKey = json.get("containmentKey")?.asString
                        val containmentLanguageKey = json.get("containmentLanguageKey")?.asString
                        val containmentLanguageVersion = json.get("containmentLanguageVersion")?.asString
                        val conceptKey = json.get("conceptKey")?.asString
                        val languageKey = json.get("languageKey")?.asString
                        if (parentId == null || containmentKey == null || conceptKey == null || languageKey == null) {
                            exchange.sendResponseHeaders(400, -1)
                            return@createContext
                        }
                        val concept =
                            knownLanguages
                                .firstOrNull { it.key == languageKey }
                                ?.elements
                                ?.filterIsInstance<Concept>()
                                ?.firstOrNull { it.key == conceptKey }
                        if (concept == null) {
                            exchange.sendResponseHeaders(400, -1)
                            return@createContext
                        }
                        val childId = UUID.randomUUID().toString()
                        val child = DynamicNode(childId, concept)
                        val containmentMeta =
                            MetaPointer.get(containmentLanguageKey, containmentLanguageVersion, containmentKey)
                        val parentNode = nodes[parentId]
                        val index = parentNode?.children?.get(containmentKey)?.size ?: 0
                        deltaClient.sendAddChildCommand(parentId, containmentMeta, child, index)
                        nodes[childId] =
                            NodeInfo(
                                id = childId,
                                classifierKey = concept.key,
                                classifierLanguageKey = concept.language?.key,
                                classifierLanguageVersion = concept.language?.version,
                                parentId = parentId,
                                containmentKey = containmentKey,
                                containmentLanguageKey = containmentLanguageKey,
                                containmentLanguageVersion = containmentLanguageVersion,
                            )
                        parentNode?.children?.getOrPut(containmentKey) { mutableListOf() }?.add(childId)
                        val resp = gson.toJson(mapOf("id" to childId)).toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "setProperty" -> {
                        val nodeId = json.get("nodeId")?.asString
                        val propertyKey = json.get("propertyKey")?.asString
                        val propertyLanguageKey = json.get("propertyLanguageKey")?.asString
                        val propertyLanguageVersion = json.get("propertyLanguageVersion")?.asString
                        val value = json.get("value")?.takeIf { !it.isJsonNull }?.asString
                        if (nodeId == null || propertyKey == null) {
                            exchange.sendResponseHeaders(400, -1)
                            return@createContext
                        }
                        val nodeInfo = nodes[nodeId]
                        val alreadySet = nodeInfo?.properties?.containsKey(propertyKey) == true
                        val propertyMeta =
                            MetaPointer.get(propertyLanguageKey, propertyLanguageVersion, propertyKey)
                        deltaClient.sendSetPropertyCommand(nodeId, propertyMeta, value, alreadySet)
                        if (nodeInfo != null) {
                            if (value != null) {
                                nodeInfo.properties[propertyKey] =
                                    PropertyValue(
                                        key = propertyKey,
                                        languageKey = propertyLanguageKey,
                                        languageVersion = propertyLanguageVersion,
                                        value = value,
                                    )
                            } else {
                                nodeInfo.properties.remove(propertyKey)
                            }
                        }
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "subscribe" -> {
                        val partitionId =
                            json.get("partitionId")?.asString ?: run {
                                exchange.sendResponseHeaders(400, -1)
                                return@createContext
                            }
                        val subResp = deltaClient.sendSubscribeToPartitionContentsRequest(partitionId)
                        val instances = subResp.contents.getClassifierInstances()
                        // Load partition root info (may already exist from ListAndSubscribe)
                        instances
                            .filter { it.parentNodeID == null }
                            .forEach { root ->
                                val id = root.id ?: return@forEach
                                partitions[id] =
                                    mapOf(
                                        "id" to id,
                                        "classifierKey" to root.classifier?.key,
                                        "classifierLanguageKey" to root.classifier?.language,
                                    )
                            }
                        // Load all nodes from subscription response
                        val byId = instances.associateBy { it.id }
                        instances.forEach { inst ->
                            val parentId = inst.parentNodeID
                            val cv =
                                if (parentId !=
                                    null
                                ) {
                                    byId[parentId]?.containments?.firstOrNull { it.childrenIds.contains(inst.id) }
                                } else {
                                    null
                                }
                            val props = java.util.concurrent.ConcurrentHashMap<String, PropertyValue>()
                            inst.properties.forEach { pv ->
                                val key = pv.metaPointer?.key ?: return@forEach
                                props[key] = PropertyValue(key, pv.metaPointer?.language, pv.metaPointer?.version, pv.value)
                            }
                            val children = java.util.concurrent.ConcurrentHashMap<String, MutableList<String>>()
                            inst.containments?.forEach { c ->
                                val cKey = c.metaPointer?.key ?: return@forEach
                                children[cKey] = c.childrenIds.toMutableList()
                            }
                            nodes[inst.id ?: return@forEach] =
                                NodeInfo(
                                    id = inst.id!!,
                                    classifierKey = inst.classifier?.key,
                                    classifierLanguageKey = inst.classifier?.language,
                                    classifierLanguageVersion = inst.classifier?.version,
                                    parentId = parentId,
                                    containmentKey = cv?.metaPointer?.key,
                                    containmentLanguageKey = cv?.metaPointer?.language,
                                    containmentLanguageVersion = cv?.metaPointer?.version,
                                    properties = props,
                                    children = children,
                                )
                        }
                        subscribedPartitions.add(partitionId)
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "unsubscribe" -> {
                        val partitionId =
                            json.get("partitionId")?.asString ?: run {
                                exchange.sendResponseHeaders(400, -1)
                                return@createContext
                            }
                        deltaClient.sendUnsubscribeFromPartitionContentsRequest(partitionId)
                        subscribedPartitions.remove(partitionId)
                        // Remove all nodes belonging to this partition
                        nodes.keys
                            .filter { nodeId ->
                                var n = nodes[nodeId]
                                while (n != null && n.parentId != null) n = nodes[n.parentId]
                                n?.id == partitionId
                            }.forEach { nodes.remove(it) }
                        nodes.remove(partitionId)
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    "deleteChild" -> {
                        val nodeId = json.get("nodeId")?.asString
                        val parentId = json.get("parentId")?.asString
                        val containmentKey = json.get("containmentKey")?.asString
                        val containmentLanguageKey = json.get("containmentLanguageKey")?.asString
                        val containmentLanguageVersion = json.get("containmentLanguageVersion")?.asString
                        val index = json.get("index")?.asInt
                        if (nodeId == null || parentId == null || containmentKey == null || index == null) {
                            exchange.sendResponseHeaders(400, -1)
                            return@createContext
                        }
                        val containmentMeta =
                            MetaPointer.get(containmentLanguageKey, containmentLanguageVersion, containmentKey)
                        deltaClient.sendDeleteChildCommand(parentId, containmentMeta, index, nodeId)
                        nodes.remove(nodeId)
                        nodes[parentId]?.children?.get(containmentKey)?.removeAt(index)
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    else -> exchange.sendResponseHeaders(400, -1)
                }
            } else if (exchange.requestMethod == "OPTIONS") {
                exchange.responseHeaders.set("Access-Control-Allow-Methods", "POST, OPTIONS")
                exchange.responseHeaders.set("Access-Control-Allow-Headers", "Content-Type")
                exchange.sendResponseHeaders(204, -1)
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }

        server.createContext("/assets") { exchange ->
            val reqPath = exchange.requestURI.path
            val resource = DemoClientWebServer::class.java.getResourceAsStream("/webui$reqPath")
            if (resource != null) {
                val bytes = resource.use { it.readBytes() }
                exchange.responseHeaders.set("Content-Type", contentTypeFor(reqPath))
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.sendResponseHeaders(404, -1)
            }
        }

        server.createContext("/") { exchange ->
            val reqPath = exchange.requestURI.path
            if (reqPath == "/" || reqPath == "/index.html") {
                val resource = DemoClientWebServer::class.java.getResourceAsStream("/webui/client.html")
                if (resource != null) {
                    val bytes = resource.use { it.readBytes() }
                    exchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                } else {
                    val msg = "Client UI not found. Build the frontend first with: cd server/web-ui && npm run build"
                    val bytes = msg.toByteArray()
                    exchange.responseHeaders.set("Content-Type", "text/plain")
                    exchange.sendResponseHeaders(503, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                }
            } else {
                exchange.sendResponseHeaders(404, -1)
            }
        }

        server.executor = null
        server.start()
        return server
    }

    private fun contentTypeFor(path: String): String =
        when {
            path.endsWith(".html") -> "text/html; charset=utf-8"
            path.endsWith(".js") -> "application/javascript"
            path.endsWith(".css") -> "text/css"
            path.endsWith(".json") -> "application/json"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".ico") -> "image/x-icon"
            else -> "application/octet-stream"
        }
}
