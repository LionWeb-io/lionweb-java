package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.language.Classifier
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Containment
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.language.PrimitiveType
import io.lionweb.lioncore.java.language.Property
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

/**
 * Create a LionWeb Language with the given name.
 */
fun lwLanguage(
    name: String,
    vararg classes: KClass<*>,
): Language {
    val cleanedName = name.lowercase().replace('.', '_')
    val language = Language(name, "language-$cleanedName-id", "language-$cleanedName-key", "1")
    // We register first the primitive types, as concepts could use them
    language.addPrimitiveTypes(*classes.filter { !it.isSubclassOf(Node::class) }.toTypedArray())
    language.addConcepts(*classes.filter { it.isSubclassOf(Node::class) }.map { it as KClass<out Node> }.toTypedArray())
    return language
}

fun Language.addConcept(name: String): Concept {
    val concept =
        Concept(
            this,
            name,
            "${this.id!!.removePrefix("language-").removeSuffix("-id")}-$name-id",
            "${this.key!!.removePrefix("language-").removeSuffix("-key")}-$name-key",
        )
    this.addElement(concept)
    return concept
}

fun Language.addPrimitiveType(name: String): PrimitiveType {
    val primitiveType =
        PrimitiveType(
            this,
            name,
            "${this.id!!.removePrefix("language-").removeSuffix("-id")}-$name-id",
        ).apply {
            key = "${this@addPrimitiveType.key!!.removePrefix("language-").removeSuffix("-key")}-$name-key"
        }

    this.addElement(primitiveType)
    return primitiveType
}

fun Language.addConcepts(vararg conceptClasses: KClass<out Node>) {
    // First we create them all
    val conceptsByClasses = mutableMapOf<KClass<out Node>, Concept>()
    conceptClasses.forEach { conceptClass ->
        val concept =
            addConcept(
                conceptClass.simpleName
                    ?: throw IllegalArgumentException("Given conceptClass has no name"),
            )
        concept.isAbstract = conceptClass.isAbstract
        conceptsByClasses[conceptClass] = concept
        MetamodelRegistry.registerMapping(conceptClass, concept)
    }

    // Then we populate them all
    conceptsByClasses.forEach { (conceptClass, concept) ->
        conceptClass.superclasses.forEach { superClass ->
            when {
                superClass == BaseNode::class -> Unit // Nothing to do
                superClass.java.isInterface -> Unit
                else -> {
                    val extendedConcept = conceptsByClasses[superClass]
                    if (extendedConcept == null) {
                        throw IllegalStateException("Cannot handle superclass $superClass for concept class $conceptClass")
                    } else {
                        concept.extendedConcept = extendedConcept
                    }
                }
            }
        }

        conceptClass.declaredMemberProperties.filter { it.annotations.none { it is Implementation } }.forEach { property ->
            when (property.returnType.classifier) {
                List::class -> {
                    val elementClassifier = property.returnType.arguments[0].type!!.classifier!! as KClass<*>
                    if (elementClassifier.isSubclassOf(ReferenceValue::class)) {
                        if (elementClassifier.isSubclassOf(SpecificReferenceValue::class)) {
                            val referenceType =
                                MetamodelRegistry.getClassifier(
                                    property.returnType.arguments[0].type!!.arguments[0].type!!.classifier!! as KClass<out Node>,
                                ) as Classifier<*>
                            concept.addReference(property.name, referenceType, Multiplicity.ZERO_TO_MANY)
                        } else {
                            throw RuntimeException(
                                "We cannot figure out the Classifier hold by the Reference when a ReferenceValue is used",
                            )
                        }
                    } else {
                        val baseClassifier = elementClassifier as KClass<out Node>
                        val containmentType =
                            MetamodelRegistry.getConcept(baseClassifier)
                                ?: throw IllegalStateException("Cannot find concept for $baseClassifier")
                        concept.addContainment(property.name, containmentType, Multiplicity.ZERO_TO_MANY)
                    }
                }
                else -> {
                    val kClass =
                        property.returnType.classifier
                            as KClass<out Node>
                    if (kClass.isSubclassOf(Node::class)) {
                        val containmentType =
                            MetamodelRegistry.getConcept(
                                kClass,
                            ) ?: throw IllegalStateException("Cannot find concept for $kClass")
                        concept.addContainment(property.name, containmentType, Multiplicity.SINGLE)
                    } else if (kClass.isSubclassOf(ReferenceValue::class)) {
                        if (kClass.isSubclassOf(SpecificReferenceValue::class)) {
                            val referenceType =
                                MetamodelRegistry.getClassifier(
                                    property.returnType.arguments[0].type!!.classifier!! as KClass<out Node>,
                                ) as Classifier<*>
                            concept.addReference(property.name, referenceType, Multiplicity.OPTIONAL)
                        } else {
                            throw RuntimeException(
                                "We cannot figure out the Classifier hold by the Reference when a ReferenceValue is used",
                            )
                        }
                    } else {
                        val primitiveType =
                            MetamodelRegistry.getPrimitiveType(kClass)
                                ?: throw IllegalStateException("Cannot find primitive type for $kClass")
                        concept.addProperty(property.name, primitiveType, Multiplicity.SINGLE)
                    }
                }
            }
        }
    }
}

fun Language.addPrimitiveTypes(vararg primitiveTypeClasses: KClass<*>) {
    primitiveTypeClasses.forEach { primitiveTypeClass ->
        require(!primitiveTypeClass.isSubclassOf(Node::class))
        val primitiveType =
            addPrimitiveType(
                primitiveTypeClass.simpleName
                    ?: throw IllegalArgumentException("Given primitiveTypeClass has no name"),
            )
        MetamodelRegistry.registerMapping(primitiveTypeClass, primitiveType)
    }
}

enum class Multiplicity(val optional: Boolean, val multiple: Boolean) {
    OPTIONAL(true, false),
    SINGLE(false, false),
    ZERO_TO_MANY(true, true),
    ONE_TO_MANY(false, true),
}

fun Classifier<*>.addContainment(
    name: String,
    containedClassifier: Classifier<*>,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Containment {
    val containment =
        Containment().apply {
            this.name = name
            this.id = "${this@addContainment.id!!.removeSuffix("-id")}-$name-id"
            this.key = "${this@addContainment.key!!.removeSuffix("-key")}-$name-key"
            this.type = containedClassifier
            this.setOptional(multiplicity.optional)
            this.setMultiple(multiplicity.multiple)
        }
    this.addFeature(containment)
    return containment
}

fun Classifier<*>.addReference(
    name: String,
    containedClassifier: Classifier<*>,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Reference {
    val reference =
        Reference().apply {
            this.name = name
            this.id = "${this@addReference.id!!.removeSuffix("-id")}-$name-id"
            this.key = "${this@addReference.key!!.removeSuffix("-key")}-$name-key"
            this.type = containedClassifier
            this.setOptional(multiplicity.optional)
            this.setMultiple(multiplicity.multiple)
        }
    this.addFeature(reference)
    return reference
}

fun Classifier<*>.addProperty(
    name: String,
    type: PrimitiveType,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Property {
    require(!multiplicity.multiple)
    val property =
        Property().apply {
            this.name = name
            this.id = "${this@addProperty.id!!.removeSuffix("-id")}-$name-id"
            this.key = "${this@addProperty.key!!.removeSuffix("-key")}-$name-key"
            this.type = type
            this.setOptional(multiplicity.optional)
        }
    this.addFeature(property)
    return property
}
