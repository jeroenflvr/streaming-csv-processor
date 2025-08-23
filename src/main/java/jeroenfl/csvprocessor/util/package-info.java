/**
 * Utility classes and helper functions for common operations.
 * 
 * <p>This package provides utility classes that support common operations
 * throughout the application, including time handling, JSON processing,
 * and data transformation utilities.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenfl.csvprocessor.util.TimeUtils} - Time formatting and JSON timestamp extraction</li>
 * </ul>
 * 
 * <p>Utility capabilities:
 * <ul>
 *   <li><strong>Time Management:</strong> Consistent timestamp generation in UTC</li>
 *   <li><strong>JSON Processing:</strong> Safe extraction of values from JSON data</li>
 *   <li><strong>Error Tolerance:</strong> Defensive programming with graceful error handling</li>
 *   <li><strong>Thread Safety:</strong> All utilities are thread-safe for concurrent use</li>
 * </ul>
 * 
 * <p>The utility classes are designed to be stateless and thread-safe,
 * making them suitable for use in the concurrent Kafka Streams processing
 * environment.
 * 
 * @since 1.0.0
 */
package jeroenfl.csvprocessor.util;
