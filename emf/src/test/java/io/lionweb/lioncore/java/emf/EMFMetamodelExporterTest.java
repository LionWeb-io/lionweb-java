package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.*;

import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import java.util.Arrays;
import org.eclipse.emf.ecore.*;
import org.junit.Test;

public class EMFMetamodelExporterTest {

  @Test
  public void exportLibraryLanguage() {
    Language libraryLang =
        (Language)
            JsonSerialization.getStandardSerialization()
                .unserializeToNodes(this.getClass().getResourceAsStream("/library-language.json"))
                .get(0);

    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter();
    EPackage libraryPkg = ecoreExporter.exportLanguage(libraryLang);

    assertEquals("library", libraryPkg.getName());
    assertEquals("https://lionweb.io/library", libraryPkg.getNsURI());
    assertEquals("library", libraryPkg.getNsPrefix());
    assertEquals(5, libraryPkg.getEClassifiers().size());

    EClass writer =
        (EClass)
            libraryPkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("Writer"))
                .findFirst()
                .get();
    assertEquals(Arrays.asList(), writer.getESuperTypes());

    EClass book =
        (EClass)
            libraryPkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("Book"))
                .findFirst()
                .get();
    assertEquals("Book", book.getName());
    assertEquals(false, book.isAbstract());
    assertEquals(false, book.isInterface());
    assertEquals(3, book.getEStructuralFeatures().size());

    EAttribute bookTitle = (EAttribute) book.getEStructuralFeature("title");
    assertEquals("title", bookTitle.getName());
    assertEquals(1, bookTitle.getLowerBound());
    assertEquals(1, bookTitle.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEString(), bookTitle.getEType());

    EAttribute bookPages = (EAttribute) book.getEStructuralFeature("pages");
    assertEquals("pages", bookPages.getName());
    assertEquals(1, bookPages.getLowerBound());
    assertEquals(1, bookPages.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEInt(), bookPages.getEType());

    EReference bookAuthor = (EReference) book.getEStructuralFeature("author");
    assertEquals("author", bookAuthor.getName());
    assertEquals(false, bookAuthor.isContainment());
    assertEquals(1, bookAuthor.getLowerBound());
    assertEquals(1, bookAuthor.getUpperBound());
    assertEquals(writer, bookAuthor.getEType());

    EClass library =
        (EClass)
            libraryPkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("Library"))
                .findFirst()
                .get();

    EClass specialistBookWriter =
        (EClass)
            libraryPkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("SpecialistBookWriter"))
                .findFirst()
                .get();
    assertEquals(Arrays.asList(writer), specialistBookWriter.getESuperTypes());

    EClass guideBookWriter =
        (EClass)
            libraryPkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("GuideBookWriter"))
                .findFirst()
                .get();
    assertEquals(Arrays.asList(writer), guideBookWriter.getESuperTypes());
  }

  @Test
  public void exportInterfaceAndEnumeration() {
    Language simpleLang = new Language("SimpleMM").setKey("simkey").setID("simid");
    Enumeration color = new Enumeration(simpleLang, "Color");
    new EnumerationLiteral(color, "red");
    new EnumerationLiteral(color, "white");
    new EnumerationLiteral(color, "green");
    Interface coloredCI = new Interface(simpleLang, "Colored");
    coloredCI.addFeature(Property.createRequired("color", color));

    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter();
    EPackage simplePkg = ecoreExporter.exportLanguage(simpleLang);

    assertEquals("SimpleMM", simplePkg.getName());
    assertEquals("https://lionweb.io/simkey", simplePkg.getNsURI());
    assertEquals("SimpleMM", simplePkg.getNsPrefix());
    assertEquals(2, simplePkg.getEClassifiers().size());

    EEnum colorDT =
        (EEnum)
            simplePkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("Color"))
                .findFirst()
                .get();
    assertEquals(3, colorDT.getELiterals().size());
    assertEquals("red", colorDT.getELiterals().get(0).getName());
    assertEquals("red", colorDT.getELiterals().get(0).getLiteral());
    assertEquals("white", colorDT.getELiterals().get(1).getName());
    assertEquals("white", colorDT.getELiterals().get(1).getLiteral());
    assertEquals("green", colorDT.getELiterals().get(2).getName());
    assertEquals("green", colorDT.getELiterals().get(2).getLiteral());

    EClass coloredEC =
        (EClass)
            simplePkg.getEClassifiers().stream()
                .filter(e -> e.getName().equals("Colored"))
                .findFirst()
                .get();
    assertEquals("Colored", coloredEC.getName());
    assertEquals(true, coloredEC.isInterface());
    assertEquals(1, coloredEC.getEStructuralFeatures().size());

    EAttribute colorAttr = coloredEC.getEAllAttributes().get(0);
    assertEquals("color", colorAttr.getName());
    assertEquals(colorDT, colorAttr.getEAttributeType());
    assertEquals(1, colorAttr.getLowerBound());
    assertEquals(1, colorAttr.getUpperBound());
  }
}
