package com.eventhub.automation.core;

import java.util.Arrays;

public enum Browser {
    CHROME,
    FIREFOX;

    public static Browser from(String value) {
        return Arrays.stream(values())
                .filter(browser -> browser.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported browser: " + value));
    }
}
