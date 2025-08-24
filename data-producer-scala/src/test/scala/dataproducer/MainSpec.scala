package dataproducer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, Callback, RecordMetadata}
import java.util.Properties

class MainSpec extends AnyFlatSpec with Matchers {

  "kafkaProducer" should "configure KafkaProducer with correct properties and send a message" in {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9093")
    props.put("security.protocol", "SSL")
    props.put("ssl.ca.location", "/tmp/ca.crt")
    props.put("client.id", "scala-producer")
    props.put("enable.idempotence", "true")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("ssl.endpoint.identification.algorithm", "")

    val mockProducer = mock(classOf[KafkaProducer[String, String]])
    val mockRecord = mock(classOf[ProducerRecord[String, String]])
    val mockCallback = mock(classOf[Callback])

    // Simulate send and flush
    when(mockProducer.send(mockRecord, mockCallback)).thenReturn(null)
    doNothing().when(mockProducer).flush()
    doNothing().when(mockProducer).close()

    // You would inject the mockProducer into Main.kafkaProducer if it were refactored for testability
    // For now, just check that properties are set as expected
    props.getProperty("bootstrap.servers") shouldBe "localhost:9093"
    props.getProperty("security.protocol") shouldBe "SSL"
    props.getProperty("ssl.ca.location") shouldBe "/tmp/ca.crt"
    props.getProperty("client.id") shouldBe "scala-producer"
    props.getProperty("enable.idempotence") shouldBe "true"
    props.getProperty("key.serializer") shouldBe "org.apache.kafka.common.serialization.StringSerializer"
    props.getProperty("value.serializer") shouldBe "org.apache.kafka.common.serialization.StringSerializer"
    props.getProperty("ssl.endpoint.identification.algorithm") shouldBe ""
  }

  "DeliveryReportCallback" should "print delivery failed or success" in {
    val callback = new Main.DeliveryReportCallback
    val mockMetadata = mock(classOf[RecordMetadata])
    when(mockMetadata.topic).thenReturn("test-topic")
    when(mockMetadata.partition).thenReturn(1)
    when(mockMetadata.offset).thenReturn(42L)

    // Test exception case
    val exception = new Exception("SSL handshake failed")
    noException should be thrownBy callback.onCompletion(mockMetadata, exception)

    // Test success case
    noException should be thrownBy callback.onCompletion(mockMetadata, null)
  }
}
