package jeroenfl.csvprocessor.config;

/**
 * Utility class for handling environment variables and system properties.
 */
public class EnvironmentUtils {
    
    /**
     * Gets value from environment variable or system property, with fallback to default.
     */
    public static String envOrProp(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets value from environment variable with fallback to default.
     */
    public static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets required environment variable, throws exception if not found.
     */
    public static String mustGetEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
