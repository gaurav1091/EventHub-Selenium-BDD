package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class RunSummary {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();
    private static final AtomicInteger RETRIED = new AtomicInteger();
    private static final List<Map<String, Object>> RETRIES = Collections.synchronizedList(new ArrayList<>());

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

    public static void retried(String scenarioName, int attempt, int maxRetries, String tags) {
        retried();
        Map<String, Object> retry = new LinkedHashMap<>();
        retry.put("scenario", scenarioName);
        retry.put("attempt", attempt);
        retry.put("maxRetries", maxRetries);
        retry.put("tags", tags);
        retry.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        RETRIES.add(retry);
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
        summary.put("maxAllowedRetries", ConfigReader.getInt("retry.max.allowed"));
        summary.put("retryThresholdPassed", RETRIED.get() <= ConfigReader.getInt("retry.max.allowed"));
        summary.put("slowestScenarios", ScenarioTelemetry.slowest(5));
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "eventhub-run-summary.json").toFile(), summary);
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "retry-governance.json").toFile(), retryGovernance());
            Files.writeString(Path.of("target", "run-summary", "github-step-summary.md"), markdownSummary(summary));
            enforceRetryThreshold();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write EventHub run summary", exception);
        }
    }

    private static Map<String, Object> retryGovernance() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("retryCount", RETRIED.get());
        report.put("maxAllowedRetries", ConfigReader.getInt("retry.max.allowed"));
        report.put("passed", RETRIED.get() <= ConfigReader.getInt("retry.max.allowed"));
        synchronized (RETRIES) {
            report.put("retries", new ArrayList<>(RETRIES));
        }
        return report;
    }

    private static void enforceRetryThreshold() {
        int maxAllowedRetries = ConfigReader.getInt("retry.max.allowed");
        if (RETRIED.get() > maxAllowedRetries) {
            throw new IllegalStateException("Retry governance failed: retried scenarios "
                    + RETRIED.get() + " exceeded retry.max.allowed=" + maxAllowedRetries);
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
                + "- Retried scenarios: `" + summary.get("retriedScenarios") + "`" + System.lineSeparator()
                + "- Retry threshold passed: `" + summary.get("retryThresholdPassed") + "`" + System.lineSeparator();
    }
}
