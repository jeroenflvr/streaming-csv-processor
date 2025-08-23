package jeroenfl.csvprocessor.model;

/**
 * Represents an S3/COS location with bucket and key.
 */
public class S3Location {
    private final String bucket;
    private final String key;

    private S3Location(String bucket, String key) {
        this.bucket = bucket;
        this.key = key.startsWith("/") ? key.substring(1) : key;
    }

    /**
     * Parses a path string into an S3Location.
     * Supports s3://, cos://, or bucket/key formats.
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

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "S3Location{bucket='" + bucket + "', key='" + key + "'}";
    }
}
