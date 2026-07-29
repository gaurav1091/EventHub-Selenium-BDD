package com.eventhub.automation.pages;

import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class NavigationBar extends BasePage {
    private static final By LOGOUT = By.xpath("//button[normalize-space()='Logout']");

    public void assertAuthenticated(String email) {
        visible(LOGOUT);
        String source = driver().getPageSource();
        assertThat(source).contains("Home", "Events", "My Bookings", "Admin", "Logout");
        assertThat(source).contains(email);
    }

    public void openHome() {
        click(By.xpath("//a[normalize-space()='Home' or @href='/']"));
    }

    public void openEvents() {
        click(By.xpath("//a[normalize-space()='Events' or contains(@href,'/events')]"));
    }

    public void openBookings() {
        click(By.xpath("//a[contains(normalize-space(.),'My Bookings') or contains(@href,'/bookings')]"));
    }

    public void openAdminEvents() {
        click(By.xpath("//a[contains(normalize-space(.),'Admin') or contains(@href,'/admin/events')]"));
    }

    public void logout() {
        click(LOGOUT);
    }
}
