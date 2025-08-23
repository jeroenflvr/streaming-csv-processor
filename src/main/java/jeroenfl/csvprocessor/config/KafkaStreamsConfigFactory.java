package jeroenfl.csvprocessor.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

/**
 * Factory class for creating Kafka Streams configuration properties.
 * 
 * <p>This factory encapsulates the creation of Kafka Streams configuration,
 * including SSL setup, serialization configuration, and processing guarantees.
 * It provides a centralized location for all Kafka-related configuration logic.
 * 
 * <p>The factory creates configurations suitable for production use with:
 * <ul>
 *   <li>SSL encryption for secure communication</li>
 *   <li>Exactly-once processing semantics</li>
 *   <li>String-based key/value serialization</li>
 *   <li>Configurable bootstrap servers and application ID</li>
 * </ul>
 * 
 * @author CSV Processor Team
 * @version 1.0.0
 * @since 1.0.0
 * @see ApplicationConfig
 * @see EnvironmentUtils
 */
public class KafkaStreamsConfigFactory {

    /**
     * Creates a Properties object configured for Kafka Streams operation.
     * 
     * <p>This method generates a complete Kafka Streams configuration including:
     * <ul>
     *   <li>Application ID from the provided configuration</li>
     *   <li>Bootstrap servers from environment variables</li>
     *   <li>SSL configuration for secure communication</li>
     *   <li>String serializers for keys and values</li>
     *   <li>Exactly-once processing guarantee</li>
     * </ul>
     * 
     * <p>Required environment variables:
     * <ul>
     *   <li>{@code SSL_TRUSTSTORE_LOCATION} - path to SSL truststore (default: truststore.jks)</li>
     *   <li>{@code SSL_TRUSTSTORE_PASSWORD} - SSL truststore password (default: changeit)</li>
     * </ul>
     * 
     * <p>Optional environment variables:
     * <ul>
     *   <li>{@code BOOTSTRAP_SERVERS} - Kafka bootstrap servers (default: localhost:9093)</li>
     * </ul>
     * 
     * @param appConfig the application configuration containing the application ID
     * @return a Properties object ready for Kafka Streams initialization
     * @throws NullPointerException if appConfig is null
     * @see StreamsConfig
     * @see SslConfigs
     */
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
