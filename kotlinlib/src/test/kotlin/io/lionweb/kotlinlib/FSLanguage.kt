package com.strumenta.lionweb.kotlin

import java.lang.IllegalStateException

val fsLanguage =
    lwLanguage(
        "fsLanguage",
        Root::class,
        Tenant::class,
        FSUser::class,
        File::class,
        Directory::class,
        TextFile::class,
        FSParsingResult::class,
        FSIssue::class,
        FSStatistics::class,
        FSStatisticsCategory::class,
        FSStatisticEntry::class,
        FSStatisticInstance::class,
        FSAttribute::class,
        FSPosition::class,
    )

class Root : BaseNode() {
    val tenants = multipleContainment<Tenant>("tenants")

    override fun calculateID(): String? = "the-code-insight-studio-root"
}

interface Named {
    val name: String?
}

class Tenant : BaseNode(), Named {
    override var name: String? by property("name")
    val users = multipleContainment<FSUser>("users")
    val directories = multipleContainment<Directory>("directories")

    override fun calculateID(): String? = "tenant-${name!!}"
}

class FSUser : BaseNode(), Named {
    // Note that this means users should be unique across all tenants
    override fun calculateID(): String? = "user-${name!!}"

    override var name: String? by property("name")

    var password: String? by property("password")
}

abstract class File : BaseNode(), Named {
    override var name: String? by property("name")

    override fun calculateID(): String {
        val base =
            if (parent == null) {
                "ROOT_"
            } else {
                parent.id!!
            }
        return "${base}___${(name ?: throw IllegalStateException("Cannot calculate ID if name is not set")).replace('.', '_')}"
    }

    @Implementation
    val path: String
        get() {
            return if (this.parent is File) {
                "${(parent as File).path}/$name!!"
            } else {
                name!!
            }
        }
}

class Directory(id: String? = null) : File() {
    init {
        this.id = id
    }

    val files = multipleContainment<File>("files")
}

class TextFile() : File() {
    var parsingResult: FSParsingResult? by singleContainment("parsingResult")

    var contents: String? by property("contents")

    @Implementation
    val isParsed: Boolean
        get() = parsingResult != null
}

class FSParsingResult() : BaseNode() {
    val issues = multipleContainment<FSIssue>("issues")
    var statistics: FSStatistics? by singleContainment("statistics")
}

/**
 * This emulates Kolasu's Position, which is not visible here
 */
data class MyPosition(val start: MyPoint, val end: MyPoint)

data class MyPoint(
    val line: Int = 0,
    val column: Int = 0,
)

class FSIssue : BaseNode() {
    var message: String? by property("message")
    var severity: String? by property("severity")
    var fsPosition: FSPosition? by singleContainment("fsPosition")
    var position: MyPosition? by property("position")
}

class FSStatistics() : BaseNode() {
    val categories = multipleContainment<FSStatisticsCategory>("categories")
}

class FSStatisticsCategory() : BaseNode(), Named {
    override var name: String? by property("name")
    val entries = multipleContainment<FSStatisticEntry>("entries")
}

class FSStatisticEntry : BaseNode(), Named {
    override var name: String? by property("name")
    val instances = multipleContainment<FSStatisticInstance>("instances")
}

class FSStatisticInstance() : BaseNode() {
    val fsPosition: FSPosition? by singleContainment("fsPosition")
    val attributes = multipleContainment<FSAttribute>("attributes")
}

class FSAttribute() : BaseNode(), Named {
    override var name: String? by property("name")

    val value: String? by property("value")
}

class FSPosition : BaseNode() {
    val startLine: Int? by property("startLine")
    val startColumn: Int? by property("startColumn")
    val endLine: Int? by property("endLine")
    val endColumn: Int? by property("endColumn")
    val fsSource: String? by property("fsSource")
}
