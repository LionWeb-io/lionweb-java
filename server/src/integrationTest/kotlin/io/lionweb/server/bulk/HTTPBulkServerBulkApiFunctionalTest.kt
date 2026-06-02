package io.lionweb.server.bulk

import io.lionweb.LionWebVersion
import io.lionweb.client.LionWebBulkClient
import io.lionweb.client.api.HistorySupport
import io.lionweb.client.api.RepositoryConfiguration
import io.lionweb.model.ClassifierInstanceUtils
import io.lionweb.model.impl.DynamicNode
import io.lionweb.serialization.data.SerializationChunk
import io.lionweb.utils.IdUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class HTTPBulkServerBulkApiFunctionalTest : AbstractHTTPBulkServerFunctionalTest() {

    @Test
    fun noPartitionsOnNewModelRepository() {
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, "default")
        val partitions = client.listPartitions()
        assertEquals(emptyList<Any>(), partitions)
    }

    @Test
    fun partitionsCRUD() {
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, "default")
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Create partition
        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(f1))

        // Check list
        val nodes1 = client.listPartitions()
        assertEquals(1, nodes1.size)
        assertEquals("f1", nodes1[0].id)
        assertEquals(PropertiesLanguage.propertiesPartition, nodes1[0].classifier)
        assertEquals(listOf("f1"), client.listPartitionsIDs())

        // Create partitions
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesPartition)
        val f3 = DynamicNode("f3", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(f2, f3))

        // Check list
        val nodes2 = client.listPartitions()
        assertEquals(3, nodes2.size)
        assertEquals(
            setOf("f1", "f2", "f3"),
            client.listPartitionsIDs().toSet()
        )

        // Delete partitions
        client.deletePartitions(listOf("f1", "f3"))

        // Check list
        val nodes3 = client.listPartitions()
        assertEquals(1, nodes3.size)
        assertEquals("f2", nodes3[0].id)
        assertEquals(PropertiesLanguage.propertiesPartition, nodes3[0].classifier)
        assertEquals(listOf("f2"), client.listPartitionsIDs())

        // Delete partition
        client.deletePartitions(listOf("f2"))

        // Check list
        val nodes4 = client.listPartitions()
        assertEquals(0, nodes4.size)
        assertEquals(emptyList<Any>(), client.listPartitionsIDs())
    }

    @Test
    fun partitionsCRUDUsingChunkLevelAPIs() {
        val repoName = "repo_partitionsCRUDUsingChunkLevelAPIs"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED)
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Create partition
        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesPartition)
        client.createPartitionsFromChunk(
            client.jsonSerialization.serializeNodesToSerializationChunk(f1).classifierInstances
        )

        // Check list
        val nodes1 = client.listPartitions()
        assertEquals(1, nodes1.size)
        assertEquals("f1", nodes1[0].id)
        assertEquals(PropertiesLanguage.propertiesPartition, nodes1[0].classifier)
        assertEquals(listOf("f1"), client.listPartitionsIDs())

        // Create partitions
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesPartition)
        val f3 = DynamicNode("f3", PropertiesLanguage.propertiesPartition)
        client.createPartitionsFromChunk(
            client.jsonSerialization.serializeNodesToSerializationChunk(f2, f3).classifierInstances
        )

        // Check list
        val nodes2 = client.listPartitions()
        assertEquals(3, nodes2.size)
        assertEquals(
            setOf("f1", "f2", "f3"),
            client.listPartitionsIDs().toSet()
        )
    }

    @Test
    fun storeOnCustomRepository() {
        val repoName = "my_repo"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED)
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Create partition
        val partition = DynamicNode("partition", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(partition))

        // Check list
        val nodes1 = client.listPartitions()
        assertEquals(1, nodes1.size)
        assertEquals("partition", nodes1[0].id)
        assertEquals(PropertiesLanguage.propertiesPartition, nodes1[0].classifier)
        assertEquals(listOf("partition"), client.listPartitionsIDs())

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.addChild(partition, "files", f1)

        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.addChild(partition, "files", f2)

        client.store(partition)

        val retrievedPartition = client.retrieve(partition.id!!)
        assertEquals(partition, retrievedPartition)
    }

    @Test
    fun ids() {
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, "default")

        val ids1 = client.ids(78)
        assertEquals(78, ids1.size)
        assertTrue(ids1.all { IdUtils.isValidID(it) })

        val ids2 = client.ids(0)
        assertEquals(0, ids2.size)

        val ids3 = client.ids(1)
        assertEquals(1, ids3.size)
        assertTrue(ids3.all { IdUtils.isValidID(it) })
    }

    @Test
    fun storeAndRetrieve() {
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, "default")
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(p1))

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)

        client.store(listOf(p1))

        val retrievedNodes1 = client.retrieve(listOf("p1"), 10)
        assertEquals(1, retrievedNodes1.size)
        assertEquals(p1, retrievedNodes1[0])
    }

    @Test
    fun storeAtChunkLevelAndRetrieve() {
        val repoName = "repo_storeAtChunkLevelAndRetrieve"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED)
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.createPartitionsFromChunk(
            client.jsonSerialization.serializeTreeToSerializationChunk(p1).classifierInstances
        )

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)

        client.storeChunk(client.jsonSerialization.serializeTreeToSerializationChunk(p1).classifierInstances)

        val retrievedNodes1Chunk =
            client.retrieveAsChunk(listOf("p1"), 0)
        assertEquals(1, retrievedNodes1Chunk.size)
        val retrievedNodes1 =
            client.jsonSerialization
                .deserializeSerializationChunk(
                    SerializationChunk.fromNodes(LionWebVersion.v2023_1, retrievedNodes1Chunk)
                )
        assertEquals(1, retrievedNodes1.size)
        assertEquals(p1, retrievedNodes1[0])
    }
}
