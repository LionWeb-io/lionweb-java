package org.lionweb.lioncore.java.emf;

import org.junit.Test;
import org.lionweb.lioncore.java.Concept;
import org.lionweb.lioncore.java.Metamodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

        Concept library = (Concept)metamodel.getElementByName("Library");

        Concept writer = (Concept)metamodel.getElementByName("Writer");

        Concept guideBookWriter = (Concept)metamodel.getElementByName("GuideBookWriter");

        Concept specialistBookWriter = (Concept)metamodel.getElementByName("SpecialistBookWriter");
    }

}
