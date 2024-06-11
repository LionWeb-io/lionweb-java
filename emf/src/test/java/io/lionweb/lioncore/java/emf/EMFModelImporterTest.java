package io.lionweb.lioncore.java.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
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

public class EMFModelImporterTest {

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
    EMFModelImporter importer = new EMFModelImporter();

    importer.getNodeInstantiator().enableDynamicNodes();

    List<Node> nodes =
        importer.importInputStream(
            is,
            ResourceType.JSON,
            (Consumer<EPackage.Registry>)
                registry -> {
                  ePackages.forEach(ep -> registry.put(ep.getNsURI(), ep));
                });
    assertEquals(1, nodes.size());

    Node result = nodes.get(0);
    assertEquals("Result", result.getClassifier().getName());

    Node root = ClassifierInstanceUtils.getOnlyChildByContainmentName(result, "root");
    assertNotNull(root);

    Node rootPosition = ClassifierInstanceUtils.getOnlyChildByContainmentName(root, "position");
    assertNotNull(rootPosition);

    assertEquals("Position", rootPosition.getClassifier().getName());

    Node rootPositionStart =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(rootPosition, "start");
    assertEquals(1, ClassifierInstanceUtils.getPropertyValueByName(rootPositionStart, "line"));
    assertEquals(0, ClassifierInstanceUtils.getPropertyValueByName(rootPositionStart, "column"));

    Node rootPositionEnd =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(rootPosition, "end");
    assertEquals(280, ClassifierInstanceUtils.getPropertyValueByName(rootPositionEnd, "line"));
    assertEquals(0, ClassifierInstanceUtils.getPropertyValueByName(rootPositionEnd, "column"));

    Node rootElement = ClassifierInstanceUtils.getOnlyChildByContainmentName(root, "elements");
    assertEquals("KClassDeclaration", rootElement.getClassifier().getName());
    assertEquals(
        "KotlinPrinter", ClassifierInstanceUtils.getPropertyValueByName(rootElement, "name"));

    Node rootElementPrimaryConstructor =
        ClassifierInstanceUtils.getOnlyChildByContainmentName(rootElement, "primaryConstructor");
    assertEquals("KPrimaryConstructor", rootElementPrimaryConstructor.getClassifier().getName());
  }
}
