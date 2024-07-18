package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializationProvider.getStandardJsonSerialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.utils.NetworkUtils;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for deserializing models.
 *
 * <p>The deserialization of each node _requires_ the deserializer to be able to resolve the Concept
 * used. If this requirement is not satisfied the deserialization will fail. The actual class
 * implementing Node being instantiated will depend on the configuration. Specific classes for
 * specific Concepts can be registered, and the usage of DynamicNode for all others can be enabled.
 *
 * <p>Note that by default JsonSerialization will require specific Node subclasses to be specified.
 * For example, it will need to know that the concept with id 'foo-library' can be deserialized to
 * instances of the class Library. If you want serialization to instantiate DynamicNodes for
 * concepts for which you do not have a corresponding Node subclass, then you need to enable that
 * behavior explicitly by calling getNodeInstantiator().enableDynamicNodes().
 */
public class JsonSerialization extends AbstractSerialization {

  public static void saveLanguageToFile(Language language, File file) throws IOException {
    String content = getStandardJsonSerialization().serializeTreesToJsonString(language);
    file.getParentFile().mkdirs();
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(content);
    writer.close();
  }

  /**
   * Load a single Language from a file. If the file contains more than one language an exception is
   * thrown.
   */
  public Language loadLanguage(File file) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(file);
    Language language = loadLanguage(fileInputStream);
    fileInputStream.close();
    return language;
  }

  /**
   * Load a single Language from an InputStream. If the InputStream contains more than one language
   * an exception is thrown.
   */
  public Language loadLanguage(InputStream inputStream) {
    JsonSerialization jsonSerialization = getStandardJsonSerialization();
    List<Node> lNodes = jsonSerialization.deserializeToNodes(inputStream);
    List<Language> languages =
        lNodes.stream()
            .filter(n -> n instanceof Language)
            .map(n -> (Language) n)
            .collect(Collectors.toList());
    if (languages.size() != 1) {
      throw new IllegalStateException();
    }
    return languages.get(0);
  }

  /**
   * We want to protect this from access, as the default constructor would not add the lioncore and
   * lioncore builtins support which most users may expect.
   */
  JsonSerialization() {
    // prevent public access
    super();
  }

  //
  // Serialization
  //

  public JsonElement serializeTreeToJsonElement(ClassifierInstance<?> classifierInstance) {
    if (classifierInstance instanceof ProxyNode) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
    ClassifierInstance.collectSelfAndDescendants(classifierInstance, true, classifierInstances);

    return serializeNodesToJsonElement(
        classifierInstances.stream()
            .filter(n -> !(n instanceof ProxyNode))
            .collect(Collectors.toList()));
  }

  public JsonElement serializeTreesToJsonElement(ClassifierInstance<?>... roots) {
    Set<String> nodesIDs = new HashSet<>();
    List<ClassifierInstance<?>> allNodes = new ArrayList<>();
    for (ClassifierInstance<?> root : roots) {
      Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
      ClassifierInstance.collectSelfAndDescendants(root, true, classifierInstances);
      classifierInstances.forEach(
          n -> {
            // We support serialization of incorrect nodes, so we allow nodes without ID to be
            // serialized
            if (n.getID() != null) {
              if (!nodesIDs.contains(n.getID())) {
                allNodes.add(n);
                nodesIDs.add(n.getID());
              }
            } else {
              allNodes.add(n);
            }
          });
    }
    return serializeNodesToJsonElement(
        allNodes.stream().filter(n -> !(n instanceof ProxyNode)).collect(Collectors.toList()));
  }

  public JsonElement serializeNodesToJsonElement(List<ClassifierInstance<?>> classifierInstances) {
    if (classifierInstances.stream().anyMatch(n -> n instanceof ProxyNode)) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    SerializedChunk serializationBlock = serializeNodesToSerializationBlock(classifierInstances);
    return new LowLevelJsonSerialization().serializeToJsonElement(serializationBlock);
  }

  public JsonElement serializeNodesToJsonElement(ClassifierInstance<?>... classifierInstances) {
    return serializeNodesToJsonElement(Arrays.asList(classifierInstances));
  }

  public String serializeTreeToJsonString(ClassifierInstance<?> classifierInstance) {
    return jsonElementToString(serializeTreeToJsonElement(classifierInstance));
  }

  public String serializeTreesToJsonString(ClassifierInstance<?>... classifierInstances) {
    return jsonElementToString(serializeTreesToJsonElement(classifierInstances));
  }

  public String serializeNodesToJsonString(List<ClassifierInstance<?>> classifierInstances) {
    return jsonElementToString(serializeNodesToJsonElement(classifierInstances));
  }

  public String serializeNodesToJsonString(ClassifierInstance<?>... classifierInstances) {
    return jsonElementToString(serializeNodesToJsonElement(classifierInstances));
  }

  //
  // Serialization - Private
  //

  private String jsonElementToString(JsonElement element) {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(element);
  }

  //
  // Deserialization
  //

  public List<Node> deserializeToNodes(File file) throws FileNotFoundException {
    return deserializeToNodes(new FileInputStream(file));
  }

  public List<Node> deserializeToNodes(JsonElement jsonElement) {
    return deserializeToClassifierInstances(jsonElement).stream()
        .filter(ci -> ci instanceof Node)
        .map(ci -> (Node) ci)
        .collect(Collectors.toList());
  }

  public List<ClassifierInstance<?>> deserializeToClassifierInstances(JsonElement jsonElement) {
    SerializedChunk serializationBlock =
        new LowLevelJsonSerialization().deserializeSerializationBlock(jsonElement);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationBlock(serializationBlock);
  }

  public List<Node> deserializeToNodes(URL url) throws IOException {
    String content = NetworkUtils.getStringFromUrl(url);
    return deserializeToNodes(content);
  }

  public List<Node> deserializeToNodes(String json) {
    return deserializeToNodes(JsonParser.parseString(json));
  }

  public List<Node> deserializeToNodes(InputStream inputStream) {
    return deserializeToNodes(JsonParser.parseReader(new InputStreamReader(inputStream)));
  }
}
