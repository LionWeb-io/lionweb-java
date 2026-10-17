package io.lionweb.emf.cli

import com.github.ajalt.clikt.core.BadParameterValue
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import io.lionweb.LionWebVersion
import io.lionweb.emf.builtins.BuiltinsPackage
import io.lionweb.emf.support.JSONResourceFactory
import io.lionweb.language.Language
import io.lionweb.model.ClassifierInstanceUtils
import io.lionweb.model.HasSettableID
import io.lionweb.model.Node
import io.lionweb.serialization.JsonSerialization
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.ByteArrayOutputStream
import java.io.File

/** The LionWeb versions which can be specified on the command line, indexed by their name. */
internal val lionWebVersionsByName: Map<String, LionWebVersion> =
    LionWebVersion.values().associateBy { it.versionString }

/**
 * Creates a ResourceSet able to load Ecore files (".ecore"), EMF models stored as JSON (".json"),
 * and EMF models stored as XMI (any other extension).
 */
internal fun createResourceSet(): ResourceSet {
    val resourceSet = ResourceSetImpl()
    val extensionToFactoryMap = resourceSet.resourceFactoryRegistry.extensionToFactoryMap
    extensionToFactoryMap["ecore"] = EcoreResourceFactoryImpl()
    extensionToFactoryMap["json"] = JSONResourceFactory()
    extensionToFactoryMap[Resource.Factory.Registry.DEFAULT_EXTENSION] = XMIResourceFactoryImpl()
    resourceSet.packageRegistry[EcorePackage.eNS_URI] = EcorePackage.eINSTANCE
    resourceSet.packageRegistry[BuiltinsPackage.eNS_URI] = BuiltinsPackage.eINSTANCE
    return resourceSet
}

internal fun ResourceSet.loadResource(file: File): Resource = getResource(URI.createFileURI(file.absolutePath), true)

/**
 * Loads the EPackages contained in the given file and registers them, so that models
 * conforming to them can be loaded using this ResourceSet.
 */
internal fun ResourceSet.loadAndRegisterEPackages(file: File): Resource {
    val resource = loadResource(file)
    resource.contents.filterIsInstance<EPackage>().forEach { packageRegistry[it.nsURI] = it }
    return resource
}

/**
 * Determines the LionWeb version of a LionWeb serialization chunk, by looking at its
 * serializationFormatVersion.
 */
internal fun detectLionWebVersion(file: File): LionWebVersion {
    val root =
        try {
            file.bufferedReader(Charsets.UTF_8).use { JsonParser.parseReader(it) }
        } catch (e: JsonParseException) {
            throw BadParameterValue("$file is not a valid JSON file: ${e.message}")
        }
    val versionString =
        if (root.isJsonObject && root.asJsonObject.has("serializationFormatVersion")) {
            root.asJsonObject.get("serializationFormatVersion").asString
        } else {
            null
        }
    return lionWebVersionsByName[versionString]
        ?: throw BadParameterValue("$file is not a LionWeb serialization chunk with a supported serializationFormatVersion")
}

/**
 * Loads all the Languages contained in the given files. Languages are registered in the given
 * JsonSerialization as they are loaded, so that a language can refer to languages loaded before it.
 */
internal fun JsonSerialization.loadLanguages(files: List<File>): List<Language> {
    val languages = mutableListOf<Language>()
    files.forEach { file ->
        deserializeToNodes(file).filterIsInstance<Language>().forEach { language ->
            registerLanguage(language)
            languages.add(language)
        }
    }
    if (languages.isEmpty()) {
        throw BadParameterValue("No languages found in ${files.joinToString(", ")}")
    }
    return languages
}

/**
 * EObjects typically have no ID, so nodes imported from EMF may lack one. As IDs are required in
 * LionWeb, we assign deterministic IDs, based on the position of each node in the model, to all the
 * nodes which do not have one.
 */
internal fun assignMissingIDs(
    roots: List<Node>,
    prefix: String,
) {
    val allNodes = mutableListOf<Node>()

    fun collect(node: Node) {
        allNodes.add(node)
        ClassifierInstanceUtils.getChildren(node).forEach { collect(it) }
    }
    roots.forEach { collect(it) }

    val usedIDs = allNodes.mapNotNull { it.id }.toMutableSet()
    var counter = 0
    allNodes.filter { it.id == null }.forEach { node ->
        check(node is HasSettableID) { "Unable to assign an ID to node $node" }
        var id: String
        do {
            id = "$prefix-${counter++}"
        } while (!usedIDs.add(id))
        node.setID(id)
    }
}

/** Derives a valid LionWeb ID prefix from the name of the given file. */
internal fun idPrefixFor(file: File): String = file.nameWithoutExtension.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifEmpty { "node" }

/**
 * Saves the contents of the given resource using the given factory and returns the result as
 * text. The contents are moved to a new resource.
 *
 * Note that the new resource needs an absolute URI: otherwise, references among elements of the
 * resource itself would not be saved as relative to the resource.
 */
internal fun Resource.saveAsString(factory: Resource.Factory): String {
    val target = factory.createResource(URI.createFileURI(File("lionweb-emf-output").absolutePath))
    target.contents.addAll(contents)
    val outputStream = ByteArrayOutputStream()
    target.save(outputStream, mapOf(org.eclipse.emf.ecore.xmi.XMLResource.OPTION_ENCODING to "UTF-8"))
    return outputStream.toString(Charsets.UTF_8.name())
}
