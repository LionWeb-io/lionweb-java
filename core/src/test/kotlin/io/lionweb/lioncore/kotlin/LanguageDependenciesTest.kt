package io.lionweb.lioncore.kotlin

import io.lionweb.language.Concept
import io.lionweb.language.Containment
import io.lionweb.language.Language
import io.lionweb.language.Reference
import io.lionweb.lioncore.LionCore
import io.lionweb.model.Node
import io.lionweb.serialization.SerializationProvider
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class LanguageDependenciesTest {
    @Test fun invalidSuperclassThrows() {
        val lang = Language("lang", "lang", "lang")
        assertFailsWith<IllegalStateException> {
            lang.createConcepts(LanguageDependenciesTest::class as KClass<out Node>)
        }
    }

    @Test fun conceptExtendsOtherLanguage() {
        val sup = Language("sup", "sup", "sup")
        sup.createConcepts(Super::class)
        val sub = Language("sub", "sub", "sub")
        sub.createConcepts(Sub::class)
        assertEquals(1, sub.elements.size)
        val stdSer = SerializationProvider.getStandardJsonSerialization()
        val jsonString = stdSer.serializeTreesToJsonString(sub, sup)
        val nodes = stdSer.deserializeToNodes(jsonString)
        assertEquals(4, nodes.size)
    }

    @Test fun conceptUsesOtherLanguage() {
        val sup = Language("sup", "sup", "sup")
        sup.createConcepts(Super::class)
        val sub = Language("sub", "sub", "sub")
        sub.createConcepts(Comp::class)
        assertEquals(1, sub.elements.size)
        val stdSer = SerializationProvider.getStandardJsonSerialization()
        val jsonString = stdSer.serializeTreesToJsonString(sub, sup)
        val nodes = stdSer.deserializeToNodes(jsonString)
        assertEquals(6, nodes.size)
    }

    @Test fun languageCanUseM3() {
        val extM3 = Language("ext-m3", "ext-m3", "ext-m3")
        extM3.createConcepts(ExtendedConcept::class, CompM3::class)
        val stdSer = SerializationProvider.getStandardJsonSerialization()
        val jsonString = stdSer.serializeTreesToJsonString(extM3)
        val nodes = stdSer.deserializeToNodes(jsonString)
        assertEquals(5, nodes.size)
        assertIs<Language>(nodes[0])
        var concept = nodes[1]
        assertIs<Concept>(concept)
        assertEquals(concept.extendedConcept, LionCore.getConcept())
        assertEquals("ExtendedConcept", concept.name)
        concept = nodes[2]
        assertIs<Concept>(concept)
        assertEquals("CompM3", concept.name)
        assertIs<Containment>(nodes[3])
        assertIs<Reference>(nodes[4])
    }
}

open class Super : BaseNode()

class Sub : Super()

class Comp : BaseNode() {
    val comp: Super? by singleContainment("comp")
    val ref: SpecificReferenceValue<Super>? by singleReference("ref")
}

class ExtendedConcept(
    language: Language? = null,
    name: String? = null,
    id: String,
    key: String? = null,
) : Concept(language, name, id, key)

class CompM3 : BaseNode() {
    val comp: ExtendedConcept? by singleContainment("comp")
    val ref: SpecificReferenceValue<Concept>? by singleReference("ref")
}
