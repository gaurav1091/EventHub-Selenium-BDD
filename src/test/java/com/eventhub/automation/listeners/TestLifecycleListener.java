package com.eventhub.automation.listeners;

import com.eventhub.automation.utils.ScreenshotUtils;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

public class TestLifecycleListener implements ITestListener {
    private static final Logger LOGGER = LogManager.getLogger(TestLifecycleListener.class);

    @Override
    public void onStart(ITestContext context) {
        ScreenshotUtils.cleanScreenshotsDirectory();
        LOGGER.info("Starting suite: {}", context.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Path screenshot = ScreenshotUtils.captureFailureScreenshot(result.getName());
        if (screenshot != null) {
            attachScreenshotToAllure();
            LOGGER.info("Failure screenshot captured: {}", screenshot);
        }
    }

    @Attachment(value = "Failure screenshot", type = "image/png")
    private byte[] attachScreenshotToAllure() {
        return ScreenshotUtils.captureBytes();
    }
}
