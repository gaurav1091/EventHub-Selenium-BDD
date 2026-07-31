package com.eventhub.automation.support;

import com.eventhub.automation.config.ConfigReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReportIndex {
    private ReportIndex() {
    }

    public static void write() {
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            Files.writeString(Path.of("target", "run-summary", "report-index.html"), html());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write report index", exception);
        }
    }

    private static String html() {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("Extent Report", "../extent-report/EventHub-Cucumber-Report.html");
        links.put("Cucumber HTML", "../cucumber-reports/cucumber.html");
        links.put("Run Summary JSON", "eventhub-run-summary.json");
        links.put("GitHub Step Summary", "github-step-summary.md");
        links.put("Scenario Durations", "scenario-durations.json");
        links.put("Slow Scenarios", "slow-scenarios.json");
        links.put("Environment Health", "environment-health.json");
        links.put("Scenario Governance JSON", "../governance/scenario-governance.json");
        links.put("Generated Test Catalog", "../governance/test-catalog.md");
        links.put("Tag Audit", "../governance/tag-audit.json");
        links.put("Axe Reports", "../axe-reports/");
        links.put("Visual Sanity Screenshots", "../visual-sanity/");
        links.put("Allure Results", "../allure-results/");
        links.put("Surefire Reports", "../surefire-reports/");

        StringBuilder builder = new StringBuilder();
        builder.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
                .append("<title>EventHub Automation Report Index</title>")
                .append("<style>body{font-family:Arial,sans-serif;margin:32px;line-height:1.5}")
                .append("table{border-collapse:collapse;width:100%;max-width:960px}")
                .append("td,th{border:1px solid #ddd;padding:8px;text-align:left}")
                .append("th{background:#f5f5f5}</style></head><body>")
                .append("<h1>EventHub Automation Report Index</h1>")
                .append("<p>Generated ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .append("</p><ul>")
                .append("<li>Run ID: <code>").append(RunContext.id()).append("</code></li>")
                .append("<li>Environment: <code>").append(ConfigReader.getRequired("environment")).append("</code></li>")
                .append("<li>Browser: <code>").append(ConfigReader.getRequired("browser")).append("</code></li>")
                .append("<li>Tags: <code>").append(escape(ConfigReader.getRequired("cucumber.filter.tags"))).append("</code></li>")
                .append("</ul><table><thead><tr><th>Artifact</th><th>Link</th></tr></thead><tbody>");
        links.forEach((name, href) -> builder.append("<tr><td>")
                .append(name)
                .append("</td><td><a href=\"")
                .append(href)
                .append("\">")
                .append(href)
                .append("</a></td></tr>"));
        return builder.append("</tbody></table></body></html>").toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
