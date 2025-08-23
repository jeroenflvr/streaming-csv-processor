package jeroenfl.csvprocessor;

import jeroenfl.csvprocessor.config.ApplicationConfig;
import jeroenfl.csvprocessor.config.KafkaStreamsConfigFactory;
import jeroenfl.csvprocessor.config.S3ClientFactory;
import jeroenfl.csvprocessor.processing.FileProcessingService;
import jeroenfl.csvprocessor.storage.S3FileReader;
import jeroenfl.csvprocessor.streaming.StreamsTopologyBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Properties;

/**
 * Main application class for the CSV processor that reads CSV files from S3/COS storage
 * and processes them through Kafka Streams.
 * 
 * <p>This application:
 * <ul>
 *   <li>Listens to Kafka messages containing S3/COS file paths</li>
 *   <li>Downloads and parses CSV files from cloud storage</li>
 *   <li>Transforms CSV rows into JSON messages with composite keys</li>
 *   <li>Publishes processed records to Kafka topics</li>
 *   <li>Maintains state to track changes and emit updates only</li>
 * </ul>
 * 
 * <p>The application is designed for exactly-once processing semantics and 
 * supports various CSV formats with semicolon delimiters.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    /**
     * Main entry point for the CSV processor application.
     * 
     * <p>This method:
     * <ol>
     *   <li>Loads application configuration from environment variables</li>
     *   <li>Initializes S3 client and file processing services</li>
     *   <li>Builds the Kafka Streams topology</li>
     *   <li>Configures and starts the Kafka Streams application</li>
     *   <li>Sets up graceful shutdown handling</li>
     * </ol>
     * 
     * <p>Environment variables required:
     * <ul>
     *   <li>{@code COS_ENDPOINT} - S3/COS endpoint URL</li>
     *   <li>{@code COS_ACCESS_KEY_ID} - Access key for S3/COS</li>
     *   <li>{@code COS_SECRET_ACCESS_KEY} - Secret key for S3/COS</li>
     * </ul>
     * 
     * <p>Optional environment variables:
     * <ul>
     *   <li>{@code INPUT_TOPIC} - Kafka input topic (default: local-input-topic)</li>
     *   <li>{@code OUTPUT_TOPIC} - Kafka output topic (default: local-stream-topic)</li>
     *   <li>{@code UPDATE_TOPIC} - Kafka updates topic (default: local-updates-only-topic)</li>
     *   <li>{@code STATE_TOPIC} - Kafka state topic (default: local-state-topic)</li>
     *   <li>{@code APP_ID} - Application ID (default: cos-csv-expander-app)</li>
     *   <li>{@code BOOTSTRAP_SERVERS} - Kafka bootstrap servers (default: localhost:9093)</li>
     * </ul>
     * 
     * @param args command line arguments (currently unused)
     * @throws IllegalStateException if required environment variables are missing
     * @throws RuntimeException if the application fails to start
     */
    public static void main(String[] args) {
        // Load configuration
        ApplicationConfig appConfig = ApplicationConfig.fromEnvironment();
        log.info("Starting CSV processor with input topic: {}", appConfig.getInputTopic());

        // Create dependencies
        S3Client s3Client = S3ClientFactory.createFromEnvironment();
        S3FileReader s3FileReader = new S3FileReader(s3Client);
        FileProcessingService fileProcessingService = new FileProcessingService(s3FileReader);

        // Build topology
        StreamsTopologyBuilder topologyBuilder = new StreamsTopologyBuilder(appConfig, fileProcessingService);
        Topology topology = topologyBuilder.build();
        log.info("Topology:\n{}", topology.describe());

        // Configure and start Kafka Streams
        Properties streamsProperties = KafkaStreamsConfigFactory.createProperties(appConfig);
        KafkaStreams streams = new KafkaStreams(topology, streamsProperties);
        
        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down CSV processor...");
            streams.close();
        }));
        
        streams.start();
        log.info("CSV processor started successfully");
    }
}
 


