package io.lionweb.repoclient;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.language.Classifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

import io.lionweb.repoclient.testing.AbstractRepoClientFunctionalTest;

@Testcontainers
public class PropertiesFunctionalTest extends AbstractRepoClientFunctionalTest {

    @Test
    public void noPartitionsOnNewModelRepository() {
        LionWebRepoClient client = new LionWebRepoClient("localhost", modelRepoPort);
        assertEquals(Collections.emptyList(), client.getPartitionIDs());
    }

    @Test
    public void isNodeExisting() {
        LionWebRepoClient client = new LionWebRepoClient("localhost", modelRepoPort);

        assertFalse(client.isNodeExisting("pp1"));
        assertFalse(client.isNodeExisting("pf1"));
        assertFalse(client.isNodeExisting("prop1"));
        assertFalse(client.isNodeExisting("prop2"));
        assertFalse(client.isNodeExisting("prop3"));

        Node pp1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
        client.createPartition(pp1);

        assertTrue(client.isNodeExisting("pp1"));
        assertFalse(client.isNodeExisting("pf1"));

        Node pf = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesFile, "pf1");
        pf.setParent(pp1);

        Node prop1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop1");
        setPropertyValueByName(prop1, "name", "Prop1");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop1);

        Node prop2 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop2");
        setPropertyValueByName(prop2, "name", "Prop2");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop2);

        Node prop3 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop3");
        setPropertyValueByName(prop3, "name", "Prop3");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop3);

        client.storeTree(pf);

        assertTrue(client.isNodeExisting("pp1"));
        assertTrue(client.isNodeExisting("pf1"));
        assertTrue(client.isNodeExisting("prop1"));
        assertTrue(client.isNodeExisting("prop2"));
        assertTrue(client.isNodeExisting("prop3"));

        client.deletePartition("pp1");
        assertFalse(client.isNodeExisting("pp1"));
        assertFalse(client.isNodeExisting("pf1"));
        assertFalse(client.isNodeExisting("prop1"));
        assertFalse(client.isNodeExisting("prop2"));
        assertFalse(client.isNodeExisting("prop3"));
    }

    @Test
    public void gettingPartitionsAfterStoringPartitions() {
        LionWebClient client = new LionWebClient("localhost", modelRepoPort);
        client.registerLanguage(PropertiesLanguageDefinition.propertiesLanguage);

        Node pp1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
        assertEquals(Collections.emptyList(), client.getPartitionIDs());
        client.createPartition(pp1);
        assertEquals(Collections.singletonList("pp1"), client.getPartitionIDs());
        assertEquals(pp1.getId(), client.retrieve("pp1").getId());
    }

    @Test
    public void gettingNodesAfterStoringNodes() {
        LionWebClient client = new LionWebClient("localhost", modelRepoPort);
        client.registerLanguage(PropertiesLanguageDefinition.propertiesLanguage);

        Node pp1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesPartition, "pp1");
        client.createPartition(pp1);

        Node pf = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.propertiesFile, "pf1");
        pf.setParent(pp1);

        Node prop1 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property, "prop1");
        setPropertyValueByName(prop1, "name", "Prop1");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop1);

        Node prop2 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property);
        setPropertyValueByName(prop2, "name", "Prop2");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop2);

        Node prop3 = DynamicNodeHelpers.dynamicNode(PropertiesLanguageDefinition.property);
        setPropertyValueByName(prop3, "name", "Prop3");
        pf.addChild(pf.getClassifier().getContainmentByName("properties"), prop3);

        client.storeTree(pf);

        Node retrieved = client.retrieve("pf1");
        assertNull(retrieved.getParent());
        assertEquals("pf1", retrieved.getId());
        assertEquals(PropertiesLanguageDefinition.propertiesFile, retrieved.getClassifier());
    }

    // Remaining tests follow the same structure, can be translated if you'd like me to continue
}