package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import io.lionweb.lioncore.java.model.Node;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EmfImporter extends AbstractEmfImporter  {

  @Override
  public List<Node> importResource(Resource resource) {
    List<Node> nodes = new LinkedList<>();
    for (EObject content : resource.getContents()) {
      throw new UnsupportedOperationException();
    }
    return nodes;
  }
}
