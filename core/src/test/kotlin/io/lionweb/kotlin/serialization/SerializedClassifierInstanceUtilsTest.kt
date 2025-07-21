package io.lionweb.kotlin.serialization

import io.lionweb.kotlin.serialization.chunk.getProperty
import io.lionweb.lioncore.LionCore
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedContainmentValue
import io.lionweb.serialization.data.SerializedPropertyValue
import java.util.Arrays
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializedClassifierInstanceUtilsTest {
    @Test
    fun getPropertyByName() {
        val sc1 =
            SerializedClassifierInstance().apply {
                id = "language-mylanguage-id"
                classifier = MetaPointer.get("LionCore-M3", "2024.1", "Language")
                addPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Language-version"), "1"),
                )
                addPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"), "language-mylanguage-key"),
                )
                addPropertyValue(
                    SerializedPropertyValue.get(
                        MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                        "MyLanguage",
                    ),
                )
                addContainmentValue(
                    SerializedContainmentValue(
                        MetaPointer.get(
                            "LionCore-M3",
                            "2024.1",
                            "Language-entities",
                        ),
                        Arrays.asList("mylanguage-MyConcept-id", "mylanguage-MyInterface-id"),
                    ),
                )
            }
        assertEquals("1", sc1.getProperty(LionCore.getLanguage(), "version"))
        assertEquals("language-mylanguage-key", sc1.getProperty(LionCore.getLanguage(), "key"))
        assertEquals("MyLanguage", sc1.getProperty(LionCore.getLanguage(), "name"))
    }

    @Test
    fun getPropertyByMetaPointer() {
        val sc1 =
            SerializedClassifierInstance().apply {
                id = "language-mylanguage-id"
                classifier = MetaPointer.get("LionCore-M3", "2024.1", "Language")
                addPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Language-version"), "1"),
                )
                addPropertyValue(
                    SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"), "language-mylanguage-key"),
                )
                addPropertyValue(
                    SerializedPropertyValue.get(
                        MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                        "MyLanguage",
                    ),
                )
                addContainmentValue(
                    SerializedContainmentValue(
                        MetaPointer.get(
                            "LionCore-M3",
                            "2024.1",
                            "Language-entities",
                        ),
                        Arrays.asList("mylanguage-MyConcept-id", "mylanguage-MyInterface-id"),
                    ),
                )
            }
        assertEquals("1", sc1.getProperty(MetaPointer.get("LionCore-M3", "2024.1", "Language-version")))
        assertEquals("language-mylanguage-key", sc1.getProperty(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key")))
        assertEquals("MyLanguage", sc1.getProperty(MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name")))
    }
}
