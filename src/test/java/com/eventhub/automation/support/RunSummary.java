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
        summary.put("slowestScenarios", ScenarioTelemetry.slowest(5));
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "eventhub-run-summary.json").toFile(), summary);
            Files.writeString(Path.of("target", "run-summary", "github-step-summary.md"), markdownSummary(summary));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write EventHub run summary", exception);
        }
    }

    private static String markdownSummary(Map<String, Object> summary) {
        return "## EventHub Test Run Summary" + System.lineSeparator()
                + System.lineSeparator()
                + "- Run ID: `" + summary.get("runId") + "`" + System.lineSeparator()
                + "- Environment: `" + summary.get("environment") + "`" + System.lineSeparator()
                + "- Browser: `" + summary.get("browser") + "`" + System.lineSeparator()
                + "- Tags: `" + summary.get("tags") + "`" + System.lineSeparator()
                + "- Passed scenarios: `" + summary.get("passedScenarios") + "`" + System.lineSeparator()
                + "- Failed scenarios: `" + summary.get("failedScenarios") + "`" + System.lineSeparator()
                + "- Retried scenarios: `" + summary.get("retriedScenarios") + "`" + System.lineSeparator();
    }
}
