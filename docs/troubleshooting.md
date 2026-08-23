# Troubleshooting

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
