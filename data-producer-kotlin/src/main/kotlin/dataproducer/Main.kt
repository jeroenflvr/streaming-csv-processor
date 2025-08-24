package dataproducer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.RecordMetadata
import java.io.File
import java.util.Properties

/**
 * Kafka CSV Producer in Kotlin
 * Reads lines from a file and produces them to a Kafka topic.
 */
object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello from csv-processor!")
        println("ok")

        val ordersFile = "orders_list_one" // or "orders_list"
        File(ordersFile).useLines { lines ->
            lines.forEach { line ->
                val s = line.trim()
                println("s=$s")
                kafkaProducer("local-input-topic", s, "orders")
            }
        }
    }

    /**
     * Produces a message to a Kafka topic using SSL configuration.
     * Reads Kafka configuration from environment variables.
     */
    fun kafkaProducer(topic: String, message: String, key: String) {
        val props = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9093")
            put("security.protocol", System.getenv("KAFKA_SECURITY_PROTOCOL") ?: "SSL")
            put("ssl.ca.location", System.getenv("KAFKA_SSL_CA_LOCATION") ?: "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt")
            put("client.id", System.getenv("KAFKA_CLIENT_ID") ?: "kotlin-producer")
            put("enable.idempotence", System.getenv("KAFKA_ENABLE_IDEMPOTENCE") ?: "true")
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("ssl.endpoint.identification.algorithm", "")
            put("ssl.truststore.location", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/truststore.jks")
            put("ssl.truststore.password", "changeit")
        }
        val producer = KafkaProducer<String, String>(props)
        val record = ProducerRecord(topic, key, message)
        producer.send(record, DeliveryReportCallback())
        producer.flush()
        producer.close()
    }

    /**
     * Callback for Kafka delivery report.
     */
    class DeliveryReportCallback : Callback {
        override fun onCompletion(metadata: RecordMetadata?, exception: Exception?) {
            if (exception != null) {
                println("delivery failed: ${exception.message}")
            } else {
                println("record produced to topic=${metadata?.topic()} partition=${metadata?.partition()} offset=${metadata?.offset()}")
            }
        }
    }
}
