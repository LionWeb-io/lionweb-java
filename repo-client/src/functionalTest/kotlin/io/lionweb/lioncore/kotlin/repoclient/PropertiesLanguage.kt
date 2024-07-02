package io.lionweb.lioncore.kotlin.repoclient

import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.kotlin.Multiplicity
import io.lionweb.lioncore.kotlin.addConcept
import io.lionweb.lioncore.kotlin.addContainment
import io.lionweb.lioncore.kotlin.lwLanguage

val propertiesPartition: Concept
val propertiesFile: Concept
val property: Concept
val propertiesLanguage =
    lwLanguage("Properties").apply {
        propertiesPartition = addConcept("PropertiesPartition")
        propertiesFile = addConcept("PropertiesFile")
        property = addConcept("Property")

        propertiesPartition.isPartition = true
        propertiesPartition.addContainment("files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        propertiesFile.addContainment("properties", property, Multiplicity.ZERO_TO_MANY)
        property.addImplementedInterface(LionCoreBuiltins.getINamed())
    }
