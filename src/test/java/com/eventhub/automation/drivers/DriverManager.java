package com.eventhub.automation.drivers;

import com.eventhub.automation.exceptions.FrameworkException;
import org.openqa.selenium.WebDriver;

public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void initializeDriver() {
        if (DRIVER.get() == null) {
            DRIVER.set(DriverFactory.createDriver());
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new FrameworkException("WebDriver was not initialized for this thread.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
        }
    }
}
