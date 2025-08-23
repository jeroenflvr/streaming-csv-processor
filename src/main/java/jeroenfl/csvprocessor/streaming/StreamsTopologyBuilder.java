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
 * Builds the Kafka Streams topology for CSV processing.
 */
public class StreamsTopologyBuilder {
    private static final Logger log = LoggerFactory.getLogger(StreamsTopologyBuilder.class);
    
    private final ApplicationConfig config;
    private final FileProcessingService fileProcessingService;

    public StreamsTopologyBuilder(ApplicationConfig config, FileProcessingService fileProcessingService) {
        this.config = config;
        this.fileProcessingService = fileProcessingService;
    }

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

    private List<KeyValue<String, String>> expandFileToRecords(String key, String pathValue) {
        return fileProcessingService.processFile(pathValue);
    }

    private String filterUpdates(String newValue, String oldValue) {
        // Only emit if values are different
        if (Objects.equals(newValue, oldValue)) {
            return null;
        }
        return newValue;
    }

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
