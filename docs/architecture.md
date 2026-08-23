# Architecture

## Runtime path

Kafka Connect owns the consumer group, partition assignment, converter
pipeline, task lifecycle, rebalances, and committed offsets. A task creates one
Elasticsearch client and processes records in this order:

```text
SinkRecord
  -> receive and track partition offset
  -> resolve configured index, document ID, and operation
  -> map Connect values to JSON-friendly values
  -> enforce record, batch, and pending-data bounds
  -> write a synchronous Elasticsearch bulk request
  -> classify each item result
  -> acknowledge or report a terminal error
  -> expose only contiguous partition progress to preCommit
```

The connector does not implement a second Kafka consumer and does not use
Schema Registry directly. Converters remain Kafka Connect responsibilities.

## Connector and task lifecycle

`start` validates the configuration, creates the client, checks the configured
Elasticsearch major version, and initializes task state. `put` builds bounded
batches and waits for their synchronous completion. `stop` closes the client.
Kafka Connect controls task creation and the maximum task count; the current
connector returns one task configuration even when a larger maximum is offered.

## IDs, indexes, and operations

Index names come only from `index.mapping.<topic>` properties and must match the
connector's safe index-name pattern. IDs are derived from the record key by
default or from a configured field. Operations are configured per topic and
support `INDEX`, `CREATE`, `UPSERT`, and `DELETE`.

## Acknowledgement and offsets

Successful bulk items and configured DLQ terminal items become terminal for the
record. The partition tracker advances only across contiguous terminal offsets,
and commits use Kafka's offset-plus-one convention. An out-of-order success
cannot commit past a missing record.

## Retry and failure behavior

HTTP 429/502/503/504 and I/O/connection timeout failures are retryable within
the configured attempt and total-time bounds. Other item failures are
permanent. Permanent failures either fail the task (`errors.mode=fail`) or use
Kafka Connect's errant-record reporter (`errors.mode=dlq`). A missing reporter
when DLQ mode is requested is an error.

## Resource bounds

Bulk operations, request bytes, record bytes, pending records, pending bytes,
and concurrent requests are explicitly bounded by configuration. The current
default concurrent request bound is one. These are implementation facts, not a
claim of a universal production sizing recommendation.
