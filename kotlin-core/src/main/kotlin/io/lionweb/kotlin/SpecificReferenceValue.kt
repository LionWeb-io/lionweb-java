package io.lionweb.kotlin

import io.lionweb.model.Node
import io.lionweb.model.ReferenceValue
import io.lionweb.model.impl.ProxyNode
import kotlin.reflect.KClass

interface Deproxifier {
    fun <T> deproxify(node: ProxyNode): T

    fun <T> deproxifyList(nodes: List<ProxyNode>): List<T>
}

class SpecificReferenceValue<T : Node>(
    val targetClass: KClass<T>,
    referred: Node? = null,
    resolveInfo: String? = null,
) : ReferenceValue(referred, resolveInfo) {
    companion object {
        inline fun <reified T : Node> create(
            resolveInfo: String?,
            referred: Node?,
        ): SpecificReferenceValue<T> = SpecificReferenceValue(T::class, referred, resolveInfo)

        inline fun <reified T : Node> createNull(): SpecificReferenceValue<T> = create(null, null)
    }

    fun getReferred(deproxifier: Deproxifier): T? {
        val value = super.getReferred()
        return when {
            value == null -> null
            value is ProxyNode -> deproxifier.deproxify(value)
            targetClass.isInstance(value) -> value as T
            else -> throw IllegalStateException("Referred node has an expected type: $value")
        }
    }
}
