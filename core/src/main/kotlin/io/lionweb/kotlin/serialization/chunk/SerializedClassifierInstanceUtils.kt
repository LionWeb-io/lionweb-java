package io.lionweb.kotlin.serialization.chunk

import io.lionweb.LionWebVersion
import io.lionweb.language.Classifier
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializedChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedReferenceValue
import java.lang.IllegalArgumentException

fun SerializedClassifierInstance.getProperty(
    classifier: Classifier<*>,
    propertyName: String,
): String? {
    val property =
        classifier.getPropertyByName(propertyName)
            ?: throw IllegalArgumentException("Cannot find property $propertyName on $classifier")
    val metapointer = MetaPointer.from(property)
    return getProperty(metapointer)
}

fun SerializedClassifierInstance.getProperty(metaPointer: MetaPointer): String? =
    this.properties
        .find {
            it.metaPointer == metaPointer
        }?.value

fun SerializedClassifierInstance.addChild(
    classifier: Classifier<*>,
    containmentName: String,
    child: SerializedClassifierInstance,
) {
    val containment = classifier.getContainmentByName(containmentName)
    val metapointer = MetaPointer.from(containment)
    addChild(metapointer, child)
}

fun SerializedClassifierInstance.addChild(
    metaPointer: MetaPointer,
    child: SerializedClassifierInstance,
) {
    addChild(metaPointer, child.id!!)
    child.parentNodeID = this.id
}

fun SerializedClassifierInstance.addReference(
    classifier: Classifier<*>,
    referenceName: String,
    resolveInfo: String?,
    target: String?,
) {
    val reference = classifier.getReferenceByName(referenceName)
    val metapointer = MetaPointer.from(reference)
    addReferenceValue(metapointer, SerializedReferenceValue.Entry(target, resolveInfo))
}

fun SerializedClassifierInstance.getChildren(
    metaPointer: MetaPointer,
    chunk: SerializedChunk,
): List<SerializedClassifierInstance> {
    val containment = this.containments.find { it.metaPointer == metaPointer } ?: return emptyList()
    return containment.value.map { childId ->
        chunk.classifierInstancesByID[childId]!!
    }
}

fun SerializedClassifierInstance.getChild(
    metaPointer: MetaPointer,
    chunk: SerializedChunk,
): SerializedClassifierInstance? {
    val children = getChildren(metaPointer, chunk)
    require(children.size <= 1)
    return children.firstOrNull()
}

fun SerializedClassifierInstance.subchunk(chunk: SerializedChunk): SerializedChunk {
    val relevantNodes = mutableListOf<SerializedClassifierInstance>()
    chunk.classifierInstancesByID

    fun grow(n: SerializedClassifierInstance) {
        relevantNodes.add(n)
        n.children.forEach {
            grow(chunk.classifierInstancesByID[it]!!)
        }
    }
    grow(this)
    val lwVersion = LionWebVersion.fromValue(chunk.serializationFormatVersion)
    return SerializedChunk.fromNodes(lwVersion, relevantNodes)
}
