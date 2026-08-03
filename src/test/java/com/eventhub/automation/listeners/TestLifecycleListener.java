package com.eventhub.automation.listeners;

import com.eventhub.automation.api.EventHubApiClient;
import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.support.CleanupService;
import com.eventhub.automation.support.EnvironmentHealthCheck;
import com.eventhub.automation.support.QualityIntelligence;
import com.eventhub.automation.support.ReportIndex;
import com.eventhub.automation.support.RunContext;
import com.eventhub.automation.support.RunSummary;
import com.eventhub.automation.support.ScenarioGovernanceReport;
import com.eventhub.automation.support.ScenarioTelemetry;
import com.eventhub.automation.utils.ScreenshotUtils;
import com.aventstack.extentreports.service.ExtentService;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TestLifecycleListener implements ITestListener {
    private static final Logger LOGGER = LogManager.getLogger(TestLifecycleListener.class);

    @Override
    public void onStart(ITestContext context) {
        EnvironmentHealthCheck.verify();
        ScreenshotUtils.cleanScreenshotsDirectory();
        writeAllureEnvironmentFile();
        writeAllureCategoriesFile();
        writeExtentSystemInfo();
        if (ConfigReader.getBoolean("cleanup.before.run")) {
            new CleanupService(new EventHubApiClient(ConfigReader.getRequired("api.base.url"))).cleanCurrentRunData();
        }
        LOGGER.info("Starting suite: {} with run id {}", context.getName(), RunContext.id());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        RunSummary.passed();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        RunSummary.failed();
        Path screenshot = ScreenshotUtils.captureFailureScreenshot(result.getName());
        if (screenshot != null) {
            attachScreenshotToAllure();
            LOGGER.info("Failure screenshot captured: {}", screenshot);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        ScenarioTelemetry.writeArtifacts();
        ScenarioGovernanceReport.write();
        RunSummary.write();
        QualityIntelligence.writeArtifacts();
        ReportIndex.write();
    }

    @Attachment(value = "Failure screenshot", type = "image/png")
    private byte[] attachScreenshotToAllure() {
        return ScreenshotUtils.captureBytes();
    }

    private void writeAllureEnvironmentFile() {
        Properties environment = new Properties();
        environment.setProperty("Run ID", RunContext.id());
        environment.setProperty("Environment", ConfigReader.getRequired("environment"));
        environment.setProperty("Browser", ConfigReader.getRequired("browser"));
        environment.setProperty("Headless", ConfigReader.getRequired("headless"));
        environment.setProperty("Parallel", ConfigReader.getRequired("parallel"));
        environment.setProperty("Thread Count", ConfigReader.getRequired("thread.count"));
        environment.setProperty("Tags", ConfigReader.getRequired("cucumber.filter.tags"));
        try {
            Files.createDirectories(Path.of("target", "allure-results"));
            try (OutputStream outputStream = Files.newOutputStream(
                    Path.of("target", "allure-results", "environment.properties"))) {
                environment.store(outputStream, "EventHub run metadata");
            }
        } catch (IOException exception) {
            LOGGER.warn("Unable to write Allure environment metadata", exception);
        }
    }

    private void writeAllureCategoriesFile() {
        try {
            Files.createDirectories(Path.of("target", "allure-results"));
            try (var inputStream = getClass().getClassLoader().getResourceAsStream("allure/categories.json")) {
                if (inputStream == null) {
                    LOGGER.warn("Allure categories template was not found on the classpath.");
                    return;
                }
                Files.copy(inputStream, Path.of("target", "allure-results", "categories.json"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            LOGGER.warn("Unable to write Allure categories metadata", exception);
        }
    }

    private void writeExtentSystemInfo() {
        ExtentService.getInstance().setSystemInfo("Run ID", RunContext.id());
        ExtentService.getInstance().setSystemInfo("Environment", ConfigReader.getRequired("environment"));
        ExtentService.getInstance().setSystemInfo("Browser", ConfigReader.getRequired("browser"));
        ExtentService.getInstance().setSystemInfo("Headless", ConfigReader.getRequired("headless"));
        ExtentService.getInstance().setSystemInfo("Parallel", ConfigReader.getRequired("parallel"));
        ExtentService.getInstance().setSystemInfo("Thread Count", ConfigReader.getRequired("thread.count"));
        ExtentService.getInstance().setSystemInfo("Tags", ConfigReader.getRequired("cucumber.filter.tags"));
    }
}
