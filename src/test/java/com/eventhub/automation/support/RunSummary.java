package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class RunSummary {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger RETRIED = new AtomicInteger();

    private RunSummary() {
    }

    public static void passed() {
        PASSED.incrementAndGet();
    }

    public static void failed() {
        FAILED.incrementAndGet();
    }

    public static void retried() {
        RETRIED.incrementAndGet();
    }

    public static void write() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runId", RunContext.id());
        summary.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        summary.put("environment", ConfigReader.getRequired("environment"));
        summary.put("browser", ConfigReader.getRequired("browser"));
        summary.put("headless", ConfigReader.getRequired("headless"));
        summary.put("parallel", ConfigReader.getRequired("parallel"));
        summary.put("threadCount", ConfigReader.getRequired("thread.count"));
        summary.put("tags", ConfigReader.getRequired("cucumber.filter.tags"));
        summary.put("passedScenarios", PASSED.get());
        summary.put("failedScenarios", FAILED.get());
        summary.put("retriedScenarios", RETRIED.get());
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "eventhub-run-summary.json").toFile(), summary);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write EventHub run summary", exception);
        }
    }
}
