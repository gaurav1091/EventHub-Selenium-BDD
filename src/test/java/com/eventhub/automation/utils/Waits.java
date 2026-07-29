package com.eventhub.automation.utils;

import com.eventhub.automation.config.ConfigReader;
import org.openqa.selenium.By;
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
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(normalize-space(.),'" + text + "')]"))).isDisplayed();
    }

    public static boolean until(WebDriver driver, ExpectedCondition<Boolean> condition) {
        return wait(driver).until(condition);
    }

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, ConfigReader.getDurationSeconds("explicit.wait.seconds"));
    }
}
