package org.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.lionweb.lioncore.java.serialization.data.*;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.lionweb.lioncore.java.serialization.SerializationUtils.*;

public class LowLevelJsonSerialization {
    private static final String CONCEPT_LABEL = "concept";
    private static final String ID_LABEL = "id";

    public LowLevelJsonSerialization() {
    }

    private void readSerializationFormatVersion(SerializationBlock serializationBlock, JsonObject topLevel) {
        if (!topLevel.has("serializationFormatVersion")) {
            throw new IllegalArgumentException("serializationFormatVersion not specified");
        }
        String serializationFormatVersion = topLevel.get("serializationFormatVersion").getAsString();
        serializationBlock.setSerializationFormatVersion(serializationFormatVersion);
    }

    private void readMetamodels(SerializationBlock serializationBlock, JsonObject topLevel) {
        if (!topLevel.has("metamodels")) {
            throw new IllegalArgumentException("metamodels not specified");
        }
        if (topLevel.get("metamodels").isJsonArray()) {
            topLevel.get("metamodels").getAsJsonArray().asList().stream().forEach(element -> {
                try {
                    MetamodelKeyVersion metamodelKeyVersion = new MetamodelKeyVersion();
                    if (element.isJsonObject()) {
                        JsonObject jsonObject = element.getAsJsonObject();
                        if (!jsonObject.has("key") || !jsonObject.has("version")) {
                            throw new IllegalArgumentException("Metamodel should have keys key and version. Found: " + element);
                        }
                        metamodelKeyVersion.setKey(jsonObject.get("key").getAsString());
                        metamodelKeyVersion.setVersion(jsonObject.get("version").getAsString());
                    } else {
                        throw new IllegalArgumentException("Metamodel should be an object. Found: " + element);
                    }
                    serializationBlock.addMetamodel(metamodelKeyVersion);
                } catch (Exception e) {
                    throw new RuntimeException("Issue while unserializing " + element, e);
                }
            });
        } else {
            throw new IllegalArgumentException("We expected a Json Array, we got instead: " + topLevel.get("metamodels"));
        }
    }

    private void readNodes(SerializationBlock serializationBlock, JsonObject topLevel) {
        if (!topLevel.has("nodes")) {
            throw new IllegalArgumentException("nodes not specified");
        }
        if (topLevel.get("nodes").isJsonArray()) {
            topLevel.get("nodes").getAsJsonArray().asList().stream().forEach(element -> {
                try {
                    SerializedNode node = unserializeNode(element);
                    serializationBlock.addNode(node);
                } catch (Exception e) {
                    throw new RuntimeException("Issue while unserializing " + element, e);
                }
            });
        } else {
            throw new IllegalArgumentException("We expected a Json Array, we got instead: " + topLevel.get("nodes"));
        }
    }

    /**
     * This will return a lower-level representation of the information stored in JSON.
     * It is intended to load broken models.
     * <p>
     * Possible usages: repair a broken model, extract a metamodel from the model (“model archeology”), etc.
     * <p>
     * This method follows a "best-effort" approach, try to limit exception thrown and return data whenever is possible,
     * in the measure that it is possible.
     */
    public SerializationBlock readSerializationBlock(JsonElement jsonElement) {
        SerializationBlock serializationBlock = new SerializationBlock();
        if (jsonElement.isJsonObject()) {
            JsonObject topLevel = jsonElement.getAsJsonObject();
            readSerializationFormatVersion(serializationBlock, topLevel);
            readMetamodels(serializationBlock, topLevel);
            readNodes(serializationBlock, topLevel);
            return serializationBlock;
        } else {
            throw new IllegalArgumentException("We expected a Json Object, we got instead: " + jsonElement);
        }
    }

    public JsonElement serializeToJson(SerializationBlock serializationBlock) {
        JsonObject topLevel = new JsonObject();
        topLevel.addProperty("serializationFormatVersion", serializationBlock.getSerializationFormatVersion());

        JsonArray metamodels = new JsonArray();
        topLevel.add("metamodels", metamodels);

        JsonArray nodes = new JsonArray();
        for (SerializedNode node: serializationBlock.getNodes()) {
            JsonObject nodeJson = new JsonObject();
            nodeJson.addProperty("id", node.getID());
            nodeJson.add("concept", serializeToJson(node.getConcept()));

            JsonArray properties = new JsonArray();
            for (SerializedPropertyValue propertyValue : node.getProperties()) {
                JsonObject property = new JsonObject();
                property.add("property", serializeToJson(propertyValue.getMetaPointer()));
                property.addProperty("value", propertyValue.getValue());
                properties.add(property);
            }
            nodeJson.add("properties", properties);

            JsonArray children = new JsonArray();
            for (SerializedContainmentValue childrenValue : node.getContainments()) {
                JsonObject childrenJ = new JsonObject();
                childrenJ.add("containment", serializeToJson(childrenValue.getMetaPointer()));
                childrenJ.add("children", toJsonArray(childrenValue.getValue()));
                children.add(childrenJ);
            }
            nodeJson.add("children", children);

            JsonArray references = new JsonArray();
            for (SerializedReferenceValue referenceValue : node.getReferences()) {
                JsonObject reference = new JsonObject();
                reference.add("reference", serializeToJson(referenceValue.getMetaPointer()));
                reference.add("targets", toJsonArrayOfReferenceValues(referenceValue.getValue()));
                references.add(reference);
            }
            nodeJson.add("references", references);

            nodeJson.addProperty("parent", node.getParentNodeID());

            nodes.add(nodeJson);
        }
        topLevel.add("nodes", nodes);

        return topLevel;
    }

    private JsonElement serializeToJson(MetaPointer metapointer) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("metamodel", metapointer.getMetamodel());
        jsonObject.addProperty("version", metapointer.getVersion());
        jsonObject.addProperty("key", metapointer.getKey());
        return jsonObject;
    }

    private void populateLinks(SerializedNode node, JsonObject data) {
        if (data.has("children")) {
            JsonObject children = data.get("children").getAsJsonObject();
            for (String containmentID : children.keySet()) {
                JsonArray value = children.get(containmentID).getAsJsonArray();
                for (JsonElement childEl : value.asList()) {
                    String childId = childEl.getAsString();
                    node.addChild(containmentID, childId);
                }
            }
        }
        if (data.has("references")) {
            JsonObject references = data.get("references").getAsJsonObject();
            for (String referenceID : references.keySet()) {
                JsonArray value = references.get(referenceID).getAsJsonArray();
                for (JsonElement referredEl : value.asList()) {
                    try {
                        JsonObject referenceObj = referredEl.getAsJsonObject();
                        String referredId = getAsStringOrNull(referenceObj.get("reference"));
                        String resolveInfo = getAsStringOrNull(referenceObj.get("resolveInfo"));
                        node.addReferenceValue(referenceID, new SerializedNode.RawReferenceValue(referredId, resolveInfo));
                    } catch (Exception e) {
                        throw new RuntimeException("Issue deserializing reference " + referenceID, e);
                    }
                }
            }
        }
        if (data.has("parent")) {
            JsonElement parentValue = data.get("parent");
            String parentNodeID = parentValue instanceof JsonNull ? null : parentValue.getAsString();
            node.setParentNodeID(parentNodeID);
        }
    }

    @Nullable
    private SerializedNode unserializeNode(JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("Malformed JSON. Object expected but found " + jsonElement);
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        SerializedNode serializedNode = new SerializedNode();
        serializedNode.setID(tryToGetStringProperty(jsonObject, "id"));
        serializedNode.setConcept(tryToGetMetaPointerProperty(jsonObject, "concept"));
        serializedNode.setParentNodeID(tryToGetStringProperty(jsonObject, "parent"));

        JsonArray properties = jsonObject.get("properties").getAsJsonArray();
        properties.forEach(property -> {
            JsonObject propertyJO = property.getAsJsonObject();
            serializedNode.addPropertyValue(new SerializedPropertyValue(
                    tryToGetMetaPointerProperty(propertyJO, "property"),
                    tryToGetStringProperty(propertyJO, "value")
            ));
        });

        JsonArray children = jsonObject.get("children").getAsJsonArray();
        children.forEach(childrenEntry -> {
            JsonObject childrenJO = childrenEntry.getAsJsonObject();
            serializedNode.addContainmentValue(new SerializedContainmentValue(
                    tryToGetMetaPointerProperty(childrenJO, "containment"),
                    tryToGetArrayOfStringsProperty(childrenJO, "children")
            ));
        });

        JsonArray references = jsonObject.get("references").getAsJsonArray();
        references.forEach(referenceEntry -> {
            JsonObject referenceJO = referenceEntry.getAsJsonObject();
            serializedNode.addReferenceValue(new SerializedReferenceValue(
                    tryToGetMetaPointerProperty(referenceJO, "reference"),
                    tryToGetArrayOfReferencesProperty(referenceJO, "targets")
            ));
        });

        return serializedNode;
    }

    private SerializedNode populateProperties(SerializedNode instance, JsonObject jsonObject) {
        if (!jsonObject.has("properties") && jsonObject.get("properties").isJsonObject()) {
            return instance;
        }
        JsonObject properties = jsonObject.getAsJsonObject("properties");
        for (String propertyId : properties.keySet()) {
            String serializedValue = properties.get(propertyId).getAsString();
            instance.setPropertyValue(propertyId, serializedValue);
        }

        return instance;
    }

}
