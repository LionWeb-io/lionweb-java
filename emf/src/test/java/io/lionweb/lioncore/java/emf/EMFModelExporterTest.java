package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Interface;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LanguageEntity;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

public class EMFModelExporterTest {

  @Test
  public void exportLibraryInstance() {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
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
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> lNodes = jsonSerialization.deserializeToNodes(languageIs);
    List<Language> languages = lNodes.stream().filter(n -> n instanceof Language).map(n -> (Language)n).collect(Collectors.toList());;
    if (languages.size() != 1) {
      throw new IllegalStateException();
    }
    Language propertiesLanguage = languages.get(0);
    jsonSerialization.registerLanguage(propertiesLanguage);
    jsonSerialization.getInstantiator().enableDynamicNodes();
    InputStream modelIs = this.getClass().getResourceAsStream("/example1-exported.lm.json");
    List<Node> nodes = jsonSerialization.deserializeToNodes(modelIs);

    List<Node> roots = nodes.stream().filter(it -> it.getParent() == null).collect(Collectors.toList());

    ConceptsToEClassesMapping conceptMapper = new ConceptsToEClassesMapping();
//    for (LanguageEntity<?> element : propertiesLanguage.getElements()) {
//      if (element instanceof Concept) {
//          EClass eClass = PropertiesPackage.eINSTANCE.EClassifiers.filter(EClass).findFirst[it.name == element.name]
//          if(eClass != null) {
//            conceptMapper.registerMapping(element, eClass)
//          }
//          else {
//            throw new RuntimeException('''Couldn't find EClass for concept «element»''')
//          }
//        } else if (element instanceof Interface) {
//          var eClass = PropertiesPackage.eINSTANCE.EClassifiers.filter(EClass).findFirst[it.name == element.name]
//          if(eClass !== null) {
//            conceptMapper.registerMapping(element, eClass)
//          }
//          else {
//            throw new RuntimeException('''Couldn't find EClass for interface «element»''')
//          }
//        }
//      }
//    }


    EMFModelExporter emfExporter = new EMFModelExporter(conceptMapper);
    Resource resource = emfExporter.exportResource(roots);
  }
}
