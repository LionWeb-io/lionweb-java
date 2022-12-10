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
}
