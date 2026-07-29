package com.eventhub.automation.hooks;

import com.eventhub.automation.drivers.DriverManager;
import com.eventhub.automation.utils.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.concurrent.locks.ReentrantLock;

public class CucumberHooks {
    private static final Logger LOGGER = LogManager.getLogger(CucumberHooks.class);
    private static final ReentrantLock STATEFUL_SCENARIO_LOCK = new ReentrantLock(true);

    @Before(value = "@stateful", order = -100)
    public void lockStatefulScenario(Scenario scenario) {
        LOGGER.info("Waiting for shared state lock: {}", scenario.getName());
        STATEFUL_SCENARIO_LOCK.lock();
        LOGGER.info("Acquired shared state lock: {}", scenario.getName());
    }

    @Before(value = "not @api", order = 0)
    public void startBrowser(Scenario scenario) {
        LOGGER.info("Starting scenario: {}", scenario.getName());
        DriverManager.initializeDriver();
    }

    @After(value = "not @api", order = 0)
    public void stopBrowser(Scenario scenario) {
        if (scenario.isFailed()) {
            ScreenshotUtils.captureFailureScreenshot(scenario.getName());
            byte[] screenshot = ScreenshotUtils.captureBytes();
            scenario.attach(screenshot, "image/png", "failure screenshot");
            Allure.addAttachment("failure screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
        }
        DriverManager.quitDriver();
    }

    @After(value = "@stateful", order = -100)
    public void unlockStatefulScenario(Scenario scenario) {
        LOGGER.info("Released shared state lock: {}", scenario.getName());
        STATEFUL_SCENARIO_LOCK.unlock();
    }
}
