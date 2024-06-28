package com.strumenta.lionweb.kotlin

import io.lionweb.lioncore.java.language.LionCoreBuiltins
import io.lionweb.lioncore.java.model.impl.ProxyNode
import io.lionweb.lioncore.java.serialization.JsonSerialization
import io.lionweb.lioncore.java.serialization.UnavailableNodePolicy
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MetamodelDefinitionTest {
    @Test
    fun addClass() {
        val language =
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
                MyPosition::class,
            )
        assertEquals(15, language.elements.size)

        val root = language.getConceptByName("Root")!!
        val tenant = language.getConceptByName("Tenant")!!
        val fsUser = language.getConceptByName("FSUser")!!
        val file = language.getConceptByName("File")!!
        val directory = language.getConceptByName("Directory")!!
        val textFile = language.getConceptByName("TextFile")!!
        val fsParsingResult = language.getConceptByName("FSParsingResult")!!
        val fsIssue = language.getConceptByName("FSIssue")!!
        val fsPosition = language.getPrimitiveTypeByName("MyPosition")!!

        assertEquals(false, root.isAbstract)
        assertEquals(null, root.extendedConcept)
        assertEquals(1, root.features.size)
        val rootTenants = root.getContainmentByName("tenants")!!
        assertEquals(true, rootTenants.isMultiple)
        assertEquals(tenant, rootTenants.type)

        assertEquals(false, tenant.isAbstract)
        assertEquals(null, tenant.extendedConcept)
        assertEquals(3, tenant.features.size)
        val tenantName = tenant.getPropertyByName("name")!!
        assertEquals(LionCoreBuiltins.getString(), tenantName.type)
        val tenantUsers = tenant.getContainmentByName("users")!!
        assertEquals(true, tenantUsers.isMultiple)
        assertEquals(fsUser, tenantUsers.type)
        val tenantDirectories = tenant.getContainmentByName("directories")!!
        assertEquals(true, tenantDirectories.isMultiple)
        assertEquals(directory, tenantDirectories.type)

        assertEquals(false, fsUser.isAbstract)
        assertEquals(null, fsUser.extendedConcept)
        assertEquals(2, fsUser.features.size)
        val fsUserName = fsUser.getPropertyByName("name")!!
        assertEquals(LionCoreBuiltins.getString(), fsUserName.type)
        val fsUserPassword = fsUser.getPropertyByName("password")!!
        assertEquals(LionCoreBuiltins.getString(), fsUserPassword.type)

        assertEquals(true, file.isAbstract)
        assertEquals(null, file.extendedConcept)
        assertEquals(1, file.features.size)
        val fileName = file.getPropertyByName("name")!!
        assertEquals(LionCoreBuiltins.getString(), fileName.type)

        assertEquals(false, directory.isAbstract)
        assertEquals(file, directory.extendedConcept)
        assertEquals(1, directory.features.size)
        val directoryFiles = directory.getContainmentByName("files")!!
        assertEquals(true, directoryFiles.isMultiple)
        assertEquals(file, directoryFiles.type)

        assertEquals(false, textFile.isAbstract)
        assertEquals(file, textFile.extendedConcept)
        assertEquals(2, textFile.features.size)
        val textFileParsingResult = textFile.getContainmentByName("parsingResult")!!
        assertEquals(false, textFileParsingResult.isMultiple)
        assertEquals(fsParsingResult, textFileParsingResult.type)
        val textFileContents = textFile.getPropertyByName("contents")!!
        assertEquals(LionCoreBuiltins.getString(), textFileContents.type)

        assertEquals(false, fsIssue.isAbstract)
        assertEquals(null, fsIssue.extendedConcept)
        assertEquals(4, fsIssue.features.size)
        val fsIssueFsPosition = fsIssue.getContainmentByName("fsPosition")!!
        val fsIssuePosition = fsIssue.getPropertyByName("position")!!
        assertEquals(fsPosition, fsIssuePosition.type)
    }

    @Test
    fun getConcept() {
        val language =
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
        val root = Root()
        val rootConcept = language.getConceptByName("Root")!!

        assertEquals(rootConcept, root.classifier)
    }

    @Test
    fun serializeAndDeserializeRoot() {
        val language =
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
        val root = Root()

        val jsonSerialization =
            JsonSerialization.getStandardSerialization().apply {
                MetamodelRegistry.prepareJsonSerialization(this)
            }

        val serializedRoot = jsonSerialization.serializeNodesToJsonString(root)
        val deserializedRoot = jsonSerialization.deserializeToNodes(serializedRoot).single()
        assert(deserializedRoot is Root)
    }

    @Test
    fun serializeAndDeserializeTextFile() {
        val language =
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
        val dir =
            Directory("foo_dir_id").apply {
                this.name = "foo"
            }
        val textFile =
            TextFile().apply {
                this.name = "MyFile"
                this.contents = "My contents"
            }
        dir.files.add(textFile)
        assertEquals("ROOT____foo", dir.id)
        assertEquals("ROOT____foo___MyFile", textFile.id)

        val jsonSerialization =
            JsonSerialization.getStandardSerialization().apply {
                MetamodelRegistry.prepareJsonSerialization(this)
                unavailableParentPolicy = UnavailableNodePolicy.PROXY_NODES
            }

        val serializedTextFile = jsonSerialization.serializeNodesToJsonString(textFile)
        val deserializedTextFile = jsonSerialization.deserializeToNodes(serializedTextFile).first() as TextFile
        assertEquals("ROOT____foo___MyFile", deserializedTextFile.id)
        assertEquals("MyFile", deserializedTextFile.name)
        assertEquals("My contents", deserializedTextFile.contents)
        assert(deserializedTextFile.parent is ProxyNode)
        assertEquals("ROOT____foo", deserializedTextFile.parent.id)
    }

    @Test
    fun childrenOfSingleContainmentDoesNotContainNull() {
        val language =
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
        val dir =
            Directory("foo_dir_id").apply {
                this.name = "foo"
            }
        val textFile =
            TextFile().apply {
                this.name = "MyFile"
                this.contents = "My contents"
            }
        dir.files.add(textFile)
        assertEquals("ROOT____foo", dir.id)
        assertEquals("ROOT____foo___MyFile", textFile.id)

        val jsonSerialization =
            JsonSerialization.getStandardSerialization().apply {
                MetamodelRegistry.prepareJsonSerialization(this)
                unavailableParentPolicy = UnavailableNodePolicy.PROXY_NODES
            }

        val serializedTextFile = jsonSerialization.serializeNodesToJsonString(textFile)
        val deserializedTextFile = jsonSerialization.deserializeToNodes(serializedTextFile).first() as TextFile
        assertEquals("ROOT____foo___MyFile", deserializedTextFile.id)
        assertEquals("MyFile", deserializedTextFile.name)
        assertEquals("My contents", deserializedTextFile.contents)
        assert(deserializedTextFile.parent is ProxyNode)
        assertEquals("ROOT____foo", deserializedTextFile.parent.id)
    }

    @Test
    fun handleSingleReferences() {
        val language =
            lwLanguage(
                "myLanguage",
                MLSimpleNode::class,
                MLNodeWithSimpleReference::class,
            )
        val simpleNode = language.getConceptByName("MLSimpleNode")!!
        val nodeWithSimpleReference = language.getConceptByName("MLNodeWithSimpleReference")!!
        val reference = nodeWithSimpleReference.getReferenceByName("simple")!!
        assertEquals("simple", reference.name)
        assertEquals(false, reference.isMultiple)
        assertEquals(false, reference.isRequired)
        assertEquals(simpleNode, reference.type)
        assertEquals(1, nodeWithSimpleReference.features.size)
    }

    @Test
    fun handleMultipleReferences() {
        val language =
            lwLanguage(
                "myLanguage",
                MLSimpleNode::class,
                MLNodeWithMultipleReference::class,
            )
        val simpleNode = language.getConceptByName("MLSimpleNode")!!
        val nodeWithMultipleReference = language.getConceptByName("MLNodeWithMultipleReference")!!
        val reference = nodeWithMultipleReference.getReferenceByName("list")!!
        assertEquals("list", reference.name)
        assertEquals(true, reference.isMultiple)
        assertEquals(false, reference.isRequired)
        assertEquals(simpleNode, reference.type)
        assertEquals(1, nodeWithMultipleReference.features.size)
    }
}
