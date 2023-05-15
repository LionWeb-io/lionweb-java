package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.lionweb.lioncore.java.language.Concept;
import io.lionweb.lioncore.java.language.Property;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import java.util.List;
import java.util.stream.Collectors;

/** Base class with some utility methods used by several tests. */
abstract class SerializationTest {

  protected List<JsonObject> getNodesByConcept(JsonArray nodes, String conceptKey) {
    return nodes.asList().stream()
        .map(JsonElement::getAsJsonObject)
        .filter(e -> e.get("concept").getAsJsonObject().get("key").getAsString().equals(conceptKey))
        .collect(Collectors.toList());
  }

  protected DynamicNode dynamicNodeByID(List<Node> nodes, String id) {
    return (DynamicNode) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
  }

  protected Concept conceptByID(List<Node> nodes, String id) {
    return (Concept) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
  }

  protected Property propertyByID(List<Node> nodes, String id) {
    return (Property) nodes.stream().filter(e -> e.getID().equals(id)).findFirst().get();
  }
}
