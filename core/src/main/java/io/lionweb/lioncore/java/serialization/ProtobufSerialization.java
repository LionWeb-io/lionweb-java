package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.UsedLanguage;
import io.lionweb.lioncore.java.serialization.protobuf.CompactedId;
import io.lionweb.lioncore.java.serialization.protobuf.ReferenceTarget;
import io.lionweb.lioncore.java.serialization.protobuf.SerializationChunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ProtobufSerialization implements ISerialization {
    private final Set<Language> languages = new HashSet<>();

    @Override
    public OutputStream serialize(Stream<Node> nodes, OutputStream out) {
        try {
            new Serializer().serialize(nodes, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    private static class Serializer {
        private final Map<String, Integer> idStrings = new HashMap<>();
        private final Map<MetaPointer, Integer> metaPointers = new HashMap<>();
        private final Map<UsedLanguage, Integer> usedLanguages = new HashMap<>();
        private final Map<String, Integer> versions = new HashMap<>();
        private final SerializationChunk.Builder chunk;
        private final PrimitiveValuesSerialization primitiveValuesSerialization = new PrimitiveValuesSerialization();

        Serializer() {
            chunk = SerializationChunk.newBuilder();
            primitiveValuesSerialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers();
            primitiveValuesSerialization.enableDynamicNodes();
        }

        void serialize(Stream<Node> nodes, OutputStream out) throws IOException {
            chunk.setSerializationFormatVersion(JsonSerialization.DEFAULT_SERIALIZATION_FORMAT);
            nodes.forEach(this::serialize);
            chunk.build().writeTo(out);
        }

        private void serialize(Node node) {
            io.lionweb.lioncore.java.serialization.protobuf.Node.Builder builder = chunk.addNodesBuilder();
            builder.setId(idString(node.getID()));
            builder.setClassifier(metaPointer(MetaPointer.from(node.getConcept())));
            if (node.getParent() != null) {
                builder.setParent(idString(node.getParent().getID()));
            }
            node.getClassifier().allProperties().forEach(p -> serialize(builder, p, node));
            node.getClassifier().allContainments().forEach(c -> serialize(builder, c, node));
            node.getClassifier().allReferences().forEach(r -> serialize(builder, r, node));
            node.getAnnotations().forEach(a -> builder.addAnnotations(idString(a.getID())));
        }

        private void serialize(io.lionweb.lioncore.java.serialization.protobuf.Node.Builder builder, Property property, Node node) {
            Object value = node.getPropertyValue(property);
            if (value == null) {
                return;
            }
            String serialized = primitiveValuesSerialization.serialize(property.getType().getID(), value);
            builder
                    .addPropertiesBuilder()
                    .setMetaPointer(metaPointer(MetaPointer.from(property)))
                    .setValue(string(serialized))
                    .build();
        }

        private void serialize(io.lionweb.lioncore.java.serialization.protobuf.Node.Builder builder, Containment c, Node node) {
            io.lionweb.lioncore.java.serialization.protobuf.Containment.Builder containmentBuilder = builder
                    .addContainmentsBuilder()
                    .setMetaPointer(metaPointer(MetaPointer.from(c)));
            IntStream stream = node.getChildren().stream().mapToInt(child -> idString(child.getID()));
            stream.forEachOrdered(containmentBuilder::addChildren);
        }

        private void serialize(io.lionweb.lioncore.java.serialization.protobuf.Node.Builder builder, Reference r, Node node) {
            io.lionweb.lioncore.java.serialization.protobuf.Reference.Builder referencesBuilder = builder
                    .addReferencesBuilder()
                    .setMetaPointer(metaPointer(MetaPointer.from(r)));

            node.getReferenceValues(r).forEach(v -> {
                ReferenceTarget.Builder targetsBuilder = referencesBuilder.addTargetsBuilder();
                if (v.getReferredID() != null) {
                    targetsBuilder.setReference(idString(v.getReferredID()));
                }
                if (v.getResolveInfo() != null) {
                    targetsBuilder.setResolveInfo(string(v.getResolveInfo()));
                }
                targetsBuilder.build();
            });
        }

        private int idString(String id) {
            return idStrings.computeIfAbsent(id, k -> {
                CompactedId compactedId = IdCompactor.compact(id);
                int key = chunk.getIdStringsCount();
                chunk.putIdStrings(key, compactedId);
                return key;
            });
        }

        private int metaPointer(MetaPointer pointer) {
            return metaPointers.computeIfAbsent(pointer, k -> {
                io.lionweb.lioncore.java.serialization.protobuf.MetaPointer.Builder builder = io.lionweb.lioncore.java.serialization.protobuf.MetaPointer.newBuilder();
                int language = idString(k.getLanguage());
                usedLanguage(new UsedLanguage(k.getLanguage(), k.getVersion()));
                builder.setLanguage(language);
                builder.setKey(idString(k.getKey()));
                builder.setVersion(string(k.getVersion()));
                int key = chunk.getMetaPointersCount();
                chunk.putMetaPointers(key, builder.build());
                return key;
            });
        }

        private int string(String str) {
            return versions.computeIfAbsent(str, k -> {
                int key = chunk.getStringsCount();
                chunk.putStrings(key, str);
                return key;
            });
        }

        private void usedLanguage(UsedLanguage usedLanguage) {
            usedLanguages.computeIfAbsent(usedLanguage, k -> {
                io.lionweb.lioncore.java.serialization.protobuf.UsedLanguage.Builder builder = io.lionweb.lioncore.java.serialization.protobuf.UsedLanguage.newBuilder();
                builder.setLanguage(idString(k.getKey()));
                builder.setVersion(string(k.getVersion()));
                int key = chunk.getLanguagesCount();
                chunk.addLanguages(builder.build());
                return key;
            });
        }
    }

    @Override
    public Stream<Node> deserialize(InputStream inputStream) {
        try {
            SerializationChunk chunk = SerializationChunk.parseFrom(inputStream);
            return new Deserializer(chunk, languages).deserialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class Deserializer {
        private final SerializationChunk chunk;
        private final Map<String, ClassifierInstance<?>> deserializedByID = new HashMap<>();

        private final ClassifierResolver classifierResolver = new ClassifierResolver();
        private final Instantiator instantiator = new Instantiator();
        private final PrimitiveValuesSerialization primitiveValuesSerialization = new PrimitiveValuesSerialization();
        private final LocalClassifierInstanceResolver instanceResolver = new LocalClassifierInstanceResolver();

        Deserializer(SerializationChunk chunk, Collection<Language> languages) {
            this.chunk = chunk;
            classifierResolver.registerLanguage(LionCore.getInstance());
            instantiator.registerLionCoreCustomDeserializers();
            primitiveValuesSerialization.registerLionBuiltinsPrimitiveSerializersAndDeserializers();
            for (Language language : languages) {
                this.classifierResolver.registerLanguage(language);
                this.instantiator.enableDynamicNodes();
                this.primitiveValuesSerialization.registerLanguage(language);
            }
        }

        public Stream<Node> deserialize() {
            chunk.getNodesList().stream().forEach(this::deserialize);

            ClassifierInstanceResolver classifierInstanceResolver =
                    new CompositeClassifierInstanceResolver(
                            new MapBasedResolver(deserializedByID), this.instanceResolver);

            return chunk.getNodesList().stream().map(this::link).filter(Objects::nonNull);
        }

        private ClassifierInstance<?> deserialize(io.lionweb.lioncore.java.serialization.protobuf.Node node) {
            Classifier<?> classifier = classifier(node.getClassifier());
            String id = idString(node.getId());
            SerializedClassifierInstance instance = new SerializedClassifierInstance(id, metaPointer(node.getClassifier()));

            Map<Property, Object> propertiesValues = new HashMap<>();
            node.getPropertiesList().forEach(prop -> {
                Property property = classifier.getPropertyByMetaPointer(metaPointer(prop.getMetaPointer()));
                Object deserializedValue = primitiveValuesSerialization.deserialize(property.getType(), string(prop.getValue()), property.isRequired());
                propertiesValues.put(property, deserializedValue);
            });

            ClassifierInstance<?> result = instantiator.instantiate(classifier, instance, deserializedByID, propertiesValues);
            propertiesValues.forEach((k, v) -> result.setPropertyValue(k, v));
            deserializedByID.put(id, result);
            return result;
        }

        private Node link(io.lionweb.lioncore.java.serialization.protobuf.Node node) {
            String nodeId = idString(node.getId());
            ClassifierInstance<?> instance = deserializedByID.get(nodeId);
            if(instance == null){
                System.err.println("Could not find node for " + nodeId);
                return null;
            }
            node.getContainmentsList().forEach(c -> {
                Containment cont = (Containment) feature(instance.getClassifier(), c.getMetaPointer());
                c.getChildrenList().forEach(child -> instance.addChild(cont, (Node) deserializedByID.get(idString(child))));
            });
            node.getReferencesList().forEach(r -> {
                Reference ref = (Reference) feature(instance.getClassifier(), r.getMetaPointer());
                r.getTargetsList().forEach(t -> instance.addReferenceValue(ref, new ReferenceValue((Node) deserializedByID.get(idString(t.getReference())), string(t.getResolveInfo()))));
            });
            node.getAnnotationsList().forEach(a -> instance.getAnnotations().add((AnnotationInstance) deserializedByID.get(idString(a))));

            return (Node) instance;
        }

        private Classifier<?> classifier(int metaPointerIndex) {
            return classifierResolver.resolveClassifier(metaPointer(metaPointerIndex));
        }

        private Feature feature(Classifier<?> classifier, int metaPointerIndex) {
            MetaPointer metaPointer = metaPointer(metaPointerIndex);
            return classifier.allFeatures().stream().filter(f -> f.getKey().equals(metaPointer.getKey())).findFirst().get();
        }

        private MetaPointer metaPointer(int metaPointerIndex) {
            return metaPointer(chunk.getMetaPointersOrThrow(metaPointerIndex));
        }

        private MetaPointer metaPointer(io.lionweb.lioncore.java.serialization.protobuf.MetaPointer pr) {
            return new MetaPointer(idString(pr.getLanguage()), string(pr.getVersion()), idString(pr.getKey()));
        }

        private String idString(int idStringIndex) {
            CompactedId compactedId = chunk.getIdStringsOrThrow(idStringIndex);
            return IdCompactor.expand(compactedId);
        }

        private String string(int stringIndex) {
            return chunk.getStringsOrThrow(stringIndex);
        }
    }


    @Override
    public void registerLanguage(Language language) {
        languages.add(language);
    }
}
