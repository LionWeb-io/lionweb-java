package io.lionweb.lioncore.kotlin.repoclient

class UnexistingNodeException(val nodeID: String, message: String = "Unexisting node $nodeID", cause: Throwable? = null) :
    RuntimeException(message, cause)
