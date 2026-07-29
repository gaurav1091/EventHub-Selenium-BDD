package com.eventhub.automation.drivers;

import com.eventhub.automation.config.ConfigReader;
import com.eventhub.automation.core.Browser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        Browser browser = Browser.from(ConfigReader.getRequired("browser"));
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver driver;
        switch (browser) {
            case CHROME:
                driver = createChromeDriver(headless);
                break;
            case FIREFOX:
                driver = createFirefoxDriver(headless);
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
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("signon.rememberSignons", false);
        if (headless) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }
}
