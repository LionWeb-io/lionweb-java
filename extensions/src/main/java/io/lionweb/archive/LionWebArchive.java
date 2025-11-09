package io.lionweb.archive;

import io.lionweb.LionWebVersion;
import io.lionweb.client.inmemory.InMemoryServer;
import io.lionweb.serialization.*;
import io.lionweb.serialization.data.SerializationChunk;
import io.lionweb.serialization.extensions.TransferFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LionWebArchive {

    public static void load(File file, LionWebVersion lionWebVersion,
                            TransferFormat format, Consumer<SerializationChunk> chunkConsumer) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("The given file does not exist");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("The given file is a directory");
        }
        AbstractSerialization serialization;
        LowLevelJsonSerialization lowLevelJsonSerialization;
        if (format == TransferFormat.JSON) {
            serialization = SerializationProvider.getStandardJsonSerialization(lionWebVersion);
            lowLevelJsonSerialization = new LowLevelJsonSerialization();
        } else if (format == TransferFormat.PROTOBUF) {
            serialization = SerializationProvider.getStandardProtoBufSerialization(lionWebVersion);
        } else {
            throw new UnsupportedOperationException("Unsupported serialization format: " + format);
        }

        final int BUF_4MiB = 4 << 20;
        try (InputStream fileIn = new BufferedInputStream(Files.newInputStream(file.toPath()), BUF_4MiB);
             ZipInputStream zipIn = new ZipInputStream(fileIn)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    throw new IllegalArgumentException("Entry is a directory: " + entry.getName());
                }

                try {
                    SerializationChunk chunk;
                    if (format == TransferFormat.JSON) {
                        String json = readZipEntryAsString(zipIn, entry);
                        chunk = (new LowLevelJsonSerialization()).deserializeSerializationBlock(json);
                    } else if (format == TransferFormat.PROTOBUF) {
                        byte[] bytes = readAllBytes(zipIn);
                        chunk = ((ProtoBufSerialization) serialization).deserializeToChunk(bytes);
                    } else {
                        throw new UnsupportedOperationException("Unsupported serialization format: " + format);
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                    chunkConsumer.accept(chunk);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to deserialize chunk from " + entry.getName(), e);
                }
            }
        }
    }

    public static void load(File file, InMemoryServer server, String repositoryName,
                            LionWebVersion lionWebVersion,
                            TransferFormat format) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void store(File file, InMemoryServer server, String repositoryName,
                             LionWebVersion lionWebVersion,
                             TransferFormat format) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64 * 1024);
        byte[] buf = new byte[64 * 1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static String readZipEntryAsString(InputStream zipInputStream, ZipEntry entry) throws IOException {
        if (entry.isDirectory()) {
            throw new IllegalArgumentException("Cannot read a directory entry: " + entry.getName());
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = zipInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
