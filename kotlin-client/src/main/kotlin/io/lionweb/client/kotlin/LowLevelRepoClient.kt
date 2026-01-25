package io.lionweb.client.kotlin

import com.google.gson.JsonParser
import io.lionweb.LionWebVersion
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    val lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
) {
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
            Request
                .Builder()
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
        urlBuilder.addQueryParameter("repository", repository)
        if (limit != null) {
            urlBuilder.addQueryParameter("limit", limit.toString())
        }
        val request: Request =
            Request
                .Builder()
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

    private var httpClient: OkHttpClient =
        OkHttpClient
            .Builder()
            .callTimeout(
                callTimeoutInSeconds,
                TimeUnit.SECONDS,
            ).readTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .connectTimeout(connectTimeOutInSeconds, TimeUnit.SECONDS)
            .build()

    private fun Request.Builder.considerAuthenticationToken(): Request.Builder =
        if (authorizationToken == null) {
            this
        } else {
            this.addHeader("Authorization", authorizationToken)
        }
}
