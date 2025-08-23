package jeroenfl.csvprocessor.processing;

import jeroenfl.csvprocessor.model.ProcessedRecord;
import jeroenfl.csvprocessor.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Processor for CSV data with semicolon delimiters.
 */
public class CsvProcessor {
    private static final Logger log = LoggerFactory.getLogger(CsvProcessor.class);
    
    private static final String[] ORDER_KEY_HEADERS = {"o_orderkey", "o_custkey"};
    private static final int KEY_INDEX = 3; // Legacy key index for compatibility

    /**
     * Processes CSV data from a BufferedReader and returns processed records.
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
