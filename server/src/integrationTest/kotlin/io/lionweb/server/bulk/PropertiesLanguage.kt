package io.lionweb.server.bulk

import io.lionweb.LionWebVersion
import io.lionweb.language.*

object PropertiesLanguage {
    val propertiesPartition: Concept
    val propertiesFile: Concept
    val property: Concept
    val propertiesLanguage: Language
    private val lionWebVersionUsed = LionWebVersion.v2023_1

    init {
        // Create language
        val name = "Properties"
        val cleanedName = name.lowercase().replace(".", "_")
        propertiesLanguage = Language(lionWebVersionUsed, name)
        propertiesLanguage.setID("language-$cleanedName-id")
        propertiesLanguage.setVersion("1")
        propertiesLanguage.setKey("language-$cleanedName-key")

        // Create concepts
        propertiesPartition = createConcept(propertiesLanguage, "PropertiesPartition")
        propertiesPartition.isPartition = true
        propertiesFile = createConcept(propertiesLanguage, "PropertiesFile")
        property = createConcept(propertiesLanguage, "Property")

        // Register concept features
        addContainment(propertiesPartition, "files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        val filePath = Property("path", propertiesFile, propertiesFile.id!! + "-path")
        filePath.setKey(propertiesFile.key!! + "-path")
        filePath.type = LionCoreBuiltins.getString(lionWebVersionUsed)
        propertiesFile.addFeature(filePath)

        addContainment(propertiesFile, "properties", property, Multiplicity.ZERO_TO_MANY)
        property.addImplementedInterface(LionCoreBuiltins.getINamed())
    }

    private fun createConcept(language: Language, name: String): Concept {
        val concept = Concept(
            language,
            name,
            language.id!!.replace("language-", "").replace("-id", "") + "-" + name + "-id"
        )
        concept.setKey(language.key!!.replace("language-", "").replace("-key", "") + "-" + name + "-key")
        language.addElement(concept)
        return concept
    }

    private fun addContainment(
        owner: Classifier<*>,
        name: String,
        target: Classifier<*>,
        multiplicity: Multiplicity
    ): Containment {
        val containment = Containment(lionWebVersionUsed)
        containment.name = name
        containment.setID(owner.id!!.replace("-id", "") + "-" + name + "-id")
        containment.setKey(owner.key!!.replace("-key", "") + "-" + name + "-key")
        containment.type = target
        containment.isOptional = multiplicity.optional
        containment.isMultiple = multiplicity.multiple
        owner.addFeature(containment)
        return containment
    }

    enum class Multiplicity(val optional: Boolean, val multiple: Boolean) {
        OPTIONAL(true, false),
        SINGLE(false, false),
        ZERO_TO_MANY(true, true),
        ONE_TO_MANY(false, true);
    }
}
