package com.eventhub.automation.support;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScenarioGovernanceReport {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ScenarioGovernanceReport() {
    }

    public static void write() {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        try (java.util.stream.Stream<Path> files = Files.walk(Path.of("src", "test", "resources", "features"))) {
            files.filter(path -> path.toString().endsWith(".feature"))
                    .sorted()
                    .forEach(path -> scenarios.addAll(scenariosIn(path)));
            Files.createDirectories(Path.of("target", "governance"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "governance", "scenario-governance.json").toFile(), scenarios);
            Files.writeString(Path.of("target", "governance", "test-catalog.md"), markdownCatalog(scenarios));
            Files.writeString(Path.of("target", "governance", "test-catalog-summary.md"), markdownSummary(scenarios));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write scenario governance report", exception);
        }
    }

    private static List<Map<String, Object>> scenariosIn(Path featureFile) {
        List<Map<String, Object>> scenarios = new ArrayList<>();
        List<String> pendingTags = new ArrayList<>();
        List<String> featureTags = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(featureFile);
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index).trim();
                if (line.startsWith("@")) {
                    pendingTags = List.of(line.split("\\s+"));
                    if (nextContentLine(lines, index).startsWith("Feature:")) {
                        featureTags = pendingTags;
                    }
                }
                if (line.startsWith("Scenario:") || line.startsWith("Scenario Outline:")) {
                    String scenarioName = line.substring(line.indexOf(':') + 1).trim();
                    List<String> tags = new ArrayList<>(featureTags);
                    tags.addAll(pendingTags);
                    scenarios.add(entry(featureFile, index + 1, featureName(lines), scenarioName, tags));
                    pendingTags = new ArrayList<>();
                }
            }
            return scenarios;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse feature file: " + featureFile, exception);
        }
    }

    private static String nextContentLine(List<String> lines, int currentIndex) {
        for (int index = currentIndex + 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (!line.isBlank()) {
                return line;
            }
        }
        return "";
    }

    private static String featureName(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> line.startsWith("Feature:"))
                .map(line -> line.substring(line.indexOf(':') + 1).trim())
                .findFirst()
                .orElse("Unknown feature");
    }

    private static Map<String, Object> entry(
            Path featureFile, int line, String featureName, String scenarioName, List<String> tags) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("featureFile", featureFile.toString());
        entry.put("line", line);
        entry.put("feature", featureName);
        entry.put("scenario", scenarioName);
        entry.put("tags", tags);
        entry.put("type", firstPresent(tags, "@api", "@ui", "@hybrid", "@accessibility", "@responsive", "@visual"));
        entry.put("priority", firstMatching(tags, "@p[0-3]"));
        entry.put("owner", firstWithPrefix(tags, "@owner-"));
        entry.put("risk", firstWithPrefix(tags, "@risk-"));
        entry.put("intent", firstWithPrefix(tags, "@intent-"));
        entry.put("impact", firstWithPrefix(tags, "@impact-"));
        entry.put("statefulness", tags.contains("@stateful") ? "@stateful" : "@parallel-safe");
        entry.put("missingRecommendedTags", missingRecommendedTags(tags));
        return entry;
    }

    private static String markdownCatalog(List<Map<String, Object>> scenarios) {
        StringBuilder builder = new StringBuilder("# Generated EventHub Test Catalog")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("| Feature | Scenario | Type | Priority | Owner | Risk | Intent | Impact | State | File |")
                .append(System.lineSeparator())
                .append("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
                .append(System.lineSeparator());
        for (Map<String, Object> scenario : scenarios) {
            builder.append("| ")
                    .append(escapePipes(String.valueOf(scenario.get("feature"))))
                    .append(" | ")
                    .append(escapePipes(String.valueOf(scenario.get("scenario"))))
                    .append(" | ")
                    .append(scenario.get("type"))
                    .append(" | ")
                    .append(scenario.get("priority"))
                    .append(" | ")
                    .append(scenario.get("owner"))
                    .append(" | ")
                    .append(scenario.get("risk"))
                    .append(" | ")
                    .append(scenario.get("intent"))
                    .append(" | ")
                    .append(scenario.get("impact"))
                    .append(" | ")
                    .append(scenario.get("statefulness"))
                    .append(" | ")
                    .append(scenario.get("featureFile"))
                    .append(":")
                    .append(scenario.get("line"))
                    .append(" |")
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static String markdownSummary(List<Map<String, Object>> scenarios) {
        StringBuilder builder = new StringBuilder("# EventHub Test Catalog Summary")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("- Scenario count: `")
                .append(scenarios.size())
                .append("`")
                .append(System.lineSeparator())
                .append("- Missing governance metadata: `")
                .append(scenarios.stream().filter(scenario -> !missing(scenario).isEmpty()).count())
                .append("`")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("## Coverage By Impact")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        countsBy(scenarios, "impact").forEach((impact, count) -> builder
                .append("- `").append(impact).append("`: `").append(count).append("`").append(System.lineSeparator()));
        builder.append(System.lineSeparator())
                .append("## Coverage By Priority")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        countsBy(scenarios, "priority").forEach((priority, count) -> builder
                .append("- `").append(priority).append("`: `").append(count).append("`").append(System.lineSeparator()));
        return builder.toString();
    }

    private static Map<String, Long> countsBy(List<Map<String, Object>> scenarios, String key) {
        Map<String, Long> counts = new LinkedHashMap<>();
        scenarios.stream()
                .map(scenario -> String.valueOf(scenario.get(key)))
                .sorted()
                .forEach(value -> counts.put(value, counts.getOrDefault(value, 0L) + 1));
        return counts;
    }

    @SuppressWarnings("unchecked")
    private static List<String> missing(Map<String, Object> scenario) {
        return (List<String>) scenario.get("missingRecommendedTags");
    }

    private static String escapePipes(String value) {
        return value.replace("|", "\\|");
    }

    private static List<String> missingRecommendedTags(List<String> tags) {
        List<String> missing = new ArrayList<>();
        requireAny(tags, missing, "@smoke", "@regression", "@contract", "@accessibility", "@responsive", "@visual");
        requireAny(tags, missing, "@api", "@ui", "@hybrid", "@accessibility", "@responsive");
        requireAny(tags, missing, "@parallel-safe", "@stateful");
        requirePriority(tags, missing);
        requirePrefix(tags, missing, "@owner-");
        requirePrefix(tags, missing, "@risk-");
        requirePrefix(tags, missing, "@intent-");
        requirePrefix(tags, missing, "@impact-");
        return missing;
    }

    private static void requireAny(List<String> tags, List<String> missing, String... expected) {
        for (String tag : expected) {
            if (tags.contains(tag)) {
                return;
            }
        }
        missing.add(String.join(" or ", expected));
    }

    private static void requirePrefix(List<String> tags, List<String> missing, String prefix) {
        if (tags.stream().anyMatch(tag -> tag.startsWith(prefix))) {
            return;
        }
        missing.add(prefix + "*");
    }

    private static void requirePriority(List<String> tags, List<String> missing) {
        if (tags.stream().anyMatch(tag -> tag.matches("@p[0-3]"))) {
            return;
        }
        missing.add("@p0 or @p1 or @p2 or @p3");
    }

    private static String firstPresent(List<String> tags, String... expected) {
        for (String tag : expected) {
            if (tags.contains(tag)) {
                return tag;
            }
        }
        return "";
    }

    private static String firstMatching(List<String> tags, String pattern) {
        return tags.stream()
                .filter(tag -> tag.matches(pattern))
                .findFirst()
                .orElse("");
    }

    private static String firstWithPrefix(List<String> tags, String prefix) {
        return tags.stream()
                .filter(tag -> tag.startsWith(prefix))
                .findFirst()
                .orElse("");
    }
}
