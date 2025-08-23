/**
 * Data models and value objects representing processed CSV data and S3 locations.
 * 
 * <p>This package contains immutable data classes that represent the core
 * business objects used throughout the CSV processing pipeline. These classes
 * provide type-safe representations of data and enforce consistency across
 * the application.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenfl.csvprocessor.model.ProcessedRecord} - Represents a processed CSV row with metadata</li>
 *   <li>{@link jeroenfl.csvprocessor.model.S3Location} - Represents an S3/COS object location</li>
 * </ul>
 * 
 * <p>Design principles:
 * <ul>
 *   <li><strong>Immutability:</strong> All model objects are immutable after construction</li>
 *   <li><strong>Validation:</strong> Constructor validation ensures data integrity</li>
 *   <li><strong>Encapsulation:</strong> Internal representation is hidden from clients</li>
 *   <li><strong>Value Semantics:</strong> Objects are compared by value, not identity</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package jeroenfl.csvprocessor.model;
