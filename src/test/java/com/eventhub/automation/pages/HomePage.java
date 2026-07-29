package com.eventhub.automation.pages;

import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class HomePage extends BasePage {
    public void assertLoaded() {
        assertThat(driver().getPageSource()).containsIgnoringCase("Featured Events");
    }

    public void browseEvents() {
        click(By.xpath("//a[contains(normalize-space(.),'Browse Events') or button[contains(normalize-space(.),'Browse Events')]]"));
    }
}
