/**
 * CSV Processor - A Kafka Streams application for processing CSV files from S3/COS storage.
 * 
 * <p>This package contains a complete Kafka Streams application that reads file path messages
 * from Kafka topics, downloads CSV files from S3-compatible storage, processes the CSV data,
 * and publishes individual records back to Kafka topics.
 * 
 * <h2>Architecture Overview</h2>
 * 
 * <p>The application is structured into several key packages:
 * 
 * <ul>
 *   <li><strong>{@link jeroenfl.csvprocessor.config}</strong> - Configuration management and factory classes</li>
 *   <li><strong>{@link jeroenfl.csvprocessor.model}</strong> - Data models and value objects</li>
 *   <li><strong>{@link jeroenfl.csvprocessor.storage}</strong> - S3/COS storage access layer</li>
 *   <li><strong>{@link jeroenfl.csvprocessor.processing}</strong> - CSV processing and business logic</li>
 *   <li><strong>{@link jeroenfl.csvprocessor.streaming}</strong> - Kafka Streams topology construction</li>
 *   <li><strong>{@link jeroenfl.csvprocessor.util}</strong> - Utility classes and helper functions</li>
 * </ul>
 * 
 * <h2>Processing Flow</h2>
 * 
 * <p>The application implements the following high-level processing flow:
 * 
 * <ol>
 *   <li><strong>Message Consumption:</strong> Kafka Streams consumes file path messages from the input topic</li>
 *   <li><strong>File Retrieval:</strong> Each file path is used to download a CSV file from S3/COS storage</li>
 *   <li><strong>CSV Processing:</strong> The CSV file is parsed, creating individual record objects</li>
 *   <li><strong>Key Generation:</strong> Composite keys are generated from business-significant columns</li>
 *   <li><strong>Change Detection:</strong> Records are compared against stored state to identify changes</li>
 *   <li><strong>Message Publishing:</strong> Records are published to output topics (all records and changes only)</li>
 * </ol>
 * 
 * <h2>Key Features</h2>
 * 
 * <ul>
 *   <li><strong>Exactly-Once Processing:</strong> Ensures data consistency and prevents duplicates</li>
 *   <li><strong>Change Detection:</strong> Tracks record state to emit only meaningful updates</li>
 *   <li><strong>Fault Tolerance:</strong> Handles network issues, missing files, and malformed data gracefully</li>
 *   <li><strong>Scalability:</strong> Kafka Streams provides horizontal scaling and load distribution</li>
 *   <li><strong>Observability:</strong> Comprehensive logging and monitoring capabilities</li>
 * </ul>
 * 
 * <h2>Configuration</h2>
 * 
 * <p>The application is configured primarily through environment variables:
 * 
 * <h3>Required Environment Variables:</h3>
 * <ul>
 *   <li>{@code COS_ENDPOINT} - S3/COS service endpoint URL</li>
 *   <li>{@code COS_ACCESS_KEY_ID} - Access key for object storage authentication</li>
 *   <li>{@code COS_SECRET_ACCESS_KEY} - Secret key for object storage authentication</li>
 * </ul>
 * 
 * <h3>Optional Environment Variables:</h3>
 * <ul>
 *   <li>{@code INPUT_TOPIC} - Kafka input topic (default: local-input-topic)</li>
 *   <li>{@code OUTPUT_TOPIC} - Kafka output topic (default: local-stream-topic)</li>
 *   <li>{@code UPDATE_TOPIC} - Kafka updates topic (default: local-updates-only-topic)</li>
 *   <li>{@code STATE_TOPIC} - Kafka state topic (default: local-state-topic)</li>
 *   <li>{@code APP_ID} - Kafka Streams application ID (default: cos-csv-expander-app)</li>
 *   <li>{@code BOOTSTRAP_SERVERS} - Kafka bootstrap servers (default: localhost:9093)</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>
 * // Set required environment variables
 * export COS_ENDPOINT=https://s3.example.com
 * export COS_ACCESS_KEY_ID=your_access_key
 * export COS_SECRET_ACCESS_KEY=your_secret_key
 * 
 * // Run the application
 * java -jar csvprocessor-1.0.0-shaded.jar
 * </pre>
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 */
package jeroenfl.csvprocessor;
