# Configuration reference

The values below are defined by the connector configuration implementation or
its documented topic-prefix conventions. Defaults are implementation defaults,
not recommendations for every workload. Secret values must remain external.
See [configuration examples](configuration-examples.md) for complete profiles.

| Property | Type / default | Required | Example | Explanation; performance and operational notes |
|---|---|---|---|---|
| `elasticsearch.url` | string; none | yes | `https://es.example:9200` | Endpoint. Network and TLS latency affect every write. |
| `elasticsearch.username` | string; empty | no | `${file:...:username}` | Inline/provider username fallback. Prefer external providers; a username file takes precedence. |
| `elasticsearch.password` | password; empty | no | `${file:...:password}` | Inline/provider password fallback. Never commit a value; a password file takes precedence. |
| `elasticsearch.username.file` | string; empty | no | `/run/secrets/es-user` | File containing one username. It is read and trimmed at task startup; the path must exist inside the worker. |
| `elasticsearch.password.file` | string; empty | no | `/run/secrets/es-pass` | File containing one password. Protect ownership and permissions; the path is inside the worker, not the host. |
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

## Kafka Connect worker properties

`name`, `connector.class`, `tasks.max`, `topics`, converters, and Kafka Connect
DLQ settings are standard Kafka Connect properties rather than connector-defined
properties. The connector currently returns one task configuration, so
`tasks.max` above `1` does not increase parallelism.

## Frequent configuration mistakes

- A topic suffix must match exactly: records from `orders` require
  `index.mapping.orders`, not a general `index.mapping` property.
- Index values must begin with a lowercase letter or digit and contain only
  lowercase letters, digits, `.`, `_`, or `-`, up to 255 characters.
- `record-key` requires a non-null, non-empty key. `record-field` requires a
  matching `document.id.field.<topic>` and a non-null field in every record.
- `pending.max.records` must be at least `bulk.max.operations`.
- `retry.max.backoff.ms` must not be lower than
  `retry.initial.backoff.ms`.
- File paths and CA paths are resolved inside the Kafka Connect process or
  container.

## Compatibility identifiers

The package namespace and plugin directory name are preserved from the original
validated implementation to maintain compatibility with existing deployments.
Use the documented connector class exactly; the StreamForge project name does
not change its Java namespace.
