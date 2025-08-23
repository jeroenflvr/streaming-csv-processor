package jeroenfl.csvprocessor.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Factory class for creating AWS S3 client instances.
 * 
 * <p>This factory creates S3 clients configured for Cloud Object Storage (COS)
 * or standard AWS S3 endpoints. It handles credential management, endpoint
 * configuration, and path-style access settings.
 * 
 * <p>The factory supports various S3-compatible storage services by allowing
 * custom endpoint configuration while maintaining the standard S3 API interface.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see S3Client
 * @see EnvironmentUtils
 */
public class S3ClientFactory {

    /**
     * Default constructor.
     * <p>This is a factory class for creating S3 client instances.
     * All methods are static and thread-safe.
     */
    public S3ClientFactory() {
        // Factory class constructor
    }

    /**
     * Creates an S3Client instance from environment variables.
     * 
     * <p>This method creates a fully configured S3 client suitable for
     * accessing S3-compatible storage services. The client is configured with:
     * <ul>
     *   <li>Custom endpoint support for non-AWS S3 services</li>
     *   <li>Basic authentication using access/secret keys</li>
     *   <li>Configurable region settings</li>
     *   <li>Path-style access for compatibility with various providers</li>
     * </ul>
     * 
     * <p>Required environment variables:
     * <ul>
     *   <li>{@code COS_ENDPOINT} - S3 service endpoint URL</li>
     *   <li>{@code COS_ACCESS_KEY_ID} - S3 access key ID</li>
     *   <li>{@code COS_SECRET_ACCESS_KEY} - S3 secret access key</li>
     * </ul>
     * 
     * <p>Optional environment variables:
     * <ul>
     *   <li>{@code COS_REGION} - S3 region (default: eu-fr2)</li>
     *   <li>{@code COS_PATH_STYLE} - Enable path-style access (default: true)</li>
     * </ul>
     * 
     * @return a configured S3Client instance
     * @throws IllegalStateException if required environment variables are missing
     * @throws RuntimeException if client creation fails
     * @see EnvironmentUtils#mustGetEnv(String)
     * @see EnvironmentUtils#envOrDefault(String, String)
     */
    public static S3Client createFromEnvironment() {
        String endpoint = EnvironmentUtils.mustGetEnv("COS_ENDPOINT");
        String region = EnvironmentUtils.envOrDefault("COS_REGION", "eu-fr2");
        String accessKey = EnvironmentUtils.mustGetEnv("COS_ACCESS_KEY_ID");
        String secretKey = EnvironmentUtils.mustGetEnv("COS_SECRET_ACCESS_KEY");
        boolean pathStyle = Boolean.parseBoolean(EnvironmentUtils.envOrDefault("COS_PATH_STYLE", "true"));

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build())
                .build();
    }
}
