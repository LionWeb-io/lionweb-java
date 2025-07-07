package io.lionweb.kotlin

import io.lionweb.LionWebVersion
import io.lionweb.language.Annotation
import io.lionweb.model.Node
import io.lionweb.model.impl.DynamicAnnotationInstance
import kotlin.properties.ReadWriteProperty

/**
 * When defining a class to represent Annotation Instances and benefit from static typing,
 * one should extend this class. It works similarly to BaseNode.
 */
abstract class BaseAnnotation(val lionWebVersion: LionWebVersion = LionWebVersion.currentVersion) : DynamicAnnotationInstance(null) {
    override fun getClassifier(): Annotation? {
        return super.getAnnotationDefinition() ?: MetamodelRegistry.getAnnotation(this.javaClass.kotlin, lionWebVersion)
    }

    inline fun <P : BaseAnnotation, reified T : Node> singleReference(
        referenceName: String,
    ): ReadWriteProperty<P, SpecificReferenceValue<T>?> {
        return io.lionweb.kotlin.singleReference(referenceName)
    }

    inline fun <P : BaseAnnotation, reified T : Node> multipleReference(
        referenceName: String,
    ): ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
        return io.lionweb.kotlin.multipleReference(referenceName)
    }

    fun <C : Node> multipleContainment(name: String): MutableList<C> {
        return multipleContainment(this, name)
    }

    protected fun <P : BaseAnnotation, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
        return io.lionweb.kotlin.singleContainment(containmentName)
    }

    protected fun <P : BaseAnnotation, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
        return io.lionweb.kotlin.property(propertyName)
    }
}
