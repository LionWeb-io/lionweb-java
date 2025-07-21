package io.lionweb.kotlin.serialization

import io.lionweb.LionWebVersion
import io.lionweb.kotlin.createConcept
import io.lionweb.kotlin.createInterface
import io.lionweb.kotlin.lwLanguage
import io.lionweb.serialization.data.MetaPointer
import io.lionweb.serialization.data.SerializedChunk
import io.lionweb.serialization.data.SerializedClassifierInstance
import io.lionweb.serialization.data.SerializedContainmentValue
import io.lionweb.serialization.data.SerializedPropertyValue
import io.lionweb.serialization.data.UsedLanguage
import java.util.Arrays
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationUtilsTest {
    @Test
    fun cleanID() {
        assertEquals("", cleanId(""))
        assertEquals("a", cleanId("a"))
        assertEquals("a1243", cleanId("a1243"))
        assertEquals("as980943KK-_at__at__", cleanId("as980943:KK::#%@@_+(*()"))
        assertEquals("", cleanId("``"))
    }

    @Test
    fun removeCharactersInvalidInLionWebIDs() {
        assertEquals("", "".removeCharactersInvalidInLionWebIDs())
        assertEquals("a", "a".removeCharactersInvalidInLionWebIDs())
        assertEquals("a1243", "a1243".removeCharactersInvalidInLionWebIDs())
        assertEquals("as980943KK_", "as980943:KK::#%@@_+(*()".removeCharactersInvalidInLionWebIDs())
        assertEquals("", "``".removeCharactersInvalidInLionWebIDs())
    }

    @Test
    fun nodeToChunk() {
        val l1 =
            lwLanguage("MyLanguage").apply {
                createConcept("MyConcept").apply {
                }
                createInterface("MyInterface")
            }
        assertEquals(
            SerializedChunk().apply {
                serializationFormatVersion = LionWebVersion.currentVersion.versionString
                addLanguage(UsedLanguage("LionCore-M3", "2024.1"))
                addLanguage(UsedLanguage("LionCore-builtins", "2024.1"))
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
                addClassifierInstance(sc1)
                val sc2 =
                    SerializedClassifierInstance().apply {
                        id = "mylanguage-MyConcept-id"
                        classifier = MetaPointer.get("LionCore-M3", "2024.1", "Concept")

                        addPropertyValue(
                            SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Concept-abstract"), "false"),
                        )
                        addPropertyValue(
                            SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "Concept-partition"), "false"),
                        )
                        addPropertyValue(
                            SerializedPropertyValue.get(MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"), "mylanguage-MyConcept-key"),
                        )
                        addPropertyValue(
                            SerializedPropertyValue.get(
                                MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                                "MyConcept",
                            ),
                        )
                        parentNodeID = sc1.id
                    }
                addClassifierInstance(sc2)
                val sc3 =
                    SerializedClassifierInstance().apply {
                        id = "mylanguage-MyInterface-id"
                        classifier = MetaPointer.get("LionCore-M3", "2024.1", "Interface")
                        addPropertyValue(
                            SerializedPropertyValue.get(
                                MetaPointer.get("LionCore-M3", "2024.1", "IKeyed-key"),
                                "mylanguage-MyInterface-key",
                            ),
                        )
                        addPropertyValue(
                            SerializedPropertyValue.get(
                                MetaPointer.get("LionCore-builtins", "2024.1", "LionCore-builtins-INamed-name"),
                                "MyInterface",
                            ),
                        )
                        parentNodeID = sc1.id
                    }
                addClassifierInstance(sc3)
            },
            l1.toChunk(),
        )
    }
}
