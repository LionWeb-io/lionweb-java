package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.LionWebVersion
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

/**
 * This object knows about the association between Concepts and Kotlin classes
 * and between PrimitiveTypes and Kotlin classes.
 */
object MetamodelRegistry {
    private val classToClassifier = mutableMapOf<LionWebVersion, MutableMap<KClass<*>, Classifier<*>>>()
    private val classToPrimitiveType = mutableMapOf<LionWebVersion, MutableMap<KClass<*>, PrimitiveType>>()
    private val serializers = mutableMapOf<PrimitiveType, PrimitiveValuesSerialization.PrimitiveSerializer<*>>()
    private val deserializers = mutableMapOf<PrimitiveType, PrimitiveValuesSerialization.PrimitiveDeserializer<*>>()
    private val toIgnoreForInstantiator = mutableSetOf<Classifier<*>>()

    init {
        LionWebVersion.entries.forEach { lionWebVersion ->
            registerMapping(Node::class, LionCoreBuiltins.getNode(lionWebVersion), true)
            registerMapping(String::class, LionCoreBuiltins.getString(lionWebVersion))
            registerMapping(Int::class, LionCoreBuiltins.getInteger(lionWebVersion))
            registerMapping(Boolean::class, LionCoreBuiltins.getBoolean(lionWebVersion))

            // Allow user languages to refer to M3 elements
            registerMapping(Annotation::class, LionCore.getAnnotation(lionWebVersion), true)
            registerMapping(Classifier::class, LionCore.getClassifier(lionWebVersion), true)
            registerMapping(Concept::class, LionCore.getConcept(lionWebVersion), true)
            registerMapping(Containment::class, LionCore.getContainment(lionWebVersion), true)
            registerMapping(DataType::class, LionCore.getDataType(lionWebVersion), true)
            registerMapping(Enumeration::class, LionCore.getEnumeration(lionWebVersion), true)
            registerMapping(EnumerationLiteral::class, LionCore.getEnumerationLiteral(lionWebVersion), true)
            registerMapping(Feature::class, LionCore.getFeature(lionWebVersion), true)
            registerMapping(Interface::class, LionCore.getInterface(lionWebVersion), true)
            registerMapping(Language::class, LionCore.getLanguage(lionWebVersion), true)
            registerMapping(LanguageEntity::class, LionCore.getLanguageEntity(lionWebVersion), true)
            registerMapping(Link::class, LionCore.getLink(lionWebVersion), true)
            registerMapping(PrimitiveType::class, LionCore.getPrimitiveType(lionWebVersion), true)
            registerMapping(Property::class, LionCore.getProperty(lionWebVersion), true)
            registerMapping(Reference::class, LionCore.getReference(lionWebVersion), true)
            if (lionWebVersion != LionWebVersion.v2023_1) {
                registerMapping(StructuredDataType::class, LionCore.getStructuredDataType(lionWebVersion), true)
                registerMapping(Field::class, LionCore.getField(lionWebVersion), true)
            }
        }
    }

    @JvmOverloads
    fun registerMapping(
        kClass: KClass<out ClassifierInstance<*>>,
        classifier: Classifier<*>,
        toIgnoreForInstantiator: Boolean = false
    ) {
        classToClassifier.computeIfAbsent(classifier.lionWebVersion) { mutableMapOf() }[kClass] = classifier
        if (toIgnoreForInstantiator) {
            this.toIgnoreForInstantiator.add(classifier)
        }
    }

    fun registerMapping(
        kClass: KClass<*>,
        primitiveType: PrimitiveType,
        serializer: PrimitiveValuesSerialization.PrimitiveSerializer<*>? = null,
        deserializer: PrimitiveValuesSerialization.PrimitiveDeserializer<*>? = null,
    ) {
        require(!kClass.isSubclassOf(Node::class))
        classToPrimitiveType.computeIfAbsent(primitiveType.lionWebVersion) { mutableMapOf() }[kClass] = primitiveType
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

    fun getConcept(
        kClass: KClass<out Node>,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ): Concept? =
        getClassifier(
            kClass,
            lionWebVersion,
        )?.let {
            it as Concept
        }

    fun getAnnotation(
        kClass: KClass<out AnnotationInstance>,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ): Annotation? = getClassifier(kClass, lionWebVersion)?.let { it as Annotation }

    fun getClassifier(
        kClass: KClass<out ClassifierInstance<*>>,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ): Classifier<*>? =
        classToClassifier[lionWebVersion]?.get(
            kClass,
        )

    fun getPrimitiveType(
        kClass: KClass<*>,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ): PrimitiveType? =
        classToPrimitiveType[lionWebVersion]?.get(
            kClass,
        )

    fun prepareInstantiator(
        instantiator: Instantiator,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ) {
        classToClassifier[lionWebVersion]?.forEach { (kClass, classifier) ->
            if (classifier !in toIgnoreForInstantiator) {
                val constructor = kClass.constructors.find { it.parameters.isEmpty() }
                if (constructor != null) {
                    instantiator.registerCustomDeserializer(classifier.id!!) {
                            _: Classifier<*>,
                            serializedClassifierInstance: SerializedClassifierInstance,
                            _: MutableMap<String, ClassifierInstance<*>>,
                            _: MutableMap<Property, Any>,
                        ->
                        val result = constructor.callBy(emptyMap()) as ClassifierInstance<*>
                        if (result is DynamicClassifierInstance<*>) {
                            result.id = serializedClassifierInstance.id
                        }
                        result
                    }
                }
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
        prepareInstantiator(serialization.instantiator, serialization.lionWebVersion)
        preparePrimitiveValuesSerialization(serialization.primitiveValuesSerialization)
    }
}
