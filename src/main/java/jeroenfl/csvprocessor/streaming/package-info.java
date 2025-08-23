/**
 * Kafka Streams topology construction and stream processing logic.
 * 
 * <p>This package contains the components responsible for building and
 * configuring the Kafka Streams topology that processes CSV file messages.
 * It implements sophisticated stream processing patterns including stateful
 * operations and change detection.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenfl.csvprocessor.streaming.StreamsTopologyBuilder} - Builds the complete Kafka Streams topology</li>
 * </ul>
 * 
 * <p>Streaming features:
 * <ul>
 *   <li><strong>Stateful Processing:</strong> Maintains state for change detection and deduplication</li>
 *   <li><strong>Multi-Output:</strong> Routes data to different topics based on processing logic</li>
 *   <li><strong>Fault Tolerance:</strong> Leverages Kafka Streams built-in fault tolerance</li>
 *   <li><strong>Scalability:</strong> Horizontal scaling through partition assignment</li>
 *   <li><strong>Exactly-Once Semantics:</strong> Ensures data consistency across the pipeline</li>
 * </ul>
 * 
 * <p>The topology is designed to handle high-throughput scenarios while
 * maintaining data consistency and providing exactly-once processing guarantees.
 * State management ensures that only meaningful changes are propagated
 * downstream, reducing noise in the data pipeline.
 * 
 * @since 1.0.0
 */
package jeroenfl.csvprocessor.streaming;
