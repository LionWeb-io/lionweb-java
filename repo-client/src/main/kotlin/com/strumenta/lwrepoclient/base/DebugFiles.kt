package com.strumenta.lwrepoclient.base

import java.io.File

fun debugFileHelper(
    debug: Boolean,
    relativePath: String,
    text: () -> String,
) {
    if (debug) {
        val debugDir = File("debug")
        if (!debugDir.exists()) {
            debugDir.mkdir()
        }
        val file = File(debugDir, relativePath)
        println("SAVING FILE ${file.absolutePath}")
        file.writeText(text.invoke())
    }
}
