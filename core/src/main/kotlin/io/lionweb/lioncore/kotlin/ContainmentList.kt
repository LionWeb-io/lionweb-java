package io.lionweb.lioncore.kotlin

import io.lionweb.language.Containment
import io.lionweb.model.Node
import io.lionweb.model.impl.DynamicClassifierInstance

/**
 * This is a list that represent the children part of a specific containment for a certain containing node.
 * When adding or removing elements to this list the necessary changes are performed to the containing node,
 * setting the parent, for example.
 */
internal class ContainmentList<E : Node>(
    private val classifierInstance: DynamicClassifierInstance<*>,
    private val containment: Containment,
) : MutableList<E> {
    override val size: Int
        get() = classifierInstance.getChildren(containment).size

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): E {
        val children = classifierInstance.getChildren(containment)
        return children[index] as E
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): MutableIterator<E> {
        val children = classifierInstance.getChildren(containment)
        return children.iterator() as MutableIterator<E>
    }

    override fun listIterator(): MutableListIterator<E> {
        val children = classifierInstance.getChildren(containment)
        return children.listIterator() as MutableListIterator<E>
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        val children = classifierInstance.getChildren(containment)
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
        classifierInstance.addChild(containment, element)
        val postSize = size
        return preSize != postSize
    }
}
