package com.eventhub.automation.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public final class UiAssertions {
    private UiAssertions() {
    }

    public static void assertPageContains(WebDriver driver, String expectedText) {
        assertThat(Waits.visibleTextContains(driver, expectedText).isDisplayed())
                .as("Expected visible page text at %s to contain: %s", driver.getCurrentUrl(), expectedText)
                .isTrue();
    }

    public static void assertPageContainsAnyOf(WebDriver driver, String... expectedTexts) {
        Waits.until(driver, webDriver -> Arrays.stream(expectedTexts)
                .anyMatch(expectedText -> !webDriver.findElements(By.xpath("//*[contains(normalize-space(.),"
                                + XpathUtils.literal(expectedText) + ")]"))
                        .isEmpty()));
        assertThat(Arrays.stream(expectedTexts)
                .anyMatch(expectedText -> !driver.findElements(By.xpath("//*[contains(normalize-space(.),"
                                + XpathUtils.literal(expectedText) + ")]"))
                        .isEmpty()))
                .as("Expected visible page text at %s to contain one of: %s",
                        driver.getCurrentUrl(), Arrays.toString(expectedTexts))
                .isTrue();
    }

    public static void assertElementTextContains(WebDriver driver, By locator, String expectedText) {
        Waits.textToContain(driver, locator, expectedText);
        assertThat(Waits.visible(driver, locator).getText())
                .as("Expected element %s text to contain: %s", locator, expectedText)
                .containsIgnoringCase(expectedText);
    }

    public static void assertCardContains(WebElement card, String... expectedTexts) {
        assertThat(card.getText())
                .as("Expected card text to contain: %s", Arrays.toString(expectedTexts))
                .contains(expectedTexts);
    }

    public static void assertToastContains(WebDriver driver, String expectedText) {
        By toast = By.xpath("//*[contains(@role,'alert') or contains(@class,'toast') or contains(@class,'Toast')]"
                + "[contains(normalize-space(.),\"" + expectedText + "\")]");
        assertThat(Waits.visible(driver, toast).isDisplayed())
                .as("Expected toast/alert to contain: %s", expectedText)
                .isTrue();
    }

    public static void assertTableRowContains(WebDriver driver, String tableText, String... rowTexts) {
        By row = By.xpath("//tr[contains(normalize-space(.),\"" + tableText + "\")]");
        WebElement tableRow = Waits.visible(driver, row);
        assertThat(tableRow.getText())
                .as("Expected table row containing %s to include: %s", tableText, Arrays.toString(rowTexts))
                .contains(rowTexts);
    }
}
