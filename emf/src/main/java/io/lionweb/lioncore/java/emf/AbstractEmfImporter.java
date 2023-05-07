package io.lionweb.lioncore.java.emf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

public abstract class AbstractEmfImporter<E> {
  enum ResourceType {
    XML,
    JSON,
    ECORE
  }

  public List<E> importFile(File ecoreFile) {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
    extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());
    extensionsToFactoryMap.put("json", new JsonResourceFactory());

    ResourceSet resourceSet = new ResourceSetImpl();

    Resource resource =
        resourceSet.getResource(URI.createFileURI(ecoreFile.getAbsolutePath()), true);
    return importResource(resource);
  }

  public List<E> importInputStream(
      InputStream inputStream, Consumer<EPackage.Registry> packageRegistryInit) throws IOException {
    return importInputStream(inputStream, ResourceType.ECORE, packageRegistryInit);
  }

  public List<E> importInputStream(InputStream inputStream) throws IOException {
    return importInputStream(inputStream, ResourceType.ECORE, null);
  }

  public List<E> importInputStream(InputStream inputStream, ResourceType resourceType)
      throws IOException {
    return importInputStream(inputStream, resourceType, null);
  }

  public List<E> importInputStream(
      InputStream inputStream,
      ResourceType resourceType,
      Consumer<EPackage.Registry> packageRegistryInit)
      throws IOException {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
    extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());
    extensionsToFactoryMap.put("json", new JsonResourceFactory());

    ResourceSet resourceSet = new ResourceSetImpl();

    if (packageRegistryInit != null) {
      packageRegistryInit.accept(resourceSet.getPackageRegistry());
    }

    URI uri;
    switch (resourceType) {
      case ECORE:
        uri = URI.createFileURI("dummy.ecore");
        break;
      case XML:
        uri = URI.createFileURI("dummy.xml");
        break;
      case JSON:
        uri = URI.createFileURI("dummy.json");
        break;
      default:
        throw new UnsupportedOperationException();
    }

    Resource resource = resourceSet.createResource(uri);
    if (resourceType == ResourceType.JSON) {
      new EMFJsonLoader().load(inputStream, resource);
    } else {
      resourceSet
          .getPackageRegistry()
          .put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
      resource.load(inputStream, new HashMap<>());
    }

    return importResource(resource);
  }

  public abstract List<E> importResource(Resource resource);
}
