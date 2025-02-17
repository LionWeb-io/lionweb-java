package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.lionweb.lioncore.java.language.Enumeration;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
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

    assertEquals(5, language.getElements().size());

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

    Concept library = (Concept) language.getElementByName("Library");
    assertNull(library.getExtendedConcept());
    assertEquals(0, library.getImplemented().size());
    assertFalse(library.isAbstract());
    assertEquals("library.Library", library.qualifiedName());
    assertEquals(2, library.getFeatures().size());
    assertEquals(2, library.allFeatures().size());

    Property libraryName = (Property) library.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), libraryName.getType());
    assertSame(library, libraryName.getContainer());
    assertEquals("library.Library.name", libraryName.qualifiedName());
    assertEquals("library-Library-name", libraryName.getKey());
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
    assertNull(writer.getExtendedConcept());
    assertEquals(0, writer.getImplemented().size());
    assertFalse(writer.isAbstract());
    assertEquals("library.Writer", writer.qualifiedName());
    assertEquals(1, writer.getFeatures().size());
    assertEquals(1, writer.allFeatures().size());

    Property writerName = (Property) writer.getFeatureByName("name");
    assertEquals(LionCoreBuiltins.getString(), writerName.getType());
    assertSame(writer, writerName.getContainer());
    assertEquals("library.Writer.name", writerName.qualifiedName());
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
    assertSame(writer, guideBookWriterName.getContainer());
    assertEquals("library.Writer.name", guideBookWriterName.qualifiedName());
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
    assertSame(writer, specialistBookWriterName.getContainer());
    assertEquals("library.Writer.name", specialistBookWriterName.qualifiedName());
    assertEquals(false, specialistBookWriterName.isOptional());
    assertEquals(true, specialistBookWriterName.isRequired());
  }


  @Test
  public void importExtendedLibraryExample() throws IOException {
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

    EMFMetamodelImporter importer = new EMFMetamodelImporter();
    List<Language> languages = importer.importResource(resource2);
    assertEquals(1, languages.size());

    Language language = languages.get(0);
    assertEquals("extendedlibrary", language.getName());
    assertEquals(1, language.dependsOn().size());

    // Two EClasses + one intermediate concept for an EAttribute with high multiplicity
    assertEquals(3, language.getElements().size());

    Concept localLibrary = (Concept) language.getElementByName("LocalLibrary");
    assertNotNull(localLibrary.getExtendedConcept());
    assertEquals(0, localLibrary.getImplemented().size());
    assertFalse(localLibrary.isAbstract());
    assertEquals("extendedlibrary.LocalLibrary", localLibrary.qualifiedName());
    assertEquals(1, localLibrary.getFeatures().size());
    assertEquals(3, localLibrary.allFeatures().size());

    Property libraryCountry = (Property) localLibrary.getFeatureByName("country");
    assertEquals(LionCoreBuiltins.getString(), libraryCountry.getType());
    assertSame(localLibrary, libraryCountry.getContainer());
    assertEquals("extendedlibrary.LocalLibrary.country", libraryCountry.qualifiedName());
    assertEquals(false, libraryCountry.isOptional());
    assertEquals(true, libraryCountry.isRequired());

    Concept copyRight = (Concept) language.getElementByName("CopyRight");
    assertNull(copyRight.getExtendedConcept());
    assertFalse(copyRight.isAbstract());
    assertEquals("extendedlibrary.CopyRight", copyRight.qualifiedName());
    assertEquals(2, copyRight.getFeatures().size());
    assertEquals(2, copyRight.allFeatures().size());

    Reference writer = (Reference) copyRight.getFeatureByName("writer");
    assertSame(language.dependsOn().get(0).getElementByName("Writer"), writer.getType());
    assertSame(copyRight, writer.getContainer());
    assertEquals("extendedlibrary.CopyRight.writer", writer.qualifiedName());
    assertEquals(false, writer.isOptional());
    assertEquals(true, writer.isRequired());
    assertEquals(false, writer.isMultiple());

    // The `countries` feature is an EAttribute with high multiplicity:
    // here we test that the corresponding intermediate concept is created
    Containment copyRightCountries = (Containment) copyRight.getFeatureByName("countries");
    assertSame(language.getElementByName("CountriesContainer"), copyRightCountries.getType());
    assertSame(copyRight, copyRightCountries.getContainer());
    assertEquals("extendedlibrary.CopyRight.countries", copyRightCountries.qualifiedName());
    assertEquals(true, copyRightCountries.isOptional());
    assertEquals(false, copyRightCountries.isRequired());
    assertEquals(true, copyRightCountries.isMultiple());

    Concept countriesContainer = (Concept) language.getElementByName("CountriesContainer");
    assertNull(countriesContainer.getExtendedConcept());
    assertFalse(countriesContainer.isAbstract());
    assertEquals("extendedlibrary.CountriesContainer", countriesContainer.qualifiedName());
    assertEquals(1, countriesContainer.getFeatures().size());
    assertEquals(1, countriesContainer.allFeatures().size());

    Property countriesAttribute = (Property) countriesContainer.getFeatureByName("content");
    assertEquals(LionCoreBuiltins.getString(), countriesAttribute.getType());
    assertSame(countriesContainer, countriesAttribute.getContainer());
    assertEquals("extendedlibrary.CountriesContainer.content", countriesAttribute.qualifiedName());
    assertEquals(false, countriesAttribute.isOptional());
    assertEquals(true, countriesAttribute.isRequired());
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
