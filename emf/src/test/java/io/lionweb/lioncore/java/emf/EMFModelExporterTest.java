package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import io.lionweb.lioncore.java.serialization.SerializationProvider;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

public class EMFModelExporterTest {

  @Test
  public void exportLibraryInstance() {
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.registerLanguage(LibraryMetamodel.LIBRARY_LANG);
    jsonSerialization.getInstantiator().enableDynamicNodes();
    List<Node> nodes =
        jsonSerialization.deserializeToNodes(
            this.getClass().getResourceAsStream("/langeng-library.json"));
    List<Node> roots =
        nodes.stream().filter(n -> n.getParent() == null).collect(Collectors.toList());

    EMFModelExporter emfExporter = new EMFModelExporter();
    Resource resource = emfExporter.exportResource(roots);

    assertEquals(3, resource.getContents().size());

    EObject mv = resource.getContents().get(0);
    assertEquals("Writer", mv.eClass().getName());
    assertEquals("Markus Voelter", mv.eGet(mv.eClass().getEStructuralFeature("name")));
    assertNull(mv.eContainer());

    EObject mb = resource.getContents().get(1);
    assertEquals("Writer", mb.eClass().getName());
    assertEquals("Meinte Boersma", mb.eGet(mb.eClass().getEStructuralFeature("name")));
    assertNull(mb.eContainer());

    EObject library = resource.getContents().get(2);
    assertEquals("Library", library.eClass().getName());
    assertEquals(
        "Language Engineering Library",
        library.eGet(library.eClass().getEStructuralFeature("name")));
    assertEquals(2, library.eContents().size());
    List<EObject> books =
        (List<EObject>) library.eGet(library.eClass().getEStructuralFeature("books"));
    assertEquals(2, books.size());

    EObject de = books.get(0);
    assertEquals("Book", de.eClass().getName());
    assertEquals("DSL Engineering", de.eGet(de.eClass().getEStructuralFeature("title")));
    assertEquals(558, de.eGet(de.eClass().getEStructuralFeature("pages")));
    assertEquals(mv, de.eGet(de.eClass().getEStructuralFeature("author")));

    EObject bfd = books.get(1);
    assertEquals("Book", bfd.eClass().getName());
    assertEquals("Business-Friendly DSLs", bfd.eGet(de.eClass().getEStructuralFeature("title")));
    assertEquals(517, bfd.eGet(bfd.eClass().getEStructuralFeature("pages")));
    assertEquals(mb, bfd.eGet(bfd.eClass().getEStructuralFeature("author")));
  }

  @Test
  public void exportSingleContainment() {
    InputStream languageIs = this.getClass().getResourceAsStream("/properties.lmm.json");
    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    Language propertiesLanguage = jsonSerialization.loadLanguage(languageIs);
    jsonSerialization.registerLanguage(propertiesLanguage);
    jsonSerialization.getInstantiator().enableDynamicNodes();
    InputStream modelIs = this.getClass().getResourceAsStream("/example1-exported.lm.json");
    List<Node> nodes = jsonSerialization.deserializeToNodes(modelIs);

    List<Node> roots =
        nodes.stream().filter(it -> it.getParent() == null).collect(Collectors.toList());

    EMFModelExporter emfExporter = new EMFModelExporter();
    Resource resource = emfExporter.exportResource(roots);
  }

  @Test
  public void exportPropertiesInstance() {
    Language propertiesLang =
        SerializationProvider.getStandardJsonSerialization()
            .loadLanguage(this.getClass().getResourceAsStream("/properties-language.json"));

    JsonSerialization jsonSerialization = SerializationProvider.getStandardJsonSerialization();
    jsonSerialization.registerLanguage(propertiesLang);
    jsonSerialization.getInstantiator().enableDynamicNodes();

    List<Node> nodes =
        jsonSerialization.deserializeToNodes(
            this.getClass().getResourceAsStream("/properties-instance.json"));
    List<Node> roots =
        nodes.stream().filter(n -> n.getParent() == null).collect(Collectors.toList());

    EMFModelExporter emfExporter = new EMFModelExporter();
    Resource resource = emfExporter.exportResource(roots);

    assertEquals(1, resource.getContents().size());

    EObject file = resource.getContents().get(0);
    assertEquals("PropertiesFile", file.eClass().getName());
    List<EObject> props = (List<EObject>) file.eGet(file.eClass().getEStructuralFeature("props"));
    assertNull(file.eContainer());
    assertEquals(3, props.size());

    EObject integerProp = props.get(0);
    assertEquals("Property", integerProp.eClass().getName());
    assertEquals(
        "integerProp", integerProp.eGet(integerProp.eClass().getEStructuralFeature("name")));
    List<EObject> intPropValues =
        (List<EObject>) integerProp.eGet(integerProp.eClass().getEStructuralFeature("value"));
    assertEquals(file, integerProp.eContainer());
    assertEquals(1, intPropValues.size());

    EObject intValue = intPropValues.get(0);
    assertEquals("IntValue", intValue.eClass().getName());
    assertEquals("1", intValue.eGet(intValue.eClass().getEStructuralFeature("value")));

    EObject booleanProp = props.get(1);
    assertEquals("Property", booleanProp.eClass().getName());
    assertEquals(
        "booleanProp", booleanProp.eGet(booleanProp.eClass().getEStructuralFeature("name")));
    List<EObject> boolPropValues =
        (List<EObject>) booleanProp.eGet(booleanProp.eClass().getEStructuralFeature("value"));
    assertEquals(file, booleanProp.eContainer());
    assertEquals(1, boolPropValues.size());

    EObject boolValue = boolPropValues.get(0);
    assertEquals("BooleanValue", boolValue.eClass().getName());
    assertEquals(true, boolValue.eGet(boolValue.eClass().getEStructuralFeature("value")));

    EObject stringProp = props.get(2);
    assertEquals("Property", stringProp.eClass().getName());
    assertEquals("stringProp", stringProp.eGet(stringProp.eClass().getEStructuralFeature("name")));
    List<EObject> stringPropValues =
        (List<EObject>) stringProp.eGet(stringProp.eClass().getEStructuralFeature("value"));
    assertEquals(file, stringProp.eContainer());
    assertEquals(1, stringPropValues.size());

    EObject stringValue = stringPropValues.get(0);
    assertEquals("StringValue", stringValue.eClass().getName());
    assertEquals(
        "Hello, StarLasu, MPS, and Freon!",
        stringValue.eGet(stringValue.eClass().getEStructuralFeature("value")));
  }
}
