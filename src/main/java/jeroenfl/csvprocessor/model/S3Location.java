package jeroenfl.csvprocessor.model;

/**
 * Immutable representation of an S3 or Cloud Object Storage (COS) location.
 * 
 * <p>This class encapsulates the bucket and key components of an S3-style
 * object storage location. It supports parsing various URI formats including
 * standard S3 URIs, COS URIs, and simple bucket/key notation.
 * 
 * <p>Supported URI formats:
 * <ul>
 *   <li>{@code s3://bucket-name/path/to/object.csv}</li>
 *   <li>{@code cos://bucket-name/path/to/object.csv}</li>
 *   <li>{@code bucket-name/path/to/object.csv}</li>
 * </ul>
 * 
 * <p>The class automatically handles leading slashes in object keys to ensure
 * consistent key formatting regardless of input format.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see #parse(String)
 */
public class S3Location {
    private final String bucket;
    private final String key;

    /**
     * Private constructor to create an S3Location instance.
     * 
     * <p>This constructor normalizes the key by removing any leading slash
     * to ensure consistent key formatting.
     * 
     * @param bucket the S3 bucket name (must not be null or empty)
     * @param key the object key within the bucket (leading slash will be removed)
     */
    private S3Location(String bucket, String key) {
        this.bucket = bucket;
        this.key = key.startsWith("/") ? key.substring(1) : key;
    }

    /**
     * Parses a path string into an S3Location instance.
     * 
     * <p>This method supports multiple URI formats for maximum flexibility:
     * <ul>
     *   <li><strong>S3 URI:</strong> {@code s3://bucket-name/path/to/file}</li>
     *   <li><strong>COS URI:</strong> {@code cos://bucket-name/path/to/file}</li>
     *   <li><strong>Simple format:</strong> {@code bucket-name/path/to/file}</li>
     * </ul>
     * 
     * <p>The parser automatically extracts the bucket name and object key,
     * handling edge cases such as missing keys or malformed URIs.
     * 
     * @param path the path string to parse (must not be null)
     * @return a new S3Location instance representing the parsed path
     * @throws IllegalArgumentException if the path format is invalid or missing required components
     * @throws NullPointerException if path is null
     * 
     * <p>Example usage:
     * <pre>
     * S3Location loc1 = S3Location.parse("s3://my-bucket/data/file.csv");
     * S3Location loc2 = S3Location.parse("cos://my-bucket/data/file.csv");
     * S3Location loc3 = S3Location.parse("my-bucket/data/file.csv");
     * </pre>
     */
    public static S3Location parse(String path) {
        String trimmedPath = path.trim();
        
        if (trimmedPath.startsWith("s3://") || trimmedPath.startsWith("cos://")) {
            String rest = trimmedPath.substring(trimmedPath.indexOf("://") + 3);
            int slashIndex = rest.indexOf('/');
            if (slashIndex < 0) {
                throw new IllegalArgumentException("Invalid S3/COS URI (no key): " + path);
            }
            String bucket = rest.substring(0, slashIndex);
            String key = rest.substring(slashIndex + 1);
            return new S3Location(bucket, key);
        }
        
        // fallback: "bucket/key..."
        int slashIndex = trimmedPath.indexOf('/');
        if (slashIndex < 0) {
            throw new IllegalArgumentException("Path must be s3://, cos://, or bucket/key: " + path);
        }
        return new S3Location(trimmedPath.substring(0, slashIndex), trimmedPath.substring(slashIndex + 1));
    }

    /**
     * Gets the S3 bucket name.
     * 
     * @return the bucket name (never null)
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Gets the object key within the S3 bucket.
     * 
     * <p>The returned key will never have a leading slash, regardless
     * of the input format used during parsing.
     * 
     * @return the object key (never null, never starts with '/')
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns a string representation of this S3Location.
     * 
     * @return a string in the format "S3Location{bucket='...', key='...'}"
     */
    @Override
    public String toString() {
        return "S3Location{bucket='" + bucket + "', key='" + key + "'}";
    }
}
