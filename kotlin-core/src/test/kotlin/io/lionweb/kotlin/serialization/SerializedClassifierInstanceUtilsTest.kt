package io.lionweb.kotlin.serialization

import io.lionweb.LionWebVersion
import io.lionweb.kotlin.serialization.chunk.getProperty
import io.lionweb.kotlin.serialization.chunk.subchunk
import io.lionweb.lioncore.LionCore
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializationChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedContainmentValue
import io.lionweb.serialization.data.SerializedPropertyValue
import java.util.Arrays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializedClassifierInstanceUtilsTest {
    @Test
    fun getPropertyByName() {
        val sc1 =
            SerializedClassifierInstance().apply {
                id = "language-mylanguage-id"
                classifier = MetaPointer.get("LionCore-M3", "2024.1", "Language")
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Language-version"), "1"),
                )
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"), "language-mylanguage-key"),
                )
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(
                        MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                        "MyLanguage",
                    ),
                )
                unsafeAppendContainmentValue(
                    SerializedContainmentValue(
                        MetaPointer.get(
                            "LionCore-M3",
                            "2024.1",
                            "Language-entities",
                        ),
                        Arrays.asList("mylanguage-MyConcept-id", "mylanguage-MyInterface-id"),
                    ),
                )
            }
        assertEquals("1", sc1.getProperty(LionCore.getLanguage(), "version"))
        assertEquals("language-mylanguage-key", sc1.getProperty(LionCore.getLanguage(), "key"))
        assertEquals("MyLanguage", sc1.getProperty(LionCore.getLanguage(), "name"))
    }

    @Test
    fun getPropertyByMetaPointer() {
        val sc1 =
            SerializedClassifierInstance().apply {
                id = "language-mylanguage-id"
                classifier = MetaPointer.get("LionCore-M3", "2024.1", "Language")
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Language-version"), "1"),
                )
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"), "language-mylanguage-key"),
                )
                unsafeAppendPropertyValue(
                    SerializedPropertyValue.get(
                        MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                        "MyLanguage",
                    ),
                )
                unsafeAppendContainmentValue(
                    SerializedContainmentValue(
                        MetaPointer.get(
                            "LionCore-M3",
                            "2024.1",
                            "Language-entities",
                        ),
                        Arrays.asList("mylanguage-MyConcept-id", "mylanguage-MyInterface-id"),
                    ),
                )
            }
        assertEquals("1", sc1.getProperty(MetaPointer.get("LionCore-M3", "2024.1", "Language-version")))
        assertEquals("language-mylanguage-key", sc1.getProperty(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key")))
        assertEquals("MyLanguage", sc1.getProperty(MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name")))
    }

    // --- subchunk tests ---

    private val classifier = MetaPointer.get("lang", "1.0", "MyClass")
    private val containment = MetaPointer.get("lang", "1.0", "children")

    private fun node(id: String): SerializedClassifierInstance = SerializedClassifierInstance(id, classifier)

    private fun chunkOf(vararg nodes: SerializedClassifierInstance): SerializationChunk =
        SerializationChunk.fromNodes(LionWebVersion.v2024_1, nodes.toList())

    @Test
    fun subchunkOfLeafNodeContainsOnlyThatNode() {
        val leaf = node("leaf")
        val chunk = chunkOf(leaf)

        val result = leaf.subchunk(chunk)

        assertEquals(listOf("leaf"), result.classifierInstances.map { it.id })
    }

    @Test
    fun subchunkPreservesSerializationFormatVersion() {
        val root = node("root")
        val chunk = chunkOf(root)

        val result = root.subchunk(chunk)

        assertEquals(LionWebVersion.v2024_1.versionString, result.serializationFormatVersion)
    }

    @Test
    fun subchunkIncludesDirectChildren() {
        val child = node("child")
        val root = node("root").also { it.addChild(containment, "child") }
        val chunk = chunkOf(root, child)

        val result = root.subchunk(chunk)

        val ids = result.classifierInstances.map { it.id }.toSet()
        assertEquals(setOf("root", "child"), ids)
    }

    @Test
    fun subchunkIncludesGrandchildren() {
        val grandchild = node("grandchild")
        val child = node("child").also { it.addChild(containment, "grandchild") }
        val root = node("root").also { it.addChild(containment, "child") }
        val chunk = chunkOf(root, child, grandchild)

        val result = root.subchunk(chunk)

        val ids = result.classifierInstances.map { it.id }.toSet()
        assertEquals(setOf("root", "child", "grandchild"), ids)
    }

    @Test
    fun subchunkIncludesAnnotations() {
        val annotation = node("ann")
        val root = node("root").also { it.addAnnotation("ann") }
        val chunk = chunkOf(root, annotation)

        val result = root.subchunk(chunk)

        val ids = result.classifierInstances.map { it.id }.toSet()
        assertEquals(setOf("root", "ann"), ids)
    }

    @Test
    fun subchunkOfSubtreeExcludesUnrelatedSiblings() {
        val sibling = node("sibling")
        val child = node("child")
        val root =
            node("root").also {
                it.addChild(containment, "child")
                it.addChild(containment, "sibling")
            }
        val chunk = chunkOf(root, child, sibling)

        val result = child.subchunk(chunk)

        val ids = result.classifierInstances.map { it.id }.toSet()
        assertEquals(setOf("child"), ids)
        assertTrue("sibling" !in ids)
        assertTrue("root" !in ids)
    }

    @Test
    fun subchunkOfChildSubtreeIncludesOnlyItsDescendants() {
        val grandchild = node("grandchild")
        val child = node("child").also { it.addChild(containment, "grandchild") }
        val sibling = node("sibling")
        val root =
            node("root").also {
                it.addChild(containment, "child")
                it.addChild(containment, "sibling")
            }
        val chunk = chunkOf(root, child, grandchild, sibling)

        val result = child.subchunk(chunk)

        val ids = result.classifierInstances.map { it.id }.toSet()
        assertEquals(setOf("child", "grandchild"), ids)
    }
}
