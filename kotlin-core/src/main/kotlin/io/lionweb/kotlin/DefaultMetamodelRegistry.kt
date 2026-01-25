package io.lionweb.kotlin

import io.lionweb.LionWebVersion
import io.lionweb.language.Annotation
import io.lionweb.language.Classifier
import io.lionweb.language.Concept
import io.lionweb.language.Containment
import io.lionweb.language.DataType
import io.lionweb.language.Enumeration
import io.lionweb.language.EnumerationLiteral
import io.lionweb.language.Feature
import io.lionweb.language.Field
import io.lionweb.language.Interface
import io.lionweb.language.Language
import io.lionweb.language.LanguageEntity
import io.lionweb.language.Link
import io.lionweb.language.LionCoreBuiltins
import io.lionweb.language.PrimitiveType
import io.lionweb.language.Property
import io.lionweb.language.Reference
import io.lionweb.language.StructuredDataType
import io.lionweb.lioncore.LionCore
import io.lionweb.model.AnnotationInstance
import io.lionweb.model.ClassifierInstance
import io.lionweb.model.Node
import io.lionweb.model.impl.DynamicClassifierInstance
import io.lionweb.serialization.AbstractSerialization
import io.lionweb.serialization.DataTypesValuesSerialization
import io.lionweb.serialization.Instantiator
import io.lionweb.serialization.data.SerializedClassifierInstance
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * This object knows about the association between Concepts and Kotlin classes
 * and between PrimitiveTypes and Kotlin classes.
 */
object DefaultMetamodelRegistry : MetamodelRegistry by MetamodelRegistryImpl()

interface MetamodelRegistry {
    fun registerMapping(
        kClass: KClass<out ClassifierInstance<*>>,
        classifier: Classifier<*>,
        consideredByInstantiator: Boolean = true,
    )

    fun registerMapping(
        kClass: KClass<*>,
        primitiveType: PrimitiveType,
        serializer: DataTypesValuesSerialization.DataTypeSerializer<*>? = null,
        deserializer: DataTypesValuesSerialization.DataTypeDeserializer<*>? = null,
    )

    fun addSerializerAndDeserializer(
        primitiveType: PrimitiveType,
        serializer: DataTypesValuesSerialization.DataTypeSerializer<*>,
        deserializer: DataTypesValuesSerialization.DataTypeDeserializer<*>,
    )

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
    ): Classifier<*>?

    fun getPrimitiveType(
        kClass: KClass<*>,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    ): PrimitiveType?

    fun prepareInstantiator(
        instantiator: Instantiator,
        lionWebVersion: LionWebVersion = LionWebVersion.currentVersion,
    )

    fun preparePrimitiveValuesSerialization(primitiveValuesSerialization: DataTypesValuesSerialization)

    fun prepareSerialization(serialization: AbstractSerialization)
}

class MetamodelRegistryImpl : MetamodelRegistry {
    private val classToClassifier = mutableMapOf<LionWebVersion, MutableMap<KClass<*>, Classifier<*>>>()
    private val classToPrimitiveType = mutableMapOf<LionWebVersion, MutableMap<KClass<*>, PrimitiveType>>()
    private val serializers = mutableMapOf<PrimitiveType, DataTypesValuesSerialization.DataTypeSerializer<*>>()
    private val deserializers = mutableMapOf<PrimitiveType, DataTypesValuesSerialization.DataTypeDeserializer<*>>()
    private val instantiatorExclusionList = mutableSetOf<Classifier<*>>()

    init {
        LionWebVersion.entries.forEach { lionWebVersion ->
            registerMapping(Node::class, LionCoreBuiltins.getNode(lionWebVersion), false)
            registerMapping(String::class, LionCoreBuiltins.getString(lionWebVersion))
            registerMapping(Int::class, LionCoreBuiltins.getInteger(lionWebVersion))
            registerMapping(Boolean::class, LionCoreBuiltins.getBoolean(lionWebVersion))

            // Allow user languages to refer to M3 elements
            registerMapping(Annotation::class, LionCore.getAnnotation(lionWebVersion), false)
            registerMapping(Classifier::class, LionCore.getClassifier(lionWebVersion), false)
            registerMapping(Concept::class, LionCore.getConcept(lionWebVersion), false)
            registerMapping(Containment::class, LionCore.getContainment(lionWebVersion), false)
            registerMapping(DataType::class, LionCore.getDataType(lionWebVersion), false)
            registerMapping(Enumeration::class, LionCore.getEnumeration(lionWebVersion), false)
            registerMapping(EnumerationLiteral::class, LionCore.getEnumerationLiteral(lionWebVersion), false)
            registerMapping(Feature::class, LionCore.getFeature(lionWebVersion), false)
            registerMapping(Interface::class, LionCore.getInterface(lionWebVersion), false)
            registerMapping(Language::class, LionCore.getLanguage(lionWebVersion), false)
            registerMapping(LanguageEntity::class, LionCore.getLanguageEntity(lionWebVersion), false)
            registerMapping(Link::class, LionCore.getLink(lionWebVersion), false)
            registerMapping(PrimitiveType::class, LionCore.getPrimitiveType(lionWebVersion), false)
            registerMapping(Property::class, LionCore.getProperty(lionWebVersion), false)
            registerMapping(Reference::class, LionCore.getReference(lionWebVersion), false)
            if (lionWebVersion != LionWebVersion.v2023_1) {
                registerMapping(StructuredDataType::class, LionCore.getStructuredDataType(lionWebVersion), false)
                registerMapping(Field::class, LionCore.getField(lionWebVersion), false)
            }
        }
    }

    @JvmOverloads
    override fun registerMapping(
        kClass: KClass<out ClassifierInstance<*>>,
        classifier: Classifier<*>,
        consideredByInstantiator: Boolean,
    ) {
        classToClassifier.computeIfAbsent(classifier.lionWebVersion) { mutableMapOf() }[kClass] = classifier
        if (!consideredByInstantiator) {
            this.instantiatorExclusionList.add(classifier)
        }
    }

    override fun registerMapping(
        kClass: KClass<*>,
        primitiveType: PrimitiveType,
        serializer: DataTypesValuesSerialization.DataTypeSerializer<*>?,
        deserializer: DataTypesValuesSerialization.DataTypeDeserializer<*>?,
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

    override fun addSerializerAndDeserializer(
        primitiveType: PrimitiveType,
        serializer: DataTypesValuesSerialization.DataTypeSerializer<*>,
        deserializer: DataTypesValuesSerialization.DataTypeDeserializer<*>,
    ) {
        serializers[primitiveType] = serializer
        deserializers[primitiveType] = deserializer
    }

    override fun getClassifier(
        kClass: KClass<out ClassifierInstance<*>>,
        lionWebVersion: LionWebVersion,
    ): Classifier<*>? =
        classToClassifier[lionWebVersion]?.get(
            kClass,
        )

    override fun getPrimitiveType(
        kClass: KClass<*>,
        lionWebVersion: LionWebVersion,
    ): PrimitiveType? =
        classToPrimitiveType[lionWebVersion]?.get(
            kClass,
        )

    override fun prepareInstantiator(
        instantiator: Instantiator,
        lionWebVersion: LionWebVersion,
    ) {
        classToClassifier[lionWebVersion]?.forEach { (kClass, classifier) ->
            if (classifier !in instantiatorExclusionList) {
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
                            result.setID(serializedClassifierInstance.id ?: throw IllegalStateException())
                        }
                        result
                    }
                }
            }
        }
    }

    override fun preparePrimitiveValuesSerialization(primitiveValuesSerialization: DataTypesValuesSerialization) {
        serializers.forEach { primitiveType, serializer ->
            primitiveValuesSerialization.registerSerializer(
                primitiveType.id ?: throw IllegalStateException("PrimitiveType id should not be null"),
                serializer,
            )
        }
        deserializers.forEach { primitiveType, deserializer ->
            primitiveValuesSerialization.registerDeserializer(
                primitiveType.id ?: throw IllegalStateException("PrimitiveType id should not be null"),
                deserializer,
            )
        }
    }

    override fun prepareSerialization(serialization: AbstractSerialization) {
        prepareInstantiator(serialization.instantiator, serialization.lionWebVersion)
        preparePrimitiveValuesSerialization(serialization.primitiveValuesSerialization)
    }
}
