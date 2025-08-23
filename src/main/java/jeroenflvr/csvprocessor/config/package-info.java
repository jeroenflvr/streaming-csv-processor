/**
 * Configuration management and factory classes for the CSV processor application.
 * 
 * <p>This package provides centralized configuration management with type-safe
 * access to application settings. It includes factory classes for creating
 * properly configured instances of external services like Kafka Streams and S3 clients.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenflvr.csvprocessor.config.ApplicationConfig} - Application settings and topic configuration</li>
 *   <li>{@link jeroenflvr.csvprocessor.config.EnvironmentUtils} - Environment variable access utilities</li>
 *   <li>{@link jeroenflvr.csvprocessor.config.KafkaStreamsConfigFactory} - Kafka Streams configuration factory</li>
 *   <li>{@link jeroenflvr.csvprocessor.config.S3ClientFactory} - S3 client configuration factory</li>
 * </ul>
 * 
 * <p>The configuration system emphasizes:
 * <ul>
 *   <li><strong>Type Safety:</strong> Strongly typed configuration objects</li>
 *   <li><strong>Default Values:</strong> Sensible defaults for development environments</li>
 *   <li><strong>Environment-based:</strong> Production configuration via environment variables</li>
 *   <li><strong>Validation:</strong> Required configuration validation with clear error messages</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package jeroenflvr.csvprocessor.config;
