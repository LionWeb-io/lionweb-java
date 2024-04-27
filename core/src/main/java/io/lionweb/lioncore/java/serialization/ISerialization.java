package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Stream;

public interface ISerialization {
    OutputStream serialize(Stream<Node> nodes, OutputStream out);

    Stream<Node> deserialize(InputStream inputStream);

    void registerLanguage(Language language);
}
