package jeroenflvr.csvprocessor.api;

import jeroenflvr.csvprocessor.kafka.FailedMessageProducer;
import jeroenflvr.csvprocessor.kafka.FailedMessageConsumer;
import jeroenflvr.csvprocessor.model.FailedMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FailedMessagesController {
    @Autowired
    private FailedMessageProducer failedMessageProducer;
    @Autowired
    private FailedMessageConsumer failedMessageConsumer;

    @GetMapping("/failed-messages")
    public List<FailedMessage> getFailedMessages() {
        List<ConsumerRecord<String, String>> records = failedMessageConsumer.getAllFailedMessages();
        return records.stream()
                .map(r -> new FailedMessage(r.key(), r.value(), null))
                .collect(Collectors.toList());
    }

    @PostMapping("/retry-failed")
    public String retryFailedMessages() {
        List<ConsumerRecord<String, String>> records = failedMessageConsumer.getAllFailedMessages();
        String inputTopic = System.getenv().getOrDefault("INPUT_TOPIC", "local-input-topic");
        int count = 0;
        for (ConsumerRecord<String, String> record : records) {
            failedMessageProducer.send(inputTopic, record.key(), record.value());
            count++;
        }
        return "Retried " + count + " failed messages.";
    }
}
