package jeroenflvr.csvprocessor.processing;

import jeroenflvr.csvprocessor.kafka.FailedMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessingErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ProcessingErrorHandler.class);

    @Autowired
    private FailedMessageProducer failedMessageProducer;

    public void handleProcessingError(String pathValue, Exception e) {
        log.error("Failed to process file {}: {}", pathValue, e.getMessage(), e);
        String errorJson = String.format("{\"error\":\"%s\"}", e.getMessage().replaceAll("\"", "\\\""));
        failedMessageProducer.send(System.getenv().getOrDefault("FAILED_MESSAGES_TOPIC", "failed-messages"), pathValue, errorJson);
    }
}
