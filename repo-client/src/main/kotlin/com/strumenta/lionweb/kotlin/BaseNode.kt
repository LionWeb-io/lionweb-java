package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Containment
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.DynamicNode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Implementation

/**
 * This is intended to be used as the base classes of classes representing LionWeb nodes,
 * when defined in Kotlin.
 */
abstract class BaseNode : DynamicNode() {
    override fun getClassifier(): Concept? {
        return super.getClassifier() ?: MetamodelRegistry.getConcept(this.javaClass.kotlin)
    }

    open fun calculateID(): String? = null

    override fun getID(): String? {
        return calculateID() ?: this.id
    }

    inline fun <P : BaseNode, reified T : Node> singleReference(referenceName: String): ReadWriteProperty<P, SpecificReferenceValue<T>?> {
        return object : ReadWriteProperty<P, SpecificReferenceValue<T>?> {
            override fun getValue(
                thisRef: P,
                property: KProperty<*>,
            ): SpecificReferenceValue<T>? {
                val reference =
                    thisRef.classifier!!.getReferenceByName(referenceName)
                        ?: throw IllegalStateException("No reference with name $referenceName found")
                val referenceValues = thisRef.getReferenceValues(reference)
                return when (referenceValues.size) {
                    0 -> null
                    1 -> {
                        if (referenceValues[0] == null) {
                            return null
                        } else if (referenceValues is SpecificReferenceValue<*>) {
                            return referenceValues[0] as SpecificReferenceValue<T>
                        } else {
                            SpecificReferenceValue(T::class)
                            val res = SpecificReferenceValue.create<T>(referenceValues[0].resolveInfo, referenceValues[0].referred)
                            return res
                        }
                    }
                    else -> throw IllegalStateException("Multiple reference values for single reference")
                }
            }

            override fun setValue(
                thisRef: P,
                property: KProperty<*>,
                value: SpecificReferenceValue<T>?,
            ) {
                val reference =
                    thisRef.classifier!!.getReferenceByName(referenceName)
                        ?: throw IllegalStateException("No reference with name $referenceName found")
                thisRef.setOnlyReferenceValue(reference, value)
            }
        }
    }

    inline fun <P : BaseNode, reified T : Node> multipleReference(
        referenceName: String,
    ): ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
        return object : ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
            override fun getValue(
                thisRef: P,
                property: KProperty<*>,
            ): MutableList<SpecificReferenceValue<T>> {
                val reference =
                    thisRef.classifier!!.getReferenceByName(referenceName)
                        ?: throw IllegalStateException("No reference with name $referenceName found")
                val referenceValues = thisRef.getReferenceValues(reference)
                return referenceValues as MutableList<SpecificReferenceValue<T>>
            }

            override fun setValue(
                thisRef: P,
                property: KProperty<*>,
                value: MutableList<SpecificReferenceValue<T>>,
            ) {
                val reference =
                    thisRef.classifier!!.getReferenceByName(referenceName)
                        ?: throw IllegalStateException("No reference with name $referenceName found")
                thisRef.setReferenceValues(reference, value)
            }
        }
    }

    fun <C : Node> multipleContainment(name: String): MutableList<C> {
        return ContainmentList(
            this,
            (
                classifier ?: throw IllegalStateException(
                    "Concept should not be null (base node $this, " +
                        "class: ${this.javaClass.canonicalName})",
                )
            ).requireContainmentByName(
                name,
            ),
        )
    }

    protected fun <P : BaseNode, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
        return object : ReadWriteProperty<P, C?> {
            override fun getValue(
                thisRef: P,
                property: KProperty<*>,
            ): C? {
                return thisRef.getOnlyChildByContainmentName(containmentName) as C?
            }

            override fun setValue(
                thisRef: P,
                property: KProperty<*>,
                value: C?,
            ) {
                val containment = thisRef.classifier!!.requireContainmentByName(containmentName)
                thisRef.addChild(containment, value)
            }
        }
    }

    protected fun <P : BaseNode, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
        return object : ReadWriteProperty<P, V?> {
            override fun getValue(
                thisRef: P,
                property: KProperty<*>,
            ): V? {
                return thisRef.getPropertyValueByName(propertyName) as V?
            }

            override fun setValue(
                thisRef: P,
                property: KProperty<*>,
                value: V?,
            ) {
                thisRef.setPropertyValueByName(propertyName, value)
            }
        }
    }
}

private class ContainmentList<E : Node>(private val node: DynamicNode, private val containment: Containment) : MutableList<E> {
    override val size: Int
        get() = node.getChildren(containment).size

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): E {
        val children = node.getChildren(containment)
        return children[index] as E
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): MutableIterator<E> {
        val children = node.getChildren(containment)
        return children.iterator() as MutableIterator<E>
    }

    override fun listIterator(): MutableListIterator<E> {
        val children = node.getChildren(containment)
        return children.listIterator() as MutableListIterator<E>
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        val children = node.getChildren(containment)
        return children.listIterator(index) as MutableListIterator<E>
    }

    override fun removeAt(index: Int): E {
        TODO("Not yet implemented")
    }

    override fun subList(
        fromIndex: Int,
        toIndex: Int,
    ): MutableList<E> {
        TODO("Not yet implemented")
    }

    override fun set(
        index: Int,
        element: E,
    ): E {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun remove(element: E): Boolean {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: E): Int {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: E): Int {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(element: E): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach { changed = add(it) || changed }
        return changed
    }

    override fun addAll(
        index: Int,
        elements: Collection<E>,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun add(
        index: Int,
        element: E,
    ) {
        TODO("Not yet implemented")
    }

    override fun add(element: E): Boolean {
        val preSize = size
        node.addChild(containment, element)
        val postSize = size
        return preSize != postSize
    }
}
