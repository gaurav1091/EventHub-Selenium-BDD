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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        if (!"local".equals(executionTarget)
                && !"grid".equals(executionTarget)
                && !"browserstack".equals(executionTarget)) {
            throw new IllegalArgumentException("Unsupported execution.target: " + executionTarget
                    + ". Supported values are local, grid, and browserstack.");
        }

        WebDriver driver;
        switch (browser) {
            case CHROME:
                if ("browserstack".equals(executionTarget)) {
                    driver = createBrowserStackChromeDriver();
                } else {
                    driver = "grid".equals(executionTarget)
                            ? createRemoteChromeDriver(headless)
                            : createChromeDriver(headless);
                }
                break;
            case FIREFOX:
                if ("browserstack".equals(executionTarget)) {
                    driver = createBrowserStackFirefoxDriver();
                } else {
                    driver = "grid".equals(executionTarget)
                            ? createRemoteFirefoxDriver(headless)
                            : createFirefoxDriver(headless);
                }
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

    private static WebDriver createBrowserStackChromeDriver() {
        ChromeOptions options = chromeOptions(false);
        applyBrowserStackOptions(options);
        return new RemoteWebDriver(browserStackUrl(), options);
    }

    private static WebDriver createBrowserStackFirefoxDriver() {
        FirefoxOptions options = firefoxOptions(false);
        applyBrowserStackOptions(options);
        return new RemoteWebDriver(browserStackUrl(), options);
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

    private static void applyBrowserStackOptions(org.openqa.selenium.MutableCapabilities options) {
        String browserVersion = ConfigReader.getRequired("browserstack.browser.version");
        if (!"latest".equalsIgnoreCase(browserVersion)) {
            options.setCapability("browserVersion", browserVersion);
        }

        Map<String, Object> browserStackOptions = new HashMap<>();
        browserStackOptions.put("os", ConfigReader.getRequired("browserstack.os"));
        browserStackOptions.put("osVersion", ConfigReader.getRequired("browserstack.os.version"));
        browserStackOptions.put("projectName", ConfigReader.getRequired("browserstack.project.name"));
        browserStackOptions.put("buildName", ConfigReader.getRequired("browserstack.build.name"));
        browserStackOptions.put("sessionName", browserStackSessionName());
        browserStackOptions.put("seleniumVersion", "4.23.1");
        browserStackOptions.put("debug", ConfigReader.getBoolean("browserstack.debug"));
        browserStackOptions.put("networkLogs", ConfigReader.getBoolean("browserstack.network.logs"));
        browserStackOptions.put("local", ConfigReader.getBoolean("browserstack.local"));
        options.setCapability("bstack:options", browserStackOptions);
    }

    private static String browserStackSessionName() {
        String configuredName = ConfigReader.get("browserstack.session.name");
        if (configuredName != null && !configuredName.isBlank()) {
            return configuredName.trim();
        }
        return ConfigReader.getRequired("suite.name") + " - " + ConfigReader.getRequired("browser");
    }

    private static URL browserStackUrl() {
        String username = ConfigReader.getRequired("browserstack.username");
        String accessKey = ConfigReader.getRequired("browserstack.access.key");
        String remoteUrl = ConfigReader.getRequired("browserstack.remote.url");
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String encodedAccessKey = URLEncoder.encode(accessKey, StandardCharsets.UTF_8);
        try {
            return new URL(remoteUrl.replace("https://", "https://" + encodedUsername + ":" + encodedAccessKey + "@"));
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid browserstack.remote.url: " + remoteUrl, exception);
        }
    }
}
