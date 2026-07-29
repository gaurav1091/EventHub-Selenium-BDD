package com.eventhub.automation.config;

import com.eventhub.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;

public final class ConfigReader {
    private static final Properties PROPERTIES = loadProperties();
    private static final Properties DOTENV = loadDotEnv();

    private ConfigReader() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String eventHubEnvValue = System.getenv("EVENTHUB_" + envKey);
        if (eventHubEnvValue != null && !eventHubEnvValue.isBlank()) {
            return eventHubEnvValue;
        }

        String dotEnvValue = DOTENV.getProperty(envKey);
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return dotEnvValue;
        }

        String eventHubDotEnvValue = DOTENV.getProperty("EVENTHUB_" + envKey);
        if (eventHubDotEnvValue != null && !eventHubDotEnvValue.isBlank()) {
            return eventHubDotEnvValue;
        }

        return PROPERTIES.getProperty(key);
    }

    public static String getRequired(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Missing required configuration: " + key);
        }
        return value.trim();
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getRequired(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(getRequired(key));
    }

    public static Duration getDurationSeconds(String key) {
        return Duration.ofSeconds(getInt(key));
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config/config.properties")) {
            if (inputStream == null) {
                throw new FrameworkException("Unable to load config/config.properties");
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException exception) {
            throw new FrameworkException("Unable to read framework configuration", exception);
        }
    }

    private static Properties loadDotEnv() {
        Properties properties = new Properties();
        Path dotEnvPath = Path.of(".env");
        if (!Files.exists(dotEnvPath)) {
            return properties;
        }

        try {
            Files.readAllLines(dotEnvPath).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> {
                        int separator = line.indexOf('=');
                        if (separator > 0) {
                            String key = line.substring(0, separator).trim();
                            String value = line.substring(separator + 1).trim();
                            properties.setProperty(key, stripQuotes(value));
                        }
                    });
            return properties;
        } catch (IOException exception) {
            throw new FrameworkException("Unable to read .env file", exception);
        }
    }

    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
