package org.lionweb.lioncore.java.emf;

import org.junit.Test;
import org.lionweb.lioncore.java.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class EcoreImporterTest {

    @Test
    public void importLibraryExample() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/library.ecore");
        EcoreImporter importer = new EcoreImporter();

        List<Metamodel> metamodels = importer.importEcoreInputStream(is);
        assertEquals(1, metamodels.size());

        Metamodel metamodel = metamodels.get(0);
        assertEquals("library", metamodel.getQualifiedName());

        assertEquals(5, metamodel.getElements().size());

        Concept book = (Concept)metamodel.getElementByName("Book");
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
        assertEquals(false, bookTitle.isDerived());
        assertEquals(false, bookTitle.isOptional());
        assertEquals(true, bookTitle.isRequired());

        Property bookPages = (Property) book.getFeatureByName("pages");
        assertEquals(LionCoreBuiltins.getInteger(), bookPages.getType());
        assertSame(book, bookPages.getContainer());
        assertEquals("library.Book.pages", bookPages.qualifiedName());
        assertEquals(false, bookPages.isDerived());
        assertEquals(false, bookPages.isOptional());
        assertEquals(true, bookPages.isRequired());

        Reference bookAuthor = (Reference) book.getFeatureByName("author");
        assertSame(metamodel.getElementByName("Writer"), bookAuthor.getType());
        assertSame(book, bookAuthor.getContainer());
        assertEquals("library.Book.author", bookAuthor.qualifiedName());
        assertEquals(false, bookAuthor.isDerived());
        assertEquals(false, bookAuthor.isOptional());
        assertEquals(true, bookAuthor.isRequired());
        assertEquals(false, bookAuthor.isMultiple());
        assertEquals(null, bookAuthor.getSpecialized());

        Concept library = (Concept)metamodel.getElementByName("Library");
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
        assertEquals(false, libraryName.isDerived());
        assertEquals(false, libraryName.isOptional());
        assertEquals(true, libraryName.isRequired());

        Containment libraryBooks = (Containment) library.getFeatureByName("books");
        assertSame(metamodel.getElementByName("Book"), libraryBooks.getType());
        assertSame(library, libraryBooks.getContainer());
        assertEquals("library.Library.books", libraryBooks.qualifiedName());
        assertEquals(false, libraryBooks.isDerived());
        assertEquals(true, libraryBooks.isOptional());
        assertEquals(false, libraryBooks.isRequired());
        assertEquals(true, libraryBooks.isMultiple());
        assertEquals(null, libraryBooks.getSpecialized());

        Concept writer = (Concept)metamodel.getElementByName("Writer");
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
        assertEquals(false, writerName.isDerived());
        assertEquals(false, writerName.isOptional());
        assertEquals(true, writerName.isRequired());

        Concept guideBookWriter = (Concept)metamodel.getElementByName("GuideBookWriter");
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
        assertEquals(false, guideBookWriterCountries.isDerived());
        assertEquals(true, guideBookWriterCountries.isOptional());
        assertEquals(false, guideBookWriterCountries.isRequired());

        Property guideBookWriterName = (Property) guideBookWriter.getFeatureByName("name");
        assertEquals(LionCoreBuiltins.getString(), guideBookWriterName.getType());
        assertSame(writer, guideBookWriterName.getContainer());
        assertEquals("library.Writer.name", guideBookWriterName.qualifiedName());
        assertEquals(false, guideBookWriterName.isDerived());
        assertEquals(false, guideBookWriterName.isOptional());
        assertEquals(true, guideBookWriterName.isRequired());

        Concept specialistBookWriter = (Concept)metamodel.getElementByName("SpecialistBookWriter");
        assertSame(writer, specialistBookWriter.getExtendedConcept());
        assertEquals(0, specialistBookWriter.getImplemented().size());
        assertFalse(specialistBookWriter.isAbstract());
        assertEquals("library.SpecialistBookWriter", specialistBookWriter.qualifiedName());
        assertEquals(1, specialistBookWriter.getFeatures().size());
        assertEquals(2, specialistBookWriter.allFeatures().size());

        Property specialistBookWriterSubject = (Property) specialistBookWriter.getFeatureByName("subject");
        assertEquals(LionCoreBuiltins.getString(), specialistBookWriterSubject.getType());
        assertSame(specialistBookWriter, specialistBookWriterSubject.getContainer());
        assertEquals("library.SpecialistBookWriter.subject", specialistBookWriterSubject.qualifiedName());
        assertEquals(false, specialistBookWriterSubject.isDerived());
        assertEquals(true, specialistBookWriterSubject.isOptional());
        assertEquals(false, specialistBookWriterSubject.isRequired());

        Property specialistBookWriterName = (Property) specialistBookWriter.getFeatureByName("name");
        assertEquals(LionCoreBuiltins.getString(), specialistBookWriterName.getType());
        assertSame(writer, specialistBookWriterName.getContainer());
        assertEquals("library.Writer.name", specialistBookWriterName.qualifiedName());
        assertEquals(false, specialistBookWriterName.isDerived());
        assertEquals(false, specialistBookWriterName.isOptional());
        assertEquals(true, specialistBookWriterName.isRequired());
    }

}
