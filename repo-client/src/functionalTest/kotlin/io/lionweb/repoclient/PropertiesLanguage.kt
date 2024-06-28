package io.lionweb.repoclient

import io.lionweb.kotlinlib.Multiplicity
import io.lionweb.kotlinlib.addConcept
import io.lionweb.kotlinlib.addContainment
import io.lionweb.kotlinlib.lwLanguage
import io.lionweb.lioncore.java.language.Concept
import io.lionweb.lioncore.java.language.LionCoreBuiltins

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
