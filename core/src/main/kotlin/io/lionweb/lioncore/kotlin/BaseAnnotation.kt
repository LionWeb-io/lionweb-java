package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Annotation
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.DynamicAnnotationInstance
import kotlin.properties.ReadWriteProperty

abstract class BaseAnnotation : DynamicAnnotationInstance(null) {
    override fun getClassifier(): Annotation? {
        return MetamodelRegistry.getAnnotation(this.javaClass.kotlin)
    }

    inline fun <P : BaseAnnotation, reified T : Node> singleReference(
        referenceName: String,
    ): ReadWriteProperty<P, SpecificReferenceValue<T>?> {
        return io.lionweb.lioncore.kotlin.singleReference(referenceName)
    }

    inline fun <P : BaseAnnotation, reified T : Node> multipleReference(
        referenceName: String,
    ): ReadWriteProperty<P, MutableList<SpecificReferenceValue<T>>> {
        return io.lionweb.lioncore.kotlin.multipleReference(referenceName)
    }

    fun <C : Node> multipleContainment(name: String): MutableList<C> {
        return multipleContainment(this, name)
    }

    protected fun <P : BaseAnnotation, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
        return io.lionweb.lioncore.kotlin.singleContainment(containmentName)
    }

    protected fun <P : BaseAnnotation, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
        return io.lionweb.lioncore.kotlin.property(propertyName)
    }
}
