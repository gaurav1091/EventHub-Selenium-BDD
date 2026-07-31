package com.eventhub.automation.pages;

import com.eventhub.automation.utils.Waits;
import org.openqa.selenium.By;

import static com.eventhub.automation.utils.UiAssertions.assertElementTextContains;
import static com.eventhub.automation.utils.UiAssertions.assertPageContains;

public class NavigationBar extends BasePage {
    private static final By LOGOUT = By.xpath("//button[normalize-space()='Logout']");

    public void assertAuthenticated(String email) {
        visible(LOGOUT);
        assertPageContains(driver(), "Home");
        assertPageContains(driver(), "Events");
        assertPageContains(driver(), "My Bookings");
        assertPageContains(driver(), "Admin");
        assertElementTextContains(driver(), LOGOUT, "Logout");
        assertPageContains(driver(), email);
    }

    public void openHome() {
        click(By.xpath("//a[normalize-space()='Home' or @href='/']"));
        Waits.visibleTextContains(driver(), "Featured Events");
    }

    public void openEvents() {
        click(By.xpath("//a[normalize-space()='Events' or contains(@href,'/events')]"));
        Waits.urlContains(driver(), "/events");
    }

    public void openBookings() {
        click(By.xpath("//a[contains(normalize-space(.),'My Bookings') or contains(@href,'/bookings')]"));
        Waits.urlContains(driver(), "/bookings");
    }

    public void openAdminEvents() {
        click(By.xpath("//a[contains(normalize-space(.),'Admin') or contains(@href,'/admin/events')]"));
        Waits.urlContains(driver(), "/admin/events");
    }

    public void logout() {
        click(LOGOUT);
    }
}
