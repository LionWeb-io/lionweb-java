package org.lionweb.lioncore.java.metamodel;

import org.junit.Assert;
import org.junit.Test;
import org.lionweb.lioncore.java.self.LionCore;

public class MetaCircularityTest {

    @Test
    public void eachElementOfM3HasRightConcept() {
        Assert.assertSame(LionCore.getConcept(), LionCore.getAnnotation().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getConcept().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getConceptInterface().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getContainment().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getDataType().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getEnumeration().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getEnumerationLiteral().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getFeature().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getFeaturesContainer().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getLink().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getMetamodel().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getMetamodelElement().getConcept());
        Assert.assertSame(LionCore.getConceptInterface(), LionCore.getNamespacedEntity().getConcept());
        Assert.assertSame(LionCore.getConceptInterface(), LionCore.getNamespaceProvider().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getPrimitiveType().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getProperty().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getReference().getConcept());
        Assert.assertSame(LionCore.getConcept(), LionCore.getTypedef().getConcept());
    }

    @Test
    public void eachElementOfM2HasRightConcept() {
        Assert.assertSame(LionCore.getAnnotation(), new Annotation().getConcept());
        Assert.assertSame(LionCore.getConcept(), new Concept().getConcept());
        Assert.assertSame(LionCore.getConceptInterface(), new ConceptInterface().getConcept());
        Assert.assertSame(LionCore.getContainment(), new Containment().getConcept());
        Assert.assertSame(LionCore.getEnumeration(), new Enumeration().getConcept());
        Assert.assertSame(LionCore.getEnumerationLiteral(), new EnumerationLiteral().getConcept());
        Assert.assertSame(LionCore.getMetamodel(), new Metamodel().getConcept());
        Assert.assertSame(LionCore.getPrimitiveType(), new PrimitiveType().getConcept());
        Assert.assertSame(LionCore.getProperty(), new Property().getConcept());
        Assert.assertSame(LionCore.getReference(), new Reference().getConcept());
        Assert.assertSame(LionCore.getTypedef(), new Typedef().getConcept());
    }
}
