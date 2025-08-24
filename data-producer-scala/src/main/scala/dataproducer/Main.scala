package dataproducer

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, Callback, RecordMetadata}
import java.io.File
import java.util.Properties

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello from csv-processor!")
    println("ok")

    val ordersFile = "orders_list_one" // or "orders_list"
    for (line <- scala.io.Source.fromFile(ordersFile).getLines()) {
      val s = line.trim
      println(s"s=$s")
      kafkaProducer("local-input-topic", s, "orders")
    }
  }

  def kafkaProducer(topic: String, message: String, key: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9093"))
    props.put("security.protocol", sys.env.getOrElse("KAFKA_SECURITY_PROTOCOL", "SSL"))
    props.put("ssl.ca.location", sys.env.getOrElse("KAFKA_SSL_CA_LOCATION", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt"))
    props.put("client.id", sys.env.getOrElse("KAFKA_CLIENT_ID", "scala-producer"))
    props.put("enable.idempotence", sys.env.getOrElse("KAFKA_ENABLE_IDEMPOTENCE", "true"))
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("ssl.endpoint.identification.algorithm", "")
    props.put("ssl.truststore.location", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/truststore.jks")
    props.put("ssl.truststore.password", "changeit")
    val producer = new KafkaProducer[String, String](props)
    val record = new ProducerRecord[String, String](topic, key, message)
    producer.send(record, new DeliveryReportCallback)
    producer.flush()
    producer.close()
  }

  class DeliveryReportCallback extends Callback {
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      if (exception != null) {
        println(s"delivery failed: ${exception.getMessage}")
      } else {
        println(s"record produced to topic=${metadata.topic} partition=${metadata.partition} offset=${metadata.offset}")
      }
    }
  }
}
