# Project Overview

Below is an ASCII art flowchart illustrating the main phases of the streaming CSV processor pipeline:

```
                  +-------------------------------+
                  | Kafka Topic: Instructions     |
                  | (S3 CSV links)                |
                  +---------------+---------------+
                                  |
                                  v
                  +---------------+---------------+
                  |   Read CSV from S3            |
                  +---------------+---------------+
                                  |
                                  v
                  +---------------+----------------+
                  |   Cleanup CSV (remove trailing |
                  |   empty column)                |
                  +---------------+----------------+
                                  |
                                  v
                  +---------------+----------------+
                  |   Transform to JSON record     |
                  |   + Add timestamp, filename    |
                  +---------------+----------------+
                                  |
                                  v
+-------------------+   +-------------------+   +-------------------+
|   Write to KStream|   |   Write to KTable |   |   Call API (todo) |
|   (append)        |   |   (upsert/state)  |   +-------------------+
+-------------------+   +-------------------+             |
          |                       |                       |
          v                       v                       v
+-------------------+   +-------------------+   +-------------------+
|   Sink to DB      |   | Sink to Delta     |   |      ...          |
|   (todo)          |   | Table on S3 (todo)|   +-------------------+
+-------------------+   +-------------------+

```

**Legend:**
- [x] = Implemented
- [ ] = Planned / To Do

This flow represents the journey from receiving instructions via Kafka, processing CSV data from S3, transforming and enriching records, streaming results, and future plans for database, delta table, and API integration.
