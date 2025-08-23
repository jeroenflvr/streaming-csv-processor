# Kafka Streams Example

## Introduction

prereqs:

- [x] docker redpanda setup (SSL)
- [x] [akhq UI](https://akhq.io/) (beats the vscode/IntelliJ plugins) ([download](https://github.com/tchiotludo/akhq/releases/download/0.26.0/akhq-0.26.0-all.jar))
- [x] quick python producer



## Setup

### akhq

### redpanda / kafka

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