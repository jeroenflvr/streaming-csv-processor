package jeroenfl.csvprocessor.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Factory for creating S3 client configuration.
 */
public class S3ClientFactory {

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
