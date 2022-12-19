package org.lionweb.lioncore.java.self;

import org.lionweb.lioncore.java.metamodel.*;

public class LionCore {

    private LionCore() {
        // prevent instantiation of instances outside of this class
    }

    private static Metamodel INSTANCE;

    public static Concept getAnnotation() {
        return getInstance().getConceptByName("Annotation");
    }

    public static Concept getConcept() {
        return getInstance().getConceptByName("Concept");
    }

    public static Concept getConceptInterface() {
        return getInstance().getConceptByName("ConceptInterface");
    }

    public static Concept getContainment() {
        return getInstance().getConceptByName("Containment");
    }

    public static Concept getDataType() {
        return getInstance().getConceptByName("DataType");
    }

    public static Concept getEnumeration() {
        return getInstance().getConceptByName("Enumeration");
    }

    public static Concept getEnumerationLiteral() {
        return getInstance().getConceptByName("EnumerationLiteral");
    }

    public static Concept getFeature() {
        return getInstance().getConceptByName("Feature");
    }

    public static Concept getFeaturesContainer() {
        return getInstance().getConceptByName("FeaturesContainer");
    }

    public static Concept getLink() {
        return getInstance().getConceptByName("Link");
    }

    public static Concept getMetamodel() {
        return getInstance().getConceptByName("Metamodel");
    }

    public static Concept getMetamodelElement() {
        return getInstance().getConceptByName("MetamodelElement");
    }

    public static ConceptInterface getNamespacedEntity() {
        return getInstance().getConceptInterfaceByName("NamespacedEntity");
    }

    public static ConceptInterface getNamespaceProvider() {
        return getInstance().getConceptInterfaceByName("NamespaceProvider");
    }

    public static Concept getPrimitiveType() {
        return getInstance().getConceptByName("PrimitiveType");
    }

    public static Concept getProperty() {
        return getInstance().getConceptByName("Property");
    }

    public static Concept getReference() {
        return getInstance().getConceptByName("Reference");
    }

    public static Concept getTypedef() {
        return getInstance().getConceptByName("Typedef");
    }

    public static Metamodel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Metamodel("lionweb.lioncore");

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
            ConceptInterface namespacedEntity = INSTANCE.addElement(new ConceptInterface("NamespacedEntity"));
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

            enumerationLiteral.addImplementedInterface(namespacedEntity);

            feature.addImplementedInterface(namespacedEntity);
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

            metamodelElement.addImplementedInterface(namespacedEntity);
            metamodel.setAbstract(true);

            // NOTE this is a violation of the current metamodel, as we stated that
            //      ConceptInterface can only have derived features. However, this does not seem to be correct
            //      in this case
            namespacedEntity.addFeature(Property.createRequired("simpleName", LionCoreBuiltins.getString()));
            namespacedEntity.addFeature(Property.createRequired("qualifiedName", LionCoreBuiltins.getString()));

            namespaceProvider.addFeature(Property.createRequired("namespaceQualifier", LionCoreBuiltins.getString()));

            primitiveType.setExtendedConcept(dataType);

            property.setExtendedConcept(feature);
            property.addFeature(Reference.createRequired("type", dataType));

            reference.setExtendedConcept(link);
        }
        return INSTANCE;
    }

}
