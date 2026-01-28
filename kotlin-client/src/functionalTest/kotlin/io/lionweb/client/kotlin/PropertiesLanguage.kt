package io.lionweb.client.kotlin

import io.lionweb.kotlin.Multiplicity
import io.lionweb.kotlin.createConcept
import io.lionweb.kotlin.createContainment
import io.lionweb.kotlin.lwLanguage
import io.lionweb.language.Concept
import io.lionweb.language.LionCoreBuiltins

val propertiesPartition: Concept
val propertiesFile: Concept
val property: Concept
val propertiesLanguage =
    lwLanguage("Properties").apply {
        propertiesPartition = createConcept("PropertiesPartition")
        propertiesFile = createConcept("PropertiesFile")
        property = createConcept("Property")

        propertiesPartition.isPartition = true
        propertiesPartition.createContainment("files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        propertiesFile.createContainment("properties", property, Multiplicity.ZERO_TO_MANY)
        property.addImplementedInterface(LionCoreBuiltins.getINamed())
    }
