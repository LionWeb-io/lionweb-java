package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.language.Enumeration;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.junit.Test;

public class EMFMetamodelImporterTest {

  private EMFResourceLoader emfResourceLoader = new EMFResourceLoader();

  @Test
  public void importLibraryExample() throws IOException {
    InputStream is = this.getClass().getResourceAsStream("/library.ecore");
    EMFMetamodelImporter importer = new EMFMetamodelImporter();

    Resource resource = emfResourceLoader.importInputStream(is);
    List<Language> languages = importer.importResource(resource);
    assertEquals(1, languages.size());

    Language language = languages.get(0);
    assertEquals("library", language.getName());

    assertEquals(6, language.getElements().size());

    Concept book = (Concept) language.getElementByName("Book");
    assertNull(book.getExtendedConcept());
    assertEquals(0, book.getImplemented().size());
    assertFalse(book.isAbstract());
    assertEquals("library.Book", book.qualifiedName());
    assertEquals(3, book.getFeatures().size());
    assertEquals(3, book.allFeatures().size());

    Property bookTitle = (Property) book.getFeatureByName("title");
    assertEquals(LionCoreBuiltins.getString(), bookTitle.getType());
    assertSame(book, bookTitle.getContainer());
    assertEquals("library.Book.title", bookTitle.qualifiedName());
    assertEquals(false, bookTitle.isOptional());
    assertEquals(true, bookTitle.isRequired());

    Property bookPages = (Property) book.getFeatureByName("pages");
    assertEquals(LionCoreBuiltins.getInteger(), bookPages.getType());
    assertSame(book, bookPages.getContainer());
    assertEquals("library.Book.pages", bookPages.qualifiedName());
    assertEquals(false, bookPages.isOptional());
    assertEquals(true, bookPages.isRequired());

    Reference bookAuthor = (Reference) book.getFeatureByName("author");
    assertSame(language.getElementByName("Writer"), bookAuthor.getType());
    assertSame(book, bookAuthor.getContainer());
    assertEquals("library.Book.author", bookAuthor.qualifiedName());
    assertEquals(false, bookAuthor.isOptional());
    assertEquals(true, bookAuthor.isRequired());
    assertEquals(false, bookAuthor.isMultiple());

    Concept namedElement = (Concept) language.getElementByName("NamedElement");
    assertTrue(namedElement.isAbstract());

    Concept library = (Concept) language.getElementByName("Library");
    assertNotNull(library.getExtendedConcept());
    assertEquals(0, library.getImplemented().size());
    assertFalse(library.isAbstract());
    assertEquals("library.Library", library.qualifiedName());
    assertEquals(1, library.getFeatures().size());
    assertEquals(2, library.allFeatures().size());

    Property libraryName = (Property) library.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), libraryName.getType());
    assertSame(library.getExtendedConcept(), libraryName.getContainer());
    assertEquals("library.NamedElement.name", libraryName.qualifiedName());
    assertEquals("library-NamedElement-name", libraryName.getKey());
    assertEquals(false, libraryName.isOptional());
    assertEquals(true, libraryName.isRequired());

    Containment libraryBooks = (Containment) library.getFeatureByName("books");
    assertSame(language.getElementByName("Book"), libraryBooks.getType());
    assertSame(library, libraryBooks.getContainer());
    assertEquals("library.Library.books", libraryBooks.qualifiedName());
    assertEquals(true, libraryBooks.isOptional());
    assertEquals(false, libraryBooks.isRequired());
    assertEquals(true, libraryBooks.isMultiple());

    Concept writer = (Concept) language.getElementByName("Writer");
    assertNotNull(writer.getExtendedConcept());
    assertSame(namedElement, writer.getExtendedConcept());
    assertEquals(0, writer.getImplemented().size());
    assertFalse(writer.isAbstract());
    assertEquals("library.Writer", writer.qualifiedName());
    assertEquals(0, writer.getFeatures().size());
    assertEquals(1, writer.allFeatures().size());

    Property writerName = (Property) writer.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), writerName.getType());
    assertSame(writer.getExtendedConcept(), writerName.getContainer());
    assertEquals("library.NamedElement.name", writerName.qualifiedName());
    assertEquals(false, writerName.isOptional());
    assertEquals(true, writerName.isRequired());

    Concept guideBookWriter = (Concept) language.getElementByName("GuideBookWriter");
    assertSame(writer, guideBookWriter.getExtendedConcept());
    assertEquals(0, guideBookWriter.getImplemented().size());
    assertFalse(guideBookWriter.isAbstract());
    assertEquals("library.GuideBookWriter", guideBookWriter.qualifiedName());
    assertEquals(1, guideBookWriter.getFeatures().size());
    assertEquals(2, guideBookWriter.allFeatures().size());

    Property guideBookWriterCountries = (Property) guideBookWriter.getFeatureByName("countries");
    assertEquals(LionCoreBuiltins.getString(), guideBookWriterCountries.getType());
    assertSame(guideBookWriter, guideBookWriterCountries.getContainer());
    assertEquals("library.GuideBookWriter.countries", guideBookWriterCountries.qualifiedName());
    assertEquals(true, guideBookWriterCountries.isOptional());
    assertEquals(false, guideBookWriterCountries.isRequired());

    Property guideBookWriterName = (Property) guideBookWriter.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), guideBookWriterName.getType());
    assertSame(writer.getExtendedConcept(), guideBookWriterName.getContainer());
    assertEquals("library.NamedElement.name", guideBookWriterName.qualifiedName());
    assertEquals(false, guideBookWriterName.isOptional());
    assertEquals(true, guideBookWriterName.isRequired());

    Concept specialistBookWriter = (Concept) language.getElementByName("SpecialistBookWriter");
    assertSame(writer, specialistBookWriter.getExtendedConcept());
    assertEquals(0, specialistBookWriter.getImplemented().size());
    assertFalse(specialistBookWriter.isAbstract());
    assertEquals("library.SpecialistBookWriter", specialistBookWriter.qualifiedName());
    assertEquals(1, specialistBookWriter.getFeatures().size());
    assertEquals(2, specialistBookWriter.allFeatures().size());

    Property specialistBookWriterSubject =
        (Property) specialistBookWriter.getFeatureByName("subject");
    assertEquals(LionCoreBuiltins.getString(), specialistBookWriterSubject.getType());
    assertSame(specialistBookWriter, specialistBookWriterSubject.getContainer());
    assertEquals(
        "library.SpecialistBookWriter.subject", specialistBookWriterSubject.qualifiedName());
    assertEquals(true, specialistBookWriterSubject.isOptional());
    assertEquals(false, specialistBookWriterSubject.isRequired());

    Property specialistBookWriterName = (Property) specialistBookWriter.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), specialistBookWriterName.getType());
    assertSame(writer.getExtendedConcept(), specialistBookWriterName.getContainer());
    assertEquals("library.NamedElement.name", specialistBookWriterName.qualifiedName());
    assertEquals(false, specialistBookWriterName.isOptional());
    assertEquals(true, specialistBookWriterName.isRequired());
  }

  @Test
  public void importExtendedLibraryExample() throws IOException {
    importExtendedLibraryExample(LionWebVersion.v2023_1);
    importExtendedLibraryExample(LionWebVersion.currentVersion);
  }

  private void importExtendedLibraryExample(LionWebVersion lionWebVersion) throws IOException {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());

    ResourceSet resourceSet = new ResourceSetImpl();

    URI fileURI1 = URI.createFileURI("library.ecore");
    Resource resource1 = resourceSet.createResource(fileURI1);
    InputStream is1 = this.getClass().getResourceAsStream("/library.ecore");
    resource1.load(is1, new HashMap<>());

    URI fileURI2 = URI.createFileURI("extended-library.ecore");
    Resource resource2 = resourceSet.createResource(fileURI2);
    InputStream is2 = this.getClass().getResourceAsStream("/extended-library.ecore");
    resource2.load(is2, new HashMap<>());

    EMFMetamodelImporter importer = new EMFMetamodelImporter(lionWebVersion);
    List<Language> languages = importer.importResource(resource2);
    assertEquals(1, languages.size());

    Language language = languages.get(0);
    assertEquals("extendedlibrary", language.getName());
    assertEquals(1, language.dependsOn().size());

    // two EClasses +
    // one intermediate concept for an EAttribute with high multiplicity +
    // one enumeration
    assertEquals(4, language.getElements().size());
    assertEquals(lionWebVersion, language.getLionWebVersion());
    LionWebVersion langLionWebVersion = language.getLionWebVersion();

    Concept localLibrary = (Concept) language.getElementByName("LocalLibrary");
    assertNotNull(localLibrary.getExtendedConcept());
    assertEquals(0, localLibrary.getImplemented().size());
    assertFalse(localLibrary.isAbstract());
    assertEquals("extendedlibrary.LocalLibrary", localLibrary.qualifiedName());
    assertEquals(1, localLibrary.getFeatures().size());
    assertEquals(3, localLibrary.allFeatures().size());
    assertEquals(langLionWebVersion, localLibrary.getLionWebVersion());

    Property libraryCountry = (Property) localLibrary.getFeatureByName("country");
    assertEquals(LionCoreBuiltins.getString(lionWebVersion), libraryCountry.getType());
    assertSame(localLibrary, libraryCountry.getContainer());
    assertEquals("extendedlibrary.LocalLibrary.country", libraryCountry.qualifiedName());
    assertEquals(false, libraryCountry.isOptional());
    assertEquals(true, libraryCountry.isRequired());
    assertEquals(langLionWebVersion, libraryCountry.getLionWebVersion());

    Concept copyRight = (Concept) language.getElementByName("CopyRight");
    assertNull(copyRight.getExtendedConcept());
    assertFalse(copyRight.isAbstract());
    assertEquals("extendedlibrary.CopyRight", copyRight.qualifiedName());
    assertEquals(2, copyRight.getFeatures().size());
    assertEquals(2, copyRight.allFeatures().size());
    assertEquals(langLionWebVersion, copyRight.getLionWebVersion());

    Reference writer = (Reference) copyRight.getFeatureByName("writer");
    assertSame(language.dependsOn().get(0).getElementByName("Writer"), writer.getType());
    assertSame(copyRight, writer.getContainer());
    assertEquals("extendedlibrary.CopyRight.writer", writer.qualifiedName());
    assertEquals(false, writer.isOptional());
    assertEquals(true, writer.isRequired());
    assertEquals(false, writer.isMultiple());
    assertEquals(langLionWebVersion, writer.getLionWebVersion());

    // The `countries` feature is an EAttribute with high multiplicity:
    // here we test that the corresponding intermediate concept is created
    Containment copyRightCountries = (Containment) copyRight.getFeatureByName("countries");
    assertSame(language.getElementByName("CountriesContainer"), copyRightCountries.getType());
    assertSame(copyRight, copyRightCountries.getContainer());
    assertEquals("extendedlibrary.CopyRight.countries", copyRightCountries.qualifiedName());
    assertEquals(true, copyRightCountries.isOptional());
    assertEquals(false, copyRightCountries.isRequired());
    assertEquals(true, copyRightCountries.isMultiple());
    assertEquals(langLionWebVersion, copyRightCountries.getLionWebVersion());

    Concept countriesContainer = (Concept) language.getElementByName("CountriesContainer");
    assertNull(countriesContainer.getExtendedConcept());
    assertFalse(countriesContainer.isAbstract());
    assertEquals("extendedlibrary.CountriesContainer", countriesContainer.qualifiedName());
    assertEquals(1, countriesContainer.getFeatures().size());
    assertEquals(1, countriesContainer.allFeatures().size());
    assertEquals(langLionWebVersion, countriesContainer.getLionWebVersion());

    Property countriesAttribute = (Property) countriesContainer.getFeatureByName("content");
    assertEquals(LionCoreBuiltins.getString(lionWebVersion), countriesAttribute.getType());
    assertSame(countriesContainer, countriesAttribute.getContainer());
    assertEquals("extendedlibrary.CountriesContainer.content", countriesAttribute.qualifiedName());
    assertEquals(false, countriesAttribute.isOptional());
    assertEquals(true, countriesAttribute.isRequired());
    assertEquals(langLionWebVersion, countriesAttribute.getLionWebVersion());

    Enumeration bookStatus = (Enumeration) language.getElementByName("BookStatus");
    assertEquals("extendedlibrary.BookStatus", bookStatus.qualifiedName());
    assertEquals(2, bookStatus.getLiterals().size());
    assertEquals(langLionWebVersion, bookStatus.getLionWebVersion());
    assertTrue(
        bookStatus.getLiterals().stream()
            .map(el -> el.getName())
            .collect(Collectors.toList())
            .contains("OnLoan"));
    assertTrue(
        bookStatus.getLiterals().stream()
            .map(el -> el.getName())
            .collect(Collectors.toList())
            .contains("OnShelf"));
    assertTrue(
        bookStatus.getLiterals().stream()
            .allMatch(el -> el.getLionWebVersion().equals(langLionWebVersion)));
  }

  @Test
  public void importExtendedLibraryAndSerialize() throws IOException {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());

    ResourceSet resourceSet = new ResourceSetImpl();

    URI fileURI1 = URI.createFileURI("library.ecore");
    Resource resource1 = resourceSet.createResource(fileURI1);
    InputStream is1 = this.getClass().getResourceAsStream("/library.ecore");
    resource1.load(is1, new HashMap<>());

    URI fileURI2 = URI.createFileURI("extended-library.ecore");
    Resource resource2 = resourceSet.createResource(fileURI2);
    InputStream is2 = this.getClass().getResourceAsStream("/extended-library.ecore");
    resource2.load(is2, new HashMap<>());

    EMFMetamodelImporter importer = new EMFMetamodelImporter(LionWebVersion.v2023_1);
    List<Language> languages = importer.importResource(resource2);
    assertEquals(1, languages.size());

    Language language = languages.get(0);
    assertEquals("extendedlibrary", language.getName());
    assertEquals(1, language.dependsOn().size());

    File outputFile =
        new File("C:\\Users\\Ujyana Tikhanova\\Documents\\lionweb\\extendedlibrary-language.json");
    JsonSerialization.saveLanguageToFile(language, outputFile);
  }

  @Test
  public void importKotlinLangExample() throws IOException {
    InputStream is = this.getClass().getResourceAsStream("/kotlinlang.json");
    EMFMetamodelImporter importer = new EMFMetamodelImporter();

    Resource resource = emfResourceLoader.importInputStream(is, ResourceType.JSON);
    List<Language> languages = importer.importResource(resource);
    assertEquals(2, languages.size());

    Concept point = languages.get(0).getConceptByName("Point");
    assertEquals(2, point.allFeatures().size());

    Property pointLine = point.getPropertyByName("line");
    assertEquals(LionCoreBuiltins.getInteger(), pointLine.getType());
    assertEquals(true, pointLine.isRequired());

    Property pointColumn = point.getPropertyByName("column");
    assertEquals(LionCoreBuiltins.getInteger(), pointColumn.getType());
    assertEquals(true, pointColumn.isRequired());

    Enumeration issueType = languages.get(0).getEnumerationByName("IssueType");
    assertEquals(3, issueType.getLiterals().size());
    assertEquals(
        new HashSet(Arrays.asList("LEXICAL", "SYNTACTIC", "SEMANTIC")),
        issueType.getLiterals().stream().map(l -> l.getName()).collect(Collectors.toSet()));

    EnumerationLiteral literal = issueType.getLiterals().get(0);
    assertEquals(issueType.getID() + "-" + literal.getName(), literal.getKey());
    assertEquals(issueType.getID() + "-" + literal.getName(), literal.getID());
  }

  @Test
  public void importOCCI() throws IOException {
    InputStream is = this.getClass().getResourceAsStream("/OCCI.ecore");
    EMFMetamodelImporter importer = new EMFMetamodelImporter();
    importer.importEPackage(EcorePackage.eINSTANCE);

    Resource resource = emfResourceLoader.importInputStream(is);
    List<Language> languages = importer.importResource(resource);
    assertEquals(1, languages.size());

    Language occiLanguage = languages.get(0);

    assertEquals(4, occiLanguage.getPrimitiveTypes().size());
    PrimitiveType URI = occiLanguage.getPrimitiveTypes().get(0);
    assertEquals("URI", URI.getName());
    PrimitiveType String = occiLanguage.getPrimitiveTypes().get(1);
    assertEquals("String", String.getName());
    PrimitiveType Number = occiLanguage.getPrimitiveTypes().get(2);
    assertEquals("Number", Number.getName());
    PrimitiveType Boolean = occiLanguage.getPrimitiveTypes().get(3);
    assertEquals("Boolean", Boolean.getName());
  }
}
