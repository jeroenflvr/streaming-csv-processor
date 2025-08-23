package jeroenfl.csvprocessor.processing;

import jeroenfl.csvprocessor.model.ProcessedRecord;
import jeroenfl.csvprocessor.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Processor for CSV data with semicolon delimiters and composite key generation.
 * 
 * <p>This class handles the parsing and transformation of CSV files with specific
 * business logic for order data processing. It generates composite keys from
 * multiple CSV columns and creates structured records suitable for downstream
 * processing.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Semicolon-delimited CSV parsing</li>
 *   <li>BOM (Byte Order Mark) handling for UTF-8 files</li>
 *   <li>Composite key generation from order and customer IDs</li>
 *   <li>Automatic metadata injection (filename, timestamp)</li>
 *   <li>Trailing empty column cleanup</li>
 *   <li>Robust error handling for malformed data</li>
 * </ul>
 * 
 * <p>The processor expects CSV files with at least the following columns:
 * {@code o_orderkey} and {@code o_custkey} for composite key generation.
 * 
 * @author JeroenFL
 * @version 1.0.0
 * @since 1.0.0
 * @see ProcessedRecord
 * @see TimeUtils
 */
public class CsvProcessor {
    private static final Logger log = LoggerFactory.getLogger(CsvProcessor.class);
    
    /**
     * Default constructor.
     * <p>Creates a new CSV processor instance ready for processing semicolon-delimited CSV files.
     * The processor handles BOM detection, header validation, and composite key generation.
     */
    public CsvProcessor() {
        // Default constructor for CSV processor
    }
    
    /**
     * Column names used to generate composite keys for order records.
     */
    private static final String[] ORDER_KEY_HEADERS = {"o_orderkey", "o_custkey"};
    
    /**
     * Legacy key index maintained for compatibility (currently unused in composite key logic).
     */
    private static final int KEY_INDEX = 3;

    /**
     * Processes CSV data from a BufferedReader and returns processed records.
     * 
     * <p>This method performs a complete CSV processing workflow:
     * <ol>
     *   <li>Reads and parses the CSV header line</li>
     *   <li>Validates the presence of required columns</li>
     *   <li>Processes each data row to create ProcessedRecord instances</li>
     *   <li>Generates composite keys from order and customer ID columns</li>
     *   <li>Adds metadata fields (source filename, import timestamp)</li>
     * </ol>
     * 
     * <p>The method handles various edge cases including:
     * <ul>
     *   <li>Empty files</li>
     *   <li>BOM markers in UTF-8 files</li>
     *   <li>Trailing empty columns from semicolon-terminated lines</li>
     *   <li>Missing or incomplete data rows</li>
     * </ul>
     * 
     * @param reader the BufferedReader containing CSV data (must not be null)
     * @param sourceFilename the name/path of the source file for metadata (must not be null)
     * @return a list of ProcessedRecord instances, may be empty if no valid data is found
     * @throws IOException if an error occurs reading from the BufferedReader
     * @throws NullPointerException if reader or sourceFilename is null
     * 
     * <p>Example usage:
     * <pre>
     * try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
     *     List&lt;ProcessedRecord&gt; records = processor.processCSV(reader, "orders.csv");
     *     for (ProcessedRecord record : records) {
     *         System.out.println("Key: " + record.getCompositeKey());
     *     }
     * }
     * </pre>
     */
    public List<ProcessedRecord> processCSV(BufferedReader reader, String sourceFilename) throws IOException {
        List<ProcessedRecord> records = new ArrayList<>();
        
        // Read and process header
        String headerLine = reader.readLine();
        if (headerLine == null) {
            log.warn("File {} is empty", sourceFilename);
            return records;
        }
        
        List<String> headers = parseHeaders(headerLine);
        if (headers.size() <= KEY_INDEX) {
            log.warn("Header has only {} columns (need at least {}). Skipping {}", 
                    headers.size(), KEY_INDEX + 1, sourceFilename);
            return records;
        }
        
        List<Integer> keyHeaderIndices = findKeyHeaderIndices(headers);
        
        // Process data rows
        String line;
        int count = 0;
        String currentTimestamp = TimeUtils.getCurrentTimestamp();
        
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            
            ProcessedRecord record = processRow(line, headers, keyHeaderIndices, 
                                              sourceFilename, currentTimestamp);
            if (record != null) {
                records.add(record);
                count++;
            }
        }
        
        log.info("Processed {} records from {}", count, sourceFilename);
        return records;
    }

    /**
     * Parses the CSV header line and normalizes column names.
     * 
     * <p>This method handles several header-specific concerns:
     * <ul>
     *   <li>Removes BOM (Byte Order Mark) if present</li>
     *   <li>Splits on semicolon delimiter</li>
     *   <li>Removes trailing empty columns caused by terminal semicolons</li>
     * </ul>
     * 
     * @param headerLine the raw header line from the CSV file
     * @return a list of cleaned column names
     */
    private List<String> parseHeaders(String headerLine) {
        // Strip BOM if present
        if (!headerLine.isEmpty() && headerLine.charAt(0) == '\uFEFF') {
            headerLine = headerLine.substring(1);
        }
        
        // Split headers and drop trailing empty headers caused by trailing ';'
        List<String> headers = new ArrayList<>(Arrays.asList(headerLine.split(";", -1)));
        while (!headers.isEmpty() && headers.get(headers.size() - 1).isBlank()) {
            headers.remove(headers.size() - 1);
        }
        
        return headers;
    }

    /**
     * Finds the column indices for the headers used in composite key generation.
     * 
     * <p>This method searches for the predefined key headers ({@code o_orderkey}
     * and {@code o_custkey}) in the provided header list and returns their
     * positions for efficient access during row processing.
     * 
     * @param headers the list of column headers from the CSV file
     * @return a list of indices corresponding to key header positions
     */
    private List<Integer> findKeyHeaderIndices(List<String> headers) {
        List<Integer> indices = new ArrayList<>();
        for (String keyHeader : ORDER_KEY_HEADERS) {
            int index = headers.indexOf(keyHeader);
            if (index >= 0) {
                indices.add(index);
            }
        }
        return indices;
    }

    /**
     * Processes a single CSV data row into a ProcessedRecord.
     * 
     * <p>This method handles the transformation of a raw CSV line into a
     * structured ProcessedRecord. It performs the following operations:
     * <ol>
     *   <li>Splits the line on semicolon delimiters</li>
     *   <li>Validates row completeness</li>
     *   <li>Generates a composite key from designated columns</li>
     *   <li>Creates a data map excluding composite key columns</li>
     *   <li>Adds metadata fields (filename, timestamp)</li>
     * </ol>
     * 
     * @param line the raw CSV line to process
     * @param headers the list of column headers
     * @param keyHeaderIndices the indices of columns used for composite key generation
     * @param sourceFilename the source filename for metadata
     * @param timestamp the processing timestamp
     * @return a ProcessedRecord instance, or null if the row is invalid
     */
    private ProcessedRecord processRow(String line, List<String> headers, 
                                     List<Integer> keyHeaderIndices, 
                                     String sourceFilename, String timestamp) {
        String[] parts = line.split(";", -1); // keep trailing empties
        if (parts.length <= KEY_INDEX) {
            return null;
        }
        
        // Create composite key
        List<String> compositeValues = new ArrayList<>();
        for (int index : keyHeaderIndices) {
            if (index < parts.length) {
                compositeValues.add(parts[index]);
            }
        }
        
        String compositeKey = String.join("_", compositeValues);
        if (compositeKey.isBlank()) {
            return null;
        }
        
        // Create value map (include all columns except composite key columns to avoid duplication)
        Map<String, String> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (header == null || header.isBlank()) continue;
            
            // Skip the columns used for composite key to avoid duplication
            if (Arrays.asList(ORDER_KEY_HEADERS).contains(header)) continue;
            
            String value = (i < parts.length) ? parts[i] : "";
            valueMap.put(header, value);
        }
        
        valueMap.put("input_filename", sourceFilename);
        valueMap.put("imported", timestamp);
        
        return new ProcessedRecord(compositeKey, valueMap, sourceFilename, timestamp);
    }
}
