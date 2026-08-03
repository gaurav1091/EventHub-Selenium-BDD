package com.eventhub.automation.drivers;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.core.Browser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        Browser browser = Browser.from(ConfigReader.getRequired("browser"));
        boolean headless = ConfigReader.getBoolean("headless");
        String executionTarget = ConfigReader.getRequired("execution.target").toLowerCase(Locale.ROOT);
        if (!"local".equals(executionTarget) && !"grid".equals(executionTarget)) {
            throw new IllegalArgumentException("Unsupported execution.target: " + executionTarget
                    + ". Supported values are local and grid.");
        }

        WebDriver driver;
        switch (browser) {
            case CHROME:
                driver = "grid".equals(executionTarget)
                        ? createRemoteChromeDriver(headless)
                        : createChromeDriver(headless);
                break;
            case FIREFOX:
                driver = "grid".equals(executionTarget)
                        ? createRemoteFirefoxDriver(headless)
                        : createFirefoxDriver(headless);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.manage().timeouts().pageLoadTimeout(ConfigReader.getDurationSeconds("page.load.timeout.seconds"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicit.wait.seconds")));
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(
                ConfigReader.getInt("window.width"),
                ConfigReader.getInt("window.height")));
        return driver;
    }

    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        return new ChromeDriver(chromeOptions(headless));
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        return new FirefoxDriver(firefoxOptions(headless));
    }

    private static WebDriver createRemoteChromeDriver(boolean headless) {
        return new RemoteWebDriver(remoteUrl(), chromeOptions(headless));
    }

    private static WebDriver createRemoteFirefoxDriver(boolean headless) {
        return new RemoteWebDriver(remoteUrl(), firefoxOptions(headless));
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new");
        }

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("signon.rememberSignons", false);
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static URL remoteUrl() {
        try {
            return new URL(ConfigReader.getRequired("selenium.remote.url"));
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid selenium.remote.url: "
                    + ConfigReader.getRequired("selenium.remote.url"), exception);
        }
    }
}
