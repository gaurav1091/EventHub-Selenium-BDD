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
        entry.put("missingRecommendedTags", missingRecommendedTags(tags));
        return entry;
    }

    private static String markdownCatalog(List<Map<String, Object>> scenarios) {
        StringBuilder builder = new StringBuilder("# Generated EventHub Test Catalog")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("| Feature | Scenario | File | Tags |")
                .append(System.lineSeparator())
                .append("| --- | --- | --- | --- |")
                .append(System.lineSeparator());
        for (Map<String, Object> scenario : scenarios) {
            builder.append("| ")
                    .append(escapePipes(String.valueOf(scenario.get("feature"))))
                    .append(" | ")
                    .append(escapePipes(String.valueOf(scenario.get("scenario"))))
                    .append(" | ")
                    .append(scenario.get("featureFile"))
                    .append(":")
                    .append(scenario.get("line"))
                    .append(" | `")
                    .append(String.join(" ", tags(scenario)))
                    .append("` |")
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<String> tags(Map<String, Object> scenario) {
        return (List<String>) scenario.get("tags");
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
}
