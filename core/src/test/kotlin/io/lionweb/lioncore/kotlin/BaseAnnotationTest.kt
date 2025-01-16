package io.lionweb.lioncore.kotlin

import io.lionweb.lioncore.java.model.ReferenceValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseAnnotationTest {
    @Test
    fun usingSimpleReference() {
        val language =
            lwLanguage(
                "myLanguage",
                MLSimpleNode::class,
                MLSimpleAnnotation::class,
                MLNodeWithSimpleReference::class,
                MLAnnotationWithSimpleReference::class,
                MLAnnotationWithMultipleReference::class,
            )
        val n1 = MLSimpleAnnotation(1)
        n1.id = "n1-id"
        val n2 = MLAnnotationWithSimpleReference()
        n2.id = "n2-id"
        val n3 = MLSimpleAnnotation(2)
        n3.id = "n3-id"
        assertEquals(emptyList(), n2.getReferenceValueByName("simple"))
        assertEquals(null, n2.simple)

        n2.simple = SpecificReferenceValue.create(null, null)
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals(null, n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals(null, n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals(null, n2.simple!!.referredID)
        assertEquals(null, n2.simple!!.resolveInfo)

        val n4 = MLSimpleNode()
        n4.id = "n4-id"
        n2.simple = SpecificReferenceValue.create("foo", n4)
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals("n4-id", n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals("foo", n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals("n4-id", n2.simple!!.referredID)
        assertEquals("foo", n2.simple!!.resolveInfo)

        n2.simple = null
        assertEquals(0, n2.getReferenceValueByName("simple").size)
        assertEquals(null, n2.simple)

        val n5 = MLSimpleNode()
        n5.id = "n5-id"
        n2.setOnlyReferenceValueByName("simple", ReferenceValue(n5, "bar"))
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals("n5-id", n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals("bar", n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals("n5-id", n2.simple!!.referredID)
        assertEquals("bar", n2.simple!!.resolveInfo)

        n2.setOnlyReferenceValueByName("simple", null)
        assertEquals(0, n2.getReferenceValueByName("simple").size)
        assertEquals(null, n2.simple)
    }

    @Test
    fun usingMultipleReference() {
        val language =
            lwLanguage(
                "myLanguage",
                MLSimpleNode::class,
                MLSimpleAnnotation::class,
                MLNodeWithSimpleReference::class,
                MLAnnotationWithSimpleReference::class,
                MLAnnotationWithMultipleReference::class,
            )
        val n1 = MLSimpleAnnotation(1)
        n1.id = "n1-id"
        val n2 = MLAnnotationWithMultipleReference()
        n2.id = "n2-id"
        val n3 = MLSimpleAnnotation(2)
        n3.id = "n3-id"
        assertEquals(emptyList(), n2.getReferenceValueByName("list"))
        assertEquals(emptyList(), n2.list)

        val n4 = MLSimpleNode(4)
        n4.id = "n4-id"
        n2.list =
            mutableListOf(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n4),
            )
        assertEquals(
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n4),
            ),
            n2.getReferenceValueByName("list"),
        )
        assertEquals(
            mutableListOf(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n4),
            ),
            n2.list,
        )

        n2.setReferenceValuesByName(
            "list",
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create("bar", n4),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n4),
            ),
        )
        assertEquals(
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create("bar", n4),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n4),
            ),
            n2.getReferenceValueByName("list"),
        )
        assertEquals(
            mutableListOf(
                SpecificReferenceValue.create("bar", n4),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n4),
            ),
            n2.list,
        )
    }
}
