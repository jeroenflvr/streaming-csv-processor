package jeroenfl.csvprocessor.config;

/**
 * Utility class for handling environment variables and system properties.
 * 
 * <p>This class provides convenient methods for accessing configuration values
 * from environment variables with fallback mechanisms. It supports both
 * optional configuration with defaults and required configuration that
 * throws exceptions when missing.
 * 
 * <p>All methods in this class are static and the class is designed to be
 * used as a utility without instantiation.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 */
public class EnvironmentUtils {
    
    /**
     * Default constructor.
     * <p>This is a utility class with static methods only and should not be instantiated.
     * All methods are thread-safe and can be called concurrently.
     */
    public EnvironmentUtils() {
        // Utility class constructor
    }
    
    /**
     * Gets value from environment variable or system property, with fallback to default.
     * 
     * <p>This method first checks for an environment variable with the given key.
     * If the environment variable is not found or is empty, the default value
     * is returned instead.
     * 
     * @param key the environment variable name to look up
     * @param defaultValue the value to return if the environment variable is not found or empty
     * @return the environment variable value or the default value
     * @throws NullPointerException if key is null
     */
    public static String envOrProp(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets value from environment variable with fallback to default.
     * 
     * <p>This method is functionally identical to {@link #envOrProp(String, String)}
     * but provides a more explicit name for cases where only environment variables
     * (not system properties) are being consulted.
     * 
     * @param key the environment variable name to look up
     * @param defaultValue the value to return if the environment variable is not found or empty
     * @return the environment variable value or the default value
     * @throws NullPointerException if key is null
     */
    public static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Gets required environment variable, throws exception if not found.
     * 
     * <p>This method retrieves an environment variable that is considered
     * mandatory for application operation. If the environment variable is
     * not set or is empty, an {@link IllegalStateException} is thrown with
     * a descriptive error message.
     * 
     * <p>Use this method for critical configuration values that must be
     * provided externally, such as database URLs, API keys, or service endpoints.
     * 
     * @param key the environment variable name to look up
     * @return the environment variable value
     * @throws IllegalStateException if the environment variable is missing or empty
     * @throws NullPointerException if key is null
     */
    public static String mustGetEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }
}
