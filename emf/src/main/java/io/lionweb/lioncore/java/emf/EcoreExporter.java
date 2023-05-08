package io.lionweb.lioncore.java.emf;

import io.lionweb.lioncore.java.emf.mapping.ConceptsToEClassesMapping;
import io.lionweb.lioncore.java.emf.mapping.DataTypeMapping;
import io.lionweb.lioncore.java.metamodel.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;

import java.util.List;

public class EcoreExporter {

    private DataTypeMapping dataTypeMapping = new DataTypeMapping();
    private ConceptsToEClassesMapping conceptsToEClassesMapping = new ConceptsToEClassesMapping();

    /**
     * This export all the metamodels received to a single Resource.
     */
    public Resource exportResource(List<Metamodel> metamodels) {
        Resource resource = new ResourceImpl();
        metamodels.forEach(m -> resource.getContents().add(exportMetamodel(m)));
        return resource;
    }

    /**
     * This export the Metamodel received to a single EPackage.
     */
    public EPackage exportMetamodel(Metamodel metamodel) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

        ePackage.setName(metamodel.getName());
        ePackage.setNsURI("https://lionweb.io/" + metamodel.getKey());
        ePackage.setNsPrefix(metamodel.getName());

        // We first create all EClasses and only later we draw relationships
        // among them
        createEClasses(metamodel, ePackage);
        populateEClasses(metamodel);

        return ePackage;
    }

    private void createEClasses(Metamodel metamodel, EPackage ePackage) {
        metamodel.getElements().forEach(e -> {
            if (e instanceof Concept) {
                Concept concept = (Concept)e;

                EClass eClass = EcoreFactory.eINSTANCE.createEClass();
                eClass.setName(concept.getName());

                ePackage.getEClassifiers().add(eClass);
                conceptsToEClassesMapping.registerMapping(concept, eClass);
            } else if (e instanceof ConceptInterface) {
                throw new UnsupportedOperationException();
            } else if (e instanceof Enumeration) {
                throw new UnsupportedOperationException();
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

    private void populateEClasses(Metamodel metamodel) {
        metamodel.getElements().forEach(e -> {
            if (e instanceof Concept) {
                Concept concept = (Concept) e;

                EClass eClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept);

                if (concept.getExtendedConcept() != null) {
                    EClass superEClass = (EClass) conceptsToEClassesMapping.getCorrespondingEClass(concept.getExtendedConcept());
                    eClass.getESuperTypes().add(superEClass);
                }
                concept.getImplemented().forEach(implemented -> {
                    throw new UnsupportedOperationException();
                });


                concept.getFeatures().forEach(f -> {
                    if (f instanceof Property) {
                        Property property = (Property) f;

                        EAttribute eAttribute = EcoreFactory.eINSTANCE.createEAttribute();
                        eAttribute.setName(property.getName());
                        if (property.isOptional()) {
                            eAttribute.setLowerBound(0);
                        } else {
                            eAttribute.setLowerBound(1);
                        }
                        eAttribute.setUpperBound(1);
                        eAttribute.setEType(dataTypeMapping.toEDataType(property.getType()));
                        eClass.getEStructuralFeatures().add(eAttribute);
                    } else if (f instanceof Containment) {
                        Containment containment = (Containment) f;
                        EReference eReference = EcoreFactory.eINSTANCE.createEReference();
                        eReference.setName(containment.getName());
                        eReference.setContainment(true);
                        if (containment.isMultiple()) {
                            eReference.setUpperBound(-1);
                        } else {
                            eReference.setUpperBound(1);
                        }
                        if (containment.isOptional()) {
                            eReference.setLowerBound(0);
                        } else {
                            eReference.setLowerBound(1);
                        }
                        eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(containment.getType()));
                        eClass.getEStructuralFeatures().add(eReference);
                    } else if (f instanceof Reference) {
                        Reference reference = (Reference) f;
                        EReference eReference = EcoreFactory.eINSTANCE.createEReference();
                        eReference.setName(reference.getName());
                        eReference.setContainment(false);
                        if (reference.isMultiple()) {
                            eReference.setUpperBound(-1);
                        } else {
                            eReference.setUpperBound(1);
                        }
                        if (reference.isOptional()) {
                            eReference.setLowerBound(0);
                        } else {
                            eReference.setLowerBound(1);
                        }
                        eReference.setEType(conceptsToEClassesMapping.getCorrespondingEClass(reference.getType()));
                        eClass.getEStructuralFeatures().add(eReference);
                    } else {
                        throw new IllegalStateException();
                    }
                });
            } else if (e instanceof ConceptInterface) {
                throw new UnsupportedOperationException();
            } else if (e instanceof Enumeration) {
                throw new UnsupportedOperationException();
            } else {
                throw new UnsupportedOperationException();
            }
        });
    }

}
