package com.eventhub.automation.support;

import com.eventhub.automation.api.EventHubApiClient;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.exceptions.FrameworkException;
import io.restassured.response.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class EnvironmentHealthCheck {
    private EnvironmentHealthCheck() {
    }

    public static void verify() {
        if (!ConfigReader.getBoolean("preflight.enabled")) {
            return;
        }
        verifyUiBaseUrl();
        verifyApiHealth();
        verifyCredentials();
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
}
