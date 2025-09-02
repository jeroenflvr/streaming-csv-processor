package jeroenflvr.csvprocessor.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Future;

@Component
public class FailedMessageProducer {
    private final KafkaProducer<String, String> producer;

    public FailedMessageProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv().getOrDefault("BOOTSTRAP_SERVERS", "localhost:9093"));
        props.put("security.protocol", System.getenv().getOrDefault("KAFKA_SECURITY_PROTOCOL", "SSL"));
        props.put("ssl.ca.location", System.getenv().getOrDefault("KAFKA_SSL_CA_LOCATION", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt"));
        props.put("client.id", System.getenv().getOrDefault("KAFKA_CLIENT_ID", "scala-producer"));
        props.put("enable.idempotence", System.getenv().getOrDefault("KAFKA_ENABLE_IDEMPOTENCE", "true"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put("ssl.endpoint.identification.algorithm", "");
        props.put("ssl.truststore.location", System.getenv().getOrDefault("KAFKA_SSL_TRUSTSTORE_LOCATION", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/truststore.jks"));
        props.put("ssl.truststore.password", System.getenv().getOrDefault("KAFKA_SSL_TRUSTSTORE_PASSWORD", "changeit"));
        this.producer = new KafkaProducer<>(props);
    }

    public Future<RecordMetadata> send(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        return producer.send(record);
    }
}
