package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.DynamicNode
import kotlin.properties.ReadWriteProperty

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
        return io.lionweb.lioncore.kotlin.singleReference(referenceName)
    }

    inline fun <P : BaseNode, reified T : Node> multipleReference(
        referenceName: String,
    ): ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
        return io.lionweb.lioncore.kotlin.multipleReference(referenceName)
    }

    fun <C : Node> multipleContainment(name: String): MutableList<C> {
        return multipleContainment(this, name)
    }

    protected fun <P : BaseNode, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
        return io.lionweb.lioncore.kotlin.singleContainment(containmentName)
    }

    protected fun <P : BaseNode, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
        return io.lionweb.lioncore.kotlin.property(propertyName)
    }
}
