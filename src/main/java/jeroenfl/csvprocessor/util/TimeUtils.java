package jeroenfl.csvprocessor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date/time operations and JSON parsing.
 */
public class TimeUtils {

    /**
     * Gets the current timestamp in YYYYmmddHHMMSS format.
     */
    public static String getCurrentTimestamp() {
        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return now.format(formatter);
    }

    /**
     * Extracts a timestamp field from JSON and converts to Long.
     * Returns null if the field is not found or cannot be parsed.
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
