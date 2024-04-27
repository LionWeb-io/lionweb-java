package io.lionweb.lioncore.java.serialization;

import com.google.protobuf.ByteString;
import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.AnnotationInstance;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.ReferenceValue;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.java.serialization.data.SerializedClassifierInstance;
import io.lionweb.lioncore.java.serialization.data.UsedLanguage;
import io.lionweb.lioncore.java.serialization.protobuf.ReferenceTarget;
import io.lionweb.lioncore.java.serialization.protobuf.SerializationChunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ProtobufSerialization implements ISerialization {

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
        private final SerializationChunk.Builder chunk;
        private final PrimitiveValuesSerialization primitiveValuesSerialization = new PrimitiveValuesSerialization();

        Serializer() {
            chunk = SerializationChunk.newBuilder();
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
            if (value != null) {
                return;
            }
            String serialized = primitiveValuesSerialization.serialize(property.getType().getID(), value);
            builder.addPropertiesBuilder().setMetaPointer(metaPointer(MetaPointer.from(property))).setValue(serialized);
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
                    targetsBuilder.setResolveInfo(v.getResolveInfo());
                }
            });
        }

        private int idString(String id) {
            return idStrings.computeIfAbsent(id, k -> {
//                byte[] bytes = Base64.getUrlDecoder().decode(id);
                byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
                int key = chunk.getIdStringsCount();
                chunk.putIdStrings(key, ByteString.copyFrom(bytes));
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
                builder.setVersion(idString(k.getVersion()));
                int key = chunk.getMetaPointersCount();
                chunk.putMetaPointers(key, builder.build());
                return key;
            });
        }

        private void usedLanguage(UsedLanguage usedLanguage) {
            usedLanguages.computeIfAbsent(usedLanguage, k -> {
                io.lionweb.lioncore.java.serialization.protobuf.UsedLanguage.Builder builder = io.lionweb.lioncore.java.serialization.protobuf.UsedLanguage.newBuilder();
                builder.setLanguage(idString(k.getKey()));
                builder.setVersion(idString(k.getVersion()));
                int key = chunk.getLanguagesCount();
                chunk.addLanguages(builder.build());
                return key;
            });
        }
    }

    @Override
    public Stream<Node> deserialize(InputStream inputStream) {
        try {
            SerializationChunk chunk = SerializationChunk.parseDelimitedFrom(inputStream);
            return new Deserializer(chunk).deserialize();
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

        Deserializer(SerializationChunk chunk) {

            this.chunk = chunk;
        }

        public Stream<Node> deserialize() {
            chunk.getNodesList().stream().forEach(this::deserialize);

            ClassifierInstanceResolver classifierInstanceResolver =
                    new CompositeClassifierInstanceResolver(
                            new MapBasedResolver(deserializedByID), this.instanceResolver);

            return chunk.getNodesList().stream().map(this::link);
        }

        private ClassifierInstance<?> deserialize(io.lionweb.lioncore.java.serialization.protobuf.Node node) {
            Classifier<?> classifier = classifier(node.getClassifier());
            SerializedClassifierInstance instance = new SerializedClassifierInstance(idString(node.getId()), metaPointer(node.getClassifier()));

            Map<Property, Object> propertiesValues = new HashMap<>();
            node.getPropertiesList().forEach(prop -> {
                Property property = classifier.getPropertyByMetaPointer(metaPointer(prop.getMetaPointer()));
                Object deserializedValue = primitiveValuesSerialization.deserialize(property.getType(), prop.getValue(), property.isRequired());
                propertiesValues.put(property, deserializedValue);
            });

            ClassifierInstance<?> result = instantiator.instantiate(classifier, instance, deserializedByID, propertiesValues);
            return result;
        }

        private Node link(io.lionweb.lioncore.java.serialization.protobuf.Node node) {
            ClassifierInstance<?> instance = deserializedByID.get(idString(node.getId()));
            node.getContainmentsList().forEach(c -> {
                Containment cont = (Containment) feature(instance.getClassifier(), c.getMetaPointer());
                c.getChildrenList().forEach(child -> instance.addChild(cont, (Node) deserializedByID.get(idString(child))));
            });
            node.getReferencesList().forEach(r -> {
                Reference ref = (Reference) feature(instance.getClassifier(), r.getMetaPointer());
                r.getTargetsList().forEach(t -> instance.addReferenceValue(ref, new ReferenceValue((Node) deserializedByID.get(idString(t.getReference())), t.getResolveInfo())));
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
            return new MetaPointer(idString(pr.getLanguage()), idString(pr.getKey()), idString(pr.getKey()));
        }

        private String idString(int idStringIndex) {
            return chunk.getIdStringsOrThrow(idStringIndex).toStringUtf8();
        }
    }


    @Override
    public void registerLanguage(Language language) {

    }
}
