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
 * Service for reading files from S3/COS storage.
 */
public class S3FileReader {
    private static final Logger log = LoggerFactory.getLogger(S3FileReader.class);
    
    private final S3Client s3Client;

    public S3FileReader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Creates a BufferedReader for the specified S3 location.
     * The caller is responsible for closing the reader.
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
