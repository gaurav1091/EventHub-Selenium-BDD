package com.eventhub.automation.listeners;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.support.RunSummary;
import io.cucumber.testng.PickleWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger(RetryAnalyzer.class);
    private int attempts;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigReader.getInt("retry.count");
        if (maxRetries <= 0 || attempts >= maxRetries || !isRetryAllowed(result)) {
            return false;
        }
        attempts++;
        RunSummary.retried();
        LOGGER.warn("Retrying scenario after failure. attempt={} maxRetries={} scenario={}",
                attempts, maxRetries, scenarioName(result));
        return true;
    }

    private boolean isRetryAllowed(ITestResult result) {
        String retryTags = ConfigReader.getRequired("retry.tags");
        if ("all".equalsIgnoreCase(retryTags)) {
            return true;
        }
        Object[] parameters = result.getParameters();
        if (parameters.length == 0 || !(parameters[0] instanceof PickleWrapper)) {
            return false;
        }
        PickleWrapper pickle = (PickleWrapper) parameters[0];
        return pickle.getPickle().getTags().stream()
                .anyMatch(tag -> retryTags.contains(tag));
    }

    private String scenarioName(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof PickleWrapper) {
            return ((PickleWrapper) parameters[0]).getPickle().getName();
        }
        return result.getName();
    }
}
