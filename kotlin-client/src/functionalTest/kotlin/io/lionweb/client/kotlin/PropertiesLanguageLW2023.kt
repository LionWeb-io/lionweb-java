package io.lionweb.client.kotlin

import io.lionweb.LionWebVersion
import io.lionweb.kotlin.Multiplicity
import io.lionweb.kotlin.createConcept
import io.lionweb.kotlin.createContainment
import io.lionweb.kotlin.lwLanguage
import io.lionweb.language.Concept
import io.lionweb.language.LionCoreBuiltins

val propertiesPartitionLW2023: Concept
val propertiesFileLW2023: Concept
val propertyLW2023: Concept
val propertiesLanguageLW2023 =
    lwLanguage("Properties", lionWebVersion = LionWebVersion.v2023_1).apply {
        propertiesPartitionLW2023 = createConcept("PropertiesPartition")
        propertiesFileLW2023 = createConcept("PropertiesFile")
        propertyLW2023 = createConcept("Property")

        propertiesPartitionLW2023.isPartition = true
        propertiesPartitionLW2023.createContainment("files", propertiesFile, Multiplicity.ZERO_TO_MANY)
        propertiesFileLW2023.createContainment("properties", property, Multiplicity.ZERO_TO_MANY)
        propertyLW2023.addImplementedInterface(LionCoreBuiltins.getINamed(LionWebVersion.v2023_1))
    }
