package com.strumenta.lwrepoclient.base

data class RequestFailureException(
    val url: String,
    val uncompressedBody: String?,
    val responseCode: Int,
    val responseBody: String?,
) : RuntimeException("Request to $url failed with code $responseCode: $responseBody")
