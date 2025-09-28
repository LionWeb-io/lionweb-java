package io.lionweb.kotlin.serialization

import io.lionweb.kotlin.Multiplicity
import io.lionweb.kotlin.createConcept
import io.lionweb.kotlin.createReference
import io.lionweb.kotlin.lwLanguage
import io.lionweb.kotlin.serialization.chunk.combine
import io.lionweb.kotlin.serialization.chunk.combineTree
import io.lionweb.kotlin.serialization.chunk.getReferenceValues
import io.lionweb.kotlin.serialization.chunk.getReferenceValuesIDs
import io.lionweb.kotlin.serialization.chunk.root
import io.lionweb.serialization.data.LanguageVersion
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializationChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedContainmentValue
import io.lionweb.serialization.data.SerializedReferenceValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SerializationChunkUtilsTest {
    private fun createTestChunk(): SerializationChunk =
        SerializationChunk().apply {
            serializationFormatVersion = "2024.1"
            addLanguage(LanguageVersion.of("TestLang", "1.0"))

            // Root node
            val rootNode =
                SerializedClassifierInstance().apply {
                    id = "root-id"
                    classifier = MetaPointer.get("TestLang", "1.0", "RootConcept")
                    parentNodeID = null
                }
            addClassifierInstance(rootNode)

            // Child node
            val childNode =
                SerializedClassifierInstance().apply {
                    id = "child-id"
                    classifier = MetaPointer.get("TestLang", "1.0", "ChildConcept")
                    parentNodeID = "root-id"
                }
            addClassifierInstance(childNode)
        }

    private fun createTestConceptWithReference(): Pair<io.lionweb.language.Language, io.lionweb.language.Concept> {
        val language =
            lwLanguage("TestLanguage").apply {
                createConcept("TestConcept").apply {
                    createReference("testReference", this, Multiplicity.OPTIONAL)
                }
            }
        val concept = language.getConceptByName("TestConcept")!!
        return Pair(language, concept)
    }

    @Test
    fun testRootProperty_withRootNode() {
        val chunk = createTestChunk()
        val root = chunk.root

        assertEquals("root-id", root.id)
        assertEquals(null, root.parentNodeID)
    }

    @Test
    fun testRootProperty_withNoRootNode() {
        val chunk =
            SerializationChunk().apply {
                // Add only non-root nodes
                val childNode =
                    SerializedClassifierInstance().apply {
                        id = "child-id"
                        parentNodeID = "some-parent"
                    }
                addClassifierInstance(childNode)
            }

        assertFailsWith<IllegalStateException> {
            chunk.root
        }
    }

    @Test
    fun testCombine_mergesChunks() {
        val chunk1 =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("Lang1", "1.0"))
                val node1 =
                    SerializedClassifierInstance().apply {
                        id = "node1"
                        classifier = MetaPointer.get("Lang1", "1.0", "Concept1")
                    }
                addClassifierInstance(node1)
            }

        val chunk2 =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("Lang2", "1.0"))
                val node2 =
                    SerializedClassifierInstance().apply {
                        id = "node2"
                        classifier = MetaPointer.get("Lang2", "1.0", "Concept2")
                    }
                addClassifierInstance(node2)
            }

        val result = chunk1.combine(chunk2)

        assertEquals(chunk1, result) // Returns the original chunk
        assertEquals(2, result.classifierInstances.size)
        assertTrue(result.classifierInstances.any { it.id == "node1" })
        assertTrue(result.classifierInstances.any { it.id == "node2" })
        assertEquals(2, result.languages.size)
    }

    @Test
    fun testGetReferenceValues_withConcept() {
        val (language, concept) = createTestConceptWithReference()
        val instance =
            SerializedClassifierInstance().apply {
                id = "test-id"
                classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                unsafeAppendReferenceValue(
                    SerializedReferenceValue(
                        MetaPointer.from(concept.getReferenceByName("testReference")),
                        listOf(
                            SerializedReferenceValue.Entry("target1", "resolveInfo1"),
                            SerializedReferenceValue.Entry("target2", "resolveInfo2"),
                        ),
                    ),
                )
            }

        val referenceValues = instance.getReferenceValues(concept, "testReference")

        assertEquals(2, referenceValues.size)
        assertEquals("target1", referenceValues[0].reference)
        assertEquals("resolveInfo1", referenceValues[0].resolveInfo)
        assertEquals("target2", referenceValues[1].reference)
        assertEquals("resolveInfo2", referenceValues[1].resolveInfo)
    }

    @Test
    fun testGetReferenceValuesIDs_withConcept() {
        val (language, concept) = createTestConceptWithReference()
        val instance =
            SerializedClassifierInstance().apply {
                id = "test-id"
                classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                unsafeAppendReferenceValue(
                    SerializedReferenceValue(
                        MetaPointer.from(concept.getReferenceByName("testReference")),
                        listOf(
                            SerializedReferenceValue.Entry("target1", "resolveInfo1"),
                            SerializedReferenceValue.Entry("target2", "resolveInfo2"),
                            SerializedReferenceValue.Entry(null, "resolveInfo3"),
                        ),
                    ),
                )
            }

        val referenceIDs = instance.getReferenceValuesIDs(concept, "testReference")

        assertEquals(3, referenceIDs.size)
        assertEquals("target1", referenceIDs[0])
        assertEquals("target2", referenceIDs[1])
        assertEquals(null, referenceIDs[2])
    }

    @Test
    fun testChunkGetReferenceValues_withConcept() {
        val (language, concept) = createTestConceptWithReference()
        val chunk =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("TestLanguage", "1.0"))

                val rootInstance =
                    SerializedClassifierInstance().apply {
                        id = "root-id"
                        classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                        parentNodeID = null
                        addReferenceValue(
                            MetaPointer.from(concept.getReferenceByName("testReference")),
                            SerializedReferenceValue.Entry("target1", "resolveInfo1"),
                        )
                    }
                addClassifierInstance(rootInstance)
            }

        val referenceValues = chunk.getReferenceValues(concept, "testReference")

        assertEquals(1, referenceValues.size)
        assertEquals("target1", referenceValues[0].reference)
        assertEquals("resolveInfo1", referenceValues[0].resolveInfo)
    }

    @Test
    fun testChunkGetReferenceValuesIDs_withConcept() {
        val (language, concept) = createTestConceptWithReference()
        val chunk =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("TestLanguage", "1.0"))

                val rootInstance =
                    SerializedClassifierInstance().apply {
                        id = "root-id"
                        classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                        parentNodeID = null
                        unsafeAppendReferenceValue(
                            SerializedReferenceValue(
                                MetaPointer.from(concept.getReferenceByName("testReference")),
                                listOf(
                                    SerializedReferenceValue.Entry("target1", "resolveInfo1"),
                                    SerializedReferenceValue.Entry(null, "resolveInfo2"),
                                ),
                            ),
                        )
                    }
                addClassifierInstance(rootInstance)
            }

        val referenceIDs = chunk.getReferenceValuesIDs(concept, "testReference")

        assertEquals(2, referenceIDs.size)
        assertEquals("target1", referenceIDs[0])
        assertEquals(null, referenceIDs[1])
    }

    @Test
    fun testCombineTree_addsSubtreeRecursively() {
        val sourceChunk =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("TestLang", "1.0"))

                // Subtree root
                val subtreeRoot =
                    SerializedClassifierInstance().apply {
                        id = "subtree-root"
                        classifier = MetaPointer.get("TestLang", "1.0", "Concept")
                        unsafeAppendContainmentValue(
                            SerializedContainmentValue(
                                MetaPointer.get("TestLang", "1.0", "containment"),
                                listOf("child1", "child2"),
                            ),
                        )
                    }
                addClassifierInstance(subtreeRoot)

                // Child 1
                val child1 =
                    SerializedClassifierInstance().apply {
                        id = "child1"
                        classifier = MetaPointer.get("TestLang", "1.0", "Concept")
                        parentNodeID = "subtree-root"
                        unsafeAppendContainmentValue(
                            SerializedContainmentValue(
                                MetaPointer.get("TestLang", "1.0", "containment"),
                                listOf("grandchild1"),
                            ),
                        )
                    }
                addClassifierInstance(child1)

                // Child 2
                val child2 =
                    SerializedClassifierInstance().apply {
                        id = "child2"
                        classifier = MetaPointer.get("TestLang", "1.0", "Concept")
                        parentNodeID = "subtree-root"
                    }
                addClassifierInstance(child2)

                // Grandchild 1
                val grandchild1 =
                    SerializedClassifierInstance().apply {
                        id = "grandchild1"
                        classifier = MetaPointer.get("TestLang", "1.0", "Concept")
                        parentNodeID = "child1"
                    }
                addClassifierInstance(grandchild1)
            }

        val targetChunk =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("TestLang", "1.0"))
            }

        val subtreeRoot = sourceChunk.classifierInstancesByID["subtree-root"]!!
        targetChunk.combineTree(sourceChunk, subtreeRoot)

        assertEquals(4, targetChunk.classifierInstances.size)
        assertNotNull(targetChunk.classifierInstancesByID["subtree-root"])
        assertNotNull(targetChunk.classifierInstancesByID["child1"])
        assertNotNull(targetChunk.classifierInstancesByID["child2"])
        assertNotNull(targetChunk.classifierInstancesByID["grandchild1"])
    }

    @Test
    fun testCombineTree_withSingleNode() {
        val sourceChunk =
            SerializationChunk().apply {
                serializationFormatVersion = "2024.1"
                addLanguage(LanguageVersion.of("TestLang", "1.0"))

                val singleNode =
                    SerializedClassifierInstance().apply {
                        id = "single-node"
                        classifier = MetaPointer.get("TestLang", "1.0", "Concept")
                    }
                addClassifierInstance(singleNode)
            }

        val targetChunk = SerializationChunk()
        val singleNode = sourceChunk.classifierInstancesByID["single-node"]!!

        targetChunk.combineTree(sourceChunk, singleNode)

        assertEquals(1, targetChunk.classifierInstances.size)
        assertNotNull(targetChunk.classifierInstancesByID["single-node"])
    }

    @Test
    fun testGetReferenceValues_withEmptyReferences() {
        val (language, concept) = createTestConceptWithReference()
        val instance =
            SerializedClassifierInstance().apply {
                id = "test-id"
                classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                // No reference values added
            }

        val referenceValues = instance.getReferenceValues(concept, "testReference")

        assertTrue(referenceValues.isEmpty())
    }

    @Test
    fun testGetReferenceValuesIDs_withEmptyReferences() {
        val (language, concept) = createTestConceptWithReference()
        val instance =
            SerializedClassifierInstance().apply {
                id = "test-id"
                classifier = MetaPointer.get("TestLanguage", "1.0", "TestConcept")
                // No reference values added
            }

        val referenceIDs = instance.getReferenceValuesIDs(concept, "testReference")

        assertTrue(referenceIDs.isEmpty())
    }
}
