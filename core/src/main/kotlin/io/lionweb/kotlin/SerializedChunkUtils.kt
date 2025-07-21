package io.lionweb.kotlin

import io.lionweb.serialization.data.SerializedChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import java.lang.IllegalStateException

val SerializedChunk.root: SerializedClassifierInstance
    get() = this.classifierInstances.find { it.parentNodeID == null } ?: throw IllegalStateException()

fun SerializedChunk.combine(other: SerializedChunk): SerializedChunk {
    other.classifierInstances.forEach { addClassifierInstance(it) }
    populateUsedLanguages()
    return this
}
