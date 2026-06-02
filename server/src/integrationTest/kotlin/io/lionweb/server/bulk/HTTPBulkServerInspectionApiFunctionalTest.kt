package io.lionweb.server.bulk

import io.lionweb.LionWebVersion
import io.lionweb.client.LionWebBulkClient
import io.lionweb.client.api.ClassifierKey
import io.lionweb.client.api.ClassifierResult
import io.lionweb.client.api.HistorySupport
import io.lionweb.client.api.RepositoryConfiguration
import io.lionweb.model.ClassifierInstanceUtils
import io.lionweb.model.impl.DynamicNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HTTPBulkServerInspectionApiFunctionalTest : AbstractHTTPBulkServerFunctionalTest() {
    @Test
    fun nodesByClassifier() {
        val repoName = "repo_nodesByClassifier"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED),
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Get data with empty repository
        val res1 = client.nodesByClassifier()
        assertEquals(emptyMap<Any, Any>(), res1)

        // Add nodes
        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(p1))

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)
        client.store(listOf(p1))

        // Get data after insertion
        val res2 = client.nodesByClassifier()
        val exp2 = mutableMapOf<ClassifierKey, ClassifierResult>()
        exp2[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesPartition.key!!,
            ),
        ] = ClassifierResult(setOf("p1"), 1)
        exp2[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesFile.key!!,
            ),
        ] = ClassifierResult(setOf("f1", "f2"), 2)
        assertEquals(exp2, res2)
    }

    @Test
    fun nodesByClassifierWithLimit() {
        val repoName = "repo_nodesByClassifierWithLimit"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED),
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Get data with empty repository
        val res1 = client.nodesByClassifier(1)
        assertEquals(emptyMap<Any, Any>(), res1)

        // Add nodes
        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(p1))

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)
        client.store(listOf(p1))

        // Get data after insertion
        val res2a = client.nodesByClassifier(1)
        val exp2a = mutableMapOf<ClassifierKey, ClassifierResult>()
        exp2a[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesPartition.key!!,
            ),
        ] = ClassifierResult(setOf("p1"), 1)
        exp2a[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesFile.key!!,
            ),
        ] = ClassifierResult(setOf("f1"), 2)
        assertEquals(exp2a, res2a)

        val res2b = client.nodesByClassifier(2)
        val exp2b = mutableMapOf<ClassifierKey, ClassifierResult>()
        exp2b[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesPartition.key!!,
            ),
        ] = ClassifierResult(setOf("p1"), 1)
        exp2b[
            ClassifierKey(
                PropertiesLanguage.propertiesLanguage.key!!,
                PropertiesLanguage.propertiesFile.key!!,
            ),
        ] = ClassifierResult(setOf("f1", "f2"), 2)
        assertEquals(exp2b, res2b)
    }

    @Test
    fun nodesByLanguage() {
        val repoName = "repo_nodesByLanguage"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED),
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Get data with empty repository
        val res1 = client.nodesByLanguage()
        assertEquals(emptyMap<Any, Any>(), res1)

        // Add nodes
        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(p1))

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)
        client.store(listOf(p1))

        // Get data after insertion
        val res2 = client.nodesByLanguage()
        val exp2 = mutableMapOf<String, ClassifierResult>()
        exp2[PropertiesLanguage.propertiesLanguage.key!!] =
            ClassifierResult(setOf("p1", "f1", "f2"), 3)
        assertEquals(exp2, res2)
    }

    @Test
    fun nodesByLanguageWithLimit() {
        val repoName = "repo_nodesByLanguageWithLimit"
        val client = LionWebBulkClient(LionWebVersion.v2023_1, "localhost", serverPort, repoName)
        client.createRepository(
            RepositoryConfiguration(repoName, LionWebVersion.v2023_1, HistorySupport.DISABLED),
        )
        client.jsonSerialization.registerLanguage(PropertiesLanguage.propertiesLanguage)

        // Get data with empty repository
        val res1 = client.nodesByLanguage(1)
        assertEquals(emptyMap<Any, Any>(), res1)

        // Add nodes
        val p1 = DynamicNode("p1", PropertiesLanguage.propertiesPartition)
        client.rawCreatePartitions(client.jsonSerialization.serializeNodesToJsonString(p1))

        val f1 = DynamicNode("f1", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f1, "path", "my-path-1.txt")
        val f2 = DynamicNode("f2", PropertiesLanguage.propertiesFile)
        ClassifierInstanceUtils.setPropertyValueByName(f2, "path", "my-path-2.txt")
        ClassifierInstanceUtils.addChild(p1, "files", f1)
        ClassifierInstanceUtils.addChild(p1, "files", f2)
        client.store(listOf(p1))

        // Get data after insertion
        val res2a = client.nodesByLanguage(1)
        val exp2a = mutableMapOf<String, ClassifierResult>()
        exp2a[PropertiesLanguage.propertiesLanguage.key!!] =
            ClassifierResult(setOf("p1"), 3)
        assertEquals(exp2a, res2a)

        val res2b = client.nodesByLanguage(2)
        val exp2b = mutableMapOf<String, ClassifierResult>()
        exp2b[PropertiesLanguage.propertiesLanguage.key!!] =
            ClassifierResult(setOf("p1", "f1"), 3)
        assertEquals(exp2b, res2b)

        val res2c = client.nodesByLanguage(3)
        val exp2c = mutableMapOf<String, ClassifierResult>()
        exp2c[PropertiesLanguage.propertiesLanguage.key!!] =
            ClassifierResult(setOf("p1", "f1", "f2"), 3)
        assertEquals(exp2c, res2c)
    }
}
