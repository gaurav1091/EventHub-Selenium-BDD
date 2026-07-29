package com.eventhub.automation.factories;

import com.eventhub.automation.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public final class TestData {
    private static final JsonNode ROOT = load();

    private TestData() {
    }

    public static String text(String path) {
        JsonNode node = node(path);
        if (!node.isTextual()) {
            throw new FrameworkException("Test data value is not text: " + path);
        }
        return node.asText();
    }

    public static int integer(String path) {
        JsonNode node = node(path);
        if (!node.isInt()) {
            throw new FrameworkException("Test data value is not an integer: " + path);
        }
        return node.asInt();
    }

    public static String textAt(String path, int index, String field) {
        JsonNode node = node(path).path(index).path(field);
        if (!node.isTextual()) {
            throw new FrameworkException("Test data value is not text: " + path + "[" + index + "]." + field);
        }
        return node.asText();
    }

    private static JsonNode node(String path) {
        JsonNode current = ROOT;
        for (String part : path.split("\\.")) {
            current = current.path(part);
            if (current.isMissingNode()) {
                throw new FrameworkException("Missing test data path: " + path);
            }
        }
        return current;
    }

    private static JsonNode load() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = TestData.class.getClassLoader()
                .getResourceAsStream("test-data/eventhub-test-data.json")) {
            if (inputStream == null) {
                throw new FrameworkException("Unable to load test-data/eventhub-test-data.json");
            }
            return mapper.readTree(inputStream);
        } catch (IOException exception) {
            throw new FrameworkException("Unable to read test data", exception);
        }
    }
}
