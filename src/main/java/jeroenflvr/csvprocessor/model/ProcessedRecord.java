package jeroenflvr.csvprocessor.model;

import java.util.Map;

/**
 * Immutable representation of a processed CSV record.
 * 
 * <p>This class encapsulates the result of processing a single CSV row,
 * including the composite key generated from multiple CSV columns,
 * the processed data fields, and metadata about the processing operation.
 * 
 * <p>The composite key is typically generated from business-significant
 * columns (e.g., order ID and customer ID) and is used for Kafka message
 * partitioning and state management.
 * 
 * <p>The data map contains all CSV columns except those used for the
 * composite key, plus additional metadata fields added during processing.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see jeroenflvr.csvprocessor.processing.CsvProcessor
 */
public class ProcessedRecord {
    private final String compositeKey;
    private final Map<String, String> data;
    private final String sourceFilename;
    private final String importTimestamp;

    /**
     * Constructs a new ProcessedRecord with the specified components.
     * 
     * @param compositeKey the composite key generated from CSV columns (must not be null)
     * @param data the processed data fields from the CSV row (must not be null)
     * @param sourceFilename the name/path of the source CSV file (must not be null)
     * @param importTimestamp the timestamp when the record was processed (must not be null)
     * @throws NullPointerException if any parameter is null
     */
    public ProcessedRecord(String compositeKey, Map<String, String> data, 
                          String sourceFilename, String importTimestamp) {
        this.compositeKey = compositeKey;
        this.data = data;
        this.sourceFilename = sourceFilename;
        this.importTimestamp = importTimestamp;
    }

    /**
     * Gets the composite key for this record.
     * 
     * <p>The composite key is typically formed by joining multiple
     * business-significant CSV columns with underscores (e.g., "123_456"
     * for order ID 123 and customer ID 456).
     * 
     * @return the composite key (never null)
     */
    public String getCompositeKey() {
        return compositeKey;
    }

    /**
     * Gets the processed data fields from the CSV row.
     * 
     * <p>This map contains all CSV columns except those used to generate
     * the composite key, plus additional metadata fields added during
     * processing (such as source filename and import timestamp).
     * 
     * @return an immutable view of the data fields (never null)
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * Gets the source filename where this record originated.
     * 
     * @return the source filename or path (never null)
     */
    public String getSourceFilename() {
        return sourceFilename;
    }

    /**
     * Gets the timestamp when this record was processed.
     * 
     * @return the import timestamp in YYYYMMddHHmmss format (never null)
     */
    public String getImportTimestamp() {
        return importTimestamp;
    }
}
