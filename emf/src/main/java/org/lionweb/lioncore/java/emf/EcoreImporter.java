package org.lionweb.lioncore.java.emf;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.lionweb.lioncore.java.Concept;
import org.lionweb.lioncore.java.Metamodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EcoreImporter {
    public List<Metamodel> importEcoreFile(File ecoreFile) {
        Map<String, Object> extensionsToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
        extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
        extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());

        ResourceSet resourceSet = new ResourceSetImpl();

        Resource resource = resourceSet.getResource(URI.createFileURI(ecoreFile.getAbsolutePath()), true);
        return importResource(resource);
    }

    public List<Metamodel> importEcoreInputStream(InputStream inputStream) throws IOException {
        Map<String, Object> extensionsToFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
        extensionsToFactoryMap.put("ecore", new EcoreResourceFactoryImpl());
        extensionsToFactoryMap.put("xmi", new XMIResourceFactoryImpl());

        ResourceSet resourceSet = new ResourceSetImpl();

        Resource resource = resourceSet.createResource(URI.createFileURI("dummy.ecore"));
        resource.load(inputStream, new HashMap<>());
        return importResource(resource);
    }

    public List<Metamodel> importResource(Resource resource) {
        List<Metamodel> metamodels = new LinkedList<>();
        for (EObject content : resource.getContents()) {
            if (content.eClass().getName().equals(EcorePackage.Literals.EPACKAGE.getName())) {
                metamodels.add(importEPackage((EPackage)content));
            }
        }
        return metamodels;
    }

    public Metamodel importEPackage(EPackage ePackage) {
        Metamodel metamodel = new Metamodel(ePackage.getName());
        for (EClassifier eClassifier : ePackage.getEClassifiers()) {
            if (eClassifier.eClass().getName().equals(EcorePackage.Literals.ECLASS.getName())) {
                EClass eClass = (EClass) eClassifier;
                if (eClass.isInterface()) {
                    throw new UnsupportedOperationException();
                } else {
                    Concept concept = new Concept(metamodel, eClass.getName());
                    metamodel.addElement(concept);
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
        return metamodel;
    }
}
