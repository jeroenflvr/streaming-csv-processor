package jeroenfl.csvprocessor.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

/**
 * Factory for creating Kafka Streams configuration.
 */
public class KafkaStreamsConfigFactory {

    public static Properties createProperties(ApplicationConfig appConfig) {
        Properties props = new Properties();
        
        // Basic Kafka Streams configuration
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appConfig.getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, 
                 EnvironmentUtils.envOrProp("BOOTSTRAP_SERVERS", "localhost:9093"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        
        // SSL configuration
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, 
                 EnvironmentUtils.envOrProp("SSL_TRUSTSTORE_LOCATION", "truststore.jks"));
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, 
                 EnvironmentUtils.envOrProp("SSL_TRUSTSTORE_PASSWORD", "changeit"));
        props.put("ssl.endpoint.identification.algorithm", "");

        // Processing guarantee
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        return props;
    }
}
