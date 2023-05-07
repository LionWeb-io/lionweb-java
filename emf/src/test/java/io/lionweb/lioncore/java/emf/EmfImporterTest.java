package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.Node;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmfImporterTest {

  private List<EPackage> loadKotlinEPackages() throws IOException {
    InputStream is = this.getClass().getResourceAsStream("/kotlinlang.json");
    ResourceSet rs = new ResourceSetImpl();
    rs.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
    rs.getResourceFactoryRegistry().getContentTypeToFactoryMap().put("application/json", new JsonResourceFactory());
    Resource resource = rs.createResource(URI.createURI("KotlinLang"), "application/json");
    resource.load(is, new HashMap<>());
    return resource.getContents().stream().map(c -> (EPackage)c).collect(Collectors.toList());
  }

  @Test
  public void importKotlinPrinterExample() throws IOException {
    // We should first load the packages
    List<EPackage> ePackages = loadKotlinEPackages();


    InputStream is = this.getClass().getResourceAsStream("/KotlinPrinterAST.json");
    EmfImporter importer = new EmfImporter();

    List<Node> nodes = importer.importInputStream(is, AbstractEmfImporter.ResourceType.JSON, (Consumer<EPackage.Registry>) registry -> {
      ePackages.forEach(ep -> registry.put(ep.getNsURI(), ep));
    });
  }
}
