curl -s -X PUT http://localhost:8083/connectors/tpch-orders/config \
  -H 'Content-Type: application/json' \
  -d '{
  "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
  "name": "tpch-orders",
  "tasks.max": "1",

  "connection.url": "jdbc:duckdb:/data/tpch.duckdb",
  "mode": "incrementing",
  "incrementing.column.name": "o_orderkey",

  "table.whitelist": "orders",
  "topic.prefix": "tpch_",
  "poll.interval.ms": "10000",
  "batch.max.rows": "5000",
  "numeric.mapping": "best_fit",

  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "false",

  "transforms": "MakeKey,ExtractKey,RemoveKeyField",
  "transforms.MakeKey.type": "org.apache.kafka.connect.transforms.ValueToKey",
  "transforms.MakeKey.fields": "o_orderkey",
  "transforms.ExtractKey.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
  "transforms.ExtractKey.field": "o_orderkey",
  "transforms.RemoveKeyField.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
  "transforms.RemoveKeyField.blacklist": "o_orderkey"
}'
