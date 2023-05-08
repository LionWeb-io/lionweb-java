package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.Concept;
import io.lionweb.lioncore.java.metamodel.Metamodel;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.List;

public class EcoreExporter {
    public Resource exportResource(List<Metamodel> metamodels) {
        throw new UnsupportedOperationException();
    }

    public EPackage exportMetamodel(Metamodel metamodel) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

        ePackage.setName(metamodel.getName());
        ePackage.setNsURI("https://lionweb.io/" + metamodel.getKey());
        ePackage.setNsPrefix(metamodel.getName());

        metamodel.getElements().forEach(e -> {
            if (e instanceof Concept) {
                Concept concept = (Concept)e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(concept.getName());

                ePackage.getEClassifiers().add(eClass);
            } else {
                throw new UnsupportedOperationException();
            }
        });

        return ePackage;
    }
}
