package com.strumenta.lwrepoclient.base

import com.strumenta.lionweb.kotlin.children
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.ProxyNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
import java.io.File

/**
 * Perform some sanity checks on the tree. This is mostly useful while debugging the export to LionWeb.
 * Eventually this could be dropped or controlled by some flag.
 */
fun treeSanityChecks(
    node: Node,
    parents: MutableMap<String, String?> = mutableMapOf(),
    jsonSerialization: JsonSerialization,
) {
    if (node is ProxyNode) {
        return
    }
    try {
        if (parents.containsKey(node.id!!)) {
            throw IllegalStateException("Node with ID ${node.id} has already a parent")
        }
        parents[node.id!!] = node.parent?.id
        node.classifier.allContainments().forEach { containment ->
            val childrenInContainment = containment.children.map { it.id }
            require(childrenInContainment.none { it !== null })
            require(childrenInContainment.distinct() == childrenInContainment)
        }
        node.children.forEach {
            if (node !is ProxyNode) {
                treeSanityChecks(it, parents, jsonSerialization)
            }
        }
    } catch (t: Throwable) {
        // This method is called when in debug mode, so let's save this file to help debugging
        File("error.json").writeText(jsonSerialization.serializeTreesToJsonString(node.root))
        throw RuntimeException(t)
    }
}
