package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Scenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScenarioTelemetry {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, Instant> START_TIMES = new ConcurrentHashMap<>();
    private static final List<Map<String, Object>> SCENARIOS = java.util.Collections.synchronizedList(new ArrayList<>());

    private ScenarioTelemetry() {
    }

    public static void start(Scenario scenario) {
        START_TIMES.put(scenario.getId(), Instant.now());
    }

    public static void finish(Scenario scenario) {
        Instant start = START_TIMES.remove(scenario.getId());
        long durationMs = start == null ? 0 : Duration.between(start, Instant.now()).toMillis();

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", scenario.getId());
        entry.put("name", scenario.getName());
        entry.put("uri", scenario.getUri().toString());
        entry.put("line", scenario.getLine());
        entry.put("status", scenario.getStatus().name());
        entry.put("durationMs", durationMs);
        entry.put("tags", scenario.getSourceTagNames());
        entry.put("browser", ConfigReader.getRequired("browser"));
        entry.put("environment", ConfigReader.getRequired("environment"));
        SCENARIOS.add(entry);
    }

    public static List<Map<String, Object>> scenarios() {
        synchronized (SCENARIOS) {
            return new ArrayList<>(SCENARIOS);
        }
    }

    public static List<Map<String, Object>> slowest(int limit) {
        return scenarios().stream()
                .sorted(Comparator.comparingLong(ScenarioTelemetry::duration).reversed())
                .limit(limit)
                .toList();
    }

    public static void writeArtifacts() {
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "scenario-durations.json").toFile(), scenarios());
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "slow-scenarios.json").toFile(), slowest(10));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write scenario telemetry artifacts", exception);
        }
    }

    private static long duration(Map<String, Object> entry) {
        Object value = entry.get("durationMs");
        return value instanceof Number ? ((Number) value).longValue() : 0;
    }
}
