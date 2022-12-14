package org.lionweb.lioncore.java.self;

import org.lionweb.lioncore.java.metamodel.Concept;
import org.lionweb.lioncore.java.metamodel.ConceptInterface;
import org.lionweb.lioncore.java.metamodel.LionCoreBuiltins;
import org.lionweb.lioncore.java.metamodel.Metamodel;

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

            Concept annotation = INSTANCE.addConcept("Annotation");
            Concept concept = INSTANCE.addConcept("Concept");
            Concept conceptInterface = INSTANCE.addConcept("ConceptInterface");
            Concept containment = INSTANCE.addConcept("Containment");
            Concept dataType = INSTANCE.addConcept("DataType");
            Concept enumeration = INSTANCE.addConcept("Enumeration");
            Concept enumerationLiteral = INSTANCE.addConcept("EnumerationLiteral");
            Concept feature = INSTANCE.addConcept("Feature");
            Concept featuresContainer = INSTANCE.addConcept("FeaturesContainer");
            Concept link = INSTANCE.addConcept("Link");
            Concept metamodel = INSTANCE.addConcept("Metamodel");
            Concept metamodelElement = INSTANCE.addConcept("MetamodelElement");
            ConceptInterface namespacedEntity = INSTANCE.addConceptInterface("NamespacedEntity");
            ConceptInterface namespaceProvider = INSTANCE.addConceptInterface("NamespaceProvider");
            Concept primitiveType = INSTANCE.addConcept("PrimitiveType");
            Concept property = INSTANCE.addConcept("Property");
            Concept reference = INSTANCE.addConcept("Reference");
            Concept typedef = INSTANCE.addConcept("Typedef");

            annotation.setExtendedConcept(featuresContainer);
            annotation.addOptionalProperty("platformSpecific", LionCoreBuiltins.getString());
            annotation.addRequiredReference("target", featuresContainer);

            concept.setExtendedConcept(featuresContainer);
            concept.addRequiredProperty("abstract", LionCoreBuiltins.getBoolean());
            concept.addOptionalReference("extended", concept);
            concept.addMultipleReference("implemented", conceptInterface);

            conceptInterface.setExtendedConcept(featuresContainer);
            conceptInterface.addMultipleReference("extended", conceptInterface);

            containment.setExtendedConcept(link);

            dataType.setExtendedConcept(metamodelElement);
            dataType.setAbstract(true);

            enumeration.setExtendedConcept(dataType);
            enumeration.addImplementedInterface(namespaceProvider);
            enumeration.addMultipleContainment("literals", enumerationLiteral);

            enumerationLiteral.addImplementedInterface(namespacedEntity);

            feature.addImplementedInterface(namespacedEntity);
            feature.addRequiredProperty("optional", LionCoreBuiltins.getBoolean());

            featuresContainer.setExtendedConcept(metamodelElement);
            featuresContainer.addImplementedInterface(namespaceProvider);
            featuresContainer.addMultipleContainment("features", feature);

            link.setExtendedConcept(featuresContainer);
            link.addRequiredProperty("multiple", LionCoreBuiltins.getBoolean());
            link.addRequiredReference("type", featuresContainer);

            metamodel.addImplementedInterface(namespaceProvider);
            metamodel.addMultipleReference("dependsOn", metamodel);
            metamodel.addMultipleContainment("elements", metamodelElement);

            metamodelElement.addImplementedInterface(namespacedEntity);
            metamodel.setAbstract(true);

            // NOTE this is a violation of the current metamodel, as we stated that
            //      ConceptInterface can only have derived features. However this does not seem to be correct
            //      in this case
            namespacedEntity.addRequiredProperty("simpleName", LionCoreBuiltins.getString());
            namespacedEntity.addProperty("qualifiedName", LionCoreBuiltins.getString(), false, true);

            namespaceProvider.addProperty("namespaceQualifier", LionCoreBuiltins.getString(), false, true);

            primitiveType.setExtendedConcept(dataType);

            property.setExtendedConcept(feature);
            property.addRequiredReference("type", dataType);

            reference.setExtendedConcept(link);

            typedef.setExtendedConcept(dataType);
            typedef.addRequiredReference("primitiveType", primitiveType);
        }
        return INSTANCE;
    }

}
