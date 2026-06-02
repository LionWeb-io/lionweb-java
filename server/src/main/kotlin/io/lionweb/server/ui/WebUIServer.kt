package io.lionweb.server.ui

import com.google.gson.Gson
import com.sun.net.httpserver.HttpServer
import io.lionweb.client.inmemory.InMemoryServer
import java.net.InetSocketAddress

class WebUIServer(
    private val port: Int,
    private val inMemoryServer: InMemoryServer,
    private val messageLog: MessageLog? = null,
) {
    private val gson = Gson()

    fun start(): HttpServer {
        val server = HttpServer.create(InetSocketAddress(port), 0)

        server.createContext("/api/data") { exchange ->
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            if (exchange.requestMethod == "GET") {
                val bytes = gson.toJson(buildData()).toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }

        server.createContext("/api/messages") { exchange ->
            exchange.responseHeaders.set("Access-Control-Allow-Origin", "*")
            if (exchange.requestMethod == "GET") {
                val bytes = gson.toJson(messageLog?.getAll() ?: emptyList<MessageLogEntry>()).toByteArray(Charsets.UTF_8)
                exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                exchange.sendResponseHeaders(405, -1)
            }
        }

        server.createContext("/") { exchange ->
            val reqPath = exchange.requestURI.path.let { if (it == "/") "/index.html" else it }
            val resource = WebUIServer::class.java.getResourceAsStream("/webui$reqPath")
            if (resource != null) {
                val bytes = resource.use { it.readBytes() }
                exchange.responseHeaders.set("Content-Type", contentTypeFor(reqPath))
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } else {
                // SPA fallback: serve index.html for unknown paths
                val index = WebUIServer::class.java.getResourceAsStream("/webui/index.html")
                if (index != null) {
                    val bytes = index.use { it.readBytes() }
                    exchange.responseHeaders.set("Content-Type", "text/html; charset=utf-8")
                    exchange.sendResponseHeaders(200, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                } else {
                    val msg = "Web UI not found. Build the frontend first with: cd server/web-ui && npm run build"
                    val bytes = msg.toByteArray()
                    exchange.responseHeaders.set("Content-Type", "text/plain")
                    exchange.sendResponseHeaders(503, bytes.size.toLong())
                    exchange.responseBody.use { it.write(bytes) }
                }
            }
        }

        server.executor = null
        server.start()
        return server
    }

    private fun buildData(): Map<String, Any> {
        val repos =
            inMemoryServer.listRepositories().map { config ->
                val name = config.name
                val partitions =
                    inMemoryServer.listPartitionIDs(name).map { id ->
                        val classifier =
                            inMemoryServer
                                .retrieve(name, listOf(id), 0)
                                .firstOrNull()
                                ?.classifier
                        mapOf(
                            "id" to id,
                            "classifierKey" to classifier?.key,
                            "classifierLanguageKey" to classifier?.language,
                        )
                    }
                val classifiers =
                    inMemoryServer.nodesByClassifier(name).map { (key, result) ->
                        mapOf(
                            "languageKey" to key.languageKey,
                            "classifierKey" to key.classifierKey,
                            "totalCount" to result.size,
                            "sampleIds" to result.ids.take(10),
                        )
                    }
                mapOf(
                    "name" to name,
                    "lionWebVersion" to config.lionWebVersion.toString(),
                    "historySupport" to config.historySupport.toString(),
                    "partitions" to partitions,
                    "classifiers" to classifiers,
                )
            }
        return mapOf("repositories" to repos)
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
