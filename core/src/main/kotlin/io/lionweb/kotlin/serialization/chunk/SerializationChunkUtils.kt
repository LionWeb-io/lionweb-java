package io.lionweb.kotlin.serialization.chunk

import io.lionweb.language.Concept
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializationChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedReferenceValue
import java.lang.IllegalStateException

/**
 * The first root node within the given SerializedChunk.
 */
val SerializationChunk.root: SerializedClassifierInstance
    get() = this.classifierInstances.find { it.parentNodeID == null } ?: throw IllegalStateException()

/**
 * Combine this and the received SerializedChunk into a single SerializedChunk containing all nodes.
 * It changes the original SerializedChunk and return it.
 */
fun SerializationChunk.combine(other: SerializationChunk): SerializationChunk {
    other.classifierInstances.forEach { addClassifierInstance(it) }
    populateUsedLanguages()
    return this
}

fun SerializedClassifierInstance.getReferenceValues(
    concept: Concept,
    referenceName: String,
): List<SerializedReferenceValue.Entry> = this.getReferenceValues(MetaPointer.from(concept.getReferenceByName(referenceName)))

fun SerializedClassifierInstance.getReferenceValuesIDs(
    concept: Concept,
    referenceName: String,
): List<String?> =
    this.getReferenceValues(concept, referenceName).map {
        it.reference
    }

fun SerializationChunk.getReferenceValues(
    concept: Concept,
    referenceName: String,
): List<SerializedReferenceValue.Entry> = this.root.getReferenceValues(MetaPointer.from(concept.getReferenceByName(referenceName)))

fun SerializationChunk.getReferenceValuesIDs(
    concept: Concept,
    referenceName: String,
): List<String?> = this.root.getReferenceValuesIDs(concept, referenceName)

fun SerializationChunk.combineTree(
    otherChunk: SerializationChunk,
    subtreeRoot: SerializedClassifierInstance,
) {
    this.addClassifierInstance(subtreeRoot)
    subtreeRoot.children.forEach { childId ->
        val child = otherChunk.classifierInstancesByID[childId]!!
        combineTree(otherChunk, child)
    }
}
