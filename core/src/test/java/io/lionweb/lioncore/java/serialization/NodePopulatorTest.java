package io.lionweb.lioncore.java.serialization;

import static org.junit.Assert.assertEquals;

import io.lionweb.lioncore.java.LionWebVersion;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.ClassifierInstanceUtils;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.SerializedChunk;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import java.util.Collections;
import org.junit.Test;

public class NodePopulatorTest {

  @Test
  public void populateReferenceToBuiltinsValueWithCorrectID() {
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    DeserializationStatus deserializationStatus =
        new DeserializationStatus(Collections.emptyList(), serialization.getInstanceResolver());
    NodePopulator nodePopulator =
        new NodePopulator(
            serialization, serialization.getInstanceResolver(), deserializationStatus);

    SerializedChunk chunk =
        new LowLevelJsonSerialization()
            .deserializeSerializationBlock(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2024.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"my-node\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"LionCore-M3\",\n"
                    + "        \"version\": \"2024.1\",\n"
                    + "        \"key\": \"Property\"\n"
                    + "      },\n"
                    + "      \"properties\": [],\n"
                    + "      \"containments\": [],\n"
                    + "      \"references\": [\n"
                    + "        {\n"
                    + "          \"reference\": {\n"
                    + "            \"language\": \"LionCore-M3\",\n"
                    + "            \"version\": \"2024.1\",\n"
                    + "            \"key\": \"Property-type\"\n"
                    + "          },\n"
                    + "          \"targets\": [\n"
                    + "            {\n"
                    + "              \"resolveInfo\": \"Boolean\",\n"
                    + "              \"reference\": \"LionCore-builtins-Boolean\"\n"
                    + "            }\n"
                    + "          ]\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"parent\": \"io-lionweb-Properties-BooleanValue\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}");
    SerializedClassifierInstance serializedNode = chunk.getClassifierInstances().get(0);

    DynamicNode node = new DynamicNode("my-node", LionCore.getProperty());
    nodePopulator.populateClassifierInstance(node, serializedNode);

    assertEquals(
        LionCoreBuiltins.getBoolean(),
        ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(node, "type").getReferred());
  }

  @Test
  public void populateReferenceToBuiltinsValueWithIncorrectID() {
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    DeserializationStatus deserializationStatus =
        new DeserializationStatus(Collections.emptyList(), serialization.getInstanceResolver());
    NodePopulator nodePopulator =
        new NodePopulator(
            serialization, serialization.getInstanceResolver(), deserializationStatus);

    SerializedChunk chunk =
        new LowLevelJsonSerialization()
            .deserializeSerializationBlock(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2024.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"my-node\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"LionCore-M3\",\n"
                    + "        \"version\": \"2024.1\",\n"
                    + "        \"key\": \"Property\"\n"
                    + "      },\n"
                    + "      \"properties\": [],\n"
                    + "      \"containments\": [],\n"
                    + "      \"references\": [\n"
                    + "        {\n"
                    + "          \"reference\": {\n"
                    + "            \"language\": \"LionCore-M3\",\n"
                    + "            \"version\": \"2024.1\",\n"
                    + "            \"key\": \"Property-type\"\n"
                    + "          },\n"
                    + "          \"targets\": [\n"
                    + "            {\n"
                    + "              \"resolveInfo\": \"Boolean\",\n"
                    + "              \"reference\": \"LionCore-builtins-Boolean-for-some-version-different-than-the-one-we-are-using\"\n"
                    + "            }\n"
                    + "          ]\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"parent\": \"io-lionweb-Properties-BooleanValue\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}");
    SerializedClassifierInstance serializedNode = chunk.getClassifierInstances().get(0);

    DynamicNode node = new DynamicNode("my-node", LionCore.getProperty());
    nodePopulator.populateClassifierInstance(node, serializedNode);

    assertEquals(
        LionCoreBuiltins.getBoolean(),
        ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(node, "type").getReferred());
  }

  @Test
  public void populateReferenceToBuiltinsValueWithNoID() {
    AbstractSerialization serialization =
        SerializationProvider.getStandardJsonSerialization(LionWebVersion.v2024_1);
    DeserializationStatus deserializationStatus =
        new DeserializationStatus(Collections.emptyList(), serialization.getInstanceResolver());
    NodePopulator nodePopulator =
        new NodePopulator(
            serialization, serialization.getInstanceResolver(), deserializationStatus);

    SerializedChunk chunk =
        new LowLevelJsonSerialization()
            .deserializeSerializationBlock(
                "{\n"
                    + "  \"serializationFormatVersion\": \"2024.1\",\n"
                    + "  \"languages\": [],\n"
                    + "  \"nodes\": [\n"
                    + "    {\n"
                    + "      \"id\": \"my-node\",\n"
                    + "      \"classifier\": {\n"
                    + "        \"language\": \"LionCore-M3\",\n"
                    + "        \"version\": \"2024.1\",\n"
                    + "        \"key\": \"Property\"\n"
                    + "      },\n"
                    + "      \"properties\": [],\n"
                    + "      \"containments\": [],\n"
                    + "      \"references\": [\n"
                    + "        {\n"
                    + "          \"reference\": {\n"
                    + "            \"language\": \"LionCore-M3\",\n"
                    + "            \"version\": \"2024.1\",\n"
                    + "            \"key\": \"Property-type\"\n"
                    + "          },\n"
                    + "          \"targets\": [\n"
                    + "            {\n"
                    + "              \"resolveInfo\": \"Boolean\",\n"
                    + "              \"reference\": null\n"
                    + "            }\n"
                    + "          ]\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"parent\": \"io-lionweb-Properties-BooleanValue\"\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}");
    SerializedClassifierInstance serializedNode = chunk.getClassifierInstances().get(0);

    DynamicNode node = new DynamicNode("my-node", LionCore.getProperty());
    nodePopulator.populateClassifierInstance(node, serializedNode);

    assertEquals(
        LionCoreBuiltins.getBoolean(),
        ClassifierInstanceUtils.getOnlyReferenceValueByReferenceName(node, "type").getReferred());
  }
}
