package com.eventhub.automation.utils;

import com.eventhub.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class Waits {
    private Waits() {
    }

    public static WebElement visible(WebDriver driver, By locator) {
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement clickable(WebDriver driver, By locator) {
        return wait(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean urlContains(WebDriver driver, String fragment) {
        return wait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    public static boolean textPresent(WebDriver driver, String text) {
        return visibleTextContains(driver, text).isDisplayed();
    }

    public static WebElement visibleTextContains(WebDriver driver, String text) {
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.)," + XpathUtils.literal(text) + ")]")));
    }

    public static boolean pageReady(WebDriver driver) {
        return wait(driver).until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")));
    }

    public static boolean appReady(WebDriver driver) {
        pageReady(driver);
        return loadingComplete(driver);
    }

    public static boolean loadingComplete(WebDriver driver) {
        return wait(driver).until(webDriver -> {
            boolean hasVisibleLoadingClass = webDriver.findElements(By.cssSelector("[class*='animate-pulse'], [class*='animate-spin']"))
                    .stream()
                    .anyMatch(Waits::isVisible);
            boolean hasVisibleLoadingText = webDriver.findElements(By.xpath("//*[contains(translate(normalize-space(.),"
                            + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'loading')]"))
                    .stream()
                    .anyMatch(Waits::isVisible);
            return !hasVisibleLoadingClass && !hasVisibleLoadingText;
        });
    }

    private static boolean isVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (StaleElementReferenceException exception) {
            return false;
        }
    }

    public static boolean textToContain(WebDriver driver, By locator, String expectedText) {
        return wait(driver).until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    public static boolean until(WebDriver driver, ExpectedCondition<Boolean> condition) {
        return wait(driver).until(condition);
    }

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, ConfigReader.getDurationSeconds("explicit.wait.seconds"));
    }
}
