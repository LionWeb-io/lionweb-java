package io.lionweb.lioncore.kotlin.repoclient

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.kotlin.Multiplicity
import io.lionweb.lioncore.kotlin.createConcept
import io.lionweb.lioncore.kotlin.addContainment
import io.lionweb.lioncore.kotlin.lwLanguage

val propertiesPartition: Concept
val propertiesFile: Concept
val property: Concept
val propertiesLanguage =
    lwLanguage("Properties").apply {
        propertiesPartition = createConcept("PropertiesPartition")
        propertiesFile = createConcept("PropertiesFile")
        property = createConcept("Property")

        propertiesPartition.isPartition = true
        propertiesPartition.addContainment("files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        propertiesFile.addContainment("properties", property, Multiplicity.ZERO_TO_MANY)
        property.addImplementedInterface(LionCoreBuiltins.getINamed())
    }
