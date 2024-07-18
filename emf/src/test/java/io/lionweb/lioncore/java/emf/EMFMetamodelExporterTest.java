package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.*;

import io.lionweb.java.emf.builtins.BuiltinsPackage;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.junit.Test;

public class EMFMetamodelExporterTest {

  @Test
  public void exportLibraryLanguage() {
    Language libraryLang =
        (Language)
            SerializationProvider.getStandardJsonSerialization()
                .deserializeToNodes(this.getClass().getResourceAsStream("/library-language.json"))
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
    assertEquals(Arrays.asList(EcorePackage.eINSTANCE.getEObject()), writer.getESuperTypes());

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

  @Test
  public void exportPropertiesLangWithINamed() {
    EClass eObject = EcorePackage.eINSTANCE.getEObject();
    EClass iNamed = BuiltinsPackage.eINSTANCE.getINamed();

    Language propertiesLang =
        (Language)
            SerializationProvider.getStandardJsonSerialization()
                .deserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-language.json"))
                .get(0);

    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter();
    EPackage propertiesPkg = ecoreExporter.exportLanguage(propertiesLang);

    assertEquals("io_lionweb_Properties", propertiesPkg.getName());
    assertEquals("https://lionweb.io/io_lionweb_Properties", propertiesPkg.getNsURI());
    assertEquals("io.lionweb.Properties", propertiesPkg.getNsPrefix());
    assertEquals(7, propertiesPkg.getEClassifiers().size());

    EClass propertiesFile = (EClass) propertiesPkg.getEClassifier("PropertiesFile");
    assertEquals(Arrays.asList(eObject, iNamed), propertiesFile.getESuperTypes());
    assertEquals("PropertiesFile", propertiesFile.getName());
    assertFalse(propertiesFile.isAbstract());
    assertFalse(propertiesFile.isInterface());
    assertEquals(1, propertiesFile.getEStructuralFeatures().size());

    EClass property = (EClass) propertiesPkg.getEClassifier("Property");
    assertEquals(Arrays.asList(eObject, iNamed), property.getESuperTypes());
    assertEquals("Property", property.getName());
    assertFalse(property.isAbstract());
    assertFalse(property.isInterface());
    assertEquals(1, property.getEStructuralFeatures().size());

    EClass value = (EClass) propertiesPkg.getEClassifier("Value");
    assertEquals(Arrays.asList(eObject), value.getESuperTypes());
    assertEquals("Value", value.getName());
    assertFalse(value.isAbstract());
    assertFalse(value.isInterface());
    assertEquals(0, value.getEStructuralFeatures().size());

    EClass booleanValue = (EClass) propertiesPkg.getEClassifier("BooleanValue");
    assertEquals(Arrays.asList(value), booleanValue.getESuperTypes());
    assertEquals("BooleanValue", booleanValue.getName());
    assertFalse(booleanValue.isAbstract());
    assertFalse(booleanValue.isInterface());
    assertEquals(1, booleanValue.getEStructuralFeatures().size());

    EClass decValue = (EClass) propertiesPkg.getEClassifier("DecValue");
    assertEquals(Arrays.asList(value), decValue.getESuperTypes());
    assertEquals("DecValue", decValue.getName());
    assertFalse(decValue.isAbstract());
    assertFalse(decValue.isInterface());
    assertEquals(1, decValue.getEStructuralFeatures().size());

    EClass intValue = (EClass) propertiesPkg.getEClassifier("IntValue");
    assertEquals(Arrays.asList(value), intValue.getESuperTypes());
    assertEquals("IntValue", intValue.getName());
    assertFalse(intValue.isAbstract());
    assertFalse(intValue.isInterface());
    assertEquals(1, intValue.getEStructuralFeatures().size());

    EClass stringValue = (EClass) propertiesPkg.getEClassifier("StringValue");
    assertEquals(Arrays.asList(value), stringValue.getESuperTypes());
    assertEquals("StringValue", stringValue.getName());
    assertFalse(stringValue.isAbstract());
    assertFalse(stringValue.isInterface());
    assertEquals(1, stringValue.getEStructuralFeatures().size());

    EReference props = (EReference) propertiesFile.getEStructuralFeature("props");
    assertEquals("props", props.getName());
    assertEquals(1, props.getLowerBound());
    assertEquals(-1, props.getUpperBound());
    assertEquals(property, props.getEType());

    EReference valueRef = (EReference) property.getEStructuralFeature("value");
    assertEquals("value", valueRef.getName());
    assertEquals(1, valueRef.getLowerBound());
    // TODO fix
    assertEquals(-1, valueRef.getUpperBound());
    assertEquals(value, valueRef.getEType());

    EAttribute boolProp = (EAttribute) booleanValue.getEStructuralFeature("value");
    assertEquals("value", boolProp.getName());
    assertEquals(0, boolProp.getLowerBound());
    assertEquals(1, boolProp.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEBoolean(), boolProp.getEType());

    EAttribute decProp = (EAttribute) decValue.getEStructuralFeature("value");
    assertEquals("value", decProp.getName());
    assertEquals(0, decProp.getLowerBound());
    assertEquals(1, decProp.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEString(), decProp.getEType());

    EAttribute intProp = (EAttribute) intValue.getEStructuralFeature("value");
    assertEquals("value", intProp.getName());
    assertEquals(0, intProp.getLowerBound());
    assertEquals(1, intProp.getUpperBound());
    // TODO correct?
    assertEquals(EcorePackage.eINSTANCE.getEString(), intProp.getEType());

    EAttribute stringProp = (EAttribute) stringValue.getEStructuralFeature("value");
    assertEquals("value", stringProp.getName());
    assertEquals(0, stringProp.getLowerBound());
    assertEquals(1, stringProp.getUpperBound());
    assertEquals(EcorePackage.eINSTANCE.getEString(), stringProp.getEType());
  }

  @Test
  public void storePropertiesLangWithINamed() throws IOException {
    Language propertiesLang =
        (Language)
            SerializationProvider.getStandardJsonSerialization()
                .deserializeToNodes(
                    this.getClass().getResourceAsStream("/properties-language.json"))
                .get(0);

    EMFMetamodelExporter ecoreExporter = new EMFMetamodelExporter();
    EPackage propertiesPkg = ecoreExporter.exportLanguage(propertiesLang);

    Resource.Factory.Registry.INSTANCE
        .getExtensionToFactoryMap()
        .put("ecore", new EcoreResourceFactoryImpl());

    ResourceSet resourceSet = new ResourceSetImpl();

    File tempFile = File.createTempFile("gen-properties-language", ".ecore");
    tempFile.deleteOnExit();
    Resource resource = resourceSet.createResource(URI.createFileURI(tempFile.getAbsolutePath()));
    resource.getContents().add(propertiesPkg);
    resource.save(Collections.emptyMap());

    List<String> expected =
        new BufferedReader(
                new InputStreamReader(
                    this.getClass().getResourceAsStream("/properties.ecore"),
                    StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.toList());
    List<String> actual =
        new BufferedReader(
                new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.toList());

    assertEquals(expected, actual);
  }
}
