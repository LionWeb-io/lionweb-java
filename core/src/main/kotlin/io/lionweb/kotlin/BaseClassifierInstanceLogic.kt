package io.lionweb.kotlin

import io.lionweb.model.ClassifierInstance
import io.lionweb.model.Node
import io.lionweb.model.impl.DynamicClassifierInstance
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//
// This file contains logic that belongs into both BaseNode and BaseAnnotation. Given they cannot share a common
// ancestor we put this logic here.
// Conceptually this logic should be considered to be internal, even if, to satisfy the compiler, we cannot mark
// it as such
//

inline fun <P : ClassifierInstance<*>, reified T : Node> singleReference(
    referenceName: String,
): ReadWriteProperty<P, SpecificReferenceValue<T>?> {
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
                        val res =
                            SpecificReferenceValue.create<T>(
                                referenceValues[0].resolveInfo,
                                referenceValues[0].referred,
                            )
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

inline fun <P : ClassifierInstance<*>, reified T : Node> multipleReference(
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

internal fun <C : Node> multipleContainment(
    classifierInstance: DynamicClassifierInstance<*>,
    name: String,
): MutableList<C> {
    return ContainmentList(
        classifierInstance,
        (
            classifierInstance.classifier ?: throw IllegalStateException(
                "Concept should not be null (classifierInstance $classifierInstance, " +
                    "class: ${classifierInstance.javaClass.canonicalName})",
            )
        ).requireContainmentByName(
            name,
        ),
    )
}

internal fun <P : ClassifierInstance<*>, C : Node> singleContainment(containmentName: String): ReadWriteProperty<P, C?> {
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
            if (value == null) {
                val currValue = getValue(thisRef, property)
                if (currValue != null) {
                    thisRef.removeChild(containment, 0)
                }
            } else {
                thisRef.addChild(containment, value)
            }
        }
    }
}

internal fun <P : ClassifierInstance<*>, V : Any> property(propertyName: String): ReadWriteProperty<P, V?> {
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
