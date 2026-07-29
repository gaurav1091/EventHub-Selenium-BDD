package com.eventhub.automation.steps;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.drivers.DriverManager;
import com.eventhub.automation.pages.LoginPage;
import com.eventhub.automation.pages.NavigationBar;
import com.eventhub.automation.utils.Waits;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.JavascriptExecutor;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {
    private final LoginPage loginPage = new LoginPage();
    private final NavigationBar navigationBar = new NavigationBar();

    @Given("I am on the EventHub login page")
    public void iAmOnTheEventHubLoginPage() {
        loginPage.openLoginPage();
    }

    @Given("I am signed in to EventHub")
    public void iAmSignedInToEventHub() {
        loginPage.openLoginPage();
        loginPage.login(ConfigReader.getRequired("user.email"), ConfigReader.getRequired("user.password"));
        navigationBar.assertAuthenticated(ConfigReader.getRequired("user.email"));
    }

    @Given("I am an anonymous visitor")
    public void iAmAnAnonymousVisitor() {
        DriverManager.getDriver().get(ConfigReader.getRequired("base.url"));
        DriverManager.getDriver().manage().deleteAllCookies();
        ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "window.localStorage.clear(); window.sessionStorage.clear();"
        );
    }

    @When("I sign in with valid registered credentials")
    public void iSignInWithValidRegisteredCredentials() {
        loginPage.login(ConfigReader.getRequired("user.email"), ConfigReader.getRequired("user.password"));
    }

    @When("I sign in with password {string}")
    public void iSignInWithPassword(String password) {
        loginPage.login(ConfigReader.getRequired("user.email"), password);
    }

    @When("I submit the login form without credentials")
    public void iSubmitTheLoginFormWithoutCredentials() {
        loginPage.submitEmpty();
    }

    @When("I open the registration page from login")
    public void iOpenTheRegistrationPageFromLogin() {
        loginPage.openRegistration();
    }

    @When("I directly open protected route {string}")
    public void iDirectlyOpenProtectedRoute(String route) {
        DriverManager.getDriver().get(ConfigReader.getRequired("base.url") + route);
    }

    @When("I sign out")
    public void iSignOut() {
        navigationBar.logout();
    }

    @Then("I should see the authenticated navigation")
    public void iShouldSeeTheAuthenticatedNavigation() {
        navigationBar.assertAuthenticated(ConfigReader.getRequired("user.email"));
    }

    @Then("I should be returned to the login page")
    public void iShouldBeReturnedToTheLoginPage() {
        Waits.until(DriverManager.getDriver(), webDriver -> {
            String currentUrl = webDriver.getCurrentUrl();
            String pageSource = webDriver.getPageSource();
            return (currentUrl.endsWith("/") || currentUrl.contains("/login") || pageSource.contains("Sign In"))
                    && !pageSource.contains("Logout");
        });
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        String pageSource = DriverManager.getDriver().getPageSource();
        assertThat(currentUrl.endsWith("/") || currentUrl.contains("/login") || pageSource.contains("Sign In")).isTrue();
        assertThat(pageSource).doesNotContain("Logout");
    }

    @Then("the login form should show required field validation")
    public void theLoginFormShouldShowRequiredFieldValidation() {
        loginPage.assertValidationOrStillOnLogin();
    }

    @Then("I should see a login error")
    public void iShouldSeeALoginError() {
        loginPage.assertLoginErrorVisible();
    }

    @Then("I should be on the registration page")
    public void iShouldBeOnTheRegistrationPage() {
        assertThat(DriverManager.getDriver().getCurrentUrl()).contains("/register");
    }
}
