
package jeroenflvr.csvprocessor.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Component
public class FailedMessageConsumer {
    private KafkaStreams streams;
    private ReadOnlyKeyValueStore<String, String> store;

    @PostConstruct
    public void init() {
        StreamsBuilder builder = new StreamsBuilder();
        String topic = System.getenv().getOrDefault("FAILED_MESSAGES_TOPIC", "failed-messages");
        KTable<String, String> table = builder.table(
            topic,
            Consumed.with(Serdes.String(), Serdes.String()),
            Materialized.<String, String, org.apache.kafka.streams.state.KeyValueStore<org.apache.kafka.common.utils.Bytes, byte[]>>as("failed-messages-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.String())
        );
    java.util.Properties props = new java.util.Properties();
    props.put("application.id", "failed-messages-consumer");
    String bootstrapServers = System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9093");
    props.put("bootstrap.servers", bootstrapServers);
    props.put("security.protocol", System.getenv().getOrDefault("KAFKA_SECURITY_PROTOCOL", "SSL"));
    props.put("ssl.ca.location", System.getenv().getOrDefault("KAFKA_SSL_CA_LOCATION", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt"));
    props.put("client.id", System.getenv().getOrDefault("KAFKA_CLIENT_ID", "scala-producer"));
    props.put("enable.idempotence", System.getenv().getOrDefault("KAFKA_ENABLE_IDEMPOTENCE", "true"));
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("ssl.endpoint.identification.algorithm", "");
    props.put("ssl.truststore.location", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/truststore.jks");
    props.put("ssl.truststore.password", "changeit");
    streams = new KafkaStreams(builder.build(), props);
    streams.start();
    store = streams.store(org.apache.kafka.streams.StoreQueryParameters.fromNameAndType("failed-messages-store", QueryableStoreTypes.keyValueStore()));
    }

    public List<ConsumerRecord<String, String>> getAllFailedMessages() {
        List<ConsumerRecord<String, String>> allRecords = new ArrayList<>();
        if (store != null) {
            store.all().forEachRemaining(kv -> {
                // topic name is needed for ConsumerRecord, use the configured topic
                String topic = System.getenv().getOrDefault("FAILED_MESSAGES_TOPIC", "failed-messages");
                // partition and offset are not available from state store, use 0 and -1 as placeholders
                ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, 0, -1L, kv.key, kv.value);
                allRecords.add(record);
            });
        }
        return allRecords;
    }
}
