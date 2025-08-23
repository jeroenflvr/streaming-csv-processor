package jeroenflvr.csvprocessor.config;

import org.junit.jupiter.api.Test;

import jeroenflvr.csvprocessor.config.EnvironmentUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnvironmentUtils.
 */
class EnvironmentUtilsTest {

    @Test
    void testEnvOrPropWithDefault() {
        // Test with non-existent environment variable
        String result = EnvironmentUtils.envOrProp("NON_EXISTENT_VAR", "default_value");
        assertEquals("default_value", result);
    }

    @Test
    void testMustGetEnvThrowsException() {
        // Test that missing required environment variable throws exception
        assertThrows(IllegalStateException.class, () -> 
            EnvironmentUtils.mustGetEnv("DEFINITELY_NON_EXISTENT_VAR"));
    }

    @Test
    void testEnvOrDefaultWithDefault() {
        // Test with non-existent environment variable
        String result = EnvironmentUtils.envOrDefault("NON_EXISTENT_VAR", "default_value");
        assertEquals("default_value", result);
    }
}
