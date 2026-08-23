# Configuration reference

The values below are defined by `DataPieElasticsearchSinkConfig` or by its
documented topic-prefix conventions. Defaults are implementation defaults and
are not recommendations for every workload. Secret values must remain external.

| Property | Type / default | Required | Example | Explanation; performance and operational notes |
|---|---|---|---|---|
| `elasticsearch.url` | string; none | yes | `https://es.example:9200` | Endpoint. Network and TLS latency affect every write. |
| `elasticsearch.username` | string; empty | no | `${file:...:username}` | Inline/provider username fallback. Prefer external providers. |
| `elasticsearch.password` | password; empty | no | `${file:...:password}` | Inline/provider password fallback. Never commit a value. |
| `elasticsearch.username.file` | string; empty | no | `/run/secrets/es-user` | File containing username. File I/O occurs at task startup. |
| `elasticsearch.password.file` | string; empty | no | `/run/secrets/es-pass` | File containing password. Protect ownership and permissions. |
| `elasticsearch.ca.cert.path` | string; empty | no | `/etc/tls/es-ca.crt` | Optional PEM CA. Enables a private trust root without disabling hostname verification. |
| `elasticsearch.expected.major` | int; `9` | no | `9` | Startup compatibility guard. A mismatch prevents task start. |
| `ordering.mode` | string; `partition` | no | `partition` | Only `partition` is accepted; ordering is partition-local, not global. |
| `document.id.strategy` | string; `record-key` | no | `record-field` | Selects deterministic key or field IDs. ID collisions affect updates. |
| `document.id.field.<topic>` | string; empty | conditional | `document.id.field.events=id` | Required for `record-field`; extraction work is per record. |
| `tombstone.mode` | enum; `ignore` | no | `ignore` | `ignore`, `delete`, or `dlq`; controls null-valued records. |
| `errors.mode` | enum; `fail` | no | `fail` | Permanent errors fail or use the Connect errant-record reporter. |
| `bulk.max.operations` | int; `200` | no | `200` | Maximum operations per request; larger batches use more memory and ES request time. |
| `bulk.max.bytes` | long; `5242880` | no | `5242880` | Maximum encoded request bytes; controls request size and memory. |
| `bulk.flush.interval.ms` | long; `1000` | no | `1000` | Declared flush interval; batch completion remains synchronous in the current task. |
| `bulk.max.concurrent.requests` | int; `1` | no | `1` | In-flight request bound; current writer is synchronous. |
| `pending.max.records` | int; `2000` | no | `2000` | Pending-record safety bound; reaching it fails the batch for Connect retry. |
| `pending.max.bytes` | long; `33554432` | no | `33554432` | Pending-byte safety bound; protects task memory. |
| `record.max.bytes` | long; `5242880` | no | `5242880` | Oversize records follow permanent-error policy. |
| `retry.max.attempts` | int; `5` | no | `5` | Maximum retry attempts for retryable failures. |
| `retry.initial.backoff.ms` | long; `250` | no | `250` | Initial exponential backoff. |
| `retry.max.backoff.ms` | long; `10000` | no | `10000` | Backoff ceiling; must not be below initial backoff. |
| `retry.total.timeout.ms` | long; `60000` | no | `60000` | Total retry time bound per bulk write. |
| `flush.timeout.ms` | long; `60000` | no | `60000` | Connector configuration bound retained for compatibility. |
| `shutdown.timeout.ms` | long; `60000` | no | `60000` | Connector shutdown bound retained for compatibility. |
| `index.mapping.<topic>` | string; none | yes, at least one | `index.mapping.events=events-v1` | Allow-listed topic-to-index mapping; no payload-controlled index names. |
| `operation.<topic>` | enum; `UPSERT` | no | `operation.events=UPSERT` | Per-topic operation: `INDEX`, `CREATE`, `UPSERT`, or `DELETE`. |

## Minimal example

```properties
connector.class=ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector
topics=events
elasticsearch.url=https://es.example:9200
elasticsearch.ca.cert.path=/run/tls/es-ca.crt
elasticsearch.username.file=/run/secrets/es-user
elasticsearch.password.file=/run/secrets/es-pass
index.mapping.events=events-v1
document.id.strategy=record-key
ordering.mode=partition
operation.events=UPSERT
errors.mode=fail
```

The example intentionally contains no credentials. Validate the complete
configuration through Kafka Connect before sending records.
