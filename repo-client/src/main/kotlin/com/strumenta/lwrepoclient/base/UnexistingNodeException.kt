package com.strumenta.lwrepoclient.base

class UnexistingNodeException(val nodeID: String, message: String = "Unexisting node $nodeID", cause: Throwable? = null) :
    RuntimeException(message, cause)
