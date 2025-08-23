package jeroenfl.csvprocessor.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jeroenfl.csvprocessor.model.ProcessedRecord;
import jeroenfl.csvprocessor.model.S3Location;
import jeroenfl.csvprocessor.storage.S3FileReader;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that orchestrates the processing of S3 files into Kafka records.
 */
public class FileProcessingService {
    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);
    
    private final S3FileReader s3FileReader;
    private final CsvProcessor csvProcessor;
    private final ObjectMapper objectMapper;

    public FileProcessingService(S3FileReader s3FileReader) {
        this.s3FileReader = s3FileReader;
        this.csvProcessor = new CsvProcessor();
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Processes a file path value and returns list of KeyValue pairs for Kafka.
     */
    public List<KeyValue<String, String>> processFile(String pathValue) {
        List<KeyValue<String, String>> result = new ArrayList<>();
        
        if (pathValue == null || pathValue.isBlank()) {
            return result;
        }

        try {
            S3Location location = S3Location.parse(pathValue.trim());
            
            try (BufferedReader reader = s3FileReader.createReader(location)) {
                List<ProcessedRecord> records = csvProcessor.processCSV(reader, pathValue.trim());
                
                for (ProcessedRecord record : records) {
                    String json = objectMapper.writeValueAsString(record.getData());
                    result.add(KeyValue.pair(record.getCompositeKey(), json));
                }
                
            }
        } catch (Exception e) {
            log.error("Failed to process file {}: {}", pathValue, e.getMessage(), e);
        }

        return result;
    }
}
