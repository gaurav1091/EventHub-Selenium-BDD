package com.eventhub.automation.pages;

import org.openqa.selenium.By;

import static com.eventhub.automation.utils.UiAssertions.assertPageContains;

public class HomePage extends BasePage {
    public void assertLoaded() {
        assertPageContains(driver(), "Featured Events");
    }

    public void browseEvents() {
        click(By.xpath("//a[contains(normalize-space(.),'Browse Events') or button[contains(normalize-space(.),'Browse Events')]]"));
    }
}
