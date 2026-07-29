package com.eventhub.automation.pages;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.drivers.DriverManager;
import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BasePage {
    protected void open(String path) {
        driver().get(ConfigReader.getRequired("base.url") + path);
    }

    protected WebElement visible(By locator) {
        return Waits.visible(driver(), locator);
    }

    protected WebElement clickable(By locator) {
        return Waits.clickable(driver(), locator);
    }

    protected void click(By locator) {
        click(clickable(locator));
    }

    protected void click(WebElement element) {
        scrollIntoView(element);
        Waits.until(driver(), webDriver -> {
            try {
                return element.isDisplayed() && element.isEnabled();
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
        try {
            element.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click();", element);
        }
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value);
    }

    protected void selectByVisibleText(By locator, String value) {
        new Select(visible(locator)).selectByVisibleText(value);
    }

    protected void selectByTextContaining(By locator, String value) {
        Select select = new Select(visible(locator));
        select.getOptions().stream()
                .filter(option -> option.getText().contains(value) || value.equalsIgnoreCase(option.getAttribute("value")))
                .findFirst()
                .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("Cannot locate option containing text: " + value))
                .click();
    }

    protected void selectAnyByTextContaining(String value) {
        List<WebElement> selects = Waits.wait(driver()).until(webDriver -> {
            List<WebElement> visibleSelects = webDriver.findElements(By.cssSelector("select")).stream()
                    .filter(WebElement::isDisplayed)
                    .collect(Collectors.toList());
            return visibleSelects.isEmpty() ? null : visibleSelects;
        });
        selects.stream()
                .map(Select::new)
                .filter(select -> select.getOptions().stream()
                        .anyMatch(option -> option.getText().contains(value)
                                || value.equalsIgnoreCase(option.getAttribute("value"))))
                .findFirst()
                .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("Cannot locate select option containing text: " + value))
                .getOptions().stream()
                .filter(option -> option.getText().contains(value) || value.equalsIgnoreCase(option.getAttribute("value")))
                .findFirst()
                .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("Cannot locate option containing text: " + value))
                .click();
    }

    protected boolean isDisplayed(By locator) {
        return !driver().findElements(locator).isEmpty() && driver().findElement(locator).isDisplayed();
    }

    protected String text(By locator) {
        return visible(locator).getText();
    }

    protected List<WebElement> elements(By locator) {
        return driver().findElements(locator);
    }

    protected boolean hasElements(By locator) {
        return !elements(locator).isEmpty();
    }

    protected WebElement elementContaining(String tag, String text) {
        return visible(By.xpath("//" + tag + "[contains(normalize-space(.),\"" + text + "\")]"));
    }

    protected WebElement articleFor(String text) {
        return visible(By.xpath("//*[self::h1 or self::h2 or self::h3 or self::h4 or self::a]"
                + "[contains(normalize-space(.),\"" + text + "\")]"
                + "/ancestor::*[contains(normalize-space(.),'Book Now') "
                + "or contains(normalize-space(.),'Sold Out') "
                + "or contains(normalize-space(.),'SOLD OUT') "
                + "or contains(normalize-space(.),'seats')][1]"));
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }
}
