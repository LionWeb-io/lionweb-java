package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.support.JSONResourceFactory;
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

/**
 * Importer that given an EMF Resource imports something out of it.
 *
 * @param <E> kind of imported element
 */
public abstract class AbstractEMFImporter<E> {

  protected final ConceptsToEClassesMapping conceptsToEClassesMapping;

  public AbstractEMFImporter() {
    this.conceptsToEClassesMapping = new ConceptsToEClassesMapping();
  }

  public AbstractEMFImporter(ConceptsToEClassesMapping conceptsToEClassesMapping) {
    this.conceptsToEClassesMapping = conceptsToEClassesMapping;
  }

  /** Import the file. The resource type is derived from the extension. */
  public List<E> importFile(File emfFile) {
    recordFactoriesForExtensions();
    ResourceSet resourceSet = new ResourceSetImpl();
    Resource resource = resourceSet.getResource(URI.createFileURI(emfFile.getAbsolutePath()), true);
    return importResource(resource);
  }

  /**
   * Load the resource from the given InputStream. The Resource is considered to be of type Ecore.
   */
  public List<E> importInputStream(
      InputStream inputStream, Consumer<EPackage.Registry> packageRegistryInit) throws IOException {
    return importInputStream(inputStream, ResourceType.ECORE, packageRegistryInit);
  }

  /**
   * Load the resource from the given InputStream. The Resource is considered to be of type Ecore.
   */
  public List<E> importInputStream(InputStream inputStream) throws IOException {
    return importInputStream(inputStream, ResourceType.ECORE, null);
  }

  public List<E> importInputStream(InputStream inputStream, ResourceType resourceType)
      throws IOException {
    return importInputStream(inputStream, resourceType, null);
  }

  /** Import a given resource as a list of elements. */
  public List<E> importInputStream(
      InputStream inputStream,
      ResourceType resourceType,
      Consumer<EPackage.Registry> packageRegistryInit)
      throws IOException {
    recordFactoriesForExtensions();
    ResourceSet resourceSet = new ResourceSetImpl();
    if (packageRegistryInit != null) {
      packageRegistryInit.accept(resourceSet.getPackageRegistry());
    }
    URI uri = URI.createFileURI("dummy." + resourceType.getExtension());

    Resource resource = resourceSet.createResource(uri);
    resourceSet.getPackageRegistry().put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
    resource.load(inputStream, new HashMap<>());

    return importResource(resource);
  }

  public abstract List<E> importResource(Resource resource);

  private void recordFactoriesForExtensions() {
    Map<String, Object> extensionsToFactoryMap =
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
    extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
    extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());
    extensionsToFactoryMap.put("json", new JSONResourceFactory());
  }
}
