package io.lionweb.lioncore.kotlin

class MLSimpleNode() : BaseNode() {
    var value: Int? by property("value")

    constructor(value: Int) : this() {
        this.value = value
    }
}

class MLNodeWithSimpleReference : BaseNode() {
    var simple: SpecificReferenceValue<MLSimpleNode>? by singleReference("simple")
}

class MLNodeWithMultipleReference : BaseNode() {
    var list: MutableList<SpecificReferenceValue<MLSimpleNode>> by multipleReference("list")
}

class MLSimpleAnnotation() : BaseAnnotation() {
    var value: Int? by property("value")

    constructor(value: Int) : this() {
        this.value = value
    }
}

class MLAnnotationWithSimpleReference : BaseAnnotation() {
    var simple: SpecificReferenceValue<MLSimpleNode>? by singleReference("simple")
}

class MLAnnotationWithMultipleReference : BaseAnnotation() {
    var list: MutableList<SpecificReferenceValue<MLSimpleNode>> by multipleReference("list")
}
