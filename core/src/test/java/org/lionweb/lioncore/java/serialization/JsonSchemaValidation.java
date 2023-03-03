package org.lionweb.lioncore.java.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class JsonSchemaValidation {

    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    private static JsonSchema getGenericSerializationSchema() throws IOException {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

        try (
                InputStream schemaStream = inputStreamFromClasspath("serialization/generic-serialization.schema.json")
        ) {
            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            return schema;
        }
    }

    private static JsonSchema getLioncoreSerializationSchema() throws IOException {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

        try (
                InputStream schemaStream = inputStreamFromClasspath("serialization/lioncore.serialization.schema.json")
        ) {
            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            return schema;
        }
    }

    private static JsonNode loadJson(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (
                InputStream jsonStream = inputStreamFromClasspath(path);
        ) {
            JsonNode json = objectMapper.readTree(jsonStream);
            return json;
        }
    }

    private void validateJson(JsonNode json, JsonSchema schema) {
        Set<ValidationMessage> messages = schema.validate(json);
        messages.forEach(m -> System.err.println(m));
        assertEquals(0, messages.size());
    }

    @Test
    public void validateLioncoreJsonAgainstGenericSchema() throws IOException {
        JsonNode json = loadJson("serialization/lioncore.json");
        JsonSchema schema = getGenericSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateLioncoreJsonAgainstLionCoreSchema() throws IOException {
        JsonNode json = loadJson("serialization/lioncore.json");
        JsonSchema schema = getLioncoreSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateBobsLibraryJsonAgainstGenericSchema() throws IOException {
        JsonNode json = loadJson("serialization/bobslibrary.json");
        JsonSchema schema = getGenericSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateLangEngLibraryJsonAgainstGenericSchema() throws IOException {
        JsonNode json = loadJson("serialization/langeng-library.json");
        JsonSchema schema = getGenericSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateTestLangJsonAgainstGenericSchema() throws IOException {
        JsonNode json = loadJson("serialization/TestLang-metamodel.json");
        JsonSchema schema = getGenericSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateTestLangJsonAgainstLionCoreSchema() throws IOException {
        JsonNode json = loadJson("serialization/TestLang-metamodel.json");
        JsonSchema schema = getLioncoreSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateLibraryM3JsonAgainstGenericSchema() throws IOException {
        JsonNode json = loadJson("serialization/library-metamodel.json");
        JsonSchema schema = getGenericSerializationSchema();
        validateJson(json, schema);
    }

    @Test
    public void validateLibraryM3JsonAgainstLionCoreSchema() throws IOException {
        JsonNode json = loadJson("serialization/library-metamodel.json");
        JsonSchema schema = getLioncoreSerializationSchema();
        validateJson(json, schema);
    }

}
