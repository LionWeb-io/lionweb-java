package io.lionweb.kotlin

import io.lionweb.LionWebVersion
import io.lionweb.language.Concept
import io.lionweb.model.Node
import io.lionweb.model.impl.DynamicNode
import kotlin.properties.ReadWriteProperty

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Implementation

/**
 * This is intended to be used as the base classes of classes representing LionWeb nodes,
 * when defined in Kotlin.
 */
abstract class BaseNode(val lionWebVersion: LionWebVersion = LionWebVersion.currentVersion) : DynamicNode() {
    override fun getClassifier(): Concept? {
        return super.getClassifier() ?: MetamodelRegistry.getConcept(this.javaClass.kotlin, lionWebVersion)
    }

    open fun calculateID(): String? = null

    override fun getID(): String? {
        return calculateID() ?: this.id
    }

    inline fun <P : BaseNode, reified T : Node> singleReference(referenceName: String): ReadWriteProperty<P, SpecificReferenceValue<T>?> {
        return io.lionweb.kotlin.singleReference(referenceName)
    }

    inline fun <P : BaseNode, reified T : Node> multipleReference(
        referenceName: String,
    ): ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
        return io.lionweb.kotlin.multipleReference(referenceName)
    }

    fun <C : Node> multipleContainment(name: String): MutableList<C> {
        return multipleContainment(this, name)
    }

    protected fun <P : BaseNode, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
        return io.lionweb.kotlin.singleContainment(containmentName)
    }

    protected fun <P : BaseNode, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
        return io.lionweb.kotlin.property(propertyName)
    }
}
