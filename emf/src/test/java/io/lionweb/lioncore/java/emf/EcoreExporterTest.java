package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
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

    EClass book = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Book")).findFirst().get();

    EClass library = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Library")).findFirst().get();

    EClass writer = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("Writer")).findFirst().get();

    EClass specialistBookWriter = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("SpecialistBookWriter")).findFirst().get();

    EClass guideBookWriter = (EClass) libraryPkg.getEClassifiers().stream().filter(e -> e.getName().equals("GuideBookWriter")).findFirst().get();
  }

}
