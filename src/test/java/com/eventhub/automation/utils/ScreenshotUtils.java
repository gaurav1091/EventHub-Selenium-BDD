package com.eventhub.automation.utils;

import com.eventhub.automation.drivers.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtils {
    private static final Logger LOGGER = LogManager.getLogger(ScreenshotUtils.class);
    private static final Path SCREENSHOTS_DIR = Path.of("screenshots");

    private ScreenshotUtils() {
    }

    public static Path captureFailureScreenshot(String scenarioName) {
        try {
            Files.createDirectories(SCREENSHOTS_DIR);
            WebDriver driver = DriverManager.getDriver();
            byte[] image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path target = SCREENSHOTS_DIR.resolve(safeName(scenarioName) + "-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")) + ".png");
            Files.write(target, image);
            return target;
        } catch (Exception exception) {
            LOGGER.warn("Unable to capture screenshot for failure.", exception);
            return null;
        }
    }

    public static byte[] captureBytes() {
        WebDriver driver = DriverManager.getDriver();
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public static void cleanScreenshotsDirectory() {
        if (!Files.exists(SCREENSHOTS_DIR)) {
            return;
        }
        try (var paths = Files.walk(SCREENSHOTS_DIR)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .filter(path -> !path.equals(SCREENSHOTS_DIR))
                    .forEach(ScreenshotUtils::deleteQuietly);
            Files.deleteIfExists(SCREENSHOTS_DIR);
        } catch (IOException exception) {
            LOGGER.warn("Unable to clean screenshots directory.", exception);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            LOGGER.warn("Unable to delete {}", path, exception);
        }
    }

    private static String safeName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
