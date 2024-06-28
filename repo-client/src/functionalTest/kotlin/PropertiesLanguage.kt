import com.strumenta.lionweb.kotlin.Multiplicity
import com.strumenta.lionweb.kotlin.addConcept
import com.strumenta.lionweb.kotlin.addContainment
import com.strumenta.lionweb.kotlin.lwLanguage
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
