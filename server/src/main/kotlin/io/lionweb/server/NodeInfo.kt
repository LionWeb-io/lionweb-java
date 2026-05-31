package io.lionweb.server

import java.util.concurrent.ConcurrentHashMap

data class PropertyValue(
    val key: String,
    val languageKey: String?,
    val languageVersion: String?,
    val value: String?,
)

class NodeInfo(
    val id: String,
    val classifierKey: String?,
    val classifierLanguageKey: String?,
    val classifierLanguageVersion: String?,
    val parentId: String?,
    val containmentKey: String?,
    val containmentLanguageKey: String?,
    val containmentLanguageVersion: String?,
    val properties: ConcurrentHashMap<String, PropertyValue> = ConcurrentHashMap(),
    val children: ConcurrentHashMap<String, MutableList<String>> = ConcurrentHashMap(),
)
