package io.lionweb.kotlin

import io.lionweb.LionWebVersion
import io.lionweb.model.Node
import io.lionweb.serialization.JsonSerialization
import io.lionweb.serialization.LowLevelJsonSerialization
import io.lionweb.serialization.SerializationProvider
import io.lionweb.serialization.data.SerializedChunk

fun cleanId(id: String) =
    id
        .replace('.', '-')
        .replace('/', '-')
        .replace('\\', '-')
        .replace('#', '-')
        .replace(' ', '_')
        .replace("@", "_at_")
        .removeCharactersInvalidInLionWebIDs()

fun Node.toChunk(lwVersion: LionWebVersion = LionWebVersion.currentVersion): SerializedChunk =
    simpleSerializations.computeIfAbsent(lwVersion) {
        SerializationProvider.getStandardJsonSerialization(it)
    }
        .serializeTreeToSerializationChunk(this)

fun String.toChunk(): SerializedChunk = LowLevelJsonSerialization().deserializeSerializationBlock(this)

fun String.removeCharactersInvalidInLionWebIDs(): String {
    return this.filter {
        it in setOf('-', '_') || it in CharRange('0', '9') ||
            it in CharRange('a', 'z') ||
            it in CharRange('A', 'Z')
    }
}

private val simpleSerializations = mutableMapOf<LionWebVersion, JsonSerialization>()
