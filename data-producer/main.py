# producer.py
from confluent_kafka import Producer
import sys, time

conf = {
    "bootstrap.servers": "localhost:9093",
    "security.protocol": "SSL",
    "ssl.ca.location": "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt",
    "client.id": "py-producer",
    "enable.idempotence": True,
}

def delivery_report(err: str | None, msg: str | None) -> None:
    """
    Callback function to report the result of a Kafka message delivery.
    Args:
        err (str | None): Error message if delivery failed, else None.
        msg (str | None): Message metadata if delivery succeeded, else None.
    """
    if err is not None:
        print(f"delivery failed: {err}")
    else:
        print(f"record produced to {msg=}")
 

def kafka_producer(topic: str, message: str, key: str) -> None:
    """
    Produces a message to a Kafka topic using SSL configuration.
    Args:
        topic (str): Kafka topic to produce to.
        message (str): Message value to send.
        key (str): Key for the message.
    Reads Kafka configuration from environment variables.
    """

    import os
    cfg = {
        "bootstrap.servers": os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:9093"),
        "security.protocol": os.getenv("KAFKA_SECURITY_PROTOCOL", "SSL"),
        "ssl.ca.location": os.getenv("KAFKA_SSL_CA_LOCATION", "/Users/jeroen/projects/streaming-csv-processor/devops/redpanda/certs/ca.crt"),
        "client.id": os.getenv("KAFKA_CLIENT_ID", "py-producer"),
        "enable.idempotence": os.getenv("KAFKA_ENABLE_IDEMPOTENCE", "True"),
    }
    producer = Producer(cfg)
    producer.produce(
        topic=topic,
        key=key,
        value=message,
        callback=delivery_report
    )

    producer.flush(10)
 
def main() -> None:
    """
    Main entry point for the CSV Kafka producer.
    Reads lines from the orders file and sends each line as a message to Kafka.
    """
    print("Hello from csv-processor!")
    print("ok")
 
    orders_files = "orders_list_one"
    # orders_files = "orders_list"
 
    with open(orders_files, "r") as f:
        for line in f.readlines():
            s = line.strip()
            print(f"{s=}")
 
            kafka_producer(
                "local-input-topic",
                s,
                "orders",
            )
 
if __name__ == "__main__":
    main()