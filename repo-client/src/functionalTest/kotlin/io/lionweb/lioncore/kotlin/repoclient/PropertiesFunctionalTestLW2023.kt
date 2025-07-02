package io.lionweb.lioncore.kotlin.repoclient

import io.lionweb.LionWebVersion
import io.lionweb.client.testing.AbstractClientFunctionalTest
import io.lionweb.lioncore.kotlin.dynamicNode
import io.lionweb.lioncore.kotlin.setPropertyValueByName
import io.lionweb.serialization.extensions.NodeInfo
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Testcontainers
class PropertiesFunctionalTestLW2023 : AbstractClientFunctionalTest(LionWebVersion.v2023_1, true) {
    @BeforeTest
    fun prepare() {
        val client = LionWebClient(port = server!!.firstMappedPort)
        client.deleteRepository("default")
        client.createRepository(name = "default", lionWebVersion = lionWebVersion, history = false)
    }

    @Test
    fun noPartitionsOnNewModelRepository() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        assertEquals(emptyList(), client.getPartitionIDs())
    }

    @Test
    fun isNodeExisting() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)

        assertEquals(false, client.isNodeExisting("pp1"))
        assertEquals(false, client.isNodeExisting("pf1"))
        assertEquals(false, client.isNodeExisting("prop1"))
        assertEquals(false, client.isNodeExisting("prop2"))
        assertEquals(false, client.isNodeExisting("prop3"))

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        client.createPartition(pp1)

        assertEquals(true, client.isNodeExisting("pp1"))
        assertEquals(false, client.isNodeExisting("pf1"))
        assertEquals(false, client.isNodeExisting("prop1"))
        assertEquals(false, client.isNodeExisting("prop2"))
        assertEquals(false, client.isNodeExisting("prop3"))

        val pf =
            propertiesFileLW2023.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            propertyLW2023.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            propertyLW2023.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            propertyLW2023.dynamicNode("prop3").apply {
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
    fun gettingPartionsAfterStoringPartitions() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        client.registerLanguage(propertiesLanguageLW2023)

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        assertEquals(emptyList(), client.getPartitionIDs())
        client.createPartition(pp1)
        assertEquals(listOf("pp1"), client.getPartitionIDs())
        assertEquals(pp1, client.retrieve("pp1"))
    }

    @Test
    fun gettingNodesAfterStoringNodes() {
        val repositoryName = "repo_gettingNodesAfterStoringNodes"
        val client =
            LionWebClient(
                port = server!!.firstMappedPort,
                lionWebVersion = lionWebVersion,
                repository = repositoryName,
            )
        client.createRepository(repositoryName, LionWebVersion.v2023_1, false)
        client.registerLanguage(propertiesLanguageLW2023)

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFileLW2023.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            propertyLW2023.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            propertyLW2023.dynamicNode().apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            propertyLW2023.dynamicNode().apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        val retrieved = client.retrieve("pp1")
        assertEquals(null, retrieved.parent)
        assertEquals("pp1", retrieved.id)
        assertEquals(propertiesPartitionLW2023, retrieved.classifier)
    }

    @Test
    fun getNodesByClassifier() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        client.registerLanguage(propertiesLanguageLW2023)

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFileLW2023.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            propertyLW2023.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            propertyLW2023.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            propertyLW2023.dynamicNode("prop3").apply {
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
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        assertThrows<UnexistingNodeException> { client.getParentId("my-unexistingNode") }
    }

    @Test
    fun getNodesWithProxyParent() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        client.registerLanguage(propertiesLanguageLW2023)

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFileLW2023.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            propertyLW2023.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            propertyLW2023.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            propertyLW2023.dynamicNode("prop3").apply {
                setPropertyValueByName("name", "Prop3")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        client.storeTree(pf)

        val prop3retrievedWithProxyParent = client.retrieve("prop3")
        assertEquals("pf1", prop3retrievedWithProxyParent.parent!!.id)
    }

    @Test
    fun getNodeTree() {
        val client = LionWebClient(port = server!!.firstMappedPort, lionWebVersion = lionWebVersion)
        client.registerLanguage(propertiesLanguageLW2023)

        val pp1 = propertiesPartitionLW2023.dynamicNode("pp1")
        client.createPartition(pp1)

        val pf =
            propertiesFileLW2023.dynamicNode("pf1").apply {
                parent = pp1
            }
        val prop1 =
            propertyLW2023.dynamicNode("prop1").apply {
                setPropertyValueByName("name", "Prop1")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop2 =
            propertyLW2023.dynamicNode("prop2").apply {
                setPropertyValueByName("name", "Prop2")
                pf.addChild(pf.classifier.getContainmentByName("properties")!!, this)
            }
        val prop3 =
            propertyLW2023.dynamicNode("prop3").apply {
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
