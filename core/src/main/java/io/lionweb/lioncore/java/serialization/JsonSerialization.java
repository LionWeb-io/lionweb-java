package io.lionweb.lioncore.java.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.utils.NetworkUtils;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

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
public class JsonSerialization {
  public static final String DEFAULT_SERIALIZATION_FORMAT = "2023.1";

  public static void saveLanguageToFile(Language language, File file) throws IOException {
    String content = getStandardSerialization().serializeTreesToJsonString(language);
    file.getParentFile().mkdirs();
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(content);
    writer.close();
  }

  public Language loadLanguage(File file) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(file);
    Language language = loadLanguage(fileInputStream);
    fileInputStream.close();
    ;
    return language;
  }

  public Language loadLanguage(InputStream inputStream) {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
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

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    jsonSerialization.classifierResolver.registerLanguage(LionCore.getInstance());
    jsonSerialization.instantiator.registerLionCoreCustomDeserializers();
    jsonSerialization.primitiveValuesSerialization
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
    jsonSerialization.instanceResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization.instanceResolver.addAll(
        LionCoreBuiltins.getInstance().thisAndAllDescendants());
    return jsonSerialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getBasicSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    return jsonSerialization;
  }

  private final ClassifierResolver classifierResolver;
  private final Instantiator instantiator;
  private final PrimitiveValuesSerialization primitiveValuesSerialization;

  private final LocalClassifierInstanceResolver instanceResolver;

  private JsonSerialization() {
    // prevent public access
    classifierResolver = new ClassifierResolver();
    instantiator = new Instantiator();
    primitiveValuesSerialization = new PrimitiveValuesSerialization();
    instanceResolver = new LocalClassifierInstanceResolver();
  }

  //
  // Configuration
  //

  public ClassifierResolver getClassifierResolver() {
    return classifierResolver;
  }

  public Instantiator getInstantiator() {
    return instantiator;
  }

  public PrimitiveValuesSerialization getPrimitiveValuesSerialization() {
    return primitiveValuesSerialization;
  }

  public LocalClassifierInstanceResolver getInstanceResolver() {
    return instanceResolver;
  }

  public void enableDynamicNodes() {
    instantiator.enableDynamicNodes();
    primitiveValuesSerialization.enableDynamicNodes();
  }

  //
  // Serialization
  //

  public SerializedChunk serializeTreeToSerializationBlock(Node root) {
    return serializeNodesToSerializationBlock(root.thisAndAllDescendants());
  }

  public SerializedChunk serializeNodesToSerializationBlock(List<Node> nodes) {
    SerializedChunk serializationBlock = new SerializedChunk();
    serializationBlock.setSerializationFormatVersion(DEFAULT_SERIALIZATION_FORMAT);
    for (Node node : nodes) {
      Objects.requireNonNull(node, "nodes should not contain null values");
      serializationBlock.addClassifierInstance(serializeNode(node));
      node.getAnnotations()
          .forEach(
              annotationInstance -> {
                serializationBlock.addClassifierInstance(
                    serializeAnnotationInstance(annotationInstance));
              });
      Objects.requireNonNull(
          node.getConcept(), "A node should have a concept in order to be serialized");
      Objects.requireNonNull(
          node.getConcept().getLanguage(),
          "A Concept should be part of a Language in order to be serialized. Concept "
              + node.getConcept()
              + " is not");
      registerLanguage(node.getConcept().getLanguage());
      UsedLanguage languageKeyVersion = UsedLanguage.fromLanguage(node.getConcept().getLanguage());
      if (!serializationBlock.getLanguages().contains(languageKeyVersion)) {
        serializationBlock.getLanguages().add(languageKeyVersion);
      }
    }
    return serializationBlock;
  }

  public SerializedChunk serializeNodesToSerializationBlock(Node... nodes) {
    return serializeNodesToSerializationBlock(Arrays.asList(nodes));
  }

  public JsonElement serializeTreeToJsonElement(Node node) {
    return serializeNodesToJsonElement(node.thisAndAllDescendants());
  }

  public JsonElement serializeTreesToJsonElement(Node... roots) {
    Set<String> nodesIDs = new HashSet<>();
    List<Node> allNodes = new ArrayList<>();
    for (Node root : roots) {
      root.thisAndAllDescendants()
          .forEach(
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
    return serializeNodesToJsonElement(allNodes);
  }

  public JsonElement serializeNodesToJsonElement(List<Node> nodes) {
    SerializedChunk serializationBlock = serializeNodesToSerializationBlock(nodes);
    return new LowLevelJsonSerialization().serializeToJsonElement(serializationBlock);
  }

  public JsonElement serializeNodesToJsonElement(Node... nodes) {
    return serializeNodesToJsonElement(Arrays.asList(nodes));
  }

  public String serializeTreeToJsonString(Node node) {
    return jsonElementToString(serializeTreeToJsonElement(node));
  }

  public String serializeTreesToJsonString(Node... nodes) {
    return jsonElementToString(serializeTreesToJsonElement(nodes));
  }

  public String serializeNodesToJsonString(List<Node> nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  public String serializeNodesToJsonString(Node... nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  //
  // Serialization - Private
  //

  private String jsonElementToString(JsonElement element) {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(element);
  }

  private SerializedClassifierInstance serializeNode(@Nonnull Node node) {
    Objects.requireNonNull(node, "Node should not be null");
    SerializedNodeInstance serializedClassifierInstance = new SerializedNodeInstance();
    serializedClassifierInstance.setID(node.getID());
    serializedClassifierInstance.setClassifier(MetaPointer.from(node.getConcept()));
    if (node.getParent() != null) {
      serializedClassifierInstance.setParentNodeID(node.getParent().getID());
    }
    serializeProperties(node, serializedClassifierInstance);
    serializeContainments(node, serializedClassifierInstance);
    serializeReferences(node, serializedClassifierInstance);
    serializeAnnotations(node, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private SerializedClassifierInstance serializeAnnotationInstance(
      @Nonnull AnnotationInstance annotationInstance) {
    Objects.requireNonNull(annotationInstance, "AnnotationInstance should not be null");
    SerializedAnnotationInstance serializedClassifierInstance = new SerializedAnnotationInstance();
    serializedClassifierInstance.setID(annotationInstance.getID());
    serializedClassifierInstance.setParentNodeID(annotationInstance.getParent().getID());
    serializedClassifierInstance.setClassifier(
        MetaPointer.from(annotationInstance.getAnnotationDefinition()));
    serializeProperties(annotationInstance, serializedClassifierInstance);
    serializeContainments(annotationInstance, serializedClassifierInstance);
    serializeReferences(annotationInstance, serializedClassifierInstance);
    serializeAnnotations(annotationInstance, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private static void serializeAnnotations(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    serializedClassifierInstance.setAnnotations(
        classifierInstance.getAnnotations().stream()
            .map(a -> a.getID())
            .collect(Collectors.toList()));
  }

  private static void serializeReferences(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allReferences()
        .forEach(
            reference -> {
              SerializedReferenceValue referenceValue = new SerializedReferenceValue();
              referenceValue.setMetaPointer(
                  MetaPointer.from(
                      reference, ((LanguageEntity) reference.getContainer()).getLanguage()));
              referenceValue.setValue(
                  classifierInstance.getReferenceValues(reference).stream()
                      .map(
                          rv -> {
                            String referredID =
                                rv.getReferred() == null ? null : rv.getReferred().getID();
                            return new SerializedReferenceValue.Entry(
                                referredID, rv.getResolveInfo());
                          })
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addReferenceValue(referenceValue);
            });
  }

  private static void serializeContainments(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allContainments()
        .forEach(
            containment -> {
              SerializedContainmentValue containmentValue = new SerializedContainmentValue();
              containmentValue.setMetaPointer(
                  MetaPointer.from(
                      containment, ((LanguageEntity) containment.getContainer()).getLanguage()));
              containmentValue.setValue(
                  classifierInstance.getChildren(containment).stream()
                      .map(c -> c.getID())
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addContainmentValue(containmentValue);
            });
  }

  private void serializeProperties(
      ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    classifierInstance
        .getClassifier()
        .allProperties()
        .forEach(
            property -> {
              SerializedPropertyValue propertyValue = new SerializedPropertyValue();
              propertyValue.setMetaPointer(
                  MetaPointer.from(
                      property, ((LanguageEntity) property.getContainer()).getLanguage()));
              propertyValue.setValue(
                  serializePropertyValue(
                      property.getType(), classifierInstance.getPropertyValue(property)));
              serializedClassifierInstance.addPropertyValue(propertyValue);
            });
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

  //
  // Deserialization - Private
  //

  private String serializePropertyValue(DataType dataType, Object value) {
    if (value == null) {
      return null;
    }
    return primitiveValuesSerialization.serialize(dataType.getID(), value);
  }

  private void validateSerializationBlock(SerializedChunk serializationBlock) {
    if (!serializationBlock.getSerializationFormatVersion().equals(DEFAULT_SERIALIZATION_FORMAT)) {
      throw new IllegalArgumentException(
          "Only serializationFormatVersion = '" + DEFAULT_SERIALIZATION_FORMAT + "' is supported");
    }
  }

  /**
   * This method returned a sorted version of the original list, so that leaves nodes comes first,
   * or in other words that a parent never precedes its children.
   */
  private List<SerializedClassifierInstance> sortLeavesFirst(
      List<SerializedClassifierInstance> originalList) {
    List<SerializedClassifierInstance> sortedList = new ArrayList<>();
    List<SerializedClassifierInstance> nodesToSort = new ArrayList<>(originalList);
    // We create the list going from the roots, to their children and so on, and then we will revert
    // the list

    // Nodes with null IDs are ambiguous but they cannot be the children of any node: they can just
    // be parent of other nodes, so we put all of them at the start (so they end up at the end when
    // we reverse
    // the list)
    nodesToSort.stream().filter(n -> n.getID() == null).forEach(n -> sortedList.add(n));
    nodesToSort.removeAll(sortedList);

    // We can start by putting at the start all the elements which either have no parent,
    // or had a parent already added to the list
    while (sortedList.size() < originalList.size()) {
      int initialLength = sortedList.size();
      for (int i = 0; i < nodesToSort.size(); i++) {
        SerializedClassifierInstance n = nodesToSort.get(i);
        if (n instanceof SerializedNodeInstance) {
          SerializedNodeInstance serializedNodeInstance = (SerializedNodeInstance) n;
          if (serializedNodeInstance.getParentNodeID() == null
              || sortedList.stream()
                  .anyMatch(
                      sn -> Objects.equals(sn.getID(), serializedNodeInstance.getParentNodeID()))) {
            sortedList.add(n);
            nodesToSort.remove(i);
            i--;
          }
        } else if (n instanceof SerializedAnnotationInstance) {
          SerializedAnnotationInstance serializedAnnotationInstance =
              (SerializedAnnotationInstance) n;
          if (serializedAnnotationInstance.getParentNodeID() == null
              || sortedList.stream()
                  .anyMatch(
                      sn ->
                          Objects.equals(
                              sn.getID(), serializedAnnotationInstance.getParentNodeID()))) {
            sortedList.add(n);
            nodesToSort.remove(i);
            i--;
          }
        }
      }
      if (initialLength == sortedList.size()) {
        if (sortedList.isEmpty()) {
          throw new DeserializationException(
              "No root found, we cannot deserialize this tree. Original list: " + originalList);
        } else {
          throw new DeserializationException(
              "Something is not right: we are unable to complete sorting the list "
                  + originalList
                  + ". Probably there is a containment loop");
        }
      }
    }

    Collections.reverse(sortedList);
    return sortedList;
  }

  public List<ClassifierInstance<?>> deserializeSerializationBlock(
      SerializedChunk serializationBlock) {
    return deserializeClassifierInstances(serializationBlock.getClassifierInstances());
  }

  private List<ClassifierInstance<?>> deserializeClassifierInstances(
      List<SerializedClassifierInstance> serializedClassifierInstances) {
    // We want to deserialize the nodes starting from the leaves. This is useful because in certain
    // cases we may want to use the children as constructor parameters of the parent
    List<SerializedClassifierInstance> sortedSerializedClassifierInstances =
        sortLeavesFirst(serializedClassifierInstances);
    if (sortedSerializedClassifierInstances.size() != serializedClassifierInstances.size()) {
      throw new IllegalStateException();
    }
    Map<String, ClassifierInstance<?>> deserializedByID = new HashMap<>();
    IdentityHashMap<SerializedClassifierInstance, ClassifierInstance<?>> serializedToInstanceMap =
        new IdentityHashMap<>();
    sortedSerializedClassifierInstances.stream()
        .forEach(
            n -> {
              ClassifierInstance<?> instantiated = instantiateFromSerialized(n, deserializedByID);
              if (n.getID() != null && deserializedByID.containsKey(n.getID())) {
                throw new IllegalStateException("Duplicate ID found: " + n.getID());
              }
              deserializedByID.put(n.getID(), instantiated);
              serializedToInstanceMap.put(n, instantiated);
            });
    if (sortedSerializedClassifierInstances.size() != serializedToInstanceMap.size()) {
      throw new IllegalStateException(
          "We got "
              + sortedSerializedClassifierInstances.size()
              + " nodes to deserialize, but we deserialized "
              + serializedToInstanceMap.size());
    }
    ClassifierInstanceResolver classifierInstanceResolver =
        new CompositeClassifierInstanceResolver(
            new MapBasedResolver(deserializedByID), this.instanceResolver);
    serializedClassifierInstances.stream()
        .forEach(
            n -> {
              populateClassifierInstance(
                  n, serializedToInstanceMap.get(n), classifierInstanceResolver);
              ClassifierInstance<?> classifierInstance = serializedToInstanceMap.get(n);
              if (classifierInstance instanceof AnnotationInstance) {
                SerializedAnnotationInstance serializedAnnotationInstance =
                    (SerializedAnnotationInstance) n;
                if (serializedAnnotationInstance == null) {
                  throw new IllegalStateException(
                      "Dangling annotation instance found (annotated node is null). ");
                }
                Node annotatedNode =
                    (Node) deserializedByID.get(serializedAnnotationInstance.getParentNodeID());
                AnnotationInstance annotationInstance = (AnnotationInstance) classifierInstance;
                if (annotatedNode != null) {
                  annotatedNode.addAnnotation(annotationInstance);
                } else {
                  throw new IllegalStateException(
                      "Cannot resolved annotated node " + annotationInstance.getParent());
                }
              }
            });

    // We want the nodes returned to be sorted as the original serializedNodes
    List<ClassifierInstance<?>> nodesWithOriginalSorting =
        serializedClassifierInstances.stream()
            .map(sn -> serializedToInstanceMap.get(sn))
            .collect(Collectors.toList());
    return nodesWithOriginalSorting;
  }

  private ClassifierInstance<?> instantiateFromSerialized(
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, ClassifierInstance<?>> deserializedByID) {
    MetaPointer serializedClassifier = serializedClassifierInstance.getClassifier();
    if (serializedClassifier == null) {
      throw new RuntimeException("No metaPointer available for " + serializedClassifierInstance);
    }
    Classifier<?> classifier = getClassifierResolver().resolveClassifier(serializedClassifier);

    // We prepare all the properties values and pass them to instantiator, as it could use them to
    // build the node
    Map<Property, Object> propertiesValues = new HashMap<>();
    serializedClassifierInstance
        .getProperties()
        .forEach(
            serializedPropertyValue -> {
              Property property =
                  classifier.getPropertyByMetaPointer(serializedPropertyValue.getMetaPointer());
              Objects.requireNonNull(
                  property,
                  "Property with metaPointer "
                      + serializedPropertyValue.getMetaPointer()
                      + " not found in classifier "
                      + classifier
                      + ". SerializedNode: "
                      + serializedClassifierInstance);
              Object deserializedValue =
                  primitiveValuesSerialization.deserialize(
                      property.getType(), serializedPropertyValue.getValue());
              propertiesValues.put(property, deserializedValue);
            });
    ClassifierInstance<?> classifierInstance =
        getInstantiator()
            .instantiate(
                classifier, serializedClassifierInstance, deserializedByID, propertiesValues);

    // We ensure that the properties values are set correctly. They could already have been set
    // while instantiating the node. If that is the case, we have nothing to do, otherwise we set
    // the values
    propertiesValues
        .entrySet()
        .forEach(
            pv -> {
              Object deserializedValue = pv.getValue();
              Property property = pv.getKey();
              // Avoiding calling setters, in case the value has been already set at construction
              // time

              if (!Objects.equals(
                  deserializedValue, classifierInstance.getPropertyValue(property))) {
                classifierInstance.setPropertyValue(property, deserializedValue);
              }
            });

    return classifierInstance;
  }

  private void populateClassifierInstance(
      SerializedClassifierInstance serializedClassifierInstance,
      ClassifierInstance<?> node,
      ClassifierInstanceResolver classifierInstanceResolver) {
    populateContainments(serializedClassifierInstance, node, classifierInstanceResolver);
    populateNodeReferences(serializedClassifierInstance, node, classifierInstanceResolver);
  }

  private void populateContainments(
      SerializedClassifierInstance serializedClassifierInstance,
      ClassifierInstance<?> node,
      ClassifierInstanceResolver classifierInstanceResolver) {
    Classifier<?> concept = node.getClassifier();
    serializedClassifierInstance
        .getContainments()
        .forEach(
            serializedContainmentValue -> {
              Containment containment =
                  concept.getContainmentByMetaPointer(serializedContainmentValue.getMetaPointer());
              Objects.requireNonNull(
                  containment,
                  "Unable to resolve containment "
                      + serializedContainmentValue.getMetaPointer()
                      + " in concept "
                      + concept);
              Objects.requireNonNull(
                  serializedContainmentValue.getValue(),
                  "The containment value should not be null");
              List<ClassifierInstance<?>> deserializedValue =
                  serializedContainmentValue.getValue().stream()
                      .map(childNodeID -> classifierInstanceResolver.strictlyResolve(childNodeID))
                      .collect(Collectors.toList());
              if (!Objects.equals(deserializedValue, node.getChildren(containment))) {
                deserializedValue.forEach(child -> node.addChild(containment, (Node) child));
              }
            });
  }

  private void populateNodeReferences(
      SerializedClassifierInstance serializedClassifierInstance,
      ClassifierInstance<?> node,
      ClassifierInstanceResolver classifierInstanceResolver) {
    Classifier<?> concept = node.getClassifier();
    // TODO resolve references to Nodes in different models
    serializedClassifierInstance
        .getReferences()
        .forEach(
            serializedReferenceValue -> {
              Reference reference =
                  concept.getReferenceByMetaPointer(serializedReferenceValue.getMetaPointer());
              if (reference == null) {
                throw new IllegalStateException(
                    "Unable to solve reference "
                        + serializedReferenceValue.getMetaPointer()
                        + ". Concept "
                        + concept
                        + ". SerializedNode "
                        + serializedClassifierInstance);
              }
              serializedReferenceValue
                  .getValue()
                  .forEach(
                      entry -> {
                        Node referred =
                            (Node) classifierInstanceResolver.resolve(entry.getReference());
                        if (entry.getReference() != null && referred == null) {
                          throw new DeserializationException(
                              "Unable to resolve reference to "
                                  + entry.getReference()
                                  + " for feature "
                                  + serializedReferenceValue.getMetaPointer());
                        }
                        ReferenceValue referenceValue =
                            new ReferenceValue(referred, entry.getResolveInfo());
                        node.addReferenceValue(reference, referenceValue);
                      });
            });
  }

  public void registerLanguage(Language language) {
    getClassifierResolver().registerLanguage(language);
    getPrimitiveValuesSerialization().registerLanguage(language);
  }
}
