package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.model.ClassifierInstance
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

fun <N : Node> ClassifierInstance<*>.walkDescendants(kClass: KClass<N>): Sequence<N> {
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

fun ClassifierInstance<*>.getOnlyReferenceValueByReferenceName(referenceName: String) =
    ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(
        this,
        referenceName,
    )

fun ClassifierInstance<*>.setOnlyReferenceValue(
    reference: Reference,
    value: ReferenceValue?,
) {
    ClassifierInstanceUtils.setOnlyReferenceValue(this, reference, value)
}

fun ClassifierInstance<*>.setOnlyReferenceValueByName(
    referenceName: String,
    value: ReferenceValue?,
) {
    ClassifierInstanceUtils.setOnlyReferenceValueByName(this, referenceName, value)
}

fun ClassifierInstance<*>.setReferenceValuesByName(
    referenceName: String,
    values: List<out ReferenceValue>,
) {
    ClassifierInstanceUtils.setReferenceValuesByName(this, referenceName, values)
}

fun ClassifierInstance<*>.getOnlyChildByContainmentName(containmentName: String) =
    ClassifierInstanceUtils.getOnlyChildByContainmentName(this, containmentName)

fun ClassifierInstance<*>.getPropertyValueByName(propertyName: String) = ClassifierInstanceUtils.getPropertyValueByName(this, propertyName)

fun ClassifierInstance<*>.setPropertyValueByName(
    propertyName: String,
    value: Any?,
) = ClassifierInstanceUtils.setPropertyValueByName(this, propertyName, value)

fun ClassifierInstance<*>.getChildrenByContainmentName(propertyName: String): List<Node> =
    ClassifierInstanceUtils.getChildrenByContainmentName(
        this,
        propertyName,
    )

fun ClassifierInstance<*>.getReferenceValueByName(propertyName: String) =
    ClassifierInstanceUtils.getReferenceValueByName(
        this,
        propertyName,
    )

val ClassifierInstance<*>.children
    get() = ClassifierInstanceUtils.getChildren(this)
