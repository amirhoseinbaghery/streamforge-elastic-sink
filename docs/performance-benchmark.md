# Performance benchmark reference

This page records the performance evidence available in the repository and its
limits. Results validate a specific run; they are not capacity promises for a
different Kafka, Connect, network, or Elasticsearch deployment.

## A. Connector validation benchmarks

The historical validation record contains one successful connector run:

- Target rate: `749.453` records/second
- Duration: `300` seconds
- Exact actual-population records: `224,836`
- Producer failures: `0`
- Kafka/Elasticsearch identity comparison: exact
- Final Kafka lag: `0`

This run demonstrates correctness under that recorded workload. It does not
establish maximum throughput, latency percentiles, CPU requirements, memory
requirements, or a generally safe production rate.

An earlier connector run targeting 1000 records/second found one unexpected
identity. It is retained as a data-integrity failure and must not be interpreted
as a successful capacity result.

## B. Deployment-specific benchmarks

The repository does not contain a controlled comparison of direct execution,
Docker, and Kubernetes. The following measurements are therefore unavailable:

- environment-by-environment records/second and MB/second
- end-to-end or bulk-request latency percentiles
- Kafka Connect and Elasticsearch CPU or memory usage
- recovery time after worker, broker, network, or Elasticsearch failures
- scaling behavior across task counts or topic partitions

A valid comparison must hold constant the connector build, Kafka and
Elasticsearch versions, dataset, record size, topic/partition model, converters,
duration, resource limits, and network path. Capture lag, rejection rates,
errors, recovery, CPU, memory, throughput, and latency for every run.

## C. Historical DataPie deployment context

DataPie is one real-world deployment of the connector, not a runtime dependency.
This repository does not contain a separate, complete benchmark dataset that
can be published as a DataPie production baseline. No DataPie throughput,
latency, resource, or capacity number is inferred here.

The historical record also mentions tests of other sink implementations:

- A separate Logstash test passed at 500 records/second for 300 seconds and
  failed at 750 records/second.
- A separate Confluent Elasticsearch Sink test passed at approximately 749.53
  records/second for 300 seconds.

Those are comparison context only. They were not runs of StreamForge
Elasticsearch Sink and do not establish relative product performance without a
controlled, reproducible test environment.

## How to benchmark your deployment

Use disposable topics and indices, define success criteria before starting,
and retain the exact connector configuration and image digest. Run a warm-up,
steady-state workload, and failure-recovery phase. Clean or uniquely name test
data between runs so identity checks cannot pass against old documents.

Do not use any number on this page as authorization for production traffic or
as a substitute for workload-specific testing.
