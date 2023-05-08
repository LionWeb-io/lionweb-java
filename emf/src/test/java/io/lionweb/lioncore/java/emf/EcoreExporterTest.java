package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import org.eclipse.emf.ecore.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class EcoreExporterTest {

  @Test
  public void exportLibraryMetamodel() throws IOException {
    Metamodel libraryMM = (Metamodel)JsonSerialization.getStandardSerialization().unserializeToNodes(this.getClass().getResourceAsStream("/library-metamodel.json")).get(0);

    EcoreExporter ecoreExporter = new EcoreExporter();
    EPackage libraryPkg = ecoreExporter.exportMetamodel(libraryMM);

    assertEquals("library", libraryPkg.getName());
    assertEquals("https://lionweb.io/library", libraryPkg.getNsURI());
    assertEquals("library", libraryPkg.getNsPrefix());
    assertEquals(5, libraryPkg.getEClassifiers().size());

    EClass writer = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Writer")).findFirst().get();

    EClass book = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Book")).findFirst().get();
    assertEquals("Book", book.getName());
    assertEquals(false, book.isAbstract());
    assertEquals(false, book.isInterface());
    assertEquals(3, book.getEStructuralFeatures().size());

    EAttribute bookTitle = (EAttribute)book.getEStructuralFeature("title");
    assertEquals("title", bookTitle.getName());
    assertEquals(1, bookTitle.getLowerBound());
    assertEquals(1, bookTitle.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEString(), bookTitle.getEType());

    EAttribute bookPages = (EAttribute)book.getEStructuralFeature("pages");
    assertEquals("pages", bookPages.getName());
    assertEquals(1, bookPages.getLowerBound());
    assertEquals(1, bookPages.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEInt(), bookPages.getEType());

    EReference bookAuthor = (EReference)book.getEStructuralFeature("author");
    assertEquals("author", bookAuthor.getName());
    assertEquals(false, bookAuthor.isContainment());
    assertEquals(1, bookPages.getLowerBound());
    assertEquals(-1, bookPages.getUpperBound());
    assertEquals(writer, bookPages.getEType());

    EClass library = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Library")).findFirst().get();


    EClass specialistBookWriter = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("SpecialistBookWriter")).findFirst().get();

    EClass guideBookWriter = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("GuideBookWriter")).findFirst().get();
  }

}
