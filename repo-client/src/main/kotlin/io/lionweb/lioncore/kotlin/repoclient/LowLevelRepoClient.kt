package io.lionweb.lioncore.kotlin.repoclient

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.net.ConnectException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * This is a low-level client for the LW Repo. It just does the requests, without any preparation or safe-guard.
 * Users of this library should use LionWebClient, which will delegate to this class.
 */
internal class LowLevelRepoClient(
    val hostname: String = "localhost",
    val port: Int = 3005,
    val authorizationToken: String? = null,
    var clientID: String = "GenericKotlinBasedLionWebClient",
    var repository: String = "default",
    val connectTimeOutInSeconds: Long = 60,
    val callTimeoutInSeconds: Long = 60,
    val debug: Boolean = false,
) {
    fun createRepository(history: Boolean = false) {
        val url = "http://$hostname:$port/createRepository?history=$history"
        val request: Request =
            Request.Builder()
                .url(url.addClientIdQueryParam())
                .considerAuthenticationToken()
                .post(EMPTY_REQUEST)
                .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("DB initialization failed, HTTP ${response.code}: ${response.body?.string()}")
            }
        }
    }

    fun deletePartition(nodeID: String) {
        val body: RequestBody = "[\"${nodeID}\"]".toRequestBody(JSON)
        val request: Request =
            Request.Builder()
                .url("http://$hostname:$port/bulk/deletePartitions".addClientIdQueryParam())
                .considerAuthenticationToken()
                .post(body)
                .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code != HttpURLConnection.HTTP_OK) {
                val body = response.body?.string()
                if (debug) {
                    println("  Response: ${response.code}")
                    println("  Response: $body")
                }
                throw RuntimeException("Request failed with code ${response.code}: $body")
            }
        }
    }

    fun getPartitionIDs(): String {
        val url = "http://$hostname:$port/bulk/listPartitions"
        val request: Request =
            Request.Builder()
                .url(url.addClientIdQueryParam())
                .considerAuthenticationToken()
                .addHeader("Accept-Encoding", "gzip")
                .post(EMPTY_REQUEST)
                .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code == HttpURLConnection.HTTP_OK) {
                val data =
                    (response.body ?: throw IllegalStateException("Response without body when querying $url")).string()
                return data
            } else {
                throw RuntimeException("Got back ${response.code}: ${response.body?.string()}")
            }
        }
    }

    fun retrieve(
        rootIds: List<String>,
        limit: Int,
    ): String {
        require(rootIds.isNotEmpty())
        require(rootIds.all { it.isNotBlank() })
        val body: RequestBody = "{\"ids\":[${rootIds.joinToString(", "){"\"$it\""}}] }".toRequestBody(JSON)
        val url = "http://$hostname:$port/bulk/retrieve"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("depthLimit", limit.toString())
        urlBuilder.addQueryParameter("clientId", clientID)
        urlBuilder.addQueryParameter("repository", repository)
        val request: Request =
            Request.Builder()
                .url(urlBuilder.build())
                .considerAuthenticationToken()
                .post(body)
                .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code == HttpURLConnection.HTTP_OK) {
                val data =
                    (response.body ?: throw IllegalStateException("Response without body when querying $url")).string()
                return data
            } else {
                throw RuntimeException(
                    "Something went wrong while querying $url: http code ${response.code}, body: ${response.body?.string()}",
                )
            }
        }
    }

    fun nodesByClassifier(limit: Int? = null): Map<ClassifierKey, ClassifierResult> {
        val url = "http://$hostname:$port/inspection/nodesByClassifier"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("clientId", clientID)
        if (limit != null) {
            urlBuilder.addQueryParameter("limit", limit.toString())
        }
        val request: Request =
            Request.Builder()
                .url(urlBuilder.build())
                .considerAuthenticationToken()
                .get()
                .build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("DB initialization failed, HTTP ${response.code}: $body")
            }
            val data = JsonParser.parseString(body)
            val result = mutableMapOf<ClassifierKey, ClassifierResult>()
            data.asJsonArray.map { it.asJsonObject }.forEach { entry ->
                val classifierKey = ClassifierKey(entry["language"].asString, entry["classifier"].asString)
                val ids: Set<String> = entry["ids"].asJsonArray.map { it.asString }.toSet()
                result[classifierKey] = ClassifierResult(ids, entry["size"].asInt)
            }
            return result
        }
    }

    fun nodeTree(
        nodeIDs: List<String>,
        depthLimit: Int? = null,
    ): List<NodeInfo> {
        val url = "http://$hostname:$port/additional/getNodeTree"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        if (depthLimit != null) {
            urlBuilder.addQueryParameter("depthLimit", depthLimit.toString())
        }
        urlBuilder.addQueryParameter("clientId", clientID)
        urlBuilder.addQueryParameter("repository", repository)
        val body = JsonObject()
        val ids = JsonArray()
        nodeIDs.forEach { ids.add(it) }
        body.add("ids", ids)
        val bodyJson = Gson().toJson(body)
        val builder =
            Request.Builder()
                .url(urlBuilder.build())
                .considerAuthenticationToken()
                .post(bodyJson.toRequestBody(JSON))

        val request: Request =
            builder
                .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("${response.code}: $body")
            }

            val data = JsonParser.parseString(body).asJsonObject.get("data").asJsonArray
            return data.map { it.asJsonObject }.map { dataElement ->
                val parent =
                    if (dataElement.has("parent") && dataElement.get("parent") !is JsonNull) {
                        dataElement.get(
                            "parent",
                        ).asString
                    } else {
                        null
                    }
                NodeInfo(dataElement.get("id").asString, parent, dataElement.get("depth").asInt)
            }
        }
    }

    fun bulkImportUsingJson(
        bodyJson: String,
        compress: Boolean = false,
    ) {
        val requestBody = bodyJson.toRequestBody(JSON).considerCompression(compress)
        bulkImport(requestBody, compress)
    }

    fun bulkImportUsingProtobuf(
        bytes: ByteArray,
        compress: Boolean = false,
    ) {
        val requestBody = bytes.toRequestBody(PROTOBUF).considerCompression(compress)
        bulkImport(requestBody, compress)
    }

    fun bulkImportUsingFlatBuffers(
        bytes: ByteArray,
        compress: Boolean = false,
    ) {
        val requestBody = bytes.toRequestBody(FLATBUFFERS).considerCompression(compress)
        bulkImport(requestBody, compress)
    }

    fun nodesStoringOperation(
        json: String,
        operation: String,
    ) {
        val body: RequestBody = json.compress()

        // TODO control with flag http or https
        val url = "http://$hostname:$port/bulk/$operation"
        val request: Request =
            Request.Builder()
                .url(url.addClientIdQueryParam().addRepositoryQueryParam())
                .considerAuthenticationToken()
                .addGZipCompressionHeader()
                .post(body)
                .build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.code != HttpURLConnection.HTTP_OK) {
                    val body = response.body?.string()
                    if (debug) {
                        println("  Response: ${response.code}")
                        println("  Response: $body")
                    }
                    throw RequestFailureException(url, json, response.code, body)
                }
            }
        } catch (e: ConnectException) {
            val jsonExcept =
                if (json.length > 10000) {
                    json.substring(0, 1000) + "..."
                } else {
                    json
                }
            throw RuntimeException("Cannot get answer from the client when contacting at URL $url. Body: $jsonExcept", e)
        }
    }

    private fun bulkImport(
        requestBody: RequestBody,
        compress: Boolean = false,
    ) {
        val url = "http://$hostname:$port/additional/bulkImport"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("clientId", clientID)
        urlBuilder.addQueryParameter("repository", repository)
        val builder =
            Request.Builder()
                .url(urlBuilder.build())
                .considerAuthenticationToken()
                .considerCompression(compress)
                .post(requestBody)

        val request: Request =
            builder
                .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string()
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("${response.code}: $body")
            }

            val success = JsonParser.parseString(body).asJsonObject.get("success").asBoolean
            if (!success) {
                throw RuntimeException("Request failed: $body")
            }
        }
    }

    private var httpClient: OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(
                callTimeoutInSeconds,
                TimeUnit.SECONDS,
            ).readTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .connectTimeout(connectTimeOutInSeconds, TimeUnit.SECONDS).build()

    private fun String.addClientIdQueryParam(): HttpUrl {
        val urlBuilder = this.toHttpUrl().newBuilder()
        urlBuilder.addQueryParameter("clientId", clientID)
        return urlBuilder.build()
    }

    private fun Request.Builder.considerAuthenticationToken(): Request.Builder {
        return if (authorizationToken == null) {
            this
        } else {
            this.addHeader("Authorization", authorizationToken)
        }
    }

    private fun Request.Builder.considerCompression(compress: Boolean): Request.Builder {
        return if (compress) {
            this.addGZipCompressionHeader()
        } else {
            this
        }
    }

    private fun HttpUrl.addRepositoryQueryParam(): HttpUrl {
        val urlBuilder = this.newBuilder()
        urlBuilder.addQueryParameter("repository", repository)
        return urlBuilder.build()
    }
}

fun Request.Builder.addGZipCompressionHeader(): Request.Builder {
    this.addHeader("Content-Encoding", "gzip")
    return this
}

fun RequestBody.considerCompression(compress: Boolean): RequestBody {
    return if (compress) {
        this.compress()
    } else {
        this
    }
}
