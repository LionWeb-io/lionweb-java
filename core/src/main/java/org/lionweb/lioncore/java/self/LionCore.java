package org.lionweb.lioncore.java.self;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.impl.BaseNode;

import java.util.Collections;
import java.util.List;

public class LionCore {

    private LionCore() {
        // prevent instantiation of instances outside of this class
    }

    private static Metamodel INSTANCE;

    public static Concept getAnnotation() {
        return getInstance().requireConceptByName("Annotation");
    }

    public static Concept getConcept() {
        return getInstance().requireConceptByName("Concept");
    }

    public static Concept getConceptInterface() {
        return getInstance().requireConceptByName("ConceptInterface");
    }

    public static Concept getContainment() {
        return getInstance().requireConceptByName("Containment");
    }

    public static Concept getDataType() {
        return getInstance().requireConceptByName("DataType");
    }

    public static Concept getEnumeration() {
        return getInstance().requireConceptByName("Enumeration");
    }

    public static Concept getEnumerationLiteral() {
        return getInstance().requireConceptByName("EnumerationLiteral");
    }

    public static Concept getFeature() {
        return getInstance().requireConceptByName("Feature");
    }

    public static Concept getFeaturesContainer() {
        return getInstance().requireConceptByName("FeaturesContainer");
    }

    public static Concept getLink() {
        return getInstance().requireConceptByName("Link");
    }

    public static Concept getMetamodel() {
        return getInstance().requireConceptByName("Metamodel");
    }

    public static Concept getMetamodelElement() {
        return getInstance().requireConceptByName("MetamodelElement");
    }

    public static ConceptInterface getNamespacedEntity() {
        return getInstance().getConceptInterfaceByName("NamespacedEntity");
    }

    public static ConceptInterface getNamespaceProvider() {
        return getInstance().getConceptInterfaceByName("NamespaceProvider");
    }

    public static Concept getPrimitiveType() {
        return getInstance().requireConceptByName("PrimitiveType");
    }

    public static Concept getProperty() {
        return getInstance().requireConceptByName("Property");
    }

    public static Concept getReference() {
        return getInstance().requireConceptByName("Reference");
    }

    public static Metamodel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Metamodel("LIonCore.M3");
            INSTANCE.setID("LIonCore_M3");

            // We first instantiate all Concepts and ConceptInterfaces
            // we add features only after as the features will have references to these elements
            Concept concept = INSTANCE.addElement(new Concept("Concept"));
            Concept conceptInterface = INSTANCE.addElement(new Concept("ConceptInterface"));
            Concept containment = INSTANCE.addElement(new Concept("Containment"));
            Concept dataType = INSTANCE.addElement(new Concept("DataType"));
            Concept enumeration = INSTANCE.addElement(new Concept("Enumeration"));
            Concept enumerationLiteral = INSTANCE.addElement(new Concept("EnumerationLiteral"));
            Concept feature = INSTANCE.addElement(new Concept("Feature"));
            Concept featuresContainer = INSTANCE.addElement(new Concept("FeaturesContainer"));
            Concept link = INSTANCE.addElement(new Concept("Link"));
            Concept metamodel = INSTANCE.addElement(new Concept("Metamodel"));
            Concept metamodelElement = INSTANCE.addElement(new Concept("MetamodelElement"));
            Concept namespacedEntity = INSTANCE.addElement(new Concept("NamespacedEntity"));
            ConceptInterface namespaceProvider = INSTANCE.addElement(new ConceptInterface("NamespaceProvider"));
            Concept primitiveType = INSTANCE.addElement(new Concept("PrimitiveType"));
            Concept property = INSTANCE.addElement(new Concept("Property"));
            Concept reference = INSTANCE.addElement(new Concept("Reference"));

            // Now we start adding the features to all the Concepts and ConceptInterfaces

            concept.setExtendedConcept(featuresContainer);
            concept.addFeature(Property.createRequired("abstract", LionCoreBuiltins.getBoolean()));
            concept.addFeature(Reference.createOptional("extended", concept));
            concept.addFeature(Reference.createMultiple("implemented", conceptInterface));

            conceptInterface.setExtendedConcept(featuresContainer);
            conceptInterface.addFeature(Reference.createMultiple("extended", conceptInterface));

            containment.setExtendedConcept(link);

            dataType.setExtendedConcept(metamodelElement);
            dataType.setAbstract(true);

            enumeration.setExtendedConcept(dataType);
            enumeration.addImplementedInterface(namespaceProvider);
            enumeration.addFeature(Containment.createMultiple("literals", enumerationLiteral));

            enumerationLiteral.setExtendedConcept(namespacedEntity);

            feature.setExtendedConcept(namespacedEntity);
            feature.addFeature(Property.createRequired("optional", LionCoreBuiltins.getBoolean()));

            featuresContainer.setExtendedConcept(metamodelElement);
            featuresContainer.addImplementedInterface(namespaceProvider);
            featuresContainer.addFeature(Containment.createMultiple("features", feature));

            link.setExtendedConcept(featuresContainer);
            link.addFeature(Property.createRequired("multiple", LionCoreBuiltins.getBoolean()));
            link.addFeature(Reference.createRequired("type", featuresContainer));

            metamodel.addImplementedInterface(namespaceProvider);
            metamodel.addFeature(Reference.createMultiple("dependsOn", metamodel));
            metamodel.addFeature(Containment.createMultiple("elements", metamodelElement));

            metamodelElement.setExtendedConcept(namespacedEntity);
            metamodel.setAbstract(true);

            namespacedEntity.setAbstract(true);
            namespacedEntity.addFeature(Property.createRequired("simpleName", LionCoreBuiltins.getString()));
            namespacedEntity.addFeature(Property.createRequired("qualifiedName", LionCoreBuiltins.getString()));

            namespaceProvider.addFeature(Property.createRequired("namespaceQualifier", LionCoreBuiltins.getString()));

            primitiveType.setExtendedConcept(dataType);

            property.setExtendedConcept(feature);
            property.addFeature(Reference.createRequired("type", dataType));

            reference.setExtendedConcept(link);
        }
        checkIDs(INSTANCE);
        return INSTANCE;
    }

    private static void checkIDs(BaseNode node) {
        if (node.getID() == null) {
            if (node instanceof NamespacedEntity) {
                NamespacedEntity namespacedEntity = (NamespacedEntity) node;
                node.setID(namespacedEntity.qualifiedName().replaceAll("\\.", "_"));
            } else {
                throw new IllegalStateException(node.toString());
            }
        }
        // TODO To be changed once getChildren is implemented correctly
        getChildrenHelper(node).forEach(c -> checkIDs(c));
    }

    private static List<? extends BaseNode> getChildrenHelper(BaseNode node) {
        if (node instanceof Metamodel) {
            return ((Metamodel)node).getElements();
        } else if (node instanceof FeaturesContainer) {
            return ((FeaturesContainer)node).getFeatures();
        } else if (node instanceof Feature) {
            return Collections.emptyList();
        } else {
            throw new UnsupportedOperationException("Unsupported " + node);
        }
    }

}
