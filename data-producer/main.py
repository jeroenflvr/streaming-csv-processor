# producer.py
from confluent_kafka import Producer
import sys, time

conf = {
    "bootstrap.servers": "localhost:9093",
    "security.protocol": "SSL",
    "ssl.ca.location": "/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt",
    "client.id": "py-producer",
    "enable.idempotence": True,
}

topic = "my-topic"
p = Producer(conf)


def delivery_report(err, msg):
    (
        print(f"Delivery failed: {err}", file=sys.stderr)
        if err
        else print(f"Produced to {msg.topic()}[{msg.partition()}]@{msg.offset()}")
    )


for i in range(10):
    p.produce(topic, key=f"k-{i}", value=f"hello #{i}", callback=delivery_report)
    p.poll(0)
    time.sleep(0.05)

p.flush(10)
