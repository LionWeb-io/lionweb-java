package io.lionweb.lioncore.kotlin.repoclient

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.model.AnnotationInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.model.impl.ProxyNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
import io.lionweb.lioncore.java.serialization.LowLevelJsonSerialization
import io.lionweb.lioncore.java.serialization.SerializationProvider
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy
import io.lionweb.lioncore.kotlin.MetamodelRegistry
import io.lionweb.lioncore.kotlin.children
import io.lionweb.lioncore.kotlin.getChildrenByContainmentName
import io.lionweb.lioncore.kotlin.getReferenceValueByName
import io.lionweb.lioncore.kotlin.setPropertyValueByName
import io.lionweb.lioncore.kotlin.setReferenceValuesByName
import io.lionweb.serialization.extensions.BulkImport
import io.lionweb.serialization.extensions.ExtraFlatBuffersSerialization
import io.lionweb.serialization.extensions.ExtraProtoBufSerialization
import java.util.LinkedList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

// This number must be lower than Number.MAX_SAFE_INTEGER, or the LionWeb Repo would crash
// Number.MAX_SAFE_INTEGER = 9,007,199,254,740,991
// Integer.MAX_VALUE       =         2,147,483,647
val MAX_DEPTH = Integer.MAX_VALUE

typealias SerializationDecorator = (JsonSerialization) -> Unit

class LionWebClient(
    val hostname: String = "localhost",
    val port: Int = 3005,
    val debug: Boolean = false,
    val jsonSerializationProvider: (() -> JsonSerialization)? = null,
    val connectTimeOutInSeconds: Long = 60,
    val callTimeoutInSeconds: Long = 60,
    val authorizationToken: String? = null,
    val clientID: String = "GenericKotlinBasedLionWebClient",
    val repository: String = "default",
) {
    // Fields
    private val languages = mutableListOf<Language>()
    private val serializationDecorators = mutableListOf<SerializationDecorator>()

    private val lowLevelRepoClient =
        LowLevelRepoClient(
            hostname = hostname,
            port = port,
            authorizationToken = authorizationToken,
            clientID = clientID,
            repository = repository,
            connectTimeOutInSeconds = connectTimeOutInSeconds,
            callTimeoutInSeconds = callTimeoutInSeconds,
            debug = debug,
        )

    /**
     * Exposed for testing purposes
     */
    val defaultJsonSerialization =
        SerializationProvider.getStandardJsonSerialization().apply {
            enableDynamicNodes()
        }

    init {
        registerSerializationDecorator { jsonSerialization ->
            languages.forEach {
                jsonSerialization.registerLanguage(it)
            }
            MetamodelRegistry.prepareJsonSerialization(jsonSerialization)
        }
    }

    var jsonSerialization: JsonSerialization = calculateJsonSerialization()
        private set

    // Configuration

    private fun calculateJsonSerialization(): JsonSerialization {
        val jsonSerialization = jsonSerializationProvider?.invoke() ?: defaultJsonSerialization
        serializationDecorators.forEach { serializationDecorator -> serializationDecorator.invoke(jsonSerialization) }
        return jsonSerialization
    }

    fun updateJsonSerialization() {
        this.jsonSerialization = calculateJsonSerialization()
    }

    fun registerLanguage(language: Language) {
        languages.add(language)
    }

    fun registerSerializationDecorator(decorator: SerializationDecorator) {
        serializationDecorators.add(decorator)
    }

    fun cleanSerializationDecorators() {
        serializationDecorators.clear()
    }

    // Setup

    fun createRepository(history: Boolean = false) {
        lowLevelRepoClient.createRepository(history)
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
        lowLevelRepoClient.deletePartition(nodeID)
    }

    fun getPartitionIDs(): List<String> {
        val data = lowLevelRepoClient.getPartitionIDs()
        return processChunkResponse(data) {
            val chunk = LowLevelJsonSerialization().deserializeSerializationBlock(it)
            chunk.classifierInstances.mapNotNull { it.id }
        }
    }

    // Nodes

    fun retrieve(
        rootId: String,
        withProxyParent: Boolean = false,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): Node {
        require(rootId.isNotBlank())
        val result = retrieve(listOf(rootId), withProxyParent, retrievalMode)
        require(result.size == 1)
        val value = result.first()
        require(value !is ProxyNode) {
            "retrieve should produce a full node and not a Proxy Node"
        }
        return value
    }

    fun retrieve(
        rootIds: List<String>,
        withProxyParent: Boolean = false,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): List<Node> {
        if (rootIds.isEmpty()) {
            return emptyList()
        }
        val limit =
            when (retrievalMode) {
                RetrievalMode.SINGLE_NODE -> 0
                RetrievalMode.ENTIRE_SUBTREE -> MAX_DEPTH
            }
        val data = lowLevelRepoClient.retrieve(rootIds, limit)

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

        val data = lowLevelRepoClient.retrieve(listOf(nodeID), limit = 0)
        return processChunkResponse(data) { chunk ->
            val nodes = chunk.asJsonObject.get("nodes").asJsonArray
            !nodes.isEmpty
        }
    }

    fun getParentId(nodeID: String): String? {
        require(nodeID.isNotBlank())
        val data = lowLevelRepoClient.retrieve(listOf(nodeID), limit = 0)
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
    }

    fun storeTree(node: Node) {
        treeStoringOperation(node, "store")
    }

    fun storeNodes(nodes: List<Node>) {
        nodesStoringOperation(nodes, "store")
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
    fun appendAnnotation(
        annotationInstance: AnnotationInstance,
        targetId: String,
    ) {
        val target = retrieve(targetId, retrievalMode = RetrievalMode.SINGLE_NODE, withProxyParent = true)
        require(target.children.all { it is ProxyNode })
        target.addAnnotation(annotationInstance)
        storeTree(target)
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

    data class ReferenceData(
        val targetIDs: List<String>,
        val containerID: String,
        val referenceName: String,
    )

    /**
     * This is useful to set multiple references at once
     */
    fun setReferences(data: List<ReferenceData>) {
        if (data.isEmpty()) {
            return
        }
        // 1. Retrieve the referrer
        val referrers = retrieve(data.map { it.containerID }, withProxyParent = true, retrievalMode = RetrievalMode.SINGLE_NODE)

        // 2. Add the reference to the referrer
        data.mapIndexed { index, referenceData ->
            val referrer = referrers[index]
            val referenceName = referenceData.referenceName
            val targetIDs = referenceData.targetIDs
            val reference =
                referrer.classifier.getReferenceByName(referenceName)
                    ?: throw IllegalArgumentException("The referrer has not containment named $referenceName")
            if (reference.isMultiple) {
                throw IllegalArgumentException("The indicated reference ${reference.name} is multiple")
            }
            referrer.setReferenceValues(reference, targetIDs.map { ReferenceValue(ProxyNode(it), null) })
        }

        // 3. Store the referrers
        storeNodes(referrers)
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
        return lowLevelRepoClient.nodesByClassifier(limit = limit)
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

    // Additional APIs

    fun nodeTree(
        nodeID: String,
        depthLimit: Int? = null,
    ): List<NodeInfo> {
        return nodeTree(listOf(nodeID), depthLimit = depthLimit)
    }

    fun nodeTree(
        nodeIDs: List<String>,
        depthLimit: Int? = null,
    ): List<NodeInfo> {
        return lowLevelRepoClient.nodeTree(nodeIDs, depthLimit)
    }

    private fun bulkImportUsingJson(
        bulkImport: BulkImport,
        compress: Boolean = false,
    ) {
        val body = JsonObject()
        val bodyAttachPoints = JsonArray()
        bulkImport.attachPoints.forEach { attachPoint ->
            val jContainment = JsonObject()
            jContainment.addProperty("language", attachPoint.containment.language)
            jContainment.addProperty("version", attachPoint.containment.version)
            jContainment.addProperty("key", attachPoint.containment.key)

            val jEl = JsonObject()
            jEl.addProperty("container", attachPoint.container)
            jEl.addProperty("root", attachPoint.rootId)
            jEl.add("containment", jContainment)
            bodyAttachPoints.add(jEl)
        }
        val bodyNodes = jsonSerialization.serializeNodesToJsonElement(bulkImport.nodes).asJsonObject.get("nodes").asJsonArray
        body.add("attachPoints", bodyAttachPoints)
        body.add("nodes", bodyNodes)
        val bodyJson = Gson().toJson(body)
        return lowLevelRepoClient.bulkImportUsingJson(bodyJson, compress = compress)
    }

    private fun bulkImportUsingProtobuf(
        bulkImport: BulkImport,
        compress: Boolean = false,
    ) {
        val bytes =
            ExtraProtoBufSerialization().apply {
                this.unavailableChildrenPolicy = jsonSerialization.unavailableChildrenPolicy
                this.unavailableParentPolicy = jsonSerialization.unavailableParentPolicy
                this.unavailableReferenceTargetPolicy = jsonSerialization.unavailableReferenceTargetPolicy
                this.classifierResolver = jsonSerialization.classifierResolver
                this.instanceResolver = jsonSerialization.instanceResolver
                this.instantiator = jsonSerialization.instantiator
                this.primitiveValuesSerialization = jsonSerialization.primitiveValuesSerialization
            }.serializeBulkImport(bulkImport).toByteArray()
        lowLevelRepoClient.bulkImportUsingProtobuf(bytes, compress = compress)
    }

    private fun bulkImportUsingFlatBuffers(
        bulkImport: BulkImport,
        compress: Boolean = false,
    ) {
        val bytes =
            ExtraFlatBuffersSerialization().apply {
                this.unavailableChildrenPolicy = jsonSerialization.unavailableChildrenPolicy
                this.unavailableParentPolicy = jsonSerialization.unavailableParentPolicy
                this.unavailableReferenceTargetPolicy = jsonSerialization.unavailableReferenceTargetPolicy
                this.classifierResolver = jsonSerialization.classifierResolver
                this.instanceResolver = jsonSerialization.instanceResolver
                this.instantiator = jsonSerialization.instantiator
                this.primitiveValuesSerialization = jsonSerialization.primitiveValuesSerialization
            }.serializeBulkImport(bulkImport)
        lowLevelRepoClient.bulkImportUsingFlatBuffers(bytes, compress = compress)
    }

    fun bulkImport(
        bulkImport: BulkImport,
        transferFormat: TransferFormat = TransferFormat.FLATBUFFERS,
        compress: Boolean = false,
    ) {
        when (transferFormat) {
            TransferFormat.JSON -> bulkImportUsingJson(bulkImport, compress)
            TransferFormat.PROTOBUF -> bulkImportUsingProtobuf(bulkImport, compress)
            TransferFormat.FLATBUFFERS -> bulkImportUsingFlatBuffers(bulkImport, compress)
        }
    }

    // Private methods

    private fun log(message: String) {
        if (debug) {
            println(message)
        }
    }

    private fun nodesStoringOperation(
        nodes: List<Node>,
        operation: String,
    ) {
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

        nodes.forEach { node -> verifyNode(node) }

        val json = jsonSerialization.serializeNodesToJsonString(nodes)

        lowLevelRepoClient.nodesStoringOperation(json, operation)
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
                if (node.classifier == null) {
                    throw IllegalStateException("Node $node has no classifier")
                }
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

        lowLevelRepoClient.nodesStoringOperation(json, operation)
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

data class NodeInfo(val id: String, val parent: String?, val depth: Int)

enum class TransferFormat {
    JSON,
    PROTOBUF,
    FLATBUFFERS,
}
