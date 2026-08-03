package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class QualityIntelligence {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path RUN_SUMMARY_DIR = Path.of("target", "run-summary");
    private static final Path AXE_REPORTS_DIR = Path.of("target", "axe-reports");
    private static final Path VISUAL_DIFF_DIR = Path.of("target", "visual-diff");
    private static final Path QUARANTINE_FILE = Path.of("src", "test", "resources", "governance", "quarantine.json");

    private QualityIntelligence() {
    }

    public static void writeArtifacts() {
        try {
            Files.createDirectories(RUN_SUMMARY_DIR);
            Map<String, Object> accessibility = accessibilitySummary();
            Map<String, Object> visual = visualSummary();
            Map<String, Object> quarantine = quarantineSummary();
            Map<String, Object> releaseReadiness = releaseReadiness(accessibility, visual, quarantine);

            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(RUN_SUMMARY_DIR.resolve("accessibility-summary.json").toFile(), accessibility);
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(RUN_SUMMARY_DIR.resolve("visual-quality-summary.json").toFile(), visual);
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(RUN_SUMMARY_DIR.resolve("quarantine-summary.json").toFile(), quarantine);
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(RUN_SUMMARY_DIR.resolve("release-readiness.json").toFile(), releaseReadiness);

            Files.writeString(RUN_SUMMARY_DIR.resolve("quarantine-dashboard.md"), quarantineMarkdown(quarantine));
            Files.writeString(RUN_SUMMARY_DIR.resolve("release-readiness.md"), releaseReadinessMarkdown(releaseReadiness));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write EventHub quality intelligence artifacts", exception);
        }
    }

    private static Map<String, Object> accessibilitySummary() throws IOException {
        List<Map<String, Object>> pages = new ArrayList<>();
        int totalViolations = 0;
        int pageCount = 0;

        for (Path report : jsonFiles(AXE_REPORTS_DIR)) {
            JsonNode json = MAPPER.readTree(report.toFile());
            int violations = json.path("violationCount").asInt(0);
            Map<String, Object> page = new LinkedHashMap<>();
            page.put("file", report.toString());
            page.put("url", json.path("url").asText(""));
            page.put("title", json.path("title").asText(""));
            page.put("mode", json.path("mode").asText(""));
            page.put("violationCount", violations);
            pages.add(page);
            totalViolations += violations;
            pageCount++;
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("generatedAt", timestamp());
        summary.put("thresholdEnabled", ConfigReader.getBoolean("accessibility.threshold.enabled"));
        summary.put("maxViolations", ConfigReader.getInt("accessibility.max.violations"));
        summary.put("pageCount", pageCount);
        summary.put("totalViolations", totalViolations);
        summary.put("passed", !ConfigReader.getBoolean("accessibility.threshold.enabled")
                || totalViolations <= ConfigReader.getInt("accessibility.max.violations"));
        summary.put("pages", pages);
        return summary;
    }

    private static Map<String, Object> visualSummary() throws IOException {
        List<Map<String, Object>> comparisons = new ArrayList<>();
        long totalDifferentPixels = 0;
        int missingBaselines = 0;
        int compared = 0;

        for (Path report : jsonFiles(VISUAL_DIFF_DIR)) {
            JsonNode json = MAPPER.readTree(report.toFile());
            long differentPixels = json.path("differentPixels").asLong(0);
            String mode = json.path("mode").asText("");
            Map<String, Object> comparison = new LinkedHashMap<>();
            comparison.put("file", report.toString());
            comparison.put("url", json.path("url").asText(""));
            comparison.put("mode", mode);
            comparison.put("baseline", json.path("baseline").asText(""));
            comparison.put("diffImage", json.path("diffImage").asText(""));
            comparison.put("differentPixels", differentPixels);
            comparison.put("maxDifferentPixels", json.path("maxDifferentPixels").asInt(0));
            comparisons.add(comparison);
            if ("missing-baseline".equals(mode)) {
                missingBaselines++;
            }
            if ("compared".equals(mode)) {
                compared++;
                totalDifferentPixels += differentPixels;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("generatedAt", timestamp());
        summary.put("baselineEnabled", ConfigReader.getBoolean("visual.baseline.enabled"));
        summary.put("baselineUpdate", ConfigReader.getBoolean("visual.baseline.update"));
        summary.put("baselineDirectory", ConfigReader.getRequired("visual.baseline.dir"));
        summary.put("maxDifferentPixels", ConfigReader.getInt("visual.diff.max.pixels"));
        summary.put("comparisonCount", comparisons.size());
        summary.put("comparedCount", compared);
        summary.put("missingBaselineCount", missingBaselines);
        summary.put("totalDifferentPixels", totalDifferentPixels);
        summary.put("passed", missingBaselines == 0 && totalDifferentPixels <= ConfigReader.getInt("visual.diff.max.pixels"));
        summary.put("comparisons", comparisons);
        return summary;
    }

    private static Map<String, Object> quarantineSummary() throws IOException {
        List<Map<String, Object>> entries = Files.exists(QUARANTINE_FILE)
                ? MAPPER.readValue(QUARANTINE_FILE.toFile(), new TypeReference<>() {})
                : List.of();
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> expired = new ArrayList<>();
        List<Map<String, Object>> incomplete = new ArrayList<>();

        for (Map<String, Object> entry : entries) {
            String expiresOn = value(entry, "expiresOn");
            if (!expiresOn.isBlank() && LocalDate.parse(expiresOn).isBefore(today)) {
                expired.add(entry);
            }
            if (value(entry, "scenario").isBlank()
                    || value(entry, "reason").isBlank()
                    || value(entry, "owner").isBlank()
                    || expiresOn.isBlank()) {
                incomplete.add(entry);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("generatedAt", timestamp());
        summary.put("source", QUARANTINE_FILE.toString());
        summary.put("count", entries.size());
        summary.put("expiredCount", expired.size());
        summary.put("incompleteCount", incomplete.size());
        summary.put("passed", expired.isEmpty() && incomplete.isEmpty());
        summary.put("entries", entries);
        summary.put("expired", expired);
        summary.put("incomplete", incomplete);
        return summary;
    }

    private static Map<String, Object> releaseReadiness(
            Map<String, Object> accessibility,
            Map<String, Object> visual,
            Map<String, Object> quarantine) throws IOException {
        Map<String, Object> runSummary = readMap(RUN_SUMMARY_DIR.resolve("eventhub-run-summary.json"));
        Map<String, Object> retry = readMap(RUN_SUMMARY_DIR.resolve("retry-governance.json"));
        List<Map<String, Object>> slowScenarios = readList(RUN_SUMMARY_DIR.resolve("slow-scenarios.json"));

        boolean testsPassed = number(runSummary, "failedScenarios") == 0;
        boolean retryPassed = bool(retry, "passed", true);
        boolean accessibilityPassed = bool(accessibility, "passed", true);
        boolean visualPassed = !ConfigReader.getBoolean("visual.baseline.enabled") || bool(visual, "passed", true);
        boolean quarantinePassed = bool(quarantine, "passed", true);
        String status = testsPassed && retryPassed && accessibilityPassed && visualPassed && quarantinePassed
                ? "ready"
                : "needs-attention";

        Map<String, Object> readiness = new LinkedHashMap<>();
        readiness.put("generatedAt", timestamp());
        readiness.put("status", status);
        readiness.put("runId", RunContext.id());
        readiness.put("environment", ConfigReader.getRequired("environment"));
        readiness.put("suiteName", ConfigReader.getRequired("suite.name"));
        readiness.put("browser", ConfigReader.getRequired("browser"));
        readiness.put("parallel", ConfigReader.getRequired("parallel"));
        readiness.put("threadCount", ConfigReader.getRequired("thread.count"));
        readiness.put("totalScenarios", number(runSummary, "totalScenarios"));
        readiness.put("passedScenarios", number(runSummary, "passedScenarios"));
        readiness.put("failedScenarios", number(runSummary, "failedScenarios"));
        readiness.put("retriedScenarios", number(runSummary, "retriedScenarios"));
        readiness.put("accessibilityViolations", number(accessibility, "totalViolations"));
        readiness.put("visualDiffPixels", number(visual, "totalDifferentPixels"));
        readiness.put("visualMissingBaselines", number(visual, "missingBaselineCount"));
        readiness.put("quarantineCount", number(quarantine, "count"));
        readiness.put("expiredQuarantineCount", number(quarantine, "expiredCount"));
        readiness.put("slowestScenarios", slowScenarios.stream()
                .sorted(Comparator.comparingLong(QualityIntelligence::duration).reversed())
                .limit(5)
                .toList());
        readiness.put("checks", Map.of(
                "tests", testsPassed,
                "retry", retryPassed,
                "accessibility", accessibilityPassed,
                "visual", visualPassed,
                "quarantine", quarantinePassed
        ));
        return readiness;
    }

    private static String releaseReadinessMarkdown(Map<String, Object> readiness) {
        return "## Release Readiness" + System.lineSeparator()
                + System.lineSeparator()
                + "- Status: `" + readiness.get("status") + "`" + System.lineSeparator()
                + "- Run ID: `" + readiness.get("runId") + "`" + System.lineSeparator()
                + "- Suite: `" + readiness.get("suiteName") + "`" + System.lineSeparator()
                + "- Browser: `" + readiness.get("browser") + "`" + System.lineSeparator()
                + "- Parallel: `" + readiness.get("parallel") + "`" + System.lineSeparator()
                + "- Threads: `" + readiness.get("threadCount") + "`" + System.lineSeparator()
                + "- Total scenarios: `" + readiness.get("totalScenarios") + "`" + System.lineSeparator()
                + "- Failed scenarios: `" + readiness.get("failedScenarios") + "`" + System.lineSeparator()
                + "- Retried scenarios: `" + readiness.get("retriedScenarios") + "`" + System.lineSeparator()
                + "- Accessibility violations: `" + readiness.get("accessibilityViolations") + "`" + System.lineSeparator()
                + "- Visual diff pixels: `" + readiness.get("visualDiffPixels") + "`" + System.lineSeparator()
                + "- Missing visual baselines: `" + readiness.get("visualMissingBaselines") + "`" + System.lineSeparator()
                + "- Quarantined scenarios: `" + readiness.get("quarantineCount") + "`" + System.lineSeparator()
                + "- Expired quarantine entries: `" + readiness.get("expiredQuarantineCount") + "`" + System.lineSeparator();
    }

    private static String quarantineMarkdown(Map<String, Object> quarantine) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) quarantine.get("entries");
        StringBuilder builder = new StringBuilder("## Test Quarantine Dashboard")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("- Entries: `").append(quarantine.get("count")).append("`").append(System.lineSeparator())
                .append("- Expired: `").append(quarantine.get("expiredCount")).append("`").append(System.lineSeparator())
                .append("- Incomplete: `").append(quarantine.get("incompleteCount")).append("`").append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("| Scenario | Owner | Reason | Expires | Issue |")
                .append(System.lineSeparator())
                .append("|---|---|---|---|---|")
                .append(System.lineSeparator());
        if (entries.isEmpty()) {
            builder.append("| No quarantined scenarios |  |  |  |  |").append(System.lineSeparator());
        } else {
            entries.forEach(entry -> builder.append("| ")
                    .append(escape(value(entry, "scenario"))).append(" | ")
                    .append(escape(value(entry, "owner"))).append(" | ")
                    .append(escape(value(entry, "reason"))).append(" | ")
                    .append(escape(value(entry, "expiresOn"))).append(" | ")
                    .append(escape(value(entry, "issue"))).append(" |")
                    .append(System.lineSeparator()));
        }
        return builder.toString();
    }

    private static List<Path> jsonFiles(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    private static Map<String, Object> readMap(Path path) throws IOException {
        return Files.exists(path) ? MAPPER.readValue(path.toFile(), new TypeReference<>() {}) : Map.of();
    }

    private static List<Map<String, Object>> readList(Path path) throws IOException {
        return Files.exists(path) ? MAPPER.readValue(path.toFile(), new TypeReference<>() {}) : List.of();
    }

    private static String value(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private static long number(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

    private static boolean bool(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value instanceof Boolean bool ? bool : defaultValue;
    }

    private static long duration(Map<String, Object> entry) {
        Object value = entry.get("durationMs");
        return value instanceof Number number ? number.longValue() : 0;
    }

    private static String escape(String value) {
        return value.replace("|", "\\|").replace(System.lineSeparator(), " ");
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
