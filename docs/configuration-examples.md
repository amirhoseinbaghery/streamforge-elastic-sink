# Configuration examples

These profiles are starting points, not universal sizing recommendations. They
use connector-defined properties plus standard Kafka Connect properties. Keep
credentials outside connector JSON and benchmark changes with representative
records before production rollout.

## Development profile

Use this profile with a local, unauthenticated Elasticsearch 9.x node and a
small disposable topic:

```json
{
  "connector.class": "ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector",
  "tasks.max": "1",
  "topics": "events",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "false",
  "elasticsearch.url": "http://localhost:9200",
  "index.mapping.events": "events-dev",
  "document.id.strategy": "record-key",
  "operation.events": "UPSERT",
  "errors.mode": "fail",
  "bulk.max.operations": "50",
  "bulk.max.bytes": "1048576",
  "pending.max.records": "200",
  "pending.max.bytes": "4194304"
}
```

The smaller bounds make behavior easier to observe. Records must have non-empty
keys because `record-key` is the selected ID strategy.

## Production-oriented profile

This profile shows TLS, mounted credential files, bounded retries, and explicit
record handling. Adjust paths and values to the reviewed deployment:

```json
{
  "connector.class": "ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector",
  "tasks.max": "1",
  "topics": "orders",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "false",
  "elasticsearch.url": "https://elasticsearch.example:9200",
  "elasticsearch.expected.major": "9",
  "elasticsearch.ca.cert.path": "/run/tls/es-ca.crt",
  "elasticsearch.username.file": "/run/secrets/es-username",
  "elasticsearch.password.file": "/run/secrets/es-password",
  "index.mapping.orders": "orders-v1",
  "document.id.strategy": "record-key",
  "ordering.mode": "partition",
  "operation.orders": "UPSERT",
  "errors.mode": "fail",
  "tombstone.mode": "delete",
  "bulk.max.operations": "200",
  "bulk.max.bytes": "5242880",
  "pending.max.records": "2000",
  "pending.max.bytes": "33554432",
  "record.max.bytes": "5242880",
  "retry.max.attempts": "5",
  "retry.initial.backoff.ms": "250",
  "retry.max.backoff.ms": "10000",
  "retry.total.timeout.ms": "60000"
}
```

The Elasticsearch principal needs permission for the configured index and
operation. `tombstone.mode=delete` also requires every tombstone to carry a
usable record key.

## Higher-throughput evaluation profile

Use this only for a controlled load test. It raises request and pending-data
bounds, which increases memory use and Elasticsearch request size:

```json
{
  "connector.class": "ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector",
  "tasks.max": "1",
  "topics": "events-high-volume",
  "key.converter": "org.apache.kafka.connect.storage.StringConverter",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "value.converter.schemas.enable": "false",
  "elasticsearch.url": "https://elasticsearch.example:9200",
  "elasticsearch.ca.cert.path": "/run/tls/es-ca.crt",
  "elasticsearch.username.file": "/run/secrets/es-username",
  "elasticsearch.password.file": "/run/secrets/es-password",
  "index.mapping.events-high-volume": "events-high-volume-v1",
  "document.id.strategy": "record-key",
  "operation.events-high-volume": "UPSERT",
  "errors.mode": "fail",
  "bulk.max.operations": "1000",
  "bulk.max.bytes": "16777216",
  "bulk.max.concurrent.requests": "1",
  "pending.max.records": "4000",
  "pending.max.bytes": "67108864",
  "record.max.bytes": "5242880",
  "retry.max.attempts": "5",
  "retry.initial.backoff.ms": "250",
  "retry.max.backoff.ms": "10000",
  "retry.total.timeout.ms": "60000"
}
```

The current implementation writes synchronously and creates only one task,
regardless of a higher `tasks.max`. Measure worker heap, Kafka lag,
Elasticsearch bulk latency, rejection rates, and recovery behavior before and
after changing these bounds. See the [benchmark reference](performance-benchmark.md)
for the measurements that are and are not available.
