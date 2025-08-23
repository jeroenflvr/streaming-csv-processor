package jeroenflvr.csvprocessor.model;

import org.junit.jupiter.api.Test;

import jeroenflvr.csvprocessor.model.S3Location;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for S3Location.
 */
class S3LocationTest {

    @Test
    void testParseS3Uri() {
        S3Location location = S3Location.parse("s3://my-bucket/path/to/file.csv");
        assertEquals("my-bucket", location.getBucket());
        assertEquals("path/to/file.csv", location.getKey());
    }

    @Test
    void testParseCosUri() {
        S3Location location = S3Location.parse("cos://my-bucket/path/to/file.csv");
        assertEquals("my-bucket", location.getBucket());
        assertEquals("path/to/file.csv", location.getKey());
    }

    @Test
    void testParseBucketKeyFormat() {
        S3Location location = S3Location.parse("my-bucket/path/to/file.csv");
        assertEquals("my-bucket", location.getBucket());
        assertEquals("path/to/file.csv", location.getKey());
    }

    @Test
    void testParseInvalidUri() {
        assertThrows(IllegalArgumentException.class, () -> 
            S3Location.parse("s3://bucket-only"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            S3Location.parse("just-a-bucket"));
    }

    @Test
    void testKeyWithLeadingSlash() {
        S3Location location = S3Location.parse("s3://my-bucket//path/to/file.csv");
        assertEquals("my-bucket", location.getBucket());
        assertEquals("path/to/file.csv", location.getKey()); // Leading slash should be removed
    }
}
