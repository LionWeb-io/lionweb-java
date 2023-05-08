package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.metamodel.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

import java.util.List;

public class EcoreExporter {
    public Resource exportResource(List<Metamodel> metamodels) {
        Resource resource = new ResourceImpl();
        metamodels.forEach(m -> resource.getContents().add(exportMetamodel(m)));
        return resource;
    }

    private EDataType toEDataType(DataType dataType) {
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

                concept.getFeatures().forEach(f ->  {
                   if (f instanceof Property) {
                       Property property = (Property)f;

                       EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
                       eAttribute.setName(property.getName());
                       if (property.isOptional()) {
                           eAttribute.setLowerBound(0);
                       } else {
                           eAttribute.setLowerBound(1);
                       }
                       eAttribute.setUpperBound(1);
                       eAttribute.setEType(toEDataType(property.getType()));
                       eClass.getEStructuralFeatures().add(eAttribute);
                   } else if (f instanceof Containment) {
                       EReference eReference = EcoreFactory.eINSTANCE.createEReference();
                       eReference.setContainment(true);
                       eClass.getEStructuralFeatures().add(eReference);
                   } else if (f instanceof Reference) {
                       EReference eReference = EcoreFactory.eINSTANCE.createEReference();
                       eReference.setContainment(false);
                       eClass.getEStructuralFeatures().add(eReference);
                   } else {
                       throw new IllegalStateException();
                   }
                });

                ePackage.getEClassifiers().add(eClass);
            } else {
                throw new UnsupportedOperationException();
            }
        });

        return ePackage;
    }
}
