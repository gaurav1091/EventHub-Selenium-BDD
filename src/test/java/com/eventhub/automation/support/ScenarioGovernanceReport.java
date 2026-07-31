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
                    scenarios.add(entry(featureFile, index + 1, scenarioName, tags));
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

    private static Map<String, Object> entry(Path featureFile, int line, String scenarioName, List<String> tags) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("featureFile", featureFile.toString());
        entry.put("line", line);
        entry.put("scenario", scenarioName);
        entry.put("tags", tags);
        entry.put("missingRecommendedTags", missingRecommendedTags(tags));
        return entry;
    }

    private static List<String> missingRecommendedTags(List<String> tags) {
        List<String> missing = new ArrayList<>();
        requireAny(tags, missing, "@smoke", "@regression");
        requireAny(tags, missing, "@api", "@ui", "@hybrid", "@accessibility", "@responsive");
        requireAny(tags, missing, "@parallel-safe", "@stateful");
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
}
