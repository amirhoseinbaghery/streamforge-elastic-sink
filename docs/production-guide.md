# Production guide

## Before activation

Validate Kafka Connect plugin discovery, Elasticsearch major version, TLS
hostname verification, credential file permissions, index mappings, topic
partitions, and a non-production smoke record. Establish a baseline for worker
memory, Kafka lag, Elasticsearch heap/disk, bulk errors, retries, and task
restarts before accepting business traffic.

## Resource and monitoring guidance

Start with the implementation bounds rather than assuming a task count or
throughput target. Monitor Kafka consumer lag, task state, retry/error counts,
Elasticsearch bulk response failures, 429 responses, heap, disk, CPU, and
pending records/bytes. Treat `pending.max.records`, `pending.max.bytes`,
`bulk.max.bytes`, and `record.max.bytes` as safety controls.

## Upgrade strategy

Keep the existing build coordinates and dependency versions for the frozen
production artifact. Build a new version in an isolated environment, run unit
tests and a bounded integration smoke, inspect the generated plugin manifest,
then roll workers through the operator's source-backed process.

## Rollback and recovery

Freeze new producer traffic, preserve Kafka topics and offsets, pause or stop the
affected connector, restore the last known-good image and connector source,
verify task health and Elasticsearch identity, and resume only after lag and
duplicate checks pass. A failed bulk request must not be treated as an offset
acknowledgement. The connector's retry limits are bounded; persistent failures
should remain visible as task errors rather than trigger unbounded local work.


## Ownership and deployment responsibility

StreamForge Elasticsearch Sink is distributed as an independent connector.

Operational characteristics depend on:

- Kafka configuration
- Elasticsearch cluster design
- infrastructure resources
- workload pattern

The connector does not manage the Kafka or Elasticsearch clusters themselves.