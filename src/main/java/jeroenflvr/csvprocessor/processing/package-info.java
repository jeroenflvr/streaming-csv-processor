/**
 * CSV processing and business logic components.
 * 
 * <p>This package contains the core business logic for processing CSV files,
 * including parsing, transformation, and record generation. It handles the
 * conversion of raw CSV data into structured records suitable for downstream
 * processing.
 * 
 * <p>Key classes:
 * <ul>
 *   <li>{@link jeroenflvr.csvprocessor.processing.CsvProcessor} - Core CSV parsing and transformation logic</li>
 *   <li>{@link jeroenflvr.csvprocessor.processing.FileProcessingService} - High-level file processing orchestration</li>
 * </ul>
 * 
 * <p>Processing capabilities:
 * <ul>
 *   <li><strong>CSV Parsing:</strong> Semicolon-delimited CSV with BOM handling</li>
 *   <li><strong>Key Generation:</strong> Composite keys from multiple business columns</li>
 *   <li><strong>Data Transformation:</strong> Field mapping and metadata injection</li>
 *   <li><strong>Error Handling:</strong> Graceful handling of malformed data</li>
 *   <li><strong>Metadata Addition:</strong> Source tracking and timestamp injection</li>
 * </ul>
 * 
 * <p>The processing pipeline is designed to be resilient and handle various
 * edge cases commonly found in real-world CSV data, including missing fields,
 * empty lines, and encoding issues.
 * 
 * @since 1.0.0
 */
package jeroenflvr.csvprocessor.processing;
