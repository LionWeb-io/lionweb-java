package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.model.impl.ProxyNode
import kotlin.reflect.KClass

interface Deproxifier {
    fun <T> deproxify(node: ProxyNode): T
}

class SpecificReferenceValue<T : Node>(val targetClass: KClass<T>) : ReferenceValue() {
    companion object {
        inline fun <reified T : Node> create(
            resolveInfo: String?,
            referred: Node?,
        ): SpecificReferenceValue<T> {
            return SpecificReferenceValue(T::class).apply {
                this.resolveInfo = resolveInfo
                this.referred = referred
            }
        }

        inline fun <reified T : Node> createNull(): SpecificReferenceValue<T> {
            return create(null, null)
        }
    }

    fun getReferred(deproxifier: Deproxifier): T? {
        val value = super.getReferred()
        return when {
            value == null -> {
                null
            }
            value is ProxyNode -> {
                deproxifier.deproxify(value)
            }
            targetClass.isInstance(value) -> {
                value as T
            }
            else -> {
                throw IllegalStateException("Referred node has an expected type: $value")
            }
        }
    }

    override fun setReferred(referred: Node?) {
        if (referred == null || targetClass.isInstance(referred) || referred is ProxyNode) {
            super.setReferred(referred)
        } else {
            throw IllegalArgumentException("Cannot set referred to $referred")
        }
    }
}
