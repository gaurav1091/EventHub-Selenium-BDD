package com.eventhub.automation.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class XpathUtils {
    private XpathUtils() {
    }

    public static String literal(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat(" + Arrays.stream(value.split("'", -1))
                .map(part -> "'" + part + "'")
                .collect(Collectors.joining(", \"'\", ")) + ")";
    }
}
