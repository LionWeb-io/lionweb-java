package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.language.Classifier
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.java.language.PrimitiveType
import io.lionweb.lioncore.java.language.Property
import io.lionweb.lioncore.java.model.ClassifierInstance
import io.lionweb.lioncore.java.model.Node
import io.lionweb.lioncore.java.model.impl.DynamicNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
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
    }

    fun registerMapping(
        kClass: KClass<out Node>,
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

    fun getClassifier(kClass: KClass<out Node>): Classifier<*>? = classToClassifier[kClass]

    fun getPrimitiveType(kClass: KClass<*>): PrimitiveType? = classToPrimitiveType[kClass]

    fun prepareJsonSerialization(jsonSerialization: JsonSerialization) {
        classToClassifier.forEach { (kClass, concept) ->
            jsonSerialization.instantiator.registerCustomDeserializer(concept.id!!) {
                    classifier: Classifier<*>,
                    serializedClassifierInstance: SerializedClassifierInstance,
                    nodes: MutableMap<String, ClassifierInstance<*>>,
                    propertyValues: MutableMap<Property, Any>,
                ->
                val result = kClass.primaryConstructor!!.callBy(emptyMap()) as Node
                if (result is DynamicNode) {
                    result.id = serializedClassifierInstance.id
                }
                result
            }
        }
        classToPrimitiveType.forEach { (kClass, primitiveType) ->
            val serializer = serializers[primitiveType]
            val deserializer = deserializers[primitiveType]
            if (serializer != null) {
                jsonSerialization.primitiveValuesSerialization.registerSerializer(primitiveType.id, serializer)
            }
            if (deserializer != null) {
                jsonSerialization.primitiveValuesSerialization.registerDeserializer(primitiveType.id, deserializer)
            }
        }
    }
}
