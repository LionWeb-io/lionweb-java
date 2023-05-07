package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.Node;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.junit.Test;

public class EmfImporterTest {

  private List<EPackage> loadKotlinEPackages() throws IOException {
    InputStream is = this.getClass().getResourceAsStream("/kotlinlang.json");
    ResourceSet rs = new ResourceSetImpl();
    rs.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
    rs.getResourceFactoryRegistry()
        .getContentTypeToFactoryMap()
        .put("application/json", new JsonResourceFactory());
    Resource resource = rs.createResource(URI.createURI("KotlinLang"), "application/json");
    resource.load(is, new HashMap<>());
    return resource.getContents().stream().map(c -> (EPackage) c).collect(Collectors.toList());
  }

  @Test
  public void importKotlinPrinterExample() throws IOException {
    // We should first load the packages
    List<EPackage> ePackages = loadKotlinEPackages();

    InputStream is = this.getClass().getResourceAsStream("/KotlinPrinterAST.json");
    EmfImporter importer = new EmfImporter();

    importer.getNodeInstantiator().enableDynamicNodes();

    List<Node> nodes =
        importer.importInputStream(
            is,
            AbstractEmfImporter.ResourceType.JSON,
            (Consumer<EPackage.Registry>)
                registry -> {
                  ePackages.forEach(ep -> registry.put(ep.getNsURI(), ep));
                });
    assertEquals(1, nodes.size());

    Node result = nodes.get(0);
    assertEquals("Result", result.getConcept().getName());

    Node root = result.getOnlyChildByContainmentName("root");
    assertNotNull(root);

    Node rootPosition = root.getOnlyChildByContainmentName("position");
    assertNotNull(rootPosition);
  }
}
