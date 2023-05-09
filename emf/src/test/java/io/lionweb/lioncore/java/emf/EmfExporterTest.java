package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.Metamodel;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class EmfExporterTest {

  @Test
  public void exportLibraryInstance() throws IOException {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    jsonSerialization.registerMetamodel(LibraryMetamodel.LIBRARY_MM);
    jsonSerialization.getNodeInstantiator().enableDynamicNodes();
    List<Node> nodes = jsonSerialization.unserializeToNodes(this.getClass().getResourceAsStream("/langeng-library.json"));
    List<Node> roots = nodes.stream().filter(n -> n.getParent() == null).collect(Collectors.toList());

    EmfExporter emfExporter = new EmfExporter();
    Resource resource = emfExporter.exportResource(roots);

    assertEquals(3, resource.getContents().size());

    EObject mv = resource.getContents().get(0);
    assertEquals("Writer", mv.eClass().getName());
    assertEquals("Markus Voelter", mv.eGet(mv.eClass().getEStructuralFeature("name")));
    assertEquals(null, mv.eContainer());

    EObject mb = resource.getContents().get(1);
    assertEquals("Writer", mb.eClass().getName());
    assertEquals("Meinte Boersma", mb.eGet(mb.eClass().getEStructuralFeature("name")));
    assertEquals(null, mb.eContainer());

    EObject library = resource.getContents().get(2);
    assertEquals("Library", library.eClass().getName());
    assertEquals("Language Engineering Library", library.eGet(library.eClass().getEStructuralFeature("name")));
    assertEquals(2, library.eContents().size());
    List<EObject> books = (List<EObject>) library.eGet(library.eClass().getEStructuralFeature("books"));
    assertEquals(2, books.size());

    EObject de = books.get(0);
    assertEquals("Book", de.eClass().getName());
    assertEquals("DSL Engineering", de.eGet(de.eClass().getEStructuralFeature("name")));
    assertEquals(558, de.eGet(de.eClass().getEStructuralFeature("pages")));
    assertEquals(mv, de.eGet(de.eClass().getEStructuralFeature("author")));

    EObject bfd = books.get(1);
    assertEquals("Book", bfd.eClass().getName());
    assertEquals("Business-Friendly DSLs", bfd.eGet(de.eClass().getEStructuralFeature("name")));
    assertEquals(517, bfd.eGet(bfd.eClass().getEStructuralFeature("pages")));
    assertEquals(mb, bfd.eGet(bfd.eClass().getEStructuralFeature("author")));
  }
}
