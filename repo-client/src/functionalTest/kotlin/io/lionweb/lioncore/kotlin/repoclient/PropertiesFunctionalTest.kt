package io.lionweb.lioncore.kotlin.repoclient

import io.lionweb.lioncore.kotlin.dynamicNode
import io.lionweb.lioncore.kotlin.setPropertyValueByName
import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest
import io.lionweb.serialization.extensions.NodeInfo
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Testcontainers
class PropertiesFunctionalTest : AbstractRepoClientFunctionalTest() {
    @BeforeTest
    fun prepare() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.deleteRepository("default")
        client.createRepository(name = "default", lionWebVersion = lionWebVersion, history = false)
    }

    @Test
    fun noPartitionsOnNewModelRepository() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        assertEquals(emptyList(), client.getPartitionIDs())
    }

    @Test
    fun isNodeExisting() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        assertEquals(false, client.isNodeExisting("pp1"))
        assertEquals(false, client.isNodeExisting("pf1"))
        assertEquals(false, client.isNodeExisting("prop1"))
        assertEquals(false, client.isNodeExisting("prop2"))
        assertEquals(false, client.isNodeExisting("prop3"))

        val pp1 = propertiesPartition.dynamicNode("pp1")
        client.createPartition(pp1)

        assertEquals(true, client.isNodeExisting("pp1"))
        assertEquals(false, client.isNodeExisting("pf1"))
        assertEquals(false, client.isNodeExisting("prop1"))
        assertEquals(false, client.isNodeExisting("prop2"))
        assertEquals(false, client.isNodeExisting("prop3"))

        val pf =
            propertiesFile.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            property.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            property.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            property.dynamicNode("prop3").apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        assertEquals(true, client.isNodeExisting("pp1"))
        assertEquals(true, client.isNodeExisting("pf1"))
        assertEquals(true, client.isNodeExisting("prop1"))
        assertEquals(true, client.isNodeExisting("prop2"))
        assertEquals(true, client.isNodeExisting("prop3"))

        client.deletePartition("pp1")
        assertEquals(false, client.isNodeExisting("pp1"))
        assertEquals(false, client.isNodeExisting("pf1"))
        assertEquals(false, client.isNodeExisting("prop1"))
        assertEquals(false, client.isNodeExisting("prop2"))
        assertEquals(false, client.isNodeExisting("prop3"))
    }

    @Test
    fun gettingPartitionsAfterStoringPartitions() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.registerLanguage(propertiesLanguage)

        val pp1 = propertiesPartition.dynamicNode("pp1")
        assertEquals(emptyList(), client.getPartitionIDs())
        client.createPartition(pp1)
        assertEquals(listOf("pp1"), client.getPartitionIDs())
        assertEquals(pp1, client.retrieve("pp1"))
    }

    @Test
    fun gettingNodesAfterStoringNodes() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.registerLanguage(propertiesLanguage)

        val pp1 = propertiesPartition.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFile.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            property.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            property.dynamicNode().apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            property.dynamicNode().apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        val retrieved = client.retrieve("pf1")
        assertEquals(null, retrieved.parent)
        assertEquals("pf1", retrieved.id)
        assertEquals(propertiesFile, retrieved.classifier)
    }

    @Test
    fun getNodesByClassifier() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.registerLanguage(propertiesLanguage)

        val pp1 = propertiesPartition.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFile.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            property.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            property.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            property.dynamicNode("prop3").apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        val nodesByClassifier = client.nodesByClassifier()
        assertEquals(
            mapOf(
                ClassifierKey("language-properties-key", "properties-Property-key") to setOf("prop1", "prop2", "prop3"),
                ClassifierKey("language-properties-key", "properties-PropertiesPartition-key") to setOf("pp1"),
                ClassifierKey("language-properties-key", "properties-PropertiesFile-key") to setOf("pf1"),
            ),
            nodesByClassifier.map { it.key to it.value.ids }.toMap(),
        )
    }

    @Test
    fun gettingParentIdOfUnexistingNode() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        assertThrows<UnexistingNodeException> { client.getParentId("my-unexistingNode") }
    }

    @Test
    fun getNodesWithProxyParent() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.registerLanguage(propertiesLanguage)

        val pp1 = propertiesPartition.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFile.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            property.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            property.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            property.dynamicNode("prop3").apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        val prop3retrievedWithoutProxyParent = client.retrieve("prop3", withProxyParent = false)
        assertEquals(null, prop3retrievedWithoutProxyParent.parent)
        val prop3retrievedWithProxyParent = client.retrieve("prop3", withProxyParent = true)
        assertEquals("pf1", prop3retrievedWithProxyParent.parent.id)
    }

    @Test
    fun getNodeTree() {
        val client = LionWebClient(port = modelRepository!!.firstMappedPort)
        client.registerLanguage(propertiesLanguage)

        val pp1 = propertiesPartition.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFile.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            property.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            property.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            property.dynamicNode("prop3").apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        assertEquals(
            listOf(
                NodeInfo("pf1", "pp1", 0),
                NodeInfo("prop1", "pf1", 1),
                NodeInfo("prop2", "pf1", 1),
                NodeInfo("prop3", "pf1", 1),
            ),
            client.nodeTree("pf1"),
        )
        assertEquals(
            listOf(
                NodeInfo("pp1", null, 0),
                NodeInfo("prop1", "pf1", 0),
                NodeInfo("pf1", "pp1", 1),
                NodeInfo("prop1", "pf1", 2),
                NodeInfo("prop2", "pf1", 2),
                NodeInfo("prop3", "pf1", 2),
            ),
            client.nodeTree(listOf("pp1", "prop1")),
        )
    }
}
