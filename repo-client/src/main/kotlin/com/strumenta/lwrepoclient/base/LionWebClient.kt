package com.strumenta.lwrepoclient.base

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.strumenta.lionweb.kotlin.MetamodelRegistry
import com.strumenta.lionweb.kotlin.children
import com.strumenta.lionweb.kotlin.getChildrenByContainmentName
import com.strumenta.lionweb.kotlin.getReferenceValueByName
import com.strumenta.lionweb.kotlin.setPropertyValueByName
import com.strumenta.lionweb.kotlin.setReferenceValuesByName
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.model.impl.ProxyNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import java.net.ConnectException
import java.net.HttpURLConnection
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class LionWebClient(
    val hostname: String = "localhost",
    val port: Int = 3005,
    val debug: Boolean = false,
    val jsonSerializationProvider: (() -> JsonSerialization)? = null,
    val connectTimeOutInSeconds: Long = 60,
    val callTimeoutInSeconds: Long = 60,
    val authorizationToken: String? = null,
) {
    // Fields

    /**
     * Exposed for testing purposes
     */
    val defaultJsonSerialization =
        JsonSerialization.getStandardSerialization().apply {
            enableDynamicNodes()
        }

    // TODO avoid re-instantiating jsonSerialization a ton of times
    val jsonSerialization: JsonSerialization
        get() {
            val jsonSerialization = jsonSerializationProvider?.invoke() ?: defaultJsonSerialization
            languages.forEach {
                jsonSerialization.registerLanguage(it)
            }
            MetamodelRegistry.prepareJsonSerialization(jsonSerialization)
            return jsonSerialization
        }

    // Configuration

    fun registerLanguage(language: Language) {
        languages.add(language)
    }

    // Setup

    /**
     * To be called exactly once, to ensure the Model Repository is initialized.
     * Note that it causes all content of the Model Repository to be lost!
     */
    fun modelRepositoryInit() {
        val url = "http://$hostname:$port/init"
        val request: Request =
            Request.Builder()
                .url(url)
                .considerAuthenticationToken()
                .post("".toRequestBody())
                .build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("DB initialization failed, HTTP ${response.code}: ${response.body?.string()}")
            }
        }
    }

    fun createDatabase() {
        val url = "http://$hostname:$port/createDatabase"
        val request: Request =
            Request.Builder()
                .url(url)
                .considerAuthenticationToken()
                .post(EMPTY_REQUEST)
                .build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("DB initialization failed, HTTP ${response.code}: ${response.body?.string()}")
            }
        }
    }

    fun createRepository(history: Boolean = false) {
        val url = "http://$hostname:$port/createRepository?history=$history"
        val request: Request =
            Request.Builder()
                .url(url)
                .considerAuthenticationToken()
                .post(EMPTY_REQUEST)
                .build()
        OkHttpClient().newCall(request).execute().use { response ->
            if (response.code != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("DB initialization failed, HTTP ${response.code}: ${response.body?.string()}")
            }
        }
    }

    // Partitions

    fun createPartition(node: Node) {
        if (node.children.isNotEmpty()) {
            throw IllegalArgumentException("When creating a partition, please specify a single node")
        }
        treeStoringOperation(node, "createPartitions")
    }

    fun deletePartition(node: Node) {
        deletePartition(node.id ?: throw IllegalStateException("Node ID not specified"))
    }

    fun deletePartition(nodeID: String) {
        val body: RequestBody = "[\"$nodeID\"]".toRequestBody(JSON)
        val request: Request =
            Request.Builder()
                .url("http://$hostname:$port/bulk/deletePartitions")
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

    private fun Request.Builder.considerAuthenticationToken(): Request.Builder {
        return if (authorizationToken == null) {
            this
        } else {
            this.addHeader("Authorization", authorizationToken)
        }
    }

    fun getPartitionIDs(): List<String> {
        val url = "http://$hostname:$port/bulk/listPartitions"
        val request: Request =
            Request.Builder()
                .url(url)
                .considerAuthenticationToken()
                .addHeader("Accept-Encoding", "gzip")
                .post(EMPTY_REQUEST)
                .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code == HttpURLConnection.HTTP_OK) {
                val data =
                    (response.body ?: throw IllegalStateException("Response without body when querying $url")).string()
                return processChunkResponse(data) {
                    val chunk = LowLevelJsonSerialization().deserializeSerializationBlock(it)
                    chunk.classifierInstances.mapNotNull { it.id }
                }
            } else {
                throw RuntimeException("Got back ${response.code}: ${response.body?.string()}")
            }
        }
    }

    // Nodes

    fun retrieve(
        rootId: String,
        withProxyParent: Boolean = false,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): Node {
        require(rootId.isNotBlank())
        val body: RequestBody = "{\"ids\":[\"$rootId\"] }".toRequestBody(JSON)
        val url = "http://$hostname:$port/bulk/retrieve"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        val limit =
            when (retrievalMode) {
                RetrievalMode.ENTIRE_SUBTREE -> "99"
                RetrievalMode.SINGLE_NODE -> "1"
            }
        urlBuilder.addQueryParameter("depthLimit", limit)
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
                debugFile("retrieved-$rootId.json") { data }

                return processChunkResponse(data) {
                    val js = jsonSerialization
                    js.unavailableParentPolicy =
                        if (withProxyParent) {
                            UnavailableNodePolicy.PROXY_NODES
                        } else {
                            UnavailableNodePolicy.NULL_REFERENCES
                        }
                    js.unavailableReferenceTargetPolicy = UnavailableNodePolicy.PROXY_NODES
                    js.unavailableChildrenPolicy =
                        when (retrievalMode) {
                            RetrievalMode.ENTIRE_SUBTREE -> UnavailableNodePolicy.THROW_ERROR
                            RetrievalMode.SINGLE_NODE -> UnavailableNodePolicy.PROXY_NODES
                        }
                    val nodes = js.deserializeToNodes(it)
                    nodes.find { it.id == rootId } ?: throw IllegalArgumentException(
                        "When requesting a subtree with rootId=$rootId we got back an answer without such ID. " +
                            "IDs we got back: ${nodes.map { it.id }.joinToString(", ")}",
                    )
                }
            } else {
                throw RuntimeException(
                    "Something went wrong while querying $url: http code ${response.code}, body: ${response.body?.string()}",
                )
            }
        }
    }

    fun retrieve(
        rootIds: List<String>,
        withProxyParent: Boolean = false,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): List<Node> {
        if (rootIds.isEmpty()) {
            return emptyList()
        }
        require(rootIds.all { it.isNotBlank() })
        val body: RequestBody = "{\"ids\":[${rootIds.joinToString(", "){"\"$it\""}}] }".toRequestBody(JSON)
        val url = "http://$hostname:$port/bulk/retrieve"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        val limit =
            when (retrievalMode) {
                RetrievalMode.ENTIRE_SUBTREE -> "99"
                RetrievalMode.SINGLE_NODE -> "1"
            }
        urlBuilder.addQueryParameter("depthLimit", limit)
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
                var ids = rootIds.toString()
                if (ids.length > 100) {
                    ids = ids.substring(0, 100)
                }
                debugFile("retrieved-$ids.json") { data }

                return processChunkResponse(data) {
                    val js = jsonSerialization
                    js.unavailableParentPolicy =
                        if (withProxyParent) {
                            UnavailableNodePolicy.PROXY_NODES
                        } else {
                            UnavailableNodePolicy.NULL_REFERENCES
                        }
                    js.unavailableReferenceTargetPolicy = UnavailableNodePolicy.PROXY_NODES
                    js.unavailableChildrenPolicy =
                        when (retrievalMode) {
                            RetrievalMode.ENTIRE_SUBTREE -> UnavailableNodePolicy.THROW_ERROR
                            RetrievalMode.SINGLE_NODE -> UnavailableNodePolicy.PROXY_NODES
                        }
                    val nodes = js.deserializeToNodes(it)
                    rootIds.map { rootId ->
                        nodes.find { node -> node.id == rootId } ?: throw IllegalArgumentException(
                            "When requesting a subtree with rootId=$rootId we got back an answer without such ID. " +
                                "IDs we got back: ${nodes.map { node -> node.id }.joinToString(", ")}",
                        )
                    }
                }
            } else {
                throw RuntimeException(
                    "Something went wrong while querying $url: http code ${response.code}, body: ${response.body?.string()}",
                )
            }
        }
    }

    fun getAncestorsId(nodeID: String): List<String> {
        val result = mutableListOf<String>()
        var currentNodeID: String? = nodeID
        while (currentNodeID != null) {
            currentNodeID = getParentId(currentNodeID)
            if (currentNodeID != null) {
                result.add(currentNodeID)
            }
        }
        return result
    }

    fun <T : Node> retrieveAncestor(
        node: Node,
        ancestorClass: KClass<*>,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): T? {
        if (node.parent == null) {
            return null
        } else {
            var parent = node.parent
            if (node.parent is ProxyNode) {
                parent = retrieve(node.parent.id!!, withProxyParent = true, retrievalMode = RetrievalMode.SINGLE_NODE)
            }
            return if (ancestorClass.isInstance(parent)) {
                if (retrievalMode == RetrievalMode.SINGLE_NODE) {
                    parent as T
                } else {
                    retrieve(node.parent.id!!, withProxyParent = true, retrievalMode = RetrievalMode.ENTIRE_SUBTREE) as T
                }
            } else {
                retrieveAncestor(parent, ancestorClass, retrievalMode)
            }
        }
    }

    fun isNodeExisting(nodeID: String): Boolean {
        require(nodeID.isNotBlank())
        val body: RequestBody = "{\"ids\":[\"$nodeID\"] }".toRequestBody(JSON)
        val url = "http://$hostname:$port/bulk/retrieve"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("depthLimit", "0")
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
                debugFile("isNodeExisting-$nodeID.json") { data }
                return processChunkResponse(data) { chunk ->
                    val nodes = chunk.asJsonObject.get("nodes").asJsonArray
                    !nodes.isEmpty
                }
            } else {
                throw RuntimeException(
                    "Something went wrong while querying $url: http code ${response.code}, body: ${response.body?.string()}",
                )
            }
        }
    }

    fun getParentId(nodeID: String): String? {
        require(nodeID.isNotBlank())
        val body: RequestBody = "{\"ids\":[\"$nodeID\"] }".toRequestBody(JSON)
        val url = "http://$hostname:$port/bulk/retrieve"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        urlBuilder.addQueryParameter("depthLimit", "0")
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
                debugFile("getParentId-$nodeID.json") { data }
                return processChunkResponse(data) { chunk ->
                    val nodes = chunk.asJsonObject.get("nodes").asJsonArray
                    if (nodes.size() != 1) {
                        throw UnexistingNodeException(
                            nodeID,
                            "When asking for the parent Id of $nodeID we were expecting to get one node back. " +
                                "We got ${nodes.size()}",
                        )
                    }
                    val node = nodes.get(0).asJsonObject
                    require(nodeID == node.get("id").asString)
                    val parentNode = node.get("parent")
                    if (parentNode.isJsonNull) {
                        null
                    } else {
                        parentNode.asString
                    }
                }
            } else {
                throw RuntimeException(
                    "Something went wrong while querying $url: http code ${response.code}, body: ${response.body?.string()}",
                )
            }
        }
    }

    fun storeTree(node: Node) {
        treeStoringOperation(node, "store")
    }

    /**
     * This operation is not atomic. We hope that no one is changing the parent at the very
     * same time.
     */
    fun appendTree(
        treeToAppend: Node,
        containerId: String,
        containmentName: String,
        containmentIndex: Int,
    ) {
        // 1. Retrieve the parent
        val parent = retrieve(containerId, retrievalMode = RetrievalMode.SINGLE_NODE)

        // 2. Add the tree to the parent
        val containment =
            parent.classifier.getContainmentByName(containmentName)
                ?: throw IllegalArgumentException("The container has not containment named $containmentName")
        if (!containment.isMultiple && parent.getChildrenByContainmentName(containmentName).isNotEmpty()) {
            throw IllegalArgumentException("The indicated containment ${containment.name} is not multiple and a child is already present")
        }
        require(parent.getChildren(containment).size == containmentIndex) {
            "We are trying to add element in containment ${containment.name} at index $containmentIndex, " +
                "however the number of children is ${containment.children.size}"
        }
        parent.addChild(containment, treeToAppend)
        require(parent.getChildren(containment).size == (containmentIndex + 1))
        (treeToAppend as DynamicNode).parent = parent

        (parent as DynamicNode).parent = getParentId(parent.id!!)?.let { ProxyNode(it) }

        // 3. Store the parent
        storeTree(parent)

        // This is just to double-check everything is working correctly
        val retrievedParent = retrieve(parent.id!!, retrievalMode = RetrievalMode.SINGLE_NODE)
        require(retrievedParent.getChildren(containment).size == (containmentIndex + 1)) {
            "Actual retrieved parent: $retrievedParent"
        }
    }

    /**
     * This operation is not atomic. We hope that no one is changing the parent at the very
     * same time.
     */
    fun appendTree(
        treeToAppend: Node,
        containerId: String,
        containmentName: String,
    ) {
        // TODO avoid retrieving the whole parent (just do level 1)
        // 1. Retrieve the parent

        val parent = retrieve(containerId, retrievalMode = RetrievalMode.SINGLE_NODE, withProxyParent = true)

        // 2. Add the tree to the parent
        val containment =
            parent.classifier.getContainmentByName(containmentName)
                ?: throw IllegalArgumentException("The container has not containment named $containmentName")
        if (!containment.isMultiple && parent.getChildrenByContainmentName(containmentName).isNotEmpty()) {
            throw IllegalArgumentException("The indicated containment is not multiple and a child is already present")
        }
        parent.addChild(containment, treeToAppend)
        (treeToAppend as DynamicNode).parent = parent

        (parent as DynamicNode).parent = getParentId(parent.id!!)?.let { ProxyNode(it) }

        // 3. Store the parent
        storeTree(parent)
    }

    fun setReferences(
        targets: List<Node>,
        container: Node,
        reference: KProperty<*>,
    ) {
        setReferences(targets.map { it.id!! }, container.id!!, reference.name)
    }

    fun setReferences(
        targetIDs: List<String>,
        containerID: String,
        referenceName: String,
    ) {
        // 1. Retrieve the referrer
        val referrer = retrieve(containerID, withProxyParent = true, retrievalMode = RetrievalMode.SINGLE_NODE)

        // 2. Add the reference to the referrer
        val reference =
            referrer.classifier.getReferenceByName(referenceName)
                ?: throw IllegalArgumentException("The referrer has not containment named $referenceName")
        if (reference.isMultiple) {
            throw IllegalArgumentException("The indicated reference ${reference.name} is multiple")
        }
        referrer.setReferenceValues(reference, targetIDs.map { ReferenceValue(ProxyNode(it), null) })

        // 3. Store the parent
        storeTree(referrer)
    }

    fun setSingleReference(
        target: Node,
        container: Node,
        reference: KProperty<*>,
    ) {
        setSingleReference(target.id!!, container.id!!, reference.name)
    }

    fun setSingleReference(
        targetId: String?,
        containerId: String,
        referenceName: String,
    ) {
        setReferences(if (targetId == null) emptyList() else listOf(targetId), containerId, referenceName)
    }

    fun addReference(
        target: Node,
        container: Node,
        reference: KProperty<*>,
    ) {
        addReference(target, container, reference.name)
    }

    fun addReference(
        target: Node,
        container: Node,
        referenceName: String,
    ) {
        val updatedContainer = retrieve(container.id!!, withProxyParent = true, RetrievalMode.SINGLE_NODE)
        val currentReferenceValues = updatedContainer.getReferenceValueByName(referenceName)
        val updateReferenceValues =
            currentReferenceValues.toMutableList().apply {
                add(ReferenceValue(target, null))
            }
        updatedContainer.setReferenceValuesByName(referenceName, updateReferenceValues)
        storeTree(updatedContainer)
    }

    fun setProperty(
        node: Node,
        propertyName: String,
        value: Any?,
    ) {
        setProperty(node.id!!, propertyName, value)
    }

    fun setProperty(
        node: Node,
        property: KProperty<*>,
        value: Any?,
    ) {
        setProperty(node.id!!, property.name, value)
    }

    fun setProperty(
        nodeId: String,
        propertyName: String,
        value: Any?,
    ) {
        val updatedNode = retrieve(nodeId, withProxyParent = true, RetrievalMode.SINGLE_NODE)
        updatedNode.setPropertyValueByName(propertyName, value)
        storeTree(updatedNode)
    }

    fun nodesByClassifier(limit: Int? = null): Map<ClassifierKey, ClassifierResult> {
        val url = "http://$hostname:$port/inspection/nodesByClassifier"
        val urlBuilder = url.toHttpUrlOrNull()!!.newBuilder()
        if (limit != null) {
            urlBuilder.addQueryParameter("limit", limit.toString())
        }
        val request: Request =
            Request.Builder()
                .url(urlBuilder.build())
                .considerAuthenticationToken()
                .get()
                .build()
        OkHttpClient().newCall(request).execute().use { response ->
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

    fun childrenInContainment(
        containerId: String,
        containmentName: String,
    ): List<String> {
        val lwNode = retrieve(containerId, retrievalMode = RetrievalMode.SINGLE_NODE)
        val containment = lwNode.classifier.getContainmentByName(containmentName) ?: throw java.lang.IllegalStateException()
        return lwNode.getChildren(containment).map { it.id!! }
    }

    fun clearContainment(
        containerId: String,
        containmentName: String,
    ) {
        val lwNode = retrieve(containerId, retrievalMode = RetrievalMode.SINGLE_NODE)
        val containment = lwNode.classifier.getContainmentByName(containmentName) ?: throw java.lang.IllegalStateException()
        // We make a copy to avoid concurrent modifications
        val children = LinkedList(lwNode.getChildren(containment))
        children.forEach {
            lwNode.removeChild(it)
        }
        storeTree(lwNode)
    }

    // Private methods

    private var httpClient: OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(
                callTimeoutInSeconds,
                TimeUnit.SECONDS,
            ).readTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .writeTimeout(callTimeoutInSeconds, TimeUnit.SECONDS)
            .connectTimeout(connectTimeOutInSeconds, TimeUnit.SECONDS).build()
    private val languages = mutableListOf<Language>()

    private fun log(message: String) {
        if (debug) {
            println(message)
        }
    }

    private fun treeStoringOperation(
        node: Node,
        operation: String,
    ) {
        if (debug) {
            try {
                treeSanityChecks(node, jsonSerialization = jsonSerialization)
            } catch (e: RuntimeException) {
                throw RuntimeException("Failed to store tree $node", e)
            }
        }

        fun verifyNode(node: Node) {
            require(node.id != null) { "Node $node should not have a null ID" }
            if (node !is ProxyNode) {
                if (node.children.any { it == null }) {
                    throw java.lang.IllegalStateException("Node $node has a null child")
                }
                node.children.forEach {
                    verifyNode(it)
                }
            }
        }

        verifyNode(node)

        val json = jsonSerialization.serializeTreesToJsonString(node)
        debugFile("sent.json") { json }

        val body: RequestBody = json.compress()

        // TODO control with flag http or https
        val url = "http://$hostname:$port/bulk/$operation"
        val request: Request =
            Request.Builder()
                .url(url)
                .considerAuthenticationToken()
                .addHeader("Content-Encoding", "gzip")
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

    private fun debugFile(
        relativePath: String,
        text: () -> String,
    ) {
        debugFileHelper(debug, relativePath, text)
    }

    private fun <T> processChunkResponse(
        data: String,
        chunkProcessor: (JsonElement) -> T,
    ): T {
        val json = JsonParser.parseString(data).asJsonObject
        val success = json.get("success").asBoolean
        val messages = json.get("messages").asJsonArray
        if (!messages.isEmpty) {
            log("Messages received: $messages")
        }
        if (!success) {
            throw RuntimeException("Request failed. Messages: $messages")
        }
        val chunkJson = json.get("chunk")
        return chunkProcessor.invoke(chunkJson)
    }
}
