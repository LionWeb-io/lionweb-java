package io.lionweb.lioncore.java.serialization;

import io.lionweb.lioncore.java.model.Node;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtobufTest {
    @Test
    public void bla() throws IOException {
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

        // TODO doesn't work yet
//        try(BufferedInputStream inputStream=new BufferedInputStream(new FileInputStream("library.protobuf"))) {
//            Stream<Node> stream = new ProtobufSerialization().deserialize(inputStream);
//            List<Node> list = stream.collect(Collectors.toList());
//            System.out.println(list);
//        }
    }
}
