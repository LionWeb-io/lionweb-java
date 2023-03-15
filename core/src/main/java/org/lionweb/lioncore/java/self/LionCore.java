package org.lionweb.lioncore.java.self;

import org.lionweb.lioncore.java.metamodel.*;
import org.lionweb.lioncore.java.model.impl.M3Node;

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

    public static Concept getNamespacedEntity() {
        return getInstance().getConceptByName("NamespacedEntity");
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
            INSTANCE.setVersion(1);

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
            ConceptInterface hasKey = INSTANCE.addElement(new ConceptInterface("HasKey"));
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
            concept.addFeature(Property.createRequired("abstract", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Concept_abstract"));
            concept.addFeature(Reference.createOptional("extends", concept, "LIonCore_M3_Concept_extends"));
            concept.addFeature(Reference.createMultiple("implements", conceptInterface, "LIonCore_M3_Concept_implements"));

            conceptInterface.setExtendedConcept(featuresContainer);
            conceptInterface.addFeature(Reference.createMultiple("extends", conceptInterface, "LIonCore_M3_ConceptInterface_extends"));

            containment.setExtendedConcept(link);

            dataType.setExtendedConcept(metamodelElement);
            dataType.setAbstract(true);

            enumeration.setExtendedConcept(dataType);
            enumeration.addImplementedInterface(namespaceProvider);
            enumeration.addFeature(Containment.createMultiple("literals", enumerationLiteral));

            enumerationLiteral.setExtendedConcept(namespacedEntity);

            feature.setExtendedConcept(namespacedEntity);
            feature.addFeature(Property.createRequired("optional", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Feature_optional"));
            feature.addFeature(Property.createRequired("derived", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Feature_derived"));

            featuresContainer.setExtendedConcept(metamodelElement);
            featuresContainer.addImplementedInterface(namespaceProvider);
            featuresContainer.addFeature(Containment.createMultiple("features", feature, "LIonCore_M3_FeaturesContainer_features"));

            hasKey.addFeature(Property.createRequired("key", LionCoreBuiltins.getString(), "LIonCore_M3_HasKey_key"));

            link.setExtendedConcept(feature);
            link.addFeature(Property.createRequired("multiple", LionCoreBuiltins.getBoolean(), "LIonCore_M3_Link_multiple"));
            link.addFeature(Reference.createRequired("type", featuresContainer, "LIonCore_M3_Link_type"));

            metamodel.addImplementedInterface(namespaceProvider);
            metamodel.addImplementedInterface(hasKey);
            metamodel.addFeature(Property.createRequired("name", LionCoreBuiltins.getString(), "LIonCore_M3_Metamodel_name"));
            metamodel.addFeature(Property.createRequired("id", LionCoreBuiltins.getString(), "LIonCore_M3_Metamodel_id"));
            metamodel.addFeature(Property.createRequired("version", LionCoreBuiltins.getInteger(), "LIonCore_M3_Metamodel_version"));
            metamodel.addFeature(Reference.createMultiple("dependsOn", metamodel));
            metamodel.addFeature(Containment.createMultiple("elements", metamodelElement, "LIonCore_M3_Metamodel_elements"));

            metamodelElement.setExtendedConcept(namespacedEntity);
            metamodelElement.addImplementedInterface(hasKey);
            metamodel.setAbstract(true);

            namespacedEntity.setAbstract(true);
            namespacedEntity.addFeature(Property.createRequired("id", LionCoreBuiltins.getString(), "LIonCore_M3_NamespacedEntity_id"));
            namespacedEntity.addFeature(Property.createRequired("simpleName", LionCoreBuiltins.getString(), "LIonCore_M3_NamespacedEntity_simpleName"));
            namespacedEntity.addFeature(Property.createRequired("qualifiedName", LionCoreBuiltins.getString(),
                    "LIonCore_M3_NamespacedEntity_qualifiedName").setDerived(true));

            namespaceProvider.addFeature(Property.createRequired("namespaceQualifier", LionCoreBuiltins.getString(), "LIonCore_M3_NamespaceProvider_namespaceQualifier").setDerived(true));

            primitiveType.setExtendedConcept(dataType);

            property.setExtendedConcept(feature);
            property.addFeature(Reference.createRequired("type", dataType, "LIonCore_M3_Property_type"));

            reference.setExtendedConcept(link);

            checkIDs(INSTANCE);
        }
        checkIDs(INSTANCE);
        INSTANCE.setKey(INSTANCE.getID());
        return INSTANCE;
    }

    private static void checkIDs(M3Node node) {
        if (node.getID() == null) {
            if (node instanceof NamespacedEntity) {
                NamespacedEntity namespacedEntity = (NamespacedEntity) node;
                node.setID(namespacedEntity.qualifiedName().replaceAll("\\.", "_"));
                if (node instanceof HasKey<?>) {
                    ((HasKey<?>) node).setKey(node.getID());
                }
            } else {
                throw new IllegalStateException(node.toString());
            }
        }

        // TODO To be changed once getChildren is implemented correctly
        getChildrenHelper(node).forEach(c -> checkIDs(c));
    }

    private static List<? extends M3Node> getChildrenHelper(M3Node node) {
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
