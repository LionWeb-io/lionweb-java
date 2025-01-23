package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Annotation
import io.lionweb.lioncore.java.language.Classifier
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Containment
import io.lionweb.lioncore.java.language.DataType
import io.lionweb.lioncore.java.language.IKeyed
import io.lionweb.lioncore.java.language.Interface
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.language.PrimitiveType
import io.lionweb.lioncore.java.language.Property
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.model.AnnotationInstance
import io.lionweb.lioncore.java.model.ClassifierInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.ReferenceValue
import io.lionweb.lioncore.java.serialization.PrimitiveValuesSerialization.PrimitiveDeserializer
import io.lionweb.lioncore.java.serialization.PrimitiveValuesSerialization.PrimitiveSerializer
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
    language.createPrimitiveTypes(*classes.filter { !it.isSubclassOf(Node::class) }.toTypedArray())
    language.createConcepts(*classes.filter { it.isSubclassOf(Node::class) }.map { it as KClass<out Node> }.toTypedArray())
    language.createAnnotations(
        *classes.filter {
            it.isSubclassOf(AnnotationInstance::class)
        }.map { it as KClass<out AnnotationInstance> }.toTypedArray(),
    )
    return language
}

fun Language.createConcept(name: String): Concept {
    val concept =
        Concept(
            this,
            name,
            this.idForContainedElement(name),
            this.keyForContainedElement(name),
        )
    this.addElement(concept)
    return concept
}

fun Language.createInterface(name: String): Interface {
    val intf =
        Interface(
            this,
            name,
            this.idForContainedElement(name),
            this.keyForContainedElement(name),
        )
    this.addElement(intf)
    return intf
}

fun Language.createAnnotation(name: String): Annotation {
    val annotation =
        Annotation(
            this,
            name,
            "${this.id!!.removePrefix("language-").removeSuffix("-id")}-$name-id",
            "${this.key!!.removePrefix("language-").removeSuffix("-key")}-$name-key",
        )
    this.addElement(annotation)
    return annotation
}

fun Language.createPrimitiveType(name: String): PrimitiveType {
    val primitiveType =
        PrimitiveType(
            this,
            name,
            "${this.id!!.removePrefix("language-").removeSuffix("-id")}-$name-id",
        ).apply {
            key = "${this@createPrimitiveType.key!!.removePrefix("language-").removeSuffix("-key")}-$name-key"
        }

    this.addElement(primitiveType)
    return primitiveType
}

fun Language.createAnnotations(vararg annotationClasses: KClass<out AnnotationInstance>) {
    // First we create them all
    val annotationsByClasses = mutableMapOf<KClass<out AnnotationInstance>, Annotation>()
    annotationClasses.forEach { annotationClass ->
        val annotation =
            createAnnotation(
                annotationClass.simpleName
                    ?: throw IllegalArgumentException("Given annotationClass has no name"),
            )
        annotationsByClasses[annotationClass] = annotation
        MetamodelRegistry.registerMapping(annotationClass, annotation)
    }

    // Then we populate them all
    annotationsByClasses.forEach { (annotationClass, annotation) ->
        annotationClass.superclasses.forEach { superClass ->
            when {
                superClass == BaseAnnotation::class -> Unit // Nothing to do
                superClass.java.isInterface -> Unit
                else -> {
                    val extendedAnnotation = annotationsByClasses[superClass]
                    if (extendedAnnotation == null) {
                        throw IllegalStateException("Cannot handle superclass $superClass for annotation class $annotationClass")
                    } else {
                        annotation.extendedAnnotation = extendedAnnotation
                    }
                }
            }
        }
        populateFeaturesInClassifier(annotationClass, annotation)
    }
}

fun Language.createConcepts(vararg conceptClasses: KClass<out Node>) {
    // First we create them all
    val conceptsByClasses = mutableMapOf<KClass<out Node>, Concept>()
    conceptClasses.forEach { conceptClass ->
        val concept =
            createConcept(
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
                superClass.isSubclassOf(Node::class) -> {
                    val extendedConcept =
                        conceptsByClasses[superClass] ?: MetamodelRegistry.getConcept(superClass as KClass<out Node>)
                    if (extendedConcept == null) {
                        throw IllegalStateException("Cannot handle superclass $superClass for concept class $conceptClass")
                    } else {
                        concept.extendedConcept = extendedConcept
                    }
                }
                else -> {
                    throw IllegalStateException("Superclass is not a node: $superClass for concept class $conceptClass")
                }
            }
        }

        populateFeaturesInClassifier(conceptClass, concept)
    }
}

private fun populateFeaturesInClassifier(
    classifierClass: KClass<out ClassifierInstance<*>>,
    classifier: Classifier<*>,
) {
    classifierClass.declaredMemberProperties.filter { it.annotations.none { it is Implementation } }.forEach { property ->
        when (property.returnType.classifier) {
            List::class -> {
                val elementClassifier = property.returnType.arguments[0].type!!.classifier!! as KClass<*>
                if (elementClassifier.isSubclassOf(ReferenceValue::class)) {
                    if (elementClassifier.isSubclassOf(SpecificReferenceValue::class)) {
                        val referenceType =
                            MetamodelRegistry.getClassifier(
                                property.returnType.arguments[0].type!!.arguments[0].type!!.classifier!! as KClass<out Node>,
                            ) as Classifier<*>
                        classifier.createReference(property.name, referenceType, Multiplicity.ZERO_TO_MANY)
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
                    classifier.createContainment(property.name, containmentType, Multiplicity.ZERO_TO_MANY)
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
                    classifier.createContainment(property.name, containmentType, Multiplicity.SINGLE)
                } else if (kClass.isSubclassOf(ReferenceValue::class)) {
                    if (kClass.isSubclassOf(SpecificReferenceValue::class)) {
                        val referenceType =
                            MetamodelRegistry.getClassifier(
                                property.returnType.arguments[0].type!!.classifier!! as KClass<out Node>,
                            ) as Classifier<*>
                        classifier.createReference(property.name, referenceType, Multiplicity.OPTIONAL)
                    } else {
                        throw RuntimeException(
                            "We cannot figure out the Classifier hold by the Reference when a ReferenceValue is used",
                        )
                    }
                } else {
                    val primitiveType =
                        MetamodelRegistry.getPrimitiveType(kClass)
                            ?: throw IllegalStateException("Cannot find primitive type for $kClass")
                    classifier.createProperty(property.name, primitiveType, Multiplicity.SINGLE)
                }
            }
        }
    }
}

fun Language.createPrimitiveTypes(vararg primitiveTypeClasses: KClass<*>): List<PrimitiveType> {
    return primitiveTypeClasses.map { primitiveTypeClass ->
        createPrimitiveType(primitiveTypeClass)
    }
}

fun <T : Any> Language.addSerializerAndDeserializer(
    primitiveTypeClass: KClass<T>,
    serializer: PrimitiveSerializer<T?>,
    deserializer: PrimitiveDeserializer<T?>,
) {
    val primitiveType =
        MetamodelRegistry.getPrimitiveType(primitiveTypeClass)
            ?: throw IllegalStateException("Unknown primitive type class $primitiveTypeClass")
    MetamodelRegistry.addSerializerAndDeserializer(primitiveType, serializer, deserializer)
}

fun Language.createPrimitiveType(
    primitiveTypeClass: KClass<*>,
    serializer: PrimitiveSerializer<*>? = null,
    deserializer: PrimitiveDeserializer<*>? = null,
): PrimitiveType {
    require(!primitiveTypeClass.isSubclassOf(Node::class))
    val primitiveType =
        createPrimitiveType(
            primitiveTypeClass.simpleName
                ?: throw IllegalArgumentException("Given primitiveTypeClass has no name"),
        )
    MetamodelRegistry.registerMapping(primitiveTypeClass, primitiveType, serializer, deserializer)
    return primitiveType
}

enum class Multiplicity(val optional: Boolean, val multiple: Boolean) {
    OPTIONAL(true, false),
    SINGLE(false, false),
    ZERO_TO_MANY(true, true),
    ONE_TO_MANY(false, true),
}

fun Classifier<*>.createContainment(
    name: String,
    containedClassifier: Classifier<*>,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Containment {
    val containment =
        Containment().apply {
            this.name = name
            this.id = this@createContainment.idForContainedElement(name)
            this.key = this@createContainment.keyForContainedElement(name)
            this.type = containedClassifier
            this.setOptional(multiplicity.optional)
            this.setMultiple(multiplicity.multiple)
        }
    this.addFeature(containment)
    return containment
}

fun Classifier<*>.createReference(
    name: String,
    containedClassifier: Classifier<*>,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Reference {
    val reference =
        Reference().apply {
            this.name = name
            this.id = this@createReference.idForContainedElement(name)
            this.key = this@createReference.keyForContainedElement(name)
            this.type = containedClassifier
            this.setOptional(multiplicity.optional)
            this.setMultiple(multiplicity.multiple)
        }
    this.addFeature(reference)
    return reference
}

fun Classifier<*>.createProperty(
    name: String,
    type: DataType<*>,
    multiplicity: Multiplicity = Multiplicity.SINGLE,
): Property {
    require(!multiplicity.multiple)
    val property =
        Property().apply {
            this.name = name
            this.id = this@createProperty.idForContainedElement(name)
            this.key = this@createProperty.keyForContainedElement(name)
            this.type = type
            this.setOptional(multiplicity.optional)
        }
    this.addFeature(property)
    return property
}

private fun Node.idPrefixForContainedElements(): String {
    return this.id!!.removePrefix("language-").removeSuffix("-id")
}

private fun IKeyed<*>.keyPrefixForContainedElements(): String {
    return this.key!!.removePrefix("language-").removeSuffix("-key")
}

fun Node.idForContainedElement(containedElementName: String): String {
    return "${this.idPrefixForContainedElements()}-${containedElementName.lwIDCleanedVersion()}-id"
}

fun IKeyed<*>.keyForContainedElement(containedElementName: String): String {
    return "${this.keyPrefixForContainedElements()}-${containedElementName.lwIDCleanedVersion()}-key"
}

fun String.lwIDCleanedVersion(): String {
    return this.replace(".", "_")
        .replace(" ", "_")
        .replace("/", "_")
}
