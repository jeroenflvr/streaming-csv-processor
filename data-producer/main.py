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

def delivery_report(err, msg):
    if err is not None:
        print(f"delivery failed: {err}")
    else:
        print(f"record produced to {msg=}")
 

def kafka_producer(topic: str, message: str, key: str):
 
    cfg = {
        "bootstrap.servers": "localhost:9093",
        "security.protocol": "SSL",
        "ssl.ca.location": "/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt",
        "security.protocol": "SSL",
    }
    producer = Producer(cfg)
    producer.produce(
        topic=topic,
        key=key,
        value=message,
        callback=delivery_report
    )
 
    producer.flush(10)
 
def main():
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