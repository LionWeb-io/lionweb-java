package io.lionweb.server

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import io.lionweb.client.delta.DeltaClient
import io.lionweb.language.Concept
import io.lionweb.language.Language
import io.lionweb.model.impl.DynamicNode
import java.net.InetSocketAddress
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

class DemoClientWebServer(
    private val port: Int,
    private val clientId: String,
    private val serverUrl: String,
    private val deltaClient: DeltaClient,
    private val messageLog: MessageLog,
    private val partitionIds: CopyOnWriteArrayList<String>,
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
                                mapOf(
                                    "key" to c.key,
                                    "name" to c.name,
                                    "languageName" to lang.name,
                                    "languageKey" to lang.key,
                                    "languageVersion" to lang.version,
                                    "isPartition" to c.isPartition,
                                )
                            }
                    }
                val state =
                    mapOf(
                        "clientId" to clientId,
                        "serverUrl" to serverUrl,
                        "partitions" to partitionIds.map { mapOf("id" to it) },
                        "messages" to messageLog.getAll(),
                        "concepts" to concepts,
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
                        partitionIds.add(id)
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
                        partitionIds.remove(partitionId)
                        val resp = "{}".toByteArray(Charsets.UTF_8)
                        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                        exchange.sendResponseHeaders(200, resp.size.toLong())
                        exchange.responseBody.use { it.write(resp) }
                    }
                    else -> exchange.sendResponseHeaders(400, -1)
                }
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
