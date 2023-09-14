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
 * This class is responsible for unserializing models.
 *
 * <p>The unserialization of each node _requires_ the unserializer to be able to resolve the Concept
 * used. If this requirement is not satisfied the unserialization will fail. The actual class
 * implementing Node being instantiated will depend on the configuration. Specific classes for
 * specific Concepts can be registered, and the usage of DynamicNode for all others can be enabled.
 *
 * <p>Note that by default JsonSerialization will require specific Node subclasses to be specified.
 * For example, it will need to know that the concept with id 'foo-library' can be unserialized to
 * instances of the class Library. If you want serialization to instantiate DynamicNodes for
 * concepts for which you do not have a corresponding Node subclass, then you need to enable that
 * behavior explicitly by calling getNodeInstantiator().enableDynamicNodes().
 */
public class JsonSerialization {
  public static void saveLanguageToFile(Language language, File file) throws IOException {
    String content = getStandardSerialization().serializeTreesToJsonString(language);
    file.getParentFile().mkdirs();
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(content);
    writer.close();
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    jsonSerialization.classifierResolver.registerLanguage(LionCore.getInstance());
    jsonSerialization.nodeInstantiator.registerLionCoreCustomUnserializers();
    jsonSerialization.primitiveValuesSerialization
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    jsonSerialization.nodeResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization.nodeResolver.addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
    return jsonSerialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getBasicSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    return jsonSerialization;
  }

  private ClassifierResolver classifierResolver;
  private Instantiator nodeInstantiator;
  private PrimitiveValuesSerialization primitiveValuesSerialization;

  private LocalClassifierInstanceResolver nodeResolver;

  private JsonSerialization() {
    // prevent public access
    classifierResolver = new ClassifierResolver();
    nodeInstantiator = new Instantiator();
    primitiveValuesSerialization = new PrimitiveValuesSerialization();
    nodeResolver = new LocalClassifierInstanceResolver();
  }

  //
  // Configuration
  //

  public ClassifierResolver getClassifierResolver() {
    return classifierResolver;
  }

  public Instantiator getNodeInstantiator() {
    return nodeInstantiator;
  }

  public PrimitiveValuesSerialization getPrimitiveValuesSerialization() {
    return primitiveValuesSerialization;
  }

  public LocalClassifierInstanceResolver getNodeResolver() {
    return nodeResolver;
  }

  public void enableDynamicNodes() {
    nodeInstantiator.enableDynamicNodes();
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
    serializationBlock.setSerializationFormatVersion("1");
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
    serializedClassifierInstance.setAnnotated(annotationInstance.getAnnotated().getID());
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
  // Unserialization
  //

  public List<Node> unserializeToNodes(File file) throws FileNotFoundException {
    return unserializeToNodes(new FileInputStream(file));
  }

  public List<Node> unserializeToNodes(JsonElement jsonElement) {
    return unserializeToClassifierInstances(jsonElement).stream()
        .filter(ci -> ci instanceof Node)
        .map(ci -> (Node) ci)
        .collect(Collectors.toList());
  }

  public List<ClassifierInstance<?>> unserializeToClassifierInstances(JsonElement jsonElement) {
    SerializedChunk serializationBlock =
        new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
    validateSerializationBlock(serializationBlock);
    return unserializeSerializationBlock(serializationBlock);
  }

  public List<Node> unserializeToNodes(URL url) throws IOException {
    String content = NetworkUtils.getStringFromUrl(url);
    return unserializeToNodes(content);
  }

  public List<Node> unserializeToNodes(String json) {
    return unserializeToNodes(JsonParser.parseString(json));
  }

  public List<Node> unserializeToNodes(InputStream inputStream) {
    return unserializeToNodes(JsonParser.parseReader(new InputStreamReader(inputStream)));
  }

  //
  // Unserialization - Private
  //

  private String serializePropertyValue(DataType dataType, Object value) {
    if (value == null) {
      return null;
    }
    return primitiveValuesSerialization.serialize(dataType.getID(), value);
  }

  private void validateSerializationBlock(SerializedChunk serializationBlock) {
    if (!serializationBlock.getSerializationFormatVersion().equals("1")) {
      throw new IllegalArgumentException("Only serializationFormatVersion = '1' is supported");
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
          if (serializedAnnotationInstance.getAnnotated() == null
              || sortedList.stream()
                  .anyMatch(
                      sn ->
                          Objects.equals(
                              sn.getID(), serializedAnnotationInstance.getAnnotated()))) {
            sortedList.add(n);
            nodesToSort.remove(i);
            i--;
          }
        }
      }
      if (initialLength == sortedList.size()) {
        if (sortedList.size() == 0) {
          throw new UnserializationException(
              "No root found, we cannot unserialize this tree. Original list: " + originalList);
        } else {
          throw new UnserializationException(
              "Something is not right: we are unable to complete sorting the list "
                  + originalList
                  + ". Probably there is a containment loop");
        }
      }
    }

    Collections.reverse(sortedList);
    return sortedList;
  }

  public List<ClassifierInstance<?>> unserializeSerializationBlock(
      SerializedChunk serializationBlock) {
    return unserializeClassifierInstances(serializationBlock.getClassifierInstances());
  }

  private List<ClassifierInstance<?>> unserializeClassifierInstances(
      List<SerializedClassifierInstance> serializedClassifierInstances) {
    // We want to unserialize the nodes starting from the leaves. This is useful because in certain
    // cases we may want to use the children as constructor parameters of the parent
    List<SerializedClassifierInstance> sortedSerializedClassifierInstances =
        sortLeavesFirst(serializedClassifierInstances);
    if (sortedSerializedClassifierInstances.size() != serializedClassifierInstances.size()) {
      throw new IllegalStateException();
    }
    Map<String, ClassifierInstance<?>> unserializedByID = new HashMap<>();
    IdentityHashMap<SerializedClassifierInstance, ClassifierInstance<?>> serializedToInstanceMap =
        new IdentityHashMap<>();
    sortedSerializedClassifierInstances.stream()
        .forEach(
            n -> {
              ClassifierInstance<?> instantiated = instantiateFromSerialized(n, unserializedByID);
              if (n.getID() != null && unserializedByID.containsKey(n.getID())) {
                throw new IllegalStateException("Duplicate ID found: " + n.getID());
              }
              unserializedByID.put(n.getID(), instantiated);
              serializedToInstanceMap.put(n, instantiated);
            });
    if (sortedSerializedClassifierInstances.size() != serializedToInstanceMap.size()) {
      throw new IllegalStateException(
          "We got "
              + sortedSerializedClassifierInstances.size()
              + " nodes to unserialize, but we unserialized "
              + serializedToInstanceMap.size());
    }
    ClassifierInstanceResolver classifierInstanceResolver =
        new CompositeClassifierInstanceResolver(
            new MapBasedResolver(unserializedByID), this.nodeResolver);
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
                      "Dangling annotation instance found (annotated node is null). "
                          + "SerializedAnnotationInstance: "
                          + n);
                }
                Node annotatedNode =
                    (Node) unserializedByID.get(serializedAnnotationInstance.getAnnotated());
                AnnotationInstance annotationInstance = (AnnotationInstance) classifierInstance;
                if (annotatedNode != null) {
                  annotatedNode.addAnnotation(annotationInstance);
                } else {
                  throw new IllegalStateException(
                      "Cannot resolved annotated node " + annotationInstance.getAnnotated());
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
      Map<String, ClassifierInstance<?>> unserializedByID) {
    Classifier<?> classifier =
        getClassifierResolver().resolveClassifier(serializedClassifierInstance.getClassifier());

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
              Object unserializedValue =
                  primitiveValuesSerialization.unserialize(
                      property.getType(), serializedPropertyValue.getValue());
              propertiesValues.put(property, unserializedValue);
            });
    ClassifierInstance<?> classifierInstance =
        getNodeInstantiator()
            .instantiate(
                classifier, serializedClassifierInstance, unserializedByID, propertiesValues);

    // We ensure that the properties values are set correctly. They could already have been set
    // while instantiating the node. If that is the case, we have nothing to do, otherwise we set
    // the values
    propertiesValues
        .entrySet()
        .forEach(
            pv -> {
              Object unserializedValue = pv.getValue();
              Property property = pv.getKey();
              // Avoiding calling setters, in case the value has been already set at construction
              // time

              if (!Objects.equals(
                  unserializedValue, classifierInstance.getPropertyValue(property))) {
                classifierInstance.setPropertyValue(property, unserializedValue);
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
              List<ClassifierInstance<?>> unserializedValue =
                  serializedContainmentValue.getValue().stream()
                      .map(childNodeID -> classifierInstanceResolver.strictlyResolve(childNodeID))
                      .collect(Collectors.toList());
              if (!Objects.equals(unserializedValue, node.getChildren(containment))) {
                unserializedValue.forEach(child -> node.addChild(containment, (Node) child));
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
                          throw new UnserializationException(
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
