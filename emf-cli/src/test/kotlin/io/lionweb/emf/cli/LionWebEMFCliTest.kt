package io.lionweb.emf.cli

import com.github.ajalt.clikt.testing.CliktCommandTestResult
import com.github.ajalt.clikt.testing.test
import io.lionweb.LionWebVersion
import io.lionweb.language.Language
import io.lionweb.model.ClassifierInstanceUtils
import io.lionweb.model.Node
import io.lionweb.serialization.SerializationProvider
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LionWebEMFCliTest {
    private val tempDir: File = Files.createTempDirectory("lionweb-emf-cli").toFile()

    @AfterTest
    fun cleanUp() {
        tempDir.deleteRecursively()
    }

    private fun run(vararg args: String): CliktCommandTestResult = createLionWebEMFCommand().test(args.toList())

    private fun CliktCommandTestResult.assertSuccess(): CliktCommandTestResult {
        assertEquals(0, statusCode, "Command failed. Output: $output")
        return this
    }

    private fun resource(name: String): String = File(LionWebEMFCliTest::class.java.getResource("/$name")!!.toURI()).absolutePath

    private fun tempFile(name: String): File = File(tempDir, name)

    private fun loadLanguages(
        lionWebVersion: LionWebVersion,
        file: File,
    ): List<Language> =
        SerializationProvider
            .getStandardJsonSerialization(lionWebVersion)
            .deserializeToNodes(file)
            .filterIsInstance<Language>()

    private fun ResourceSet.loadEPackages(file: File): List<EPackage> = loadAndRegisterEPackages(file).contents.map { it as EPackage }

    @Test
    fun helpListsAllCommands() {
        val result = run("--help").assertSuccess()
        listOf("import-metamodel", "export-metamodel", "import-model", "export-model").forEach {
            assertTrue(result.stdout.contains(it), "Help should mention $it")
        }
    }

    @Test
    fun importMetamodel() {
        val output = tempFile("library.json")
        run("import-metamodel", resource("library.ecore"), "-o", output.absolutePath).assertSuccess()

        val languages = loadLanguages(LionWebVersion.currentVersion, output)
        assertEquals(1, languages.size)
        val library = languages[0]
        assertEquals("library", library.name)
        val book = assertNotNull(library.getConceptByName("Book"))
        assertNotNull(book.getFeatureByName("title"))
        assertNotNull(book.getFeatureByName("author"))
        assertTrue(library.getConceptByName("NamedElement")!!.isAbstract)
    }

    @Test
    fun importMetamodelWithSpecificLionWebVersion() {
        val output = tempFile("library.json")
        run(
            "import-metamodel",
            resource("library.ecore"),
            "--lionweb-version",
            "2023.1",
            "-o",
            output.absolutePath,
        ).assertSuccess()
        assertEquals(LionWebVersion.v2023_1, detectLionWebVersion(output))
        assertEquals(1, loadLanguages(LionWebVersion.v2023_1, output).size)
    }

    @Test
    fun importMetamodelWithUnsupportedLionWebVersion() {
        val result = run("import-metamodel", resource("library.ecore"), "--lionweb-version", "1999.1")
        assertNotEquals(0, result.statusCode)
        assertTrue(result.stderr.contains("--lionweb-version"), result.stderr)
    }

    @Test
    fun importMetamodelToStdout() {
        val result = run("import-metamodel", resource("library.ecore")).assertSuccess()
        assertTrue(result.stdout.contains("\"serializationFormatVersion\": \"2024.1\""))
        assertTrue(result.stdout.contains("library-Book"))
    }

    @Test
    fun exportMetamodel() {
        val output = tempFile("library.ecore")
        run("export-metamodel", resource("library-language.json"), "-o", output.absolutePath).assertSuccess()

        val ePackages = createResourceSet().loadEPackages(output)
        assertEquals(1, ePackages.size)
        val library = ePackages[0]
        assertEquals("library", library.name)
        val book = library.getEClassifier("Book") as EClass
        assertNotNull(book.getEStructuralFeature("title"))
        assertNotNull(book.getEStructuralFeature("author"))
    }

    @Test
    fun metamodelRoundTrip() {
        val lionWebFile = tempFile("shapes.json")
        val ecoreFile = tempFile("shapes.ecore")
        run("import-metamodel", resource("shapes.ecore"), "-o", lionWebFile.absolutePath).assertSuccess()
        run("export-metamodel", lionWebFile.absolutePath, "-o", ecoreFile.absolutePath).assertSuccess()

        val shapes = createResourceSet().loadEPackages(ecoreFile).single()
        assertEquals("shapes", shapes.name)
        val shape = shapes.getEClassifier("Shape") as EClass
        val circle = shapes.getEClassifier("Circle") as EClass
        assertTrue(shape.isAbstract)
        assertTrue(circle.eSuperTypes.contains(shape))
        assertNotNull(circle.getEStructuralFeature("radius"))
    }

    @Test
    fun importModel() {
        val output = tempFile("drawing.json")
        run(
            "import-model",
            resource("drawing.xmi"),
            "--metamodel",
            resource("shapes.ecore"),
            "-o",
            output.absolutePath,
        ).assertSuccess()

        // To read the nodes we need the language, which we obtain through import-metamodel
        val languageFile = tempFile("shapes.json")
        run("import-metamodel", resource("shapes.ecore"), "-o", languageFile.absolutePath).assertSuccess()
        val jsonSerialization = SerializationProvider.getStandardJsonSerialization(LionWebVersion.currentVersion)
        loadLanguages(LionWebVersion.currentVersion, languageFile).forEach { jsonSerialization.registerLanguage(it) }
        jsonSerialization.instantiator.enableDynamicNodes()
        val roots = jsonSerialization.deserializeToNodes(output).filter { it.parent == null }

        val drawing = roots.single()
        assertEquals("Drawing", drawing.classifier.name)
        // EObjects have no IDs, so IDs are derived from the file name and the position of the node
        assertEquals("drawing-0", drawing.id)
        assertEquals("My drawing", ClassifierInstanceUtils.getPropertyValueByName(drawing, "name"))
        val shapes: List<Node> = ClassifierInstanceUtils.getChildrenByContainmentName(drawing, "shapes")
        assertEquals(2, shapes.size)
        assertEquals("Circle", shapes[0].classifier.name)
        assertEquals(10, ClassifierInstanceUtils.getPropertyValueByName(shapes[0], "radius"))
        assertEquals(true, ClassifierInstanceUtils.getPropertyValueByName(shapes[0], "filled"))
        assertEquals("Square", shapes[1].classifier.name)
        assertEquals(5, ClassifierInstanceUtils.getPropertyValueByName(shapes[1], "side"))
    }

    @Test
    fun importModelRequiresMetamodel() {
        val result = run("import-model", resource("drawing.xmi"))
        assertNotEquals(0, result.statusCode)
        assertTrue(result.stderr.contains("--metamodel"), result.stderr)
    }

    @Test
    fun exportModel() {
        val ecoreFile = tempFile("library.ecore")
        val output = tempFile("out/library.xmi")
        run("export-metamodel", resource("library-language.json"), "-o", ecoreFile.absolutePath).assertSuccess()
        run(
            "export-model",
            resource("langeng-library.json"),
            "--language",
            resource("library-language.json"),
            "-o",
            output.absolutePath,
        ).assertSuccess()

        // The exported model can be loaded using the metamodel produced by export-metamodel
        val resourceSet = createResourceSet()
        resourceSet.loadAndRegisterEPackages(ecoreFile)
        val contents = resourceSet.loadResource(output).contents
        assertEquals(3, contents.size)
        val markus = contents[0]
        assertEquals("Writer", markus.eClass().name)
        assertEquals("Markus Voelter", markus.eGet(markus.eClass().getEStructuralFeature("name")))
        val library = contents[2]
        assertEquals("Library", library.eClass().name)
        val books = library.eGet(library.eClass().getEStructuralFeature("books")) as List<*>
        assertEquals(2, books.size)
        val book = books[0] as EObject
        assertEquals(markus, book.eGet(book.eClass().getEStructuralFeature("author")))
    }

    @Test
    fun exportModelToStdout() {
        val result =
            run(
                "export-model",
                resource("langeng-library.json"),
                "-l",
                resource("library-language.json"),
            ).assertSuccess()
        assertTrue(result.stdout.contains("Language Engineering Library"))
    }

    @Test
    fun missingInputFileIsReported() {
        val result = run("import-metamodel", tempFile("missing.ecore").absolutePath)
        assertNotEquals(0, result.statusCode)
        assertTrue(result.stderr.contains("does not exist"), result.stderr)
    }

    @Test
    fun conversionErrorsAreReported() {
        // A LionWeb chunk is passed where an Ecore file is expected
        val result = run("import-metamodel", resource("library-language.json"))
        assertNotEquals(0, result.statusCode)
        assertTrue(result.stderr.contains("Conversion failed"), result.stderr)
    }

    @Test
    fun exportRequiresLionWebChunk() {
        val result = run("export-metamodel", resource("library.ecore"))
        assertNotEquals(0, result.statusCode)
        assertTrue(result.stderr.contains("is not a valid JSON file"), result.stderr)
    }
}
