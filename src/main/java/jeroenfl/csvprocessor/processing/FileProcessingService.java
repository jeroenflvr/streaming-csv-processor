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
 * High-level service that orchestrates the processing of S3 files into Kafka records.
 * 
 * <p>This service coordinates the entire file processing workflow, from S3 file
 * retrieval through CSV parsing to Kafka message preparation. It serves as the
 * primary integration point between storage, processing, and messaging components.
 * 
 * <p>Processing workflow:
 * <ol>
 *   <li>Parse the file path to extract S3 location information</li>
 *   <li>Retrieve the file content from S3 storage</li>
 *   <li>Process the CSV data using the CSV processor</li>
 *   <li>Convert processed records to JSON format</li>
 *   <li>Create Kafka KeyValue pairs for message publishing</li>
 * </ol>
 * 
 * <p>The service handles all error conditions gracefully, logging issues
 * and returning empty results rather than propagating exceptions to
 * the Kafka Streams processing pipeline.
 * 
 * @author CSV Processor Team
 * @version 1.0.0
 * @since 1.0.0
 * @see S3FileReader
 * @see CsvProcessor
 * @see ProcessedRecord
 */
public class FileProcessingService {
    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);
    
    private final S3FileReader s3FileReader;
    private final CsvProcessor csvProcessor;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new FileProcessingService with the specified S3 file reader.
     * 
     * <p>This constructor initializes all required components for file processing,
     * including the CSV processor and JSON object mapper with appropriate configuration.
     * 
     * @param s3FileReader the S3 file reader for retrieving file content (must not be null)
     * @throws NullPointerException if s3FileReader is null
     */
    public FileProcessingService(S3FileReader s3FileReader) {
        this.s3FileReader = s3FileReader;
        this.csvProcessor = new CsvProcessor();
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Processes a file path value and returns list of KeyValue pairs for Kafka.
     * 
     * <p>This method represents the complete file processing pipeline:
     * <ol>
     *   <li>Validates the input path</li>
     *   <li>Parses the S3 location from the path</li>
     *   <li>Retrieves and processes the CSV file</li>
     *   <li>Converts records to JSON format</li>
     *   <li>Creates Kafka message key-value pairs</li>
     * </ol>
     * 
     * <p>Error handling is comprehensive - any processing errors are logged
     * but do not interrupt the Kafka Streams processing flow. Empty results
     * are returned for invalid inputs or processing failures.
     * 
     * @param pathValue the S3 file path to process (may be null or empty)
     * @return a list of KeyValue pairs ready for Kafka publishing, never null but may be empty
     * 
     * @example
     * <pre>
     * List&lt;KeyValue&lt;String, String&gt;&gt; messages = 
     *     service.processFile("s3://my-bucket/orders.csv");
     * for (KeyValue&lt;String, String&gt; message : messages) {
     *     kafkaProducer.send(new ProducerRecord&lt;&gt;(topic, message.key, message.value));
     * }
     * </pre>
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
