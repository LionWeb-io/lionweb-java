package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Annotation
import io.lionweb.lioncore.java.language.Classifier
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.Containment
import io.lionweb.lioncore.java.language.DataType
import io.lionweb.lioncore.java.language.Enumeration
import io.lionweb.lioncore.java.language.EnumerationLiteral
import io.lionweb.lioncore.java.language.Feature
import io.lionweb.lioncore.java.language.Field
import io.lionweb.lioncore.java.language.Interface
import io.lionweb.lioncore.java.language.Language
import io.lionweb.lioncore.java.language.LanguageEntity
import io.lionweb.lioncore.java.language.Link
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.java.language.PrimitiveType
import io.lionweb.lioncore.java.language.Property
import io.lionweb.lioncore.java.language.Reference
import io.lionweb.lioncore.java.language.StructuredDataType
import io.lionweb.lioncore.java.model.AnnotationInstance
import io.lionweb.lioncore.java.model.ClassifierInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.DynamicClassifierInstance
import io.lionweb.lioncore.java.self.LionCore
import io.lionweb.lioncore.java.serialization.AbstractSerialization
import io.lionweb.lioncore.java.serialization.Instantiator
import io.lionweb.lioncore.java.serialization.PrimitiveValuesSerialization
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

/**
 * This object knows about the association between Concepts and Kotlin classes
 * and between PrimitiveTypes and Kotlin classes.
 */
object MetamodelRegistry {
    private val classToClassifier = mutableMapOf<KClass<*>, Classifier<*>>()
    private val classToPrimitiveType = mutableMapOf<KClass<*>, PrimitiveType>()
    private val serializers = mutableMapOf<PrimitiveType, PrimitiveValuesSerialization.PrimitiveSerializer<*>>()
    private val deserializers = mutableMapOf<PrimitiveType, PrimitiveValuesSerialization.PrimitiveDeserializer<*>>()

    init {
        registerMapping(Node::class, LionCoreBuiltins.getNode())
        registerMapping(String::class, LionCoreBuiltins.getString())
        registerMapping(Int::class, LionCoreBuiltins.getInteger())
        registerMapping(Boolean::class, LionCoreBuiltins.getBoolean())

        // Allow user languages to refer to M3 elements
        registerMapping(Annotation::class, LionCore.getAnnotation())
        registerMapping(Classifier::class, LionCore.getClassifier())
        registerMapping(Concept::class, LionCore.getConcept())
        registerMapping(Containment::class, LionCore.getContainment())
        registerMapping(DataType::class, LionCore.getDataType())
        registerMapping(Enumeration::class, LionCore.getEnumeration())
        registerMapping(EnumerationLiteral::class, LionCore.getEnumerationLiteral())
        registerMapping(Feature::class, LionCore.getFeature())
        registerMapping(Field::class, LionCore.getField())
        registerMapping(Interface::class, LionCore.getInterface())
        registerMapping(Language::class, LionCore.getLanguage())
        registerMapping(LanguageEntity::class, LionCore.getLanguageEntity())
        registerMapping(Link::class, LionCore.getLink())
        registerMapping(PrimitiveType::class, LionCore.getPrimitiveType())
        registerMapping(Property::class, LionCore.getProperty())
        registerMapping(Reference::class, LionCore.getReference())
        registerMapping(StructuredDataType::class, LionCore.getStructuredDataType())
    }

    fun registerMapping(
        kClass: KClass<out ClassifierInstance<*>>,
        classifier: Classifier<*>,
    ) {
        classToClassifier[kClass] = classifier
    }

    fun registerMapping(
        kClass: KClass<*>,
        primitiveType: PrimitiveType,
        serializer: PrimitiveValuesSerialization.PrimitiveSerializer<*>? = null,
        deserializer: PrimitiveValuesSerialization.PrimitiveDeserializer<*>? = null,
    ) {
        require(!kClass.isSubclassOf(Node::class))
        classToPrimitiveType[kClass] = primitiveType
        if (serializer != null) {
            serializers[primitiveType] = serializer
        }
        if (deserializer != null) {
            deserializers[primitiveType] = deserializer
        }
    }

    fun addSerializerAndDeserializer(
        primitiveType: PrimitiveType,
        serializer: PrimitiveValuesSerialization.PrimitiveSerializer<*>,
        deserializer: PrimitiveValuesSerialization.PrimitiveDeserializer<*>,
    ) {
        serializers[primitiveType] = serializer
        deserializers[primitiveType] = deserializer
    }

    fun getConcept(kClass: KClass<out Node>): Concept? = getClassifier(kClass)?.let { it as Concept }

    fun getAnnotation(kClass: KClass<out AnnotationInstance>): Annotation? = getClassifier(kClass)?.let { it as Annotation }

    fun getClassifier(kClass: KClass<out ClassifierInstance<*>>): Classifier<*>? = classToClassifier[kClass]

    fun getPrimitiveType(kClass: KClass<*>): PrimitiveType? = classToPrimitiveType[kClass]

    fun prepareInstantiator(instantiator: Instantiator) {
        classToClassifier.forEach { (kClass, classifier) ->
            instantiator.registerCustomDeserializer(classifier.id!!) {
                    _: Classifier<*>,
                    serializedClassifierInstance: SerializedClassifierInstance,
                    _: MutableMap<String, ClassifierInstance<*>>,
                    _: MutableMap<Property, Any>,
                ->
                val result = kClass.primaryConstructor!!.callBy(emptyMap()) as ClassifierInstance<*>
                if (result is DynamicClassifierInstance<*>) {
                    result.id = serializedClassifierInstance.id
                }
                result
            }
        }
    }

    fun preparePrimitiveValuesSerialization(primitiveValuesSerialization: PrimitiveValuesSerialization) {
        serializers.forEach { primitiveType, serializer ->
            primitiveValuesSerialization.registerSerializer(primitiveType.id, serializer)
        }
        deserializers.forEach { primitiveType, deserializer ->
            primitiveValuesSerialization.registerDeserializer(primitiveType.id, deserializer)
        }
    }

    fun prepareJsonSerialization(serialization: AbstractSerialization) {
        prepareInstantiator(serialization.instantiator)
        preparePrimitiveValuesSerialization(serialization.primitiveValuesSerialization)
    }
}
