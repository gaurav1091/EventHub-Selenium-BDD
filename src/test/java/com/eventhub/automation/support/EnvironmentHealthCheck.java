package com.eventhub.automation.support;

import com.eventhub.automation.api.EventHubApiClient;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.exceptions.FrameworkException;
import io.restassured.response.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EnvironmentHealthCheck {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EnvironmentHealthCheck() {
    }

    public static void verify() {
        if (!ConfigReader.getBoolean("preflight.enabled")) {
            writeHealthSummary(Map.of("enabled", false));
            return;
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enabled", true);
        summary.put("uiBaseUrl", ConfigReader.getRequired("base.url"));
        summary.put("apiBaseUrl", ConfigReader.getRequired("api.base.url"));
        Instant start = Instant.now();
        try {
            verifyUiBaseUrl();
            summary.put("uiReachable", true);
            verifyApiHealth();
            summary.put("apiHealthy", true);
            verifyCredentials();
            summary.put("credentialsValid", true);
            summary.put("status", "PASSED");
        } catch (RuntimeException exception) {
            summary.put("status", "FAILED");
            summary.put("failure", exception.getMessage());
            throw exception;
        } finally {
            summary.put("durationMs", Duration.between(start, Instant.now()).toMillis());
            writeHealthSummary(summary);
        }
    }

    private static void verifyUiBaseUrl() {
        String baseUrl = ConfigReader.getRequired("base.url");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            int status = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.discarding())
                    .statusCode();
            if (status >= 400) {
                throw new FrameworkException("Preflight failed: UI base URL returned HTTP " + status + ": " + baseUrl);
            }
        } catch (Exception exception) {
            throw new FrameworkException("Preflight failed: UI base URL is not reachable: " + baseUrl, exception);
        }
    }

    private static void verifyApiHealth() {
        EventHubApiClient apiClient = new EventHubApiClient(ConfigReader.getRequired("api.base.url"));
        Response response = apiClient.health();
        if (response.statusCode() >= 400) {
            throw new FrameworkException("Preflight failed: API health returned HTTP " + response.statusCode());
        }
    }

    private static void verifyCredentials() {
        EventHubApiClient apiClient = new EventHubApiClient(ConfigReader.getRequired("api.base.url"));
        Response response = apiClient.login();
        if (response.statusCode() != 200) {
            throw new FrameworkException("Preflight failed: configured EventHub credentials were rejected with HTTP "
                    + response.statusCode());
        }
    }

    private static void writeHealthSummary(Map<String, Object> summary) {
        try {
            Files.createDirectories(Path.of("target", "run-summary"));
            MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValue(Path.of("target", "run-summary", "environment-health.json").toFile(), summary);
        } catch (IOException exception) {
            throw new FrameworkException("Unable to write environment health summary", exception);
        }
    }
}
