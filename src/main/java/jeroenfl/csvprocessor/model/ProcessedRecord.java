package jeroenfl.csvprocessor.model;

import java.util.Map;

/**
 * Represents a processed CSV record with its composite key and data.
 */
public class ProcessedRecord {
    private final String compositeKey;
    private final Map<String, String> data;
    private final String sourceFilename;
    private final String importTimestamp;

    public ProcessedRecord(String compositeKey, Map<String, String> data, 
                          String sourceFilename, String importTimestamp) {
        this.compositeKey = compositeKey;
        this.data = data;
        this.sourceFilename = sourceFilename;
        this.importTimestamp = importTimestamp;
    }

    public String getCompositeKey() {
        return compositeKey;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public String getImportTimestamp() {
        return importTimestamp;
    }
}
