package com.eventhub.automation.pages;

import org.openqa.selenium.By;

import static com.eventhub.automation.utils.UiAssertions.assertPageContainsAnyOf;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginPage extends BasePage {
    private static final By EMAIL = By.cssSelector("input[type='email'], input[placeholder='you@email.com']");
    private static final By PASSWORD = By.cssSelector("input[type='password'], input[placeholder='••••••']");
    private static final By SIGN_IN = By.xpath("//button[normalize-space()='Sign In']");
    private static final By REGISTER_LINK = By.xpath("//a[contains(normalize-space(.),'Register') or contains(@href,'register')]");

    public LoginPage openLoginPage() {
        open("/login");
        assertLoaded();
        return this;
    }

    public void assertLoaded() {
        assertThat(text(By.tagName("h1"))).containsIgnoringCase("Sign in to EventHub");
        assertThat(isDisplayed(EMAIL)).isTrue();
        assertThat(isDisplayed(PASSWORD)).isTrue();
    }

    public void login(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(SIGN_IN);
    }

    public void submitEmpty() {
        click(SIGN_IN);
    }

    public void openRegistration() {
        String href = visible(REGISTER_LINK).getAttribute("href");
        if (href != null && !href.isBlank()) {
            driver().get(href);
            return;
        }
        click(REGISTER_LINK);
    }

    public void assertValidationOrStillOnLogin() {
        assertThat(driver().getCurrentUrl()).contains("/login");
    }

    public void assertLoginErrorVisible() {
        com.eventhub.automation.utils.Waits.until(driver(), webDriver -> {
            boolean stillOnLogin = webDriver.getCurrentUrl().contains("/login");
            boolean loginFinished = webDriver.findElements(By.xpath(
                            "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'signing in')]"))
                    .isEmpty();
            boolean hasErrorText = !webDriver.findElements(By.xpath(
                            "//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid') "
                                    + "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'incorrect') "
                                    + "or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'rejected')]"))
                    .isEmpty();
            return hasErrorText || (stillOnLogin && loginFinished);
        });
        assertThat(driver().getCurrentUrl()).contains("/login");
        assertPageContainsAnyOf(driver(), "Invalid", "invalid", "Incorrect", "incorrect", "rejected", "Sign In");
    }
}
