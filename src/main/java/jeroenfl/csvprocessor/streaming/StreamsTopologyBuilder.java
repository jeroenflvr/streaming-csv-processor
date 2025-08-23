package jeroenfl.csvprocessor.streaming;

import jeroenfl.csvprocessor.config.ApplicationConfig;
import jeroenfl.csvprocessor.processing.FileProcessingService;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Builds the Kafka Streams topology for CSV file processing with change detection.
 * 
 * <p>This class constructs a sophisticated Kafka Streams topology that:
 * <ul>
 *   <li>Consumes file path messages from an input topic</li>
 *   <li>Downloads and processes CSV files from S3/COS storage</li>
 *   <li>Expands each file into multiple individual record messages</li>
 *   <li>Maintains state to detect record changes over time</li>
 *   <li>Publishes all records and change-only streams to separate topics</li>
 * </ul>
 * 
 * <p>The topology implements change detection by maintaining a state table
 * that tracks the last known value for each record key. This allows the
 * system to emit only changed records to the updates topic while still
 * providing a complete stream on the output topic.
 * 
 * <p>Topology structure:
 * <pre>
 * Input Topic (file paths) 
 *   ↓
 * File Processing (flatMap)
 *   ↓
 * Record Expansion
 *   ↓ ↘
 * State Join    Output Topic (all records)
 *   ↓
 * Updates Topic (changes only)
 *   ↓
 * State Topic (state maintenance)
 * </pre>
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see ApplicationConfig
 * @see FileProcessingService
 */
public class StreamsTopologyBuilder {
    private static final Logger log = LoggerFactory.getLogger(StreamsTopologyBuilder.class);
    
    private final ApplicationConfig config;
    private final FileProcessingService fileProcessingService;

    /**
     * Constructs a new StreamsTopologyBuilder with the specified configuration and services.
     * 
     * @param config the application configuration containing topic names (must not be null)
     * @param fileProcessingService the service for processing S3 files (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public StreamsTopologyBuilder(ApplicationConfig config, FileProcessingService fileProcessingService) {
        this.config = config;
        this.fileProcessingService = fileProcessingService;
    }

    /**
     * Builds and returns the complete Kafka Streams topology.
     * 
     * <p>This method constructs a topology that implements the following processing flow:
     * <ol>
     *   <li><strong>Source Stream:</strong> Reads file path messages from the input topic</li>
     *   <li><strong>File Expansion:</strong> Downloads and processes each CSV file into individual records</li>
     *   <li><strong>State Management:</strong> Maintains a table of current record states for change detection</li>
     *   <li><strong>Change Detection:</strong> Compares new records against stored state to identify changes</li>
     *   <li><strong>Output Routing:</strong> Sends all records to output topic and changes to updates topic</li>
     * </ol>
     * 
     * <p>The topology uses exactly-once semantics and maintains state in a local store
     * backed by the state topic for fault tolerance and rebalancing support.
     * 
     * @return a complete Kafka Streams topology ready for execution
     * @see Topology
     * @see KStream
     * @see KTable
     */
    public Topology build() {
        StreamsBuilder builder = new StreamsBuilder();

        // Source stream from input topic
        KStream<String, String> source = builder.stream(
                config.getInputTopic(),
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // Expand each file path into multiple records
        KStream<String, String> expanded = source.flatMap(this::expandFileToRecords);

        // State table for tracking changes
        KTable<String, String> stateTable = builder.table(
                config.getStateTopic(),
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("rows-state-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.String())
        );

        // Filter for updates only
        KStream<String, String> updatesOnly = expanded.leftJoin(
                stateTable,
                this::filterUpdates
        ).filter((k, v) -> v != null);

        // Output streams
        setupOutputStreams(expanded, updatesOnly);

        return builder.build();
    }

    /**
     * Expands a file path message into multiple CSV record messages.
     * 
     * <p>This method serves as the bridge between Kafka Streams and the file
     * processing service. It delegates the actual file processing work and
     * returns the results in a format suitable for stream processing.
     * 
     * @param key the message key (typically unused for file path messages)
     * @param pathValue the S3 file path to process
     * @return a list of key-value pairs representing individual CSV records
     */
    private List<KeyValue<String, String>> expandFileToRecords(String key, String pathValue) {
        return fileProcessingService.processFile(pathValue);
    }

    /**
     * Filters record updates by comparing new values against stored state.
     * 
     * <p>This method implements the change detection logic by comparing
     * incoming record values against the current state. Only records that
     * represent actual changes are passed through to the updates stream.
     * 
     * @param newValue the incoming record value
     * @param oldValue the stored state value (may be null for new records)
     * @return the new value if it represents a change, null otherwise
     */
    private String filterUpdates(String newValue, String oldValue) {
        // Only emit if values are different
        if (Objects.equals(newValue, oldValue)) {
            return null;
        }
        return newValue;
    }

    /**
     * Configures the output streams for the topology.
     * 
     * <p>This method sets up the final output routing for the processed data:
     * <ul>
     *   <li>Updates stream → updates topic and state topic</li>
     *   <li>All records stream → output topic</li>
     * </ul>
     * 
     * <p>Each output includes logging for observability and debugging.
     * 
     * @param expanded the stream of all processed records
     * @param updatesOnly the stream of changed records only
     */
    private void setupOutputStreams(KStream<String, String> expanded, KStream<String, String> updatesOnly) {
        // Send updates to update topic and state topic
        updatesOnly
                .peek((k, v) -> log.info("UPDATE -> key='{}'", k))
                .to(config.getUpdateTopic(), Produced.with(Serdes.String(), Serdes.String()));

        updatesOnly.to(config.getStateTopic(), Produced.with(Serdes.String(), Serdes.String()));

        // Send all expanded records to output topic
        expanded
                .peek((k, v) -> log.info("OUT -> key='{}' value='{}'", k, v))
                .to(config.getOutputTopic(), Produced.with(Serdes.String(), Serdes.String()));
    }
}
