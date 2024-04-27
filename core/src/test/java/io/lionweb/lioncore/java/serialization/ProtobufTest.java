package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtobufTest {
    @Test
    public void serializeLibrary() throws IOException {
        Library library = new Library("lib-1", "Language Engineering Library");
        Writer mv = new Writer("mv", "Markus Völter");
        Writer mb = new Writer("mb", "Meinte Boersma");
        Book de = new Book("de", "DSL Engineering", mv).setPages(558);
        Book bfd = new Book("bfd", "Business-Friendly DSLs", mb).setPages(517);
        library.addBook(de);
        library.addBook(bfd);

        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("library.protobuf"))) {
            new ProtobufSerialization().serialize(Stream.of(library, mv, mb, de, bfd), outputStream);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("library.protobuf"))) {
            ProtobufSerialization protobufSerialization = new ProtobufSerialization();
            protobufSerialization.registerLanguage(LibraryLanguage.LIBRARY_MM);
            Stream<Node> stream = protobufSerialization.deserialize(inputStream);

            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("library2.protobuf"))) {
                new ProtobufSerialization().serialize(stream, outputStream);
            }
        }
    }

    @Test
    public void convertToProtobuf() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("/serialization/TestLang-language.json")) {
            List<Node> nodes = JsonSerialization.getStandardSerialization().deserializeToNodes(inputStream);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("export.protobuf"))) {
                new ProtobufSerialization().serialize(nodes.stream(), outputStream);
            }
        }
    }

    @Test
    public void convertToJson() throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream("export.protobuf"))) {
            Stream<Node> nodes = new ProtobufSerialization().deserialize(inputStream);
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("export.json"))) {
                JsonSerialization.getStandardSerialization().serialize(nodes, outputStream);
            }
        }
    }
}
