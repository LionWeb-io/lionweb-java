package io.lionweb.emf.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import io.lionweb.LionWebVersion
import io.lionweb.emf.EMFMetamodelExporter
import io.lionweb.emf.EMFMetamodelImporter
import io.lionweb.emf.EMFModelExporter
import io.lionweb.emf.EMFModelImporter
import io.lionweb.emf.mapping.LanguageEntitiesToEElementsMapping
import io.lionweb.language.Language
import io.lionweb.model.Node
import io.lionweb.serialization.SerializationProvider
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl

/**
 * Base class for the commands converting files: it takes care of writing the result either to the
 * output file or to stdout, and of reporting failures of the conversion.
 */
abstract class ConversionCommand(
    name: String,
    private val description: String,
) : CliktCommand(name = name) {
    protected val output by option("-o", "--output", help = "File to write. If omitted, the result is printed on stdout.")
        .file(canBeDir = false)

    override fun help(context: Context) = description

    /** Performs the conversion, returning the content to be written. */
    protected abstract fun convert(): String

    override fun run() {
        val result =
            try {
                convert()
            } catch (e: CliktError) {
                throw e
            } catch (e: Exception) {
                throw CliktError("Conversion failed: ${e.message ?: e.toString()}", cause = e)
            }
        val outputFile = output
        if (outputFile == null) {
            echo(result, trailingNewline = !result.endsWith("\n"))
        } else {
            outputFile.absoluteFile.parentFile?.mkdirs()
            outputFile.writeText(result, Charsets.UTF_8)
        }
    }
}

private fun CliktCommand.lionWebVersionOption() =
    option(
        "-v",
        "--lionweb-version",
        help = "LionWeb version to use (default: ${LionWebVersion.currentVersion.versionString})",
    ).choice(lionWebVersionsByName).default(LionWebVersion.currentVersion)

/** Converts Ecore metamodels into LionWeb Languages. */
class ImportMetamodelCommand :
    ConversionCommand(
        "import-metamodel",
        "Convert Ecore metamodels (.ecore) into LionWeb Languages (LionWeb JSON).",
    ) {
    private val inputs by argument("ECORE_FILE", help = "Ecore file(s) to import. All their EPackages are converted.")
        .file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    private val lionWebVersion by lionWebVersionOption()

    override fun convert(): String {
        val resourceSet = createResourceSet()
        val importer = EMFMetamodelImporter(lionWebVersion)
        val languages = inputs.flatMap { importer.importResource(resourceSet.loadAndRegisterEPackages(it)) }
        if (languages.isEmpty()) {
            throw UsageError("No EPackages found in ${inputs.joinToString(", ")}")
        }
        return SerializationProvider
            .getStandardJsonSerialization(lionWebVersion)
            .serializeTreesToJsonString(*languages.toTypedArray())
    }
}

/** Converts LionWeb Languages into an Ecore metamodel. */
class ExportMetamodelCommand :
    ConversionCommand(
        "export-metamodel",
        "Convert LionWeb Languages (LionWeb JSON) into an Ecore metamodel (.ecore).",
    ) {
    private val inputs by argument(
        "LANGUAGE_FILE",
        help = "LionWeb JSON file(s) containing the Languages to export. Languages a file depends on should be listed before it.",
    ).file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    override fun convert(): String {
        val lionWebVersion = detectLionWebVersion(inputs.first())
        val languages =
            SerializationProvider
                .getStandardJsonSerialization(lionWebVersion)
                .loadLanguages(inputs)
        return EMFMetamodelExporter(lionWebVersion)
            .exportResource(languages)
            .saveAsString(EcoreResourceFactoryImpl())
    }
}

/** Converts an EMF model into LionWeb nodes. */
class ImportModelCommand :
    ConversionCommand(
        "import-model",
        "Convert an EMF model (XMI, or JSON when the extension is .json) into LionWeb nodes (LionWeb JSON).",
    ) {
    private val input by argument("MODEL_FILE", help = "EMF model to import.")
        .file(mustExist = true, canBeDir = false)

    private val metamodels by option(
        "-m",
        "--metamodel",
        help = "Ecore file defining the metamodel of the model. Can be repeated. Metamodels a file depends on should be listed before it.",
    ).file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    private val lionWebVersion by lionWebVersionOption()

    override fun convert(): String {
        val mapping = LanguageEntitiesToEElementsMapping(lionWebVersion)
        val jsonSerialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion)

        // We first import the metamodels, so that the model importer knows how to map EClasses
        val resourceSet = createResourceSet()
        val metamodelImporter = EMFMetamodelImporter(mapping)
        metamodels.forEach { metamodel ->
            metamodelImporter
                .importResource(resourceSet.loadAndRegisterEPackages(metamodel))
                .forEach { jsonSerialization.registerLanguage(it) }
        }

        val modelImporter = EMFModelImporter(mapping)
        modelImporter.nodeInstantiator.enableDynamicNodes()
        val roots = modelImporter.importResource(resourceSet.loadResource(input))
        assignMissingIDs(roots, idPrefixFor(input))
        return jsonSerialization.serializeTreesToJsonString(*roots.toTypedArray())
    }
}

/** Converts LionWeb nodes into an EMF model. */
class ExportModelCommand :
    ConversionCommand(
        "export-model",
        "Convert LionWeb nodes (LionWeb JSON) into an EMF model (XMI). " +
            "The corresponding metamodel can be obtained through the export-metamodel command.",
    ) {
    private val input by argument("MODEL_FILE", help = "LionWeb JSON file containing the nodes to export.")
        .file(mustExist = true, canBeDir = false)

    private val languageFiles by option(
        "-l",
        "--language",
        help =
            "LionWeb JSON file containing Languages used by the model. Can be repeated. " +
                "Languages a file depends on should be listed before it.",
    ).file(mustExist = true, canBeDir = false)
        .multiple(required = true)

    override fun convert(): String {
        val lionWebVersion = detectLionWebVersion(input)
        val jsonSerialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion)
        val languages: List<Language> = jsonSerialization.loadLanguages(languageFiles)
        jsonSerialization.instantiator.enableDynamicNodes()
        val roots: List<Node> = jsonSerialization.deserializeToNodes(input).filter { it.parent == null }

        // We first export the languages, so that the model exporter knows how to map Concepts
        val mapping = LanguageEntitiesToEElementsMapping(lionWebVersion)
        EMFMetamodelExporter(lionWebVersion, mapping).exportResource(languages)
        return EMFModelExporter(mapping)
            .exportResource(roots)
            .saveAsString(XMIResourceFactoryImpl())
    }
}
