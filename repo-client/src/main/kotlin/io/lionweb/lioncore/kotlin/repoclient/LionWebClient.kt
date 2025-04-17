package io.lionweb.lioncore.kotlin.repoclient

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.lionweb.lioncore.java.LionWebVersion
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.model.AnnotationInstance
import io.lionweb.lioncore.java.model.ClassifierInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.model.impl.ProxyNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
import io.lionweb.lioncore.java.serialization.SerializationProvider
import io.lionweb.lioncore.kotlin.MetamodelRegistry
import io.lionweb.lioncore.kotlin.children
import io.lionweb.lioncore.kotlin.getChildrenByContainmentName
import io.lionweb.lioncore.kotlin.getReferenceValueByName
import io.lionweb.lioncore.kotlin.setPropertyValueByName
import io.lionweb.lioncore.kotlin.setReferenceValuesByName
import io.lionweb.repoclient.ExtendedLionWebRepoClient
import io.lionweb.repoclient.api.HistorySupport
import io.lionweb.repoclient.api.RepositoryConfiguration
import io.lionweb.serialization.extensions.NodeInfo
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
    val lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
) {
    // Fields
    private val languages = mutableListOf<Language>()

    @Deprecated("We should use jRepoClient instead")
    private val serializationDecorators = mutableListOf<SerializationDecorator>()

    @Deprecated("We should use jRepoClient instead")
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
            lionWebVersion = lionWebVersion,
        )

    private val jRepoClient =
        ExtendedLionWebRepoClient(
            lionWebVersion,
            hostname,
            port,
            authorizationToken,
            clientID,
            repository,
            connectTimeOutInSeconds,
            callTimeoutInSeconds,
        )

    /**
     * Exposed for testing purposes
     */
    @Deprecated("We should use jRepoClient instead")
    val defaultJsonSerialization =
        SerializationProvider.getStandardJsonSerialization(lionWebVersion).apply {
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

    @Deprecated("We should use jRepoClient instead")
    var jsonSerialization: JsonSerialization = calculateJsonSerialization()
        private set

    // Configuration

    @Deprecated("We should use jRepoClient instead")
    private fun calculateJsonSerialization(): JsonSerialization {
        val jsonSerialization = jsonSerializationProvider?.invoke() ?: defaultJsonSerialization
        serializationDecorators.forEach { serializationDecorator -> serializationDecorator.invoke(jsonSerialization) }
        return jsonSerialization
    }

    @Deprecated("We should use jRepoClient instead")
    fun updateJsonSerialization() {
        this.jsonSerialization = calculateJsonSerialization()
    }

    fun registerLanguage(language: Language) {
        languages.add(language)
        jRepoClient.jsonSerialization.registerLanguage(language)
    }

    fun registerSerializationDecorator(decorator: SerializationDecorator) {
        serializationDecorators.add(decorator)
    }

    @Deprecated("We should use jRepoClient instead")
    fun cleanSerializationDecorators() {
        serializationDecorators.clear()
    }

    // Setup

    fun createRepository(
        name: String,
        lionWebVersion: LionWebVersion,
        history: Boolean = false,
    ) {
        jRepoClient.createRepository(RepositoryConfiguration(name, lionWebVersion, HistorySupport.fromBoolean(history)))
    }

    fun deleteRepository(repositoryName: String) {
        jRepoClient.deleteRepository(repositoryName)
    }

    // Partitions

    fun createPartition(node: Node) {
        if (node.children.isNotEmpty()) {
            throw IllegalArgumentException("When creating a partition, please specify a single node")
        }
        jRepoClient.createPartition(node)
    }

    fun deletePartition(node: Node) {
        deletePartition(node.id ?: throw IllegalStateException("Node ID not specified"))
    }

    fun deletePartition(nodeID: String) {
        jRepoClient.deletePartitions(listOf(nodeID))
    }

    fun getPartitionIDs(): List<String> {
        return jRepoClient.listPartitionsIDs()
    }

    // Nodes

    fun retrieve(
        rootId: String,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): Node {
        require(rootId.isNotBlank())
        val result = retrieve(listOf(rootId), retrievalMode).filter { it.id == rootId }
        require(result.size == 1) {
            "Got ${result.size} nodes"
        }
        val value = result.first()
        require(value !is ProxyNode) {
            "retrieve should produce a full node and not a Proxy Node"
        }
        return value
    }

    fun retrieve(
        rootIds: List<String>,
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
        return jRepoClient.retrieve(rootIds, limit)
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

    fun <T : ClassifierInstance<*>> retrieveAncestor(
        node: ClassifierInstance<*>,
        ancestorClass: KClass<*>,
        retrievalMode: RetrievalMode = RetrievalMode.ENTIRE_SUBTREE,
    ): T? {
        if (node.parent == null) {
            return null
        } else {
            var parent = node.parent
            if (node.parent is ProxyNode) {
                parent = retrieve(node.parent.id!!, retrievalMode = RetrievalMode.SINGLE_NODE)
            }
            return if (ancestorClass.isInstance(parent)) {
                if (retrievalMode == RetrievalMode.SINGLE_NODE) {
                    parent as T
                } else {
                    retrieve(node.parent.id!!, retrievalMode = RetrievalMode.ENTIRE_SUBTREE) as T
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

    fun listRepositiories(): Set<RepositoryConfiguration> {
        return jRepoClient.listRepositories()
    }

    fun storeTree(node: Node) {
        jRepoClient.store(node)
    }

    fun storeTrees(nodes: List<Node>) {
        jRepoClient.store(nodes)
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
        val target = retrieve(targetId, retrievalMode = RetrievalMode.SINGLE_NODE)
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

        val parent = retrieve(containerId, retrievalMode = RetrievalMode.SINGLE_NODE)

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
        val referrer = retrieve(containerID, retrievalMode = RetrievalMode.SINGLE_NODE)

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
        val referrers = retrieve(data.map { it.containerID }, retrievalMode = RetrievalMode.SINGLE_NODE)

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
        storeTrees(referrers)
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
        val updatedContainer = retrieve(container.id!!, RetrievalMode.SINGLE_NODE)
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
        val updatedNode = retrieve(nodeId, RetrievalMode.SINGLE_NODE)
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
        return jRepoClient.getNodeTree(nodeIDs, depthLimit)
    }

    // Private methods

    private fun log(message: String) {
        if (debug) {
            println(message)
        }
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
