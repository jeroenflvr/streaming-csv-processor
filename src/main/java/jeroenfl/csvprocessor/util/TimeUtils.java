package jeroenfl.csvprocessor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date/time operations and JSON timestamp extraction.
 * 
 * <p>This class provides convenient methods for generating timestamps
 * in a consistent format and extracting timestamp values from JSON data.
 * All timestamps are generated in UTC timezone for consistency across
 * different deployment environments.
 * 
 * <p>The timestamp format used is {@code yyyyMMddHHmmss}, which provides
 * a sortable, compact representation suitable for both human reading
 * and programmatic processing.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 */
public class TimeUtils {

    /**
     * Default constructor.
     * <p>This is a utility class with static methods only and should not be instantiated.
     * All methods are thread-safe and can be called concurrently.
     */
    public TimeUtils() {
        // Utility class constructor
    }

    /**
     * The timestamp format pattern used throughout the application.
     */
    private static final String TIMESTAMP_PATTERN = "yyyyMMddHHmmss";
    
    /**
     * Thread-safe formatter for generating timestamps.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN);

    /**
     * Gets the current timestamp in YYYYMMddHHMMSS format.
     * 
     * <p>This method generates a timestamp in UTC timezone using the
     * format {@code yyyyMMddHHmmss}. The resulting timestamp is suitable
     * for use as a sortable identifier or for tracking when records
     * were processed.
     * 
     * @return the current timestamp as a string in YYYYMMddHHMMSS format
     * 
     * <p>Example usage:
     * <pre>
     * String timestamp = TimeUtils.getCurrentTimestamp();
     * // Result: "20250823180000" (for 2025-08-23 18:00:00 UTC)
     * </pre>
     */
    public static String getCurrentTimestamp() {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        return now.format(FORMATTER);
    }

    /**
     * Extracts a timestamp field from JSON and converts to Long.
     * 
     * <p>This method attempts to extract a numeric timestamp from a JSON string.
     * It supports both numeric JSON values and string values that contain only digits.
     * The method is defensive and returns null for any parsing errors or invalid formats.
     * 
     * <p>Supported JSON formats:
     * <ul>
     *   <li>Numeric field: {@code {"timestamp": 1234567890}}</li>
     *   <li>String field with digits: {@code {"timestamp": "1234567890"}}</li>
     *   <li>Null or missing field: returns null</li>
     * </ul>
     * 
     * @param json the JSON string to parse (may be null)
     * @param mapper the Jackson ObjectMapper to use for parsing (must not be null)
     * @param fieldName the name of the field to extract (must not be null)
     * @return the timestamp as a Long, or null if extraction fails or field is invalid
     * @throws NullPointerException if mapper or fieldName is null
     * 
     * <p>Example usage:
     * <pre>
     * ObjectMapper mapper = new ObjectMapper();
     * String json = "{\"ExtractionTS\": \"1234567890\"}";
     * Long timestamp = TimeUtils.extractTimestampFromJson(json, mapper, "ExtractionTS");
     * // Result: 1234567890L
     * </pre>
     */
    public static Long extractTimestampFromJson(String json, ObjectMapper mapper, String fieldName) {
        try {
            JsonNode node = mapper.readTree(json).get(fieldName);
            if (node == null || node.isNull()) {
                return null;
            }
            if (node.isNumber()) {
                return node.longValue();
            }
            String text = node.asText().trim();
            if (text.isEmpty()) {
                return null;
            }
            // Ensure digits only, then parse
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (ch < '0' || ch > '9') {
                    return null;
                }
            }
            return Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
    }
}
