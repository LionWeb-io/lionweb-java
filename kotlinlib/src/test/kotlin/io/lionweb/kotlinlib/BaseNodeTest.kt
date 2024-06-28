package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.model.ReferenceValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseNodeTest {
    @Test
    fun usingSimpleReference() {
        val language =
            lwLanguage(
                "myLanguage",
                MLSimpleNode::class,
                MLNodeWithSimpleReference::class,
            )
        val n1 = MLSimpleNode(1)
        n1.id = "n1-id"
        val n2 = MLNodeWithSimpleReference()
        n2.id = "n2-id"
        val n3 = MLSimpleNode(2)
        n3.id = "n3-id"
        assertEquals(emptyList(), n2.getReferenceValueByName("simple"))
        assertEquals(null, n2.simple)

        n2.simple = SpecificReferenceValue.create(null, null)
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals(null, n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals(null, n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals(null, n2.simple!!.referredID)
        assertEquals(null, n2.simple!!.resolveInfo)

        n2.simple = SpecificReferenceValue.create("foo", n1)
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals("n1-id", n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals("foo", n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals("n1-id", n2.simple!!.referredID)
        assertEquals("foo", n2.simple!!.resolveInfo)

        n2.simple = null
        assertEquals(0, n2.getReferenceValueByName("simple").size)
        assertEquals(null, n2.simple)

        n2.setOnlyReferenceValueByName("simple", ReferenceValue(n3, "bar"))
        assertEquals(1, n2.getReferenceValueByName("simple").size)
        assertEquals("n3-id", n2.getReferenceValueByName("simple")[0].referredID)
        assertEquals("bar", n2.getReferenceValueByName("simple")[0].resolveInfo)
        assertEquals("n3-id", n2.simple!!.referredID)
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
                MLNodeWithMultipleReference::class,
            )
        val n1 = MLSimpleNode(1)
        n1.id = "n1-id"
        val n2 = MLNodeWithMultipleReference()
        n2.id = "n2-id"
        val n3 = MLSimpleNode(2)
        n3.id = "n3-id"
        assertEquals(emptyList(), n2.getReferenceValueByName("list"))
        assertEquals(emptyList(), n2.list)

        n2.list =
            mutableListOf(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n1),
            )
        assertEquals(
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n1),
            ),
            n2.getReferenceValueByName("list"),
        )
        assertEquals(
            mutableListOf(
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("bar", n1),
            ),
            n2.list,
        )

        n2.setReferenceValuesByName(
            "list",
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create("bar", n1),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n1),
            ),
        )
        assertEquals(
            mutableListOf<SpecificReferenceValue<MLSimpleNode>>(
                SpecificReferenceValue.create("bar", n1),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n1),
            ),
            n2.getReferenceValueByName("list"),
        )
        assertEquals(
            mutableListOf(
                SpecificReferenceValue.create("bar", n1),
                SpecificReferenceValue.create(null, null),
                SpecificReferenceValue.create("zum", n1),
            ),
            n2.list,
        )
    }
}
