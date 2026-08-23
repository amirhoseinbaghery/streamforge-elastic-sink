# Troubleshooting

Start with the Connect REST status, worker log, Elasticsearch health, and
Kafka lag. Collect connector name, task number, topic/partition, and sanitized
errors only; never include credentials or secret-bearing configuration.

## Connector does not start

**Possible causes:** invalid configuration, no `index.mapping.<topic>`, an
unreachable URL, or an Elasticsearch major-version mismatch.

**Solution:** call `/connector-plugins`, configuration validation, and the
connector `/status` endpoint. Confirm the URL is reachable from the worker and
compare `elasticsearch.expected.major` with the server. Find the first nested
cause in the worker log.

## Plugin is not discovered

**Possible cause:** only the jar was copied, the directory is not on
`plugin.path`, or the worker was not restarted.

**Solution:** install the entire `build/plugin/datapie-elasticsearch-sink`
directory, ensure it is readable, restart or roll the worker, and confirm the
connector class appears in `/connector-plugins`.

## Elasticsearch unavailable or TLS failures

Check DNS/routing from the Connect worker, the URL scheme, CA path, hostname,
and external credential references. The task validates the Elasticsearch major
version at startup. Do not disable hostname verification as a workaround.

## Kafka lag grows

Inspect task state, worker CPU/memory, Elasticsearch response latency, bulk
errors, retry counters, and pending bounds. A full pending bound causes a
Connect exception so Kafka Connect can retry the batch; it is not an invitation
to increase limits blindly.

## Authentication or authorization failure

Confirm the referenced username/password files exist in the worker, are
readable by the Connect process, and identify a principal with the required
index privileges. Never place the secret value in logs or a support bundle.

## DLQ problems

**Possible causes:** DLQ mode is enabled without an errant-record reporter, or
the reporter cannot publish to its topic.

**Solution:** configure and permission the worker's DLQ reporter, validate the
connector configuration, and inspect reporter producer errors. Use `fail` while
diagnosing if DLQ behavior is not required.

## Offset or duplicate concerns

**Possible cause:** a bulk request failed, a task restarted, or records were
acknowledged out of order.

**Solution:** do not manually advance offsets. The connector commits only
through contiguous acknowledged offsets, so replay after a failure is possible.
Use deterministic IDs and `UPSERT` when replay should update the same document.

## Mapping, ID, or operation errors

Verify every input topic has an `index.mapping.<topic>`, the selected ID strategy
has a key or field, and the operation is one of `INDEX`, `CREATE`, `UPSERT`, or
`DELETE`. Index names are intentionally restricted to the connector's
allow-listing pattern.

## Bulk failures and retry loops

HTTP 429/502/503/504 and connection/I/O failures are retried only within
`retry.max.attempts` and `retry.total.timeout.ms`. Permanent failures follow
`errors.mode`. Check Elasticsearch rejection and breaker metrics before changing
bulk bounds.

## Memory pressure

Inspect pending records/bytes, request size, Connect heap, and Elasticsearch
heap separately. Reduce incoming load or stop the connector through the normal
control path; do not infer a safe resource change from one snapshot.
