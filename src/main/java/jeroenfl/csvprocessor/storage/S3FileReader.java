package jeroenfl.csvprocessor.storage;

import jeroenfl.csvprocessor.model.S3Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Service for reading files from S3 or Cloud Object Storage.
 * 
 * <p>This class provides a high-level interface for accessing CSV files
 * stored in S3-compatible object storage systems. It handles the conversion
 * from S3 input streams to BufferedReader objects for convenient text processing.
 * 
 * <p>The service automatically handles:
 * <ul>
 *   <li>S3 authentication using the provided client</li>
 *   <li>UTF-8 encoding for text files</li>
 *   <li>Error handling for missing files</li>
 *   <li>Resource management (though callers must close returned readers)</li>
 * </ul>
 * 
 * <p><strong>Important:</strong> Callers are responsible for closing the
 * BufferedReader returned by {@link #createReader(S3Location)} to prevent
 * resource leaks.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see S3Location
 * @see S3Client
 */
public class S3FileReader {
    private static final Logger log = LoggerFactory.getLogger(S3FileReader.class);
    
    private final S3Client s3Client;

    /**
     * Constructs a new S3FileReader with the specified S3 client.
     * 
     * @param s3Client the S3 client to use for file operations (must not be null)
     * @throws NullPointerException if s3Client is null
     */
    public S3FileReader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Creates a BufferedReader for the specified S3 location.
     * 
     * <p>This method establishes a connection to S3, retrieves the specified
     * object, and wraps it in a BufferedReader for convenient text processing.
     * The reader uses UTF-8 encoding and is suitable for processing CSV files.
     * 
     * <p><strong>Resource Management:</strong> The caller is responsible for
     * closing the returned BufferedReader. Failure to close the reader may
     * result in resource leaks and connection pool exhaustion.
     * 
     * @param location the S3 location to read from (must not be null)
     * @return a BufferedReader for the S3 object content
     * @throws IOException if the S3 object cannot be found, accessed, or read
     * @throws NullPointerException if location is null
     * 
     * @example
     * <pre>
     * S3Location location = S3Location.parse("s3://my-bucket/data.csv");
     * try (BufferedReader reader = s3FileReader.createReader(location)) {
     *     String line;
     *     while ((line = reader.readLine()) != null) {
     *         // Process line
     *     }
     * }
     * </pre>
     */
    public BufferedReader createReader(S3Location location) throws IOException {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(location.getBucket())
                    .key(location.getKey())
                    .build();

            ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request);
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
        } catch (NoSuchKeyException e) {
            log.error("S3 key not found: {}", location);
            throw new IOException("S3 key not found: " + location, e);
        } catch (Exception e) {
            log.error("Failed to read from S3 location: {}", location, e);
            throw new IOException("Failed to read from S3: " + location, e);
        }
    }
}
