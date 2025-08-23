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
 * Main application class for the CSV processor.
 * Orchestrates the setup and execution of the Kafka Streams topology.
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

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
 


