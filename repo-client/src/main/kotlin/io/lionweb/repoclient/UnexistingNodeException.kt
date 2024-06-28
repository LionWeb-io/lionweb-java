package io.lionweb.repoclient

class UnexistingNodeException(val nodeID: String, message: String = "Unexisting node $nodeID", cause: Throwable? = null) :
    RuntimeException(message, cause)
