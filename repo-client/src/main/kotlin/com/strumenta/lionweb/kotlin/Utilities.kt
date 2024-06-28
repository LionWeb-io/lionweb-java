package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils
import io.lionweb.lioncore.java.model.HasSettableParent
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.model.impl.ProxyNode
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * Create a Dynamic Node with the given Concept and a random node ID.
 */
fun Concept.dynamicNode(nodeId: String = "node-id-rand-${Random.nextInt()}"): DynamicNode {
    return DynamicNode(nodeId, this)
}

fun <N> N.withParent(parent: Node?): N where N : Node, N : HasSettableParent {
    this.setParent(parent)
    return this
}

fun <N : Node> Node.walkDescendants(kClass: KClass<N>): Sequence<N> {
    if (this is ProxyNode) {
        throw IllegalStateException("Cannot call walkDescendants on a ProxyNode")
    }
    return sequence {
        this@walkDescendants.children.forEach { child ->
            if (child is ProxyNode) {
                throw IllegalStateException("Cannot call walkDescendants on a ProxyNode (parent is ${this@walkDescendants}")
            }
            if (kClass.isInstance(child)) {
                yield(child as N)
            }
            yieldAll(child.walkDescendants(kClass))
        }
    }
}

fun Node.getOnlyReferenceValueByReferenceName(referenceName: String) =
    ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(
        this,
        referenceName,
    )

fun Node.setOnlyReferenceValue(
    reference: Reference,
    value: ReferenceValue?,
) {
    ClassifierInstanceUtils.setOnlyReferenceValue(this, reference, value)
}

fun Node.setOnlyReferenceValueByName(
    referenceName: String,
    value: ReferenceValue?,
) {
    ClassifierInstanceUtils.setOnlyReferenceValueByName(this, referenceName, value)
}

fun Node.setReferenceValuesByName(
    referenceName: String,
    values: List<out ReferenceValue>,
) {
    ClassifierInstanceUtils.setReferenceValuesByName(this, referenceName, values)
}

fun Node.getOnlyChildByContainmentName(containmentName: String) =
    ClassifierInstanceUtils.getOnlyChildByContainmentName(this, containmentName)

fun Node.getPropertyValueByName(propertyName: String) = ClassifierInstanceUtils.getPropertyValueByName(this, propertyName)

fun Node.setPropertyValueByName(
    propertyName: String,
    value: Any?,
) = ClassifierInstanceUtils.setPropertyValueByName(this, propertyName, value)

fun Node.getChildrenByContainmentName(propertyName: String) = ClassifierInstanceUtils.getChildrenByContainmentName(this, propertyName)

fun Node.getReferenceValueByName(propertyName: String) = ClassifierInstanceUtils.getReferenceValueByName(this, propertyName)

val Node.children
    get() = ClassifierInstanceUtils.getChildren(this)
