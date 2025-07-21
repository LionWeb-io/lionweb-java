package io.lionweb.kotlin.serialization.chunk

import io.lionweb.serialization.data.SerializedChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import java.lang.IllegalStateException

/**
 * The first root node within the given SerializedChunk.
 */
val SerializedChunk.root: SerializedClassifierInstance
    get() = this.classifierInstances.find { it.parentNodeID == null } ?: throw IllegalStateException()

/**
 * Combine this and the received SerializedChunk into a single SerializedChunk containing all nodes.
 * It changes the original SerializedChunk and return it.
 */
fun SerializedChunk.combine(other: SerializedChunk): SerializedChunk {
    other.classifierInstances.forEach { addClassifierInstance(it) }
    populateUsedLanguages()
    return this
}
