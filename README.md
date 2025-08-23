# Streaming CSV Processor: A Kafka Streams Example

Triggered by a recent event, I realized that I had never documented any of this.

A lightweight real-world kafka streams example.  To be completed/extended.

Find the [java docs here](https://jeroenflvr.github.io/ks-example/api/)

- pipeline 1: read data from csv/parquet on s3, transform to json and sink to kafka topic
- pipeline 2 (kafka streams): read kafka data, enrich, sink to KStream (append) and KTable (upsert) topics
- pipeline 3: sink to postgresql, deltalake, web ticker (SSE), ...  Not sure yet if we'll be doing spark, flink, dbt, ...

This will cover a collection of technologies and programming languages.  I wanted something as a go-to reference with quick setup, that I could also use for Proof-Of-Concepts, demos, new tech integration tests, etc.

Tech stack:

- docker
- kafka
- kafka streams
- minio / aws s3
- redpanda
- python (uv, ..)
- java (mvn, graalvm, ..)
- git



## Introduction

prereqs:

Get java 17, python3, docker or docker desktop, rpk (redpanda cli), akhq, ...

- [x] docker redpanda setup (SSL)
- [x] docker minio (s3 store)
- [x] [akhq UI](https://akhq.io/) (beats the vscode/IntelliJ plugins) ([download](https://github.com/tchiotludo/akhq/releases/download/0.26.0/akhq-0.26.0-all.jar))
- [x] quick python producer

## Why Kafka Streams?

When building real-time applications, one of the key challenges is how to process, transform, and react to data as it flows through your system. Kafka Streams is designed specifically for this purpose, providing a simple yet powerful way to build stream processing logic directly into your applications.

### Key Advantages

- Lightweight and Library-Based

    Kafka Streams is not a separate cluster or external service. It’s just a client library you embed into your existing JVM applications. This keeps operations simple—no additional infrastructure to manage.

- Integration with Apache Kafka

    Because it's part of the Kafka ecosystem, Kafka Streams uses Kafka topics directly as input and output. This makes it naturally fault-tolerant, scalable, and easy to integrate with existing Kafka-based pipelines.

- Scalability and Elasticity

    Applications can scale horizontally just by adding more instances. Kafka Streams automatically rebalances work across instances using Kafka’s group coordination.

- Fault Tolerance

    Kafka Streams leverages Kafka’s durable storage for state and reprocessing. If an instance fails, state and processing tasks are reassigned, ensuring resilient processing without data loss.

- Exactly-Once Processing

    It supports exactly-once semantics (EOS), which ensures that every record is processed once—even in the face of retries, failures, or restarts.

- Stateful and Stateless Processing

    It supports both stateless operations (map, filter, merge) and stateful ones (aggregations, joins, windowed computations), making it versatile for many use cases.

- Production-Ready by Design

    Built-in features like monitoring, fault tolerance, and state management mean you can go from prototype to production without a major rewrite.

- Requires Java Developer Skills

    Kafka Streams is a Java library. To effectively design, develop, and maintain Kafka Streams applications, you’ll need Java (or Kotlin/Scala) programming skills. Teams without JVM experience may face a steeper learning curve compared to using SQL-based or no-code streaming tools.

### When to Use Kafka Streams

-   You already have Kafka and want to build real-time processing apps without deploying extra cluster frameworks (like Flink or Spark).
-   You need to enrich, aggregate, or join streaming data before serving it to downstream systems.
-   You want applications that are easy to scale out while remaining lightweight and cloud-friendly.
-   You have a team of Java developers who can directly implement and maintain stream processing logic.

## Setup



### akhq
...

### redpanda / kafka
...

### minio (s3)

Use [aws cli](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
Tip: for 3rd party s3 services (ibm, gcp, azure, ..), set these env vars:

```bash
export AWS_REQUEST_CHECKSUM_CALCULATION=WHEN_REQUIRED
export AWS_RESPONSE_CHECKSUM_CALCULATION=WHEN_REQUIRED
```

see [this](https://github.com/aws/aws-cli/issues/9214) for more details

aws config in ~/.aws/config and credentials in ~/.aws/credentials

You'll need path addressing style:
this: http://localhost/bucket/prefix/key
instead of this (virtual): http://bucket.localhost/prefix/key


~/.aws/config
```
[profile minio]
region = eu-west-3
output = json
services = minio-services
s3 =
    addressing_style = path

[services minio-services]
s3 =
  endpoint_url = http://localhost:9000
```

~/.aws/credentials
```
[minio]
aws_access_key_id = minioadmin
aws_secret_access_key = minioadmin123
```



## The Flow

For test data, we'll use the TPC-H stuff from [here](https://github.com/jeroenflvr/dbgen) and store to our minio bucket.

```bash
```

Get the duckdb JDBC jar
```
mvn dependency:get \
  -DgroupId=org.duckdb \
  -DartifactId=duckdb_jdbc \
  -Dversion=1.3.2.0
```

then copy it from cp ~/.m2/repository/org/duckdb/duckdb_jdbc/1.3.2.0/duckdb_jdbc-1.3.2.0.jar into your workdir for the kafka connector

dropping the duckdb jdbc connector here, too much hassle setting up kafka connect


## Compile and Run

std java

compile
```bash
mvn -e -DskipTests package
```

run
```bash
java -jar target/csvprocessor-1.0.0-shaded.jar
```

graalvm

compile using the pom.xml native profile
```bash
mvn -e -DskipTests -Pnative package
```

run
```bash
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
  -jar target/csvprocessor-1.0.0-shaded.jar
```

tip: when killing the app or it fails, remove the src/main/resources/META-INF/native-image/.lock