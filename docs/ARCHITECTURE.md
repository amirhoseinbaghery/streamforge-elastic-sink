# Architecture

`SinkRecord -> resolve index/id/operation -> encode -> bounded bulk -> item result -> ACK/DLQ -> contiguous preCommit`

Kafka Connect retains consumer-group, partition assignment, rebalance, offset
storage, task lifecycle and converter responsibilities. The connector does not
implement a second consumer or Schema Registry protocol.

One Elasticsearch client is created per task. Bulk writes are synchronous in the
initial bring-up so an acknowledged item is a safe offset terminal state. The
partition tracker prevents an out-of-order ACK from advancing a commit past a
missing offset.
