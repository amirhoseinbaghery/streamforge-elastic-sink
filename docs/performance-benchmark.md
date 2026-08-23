# Performance benchmark reference

## Methodology

A valid comparison between direct execution, Docker, and Kubernetes must hold
constant the Kafka version, Elasticsearch version, connector build, dataset,
message size, topic/partition model, workload duration, converters, and
resource limits. Capture records/sec, MB/sec, CPU, memory, latency, error rate,
Kafka lag, Elasticsearch rejections, and recovery behavior. Clean the test
indices and topics between runs.

## Measurements present in this repository

The local historical benchmark record contains:

- A separate Logstash test at 500 RPS for 300 seconds passed and 750 RPS failed;
  this is not a measurement of this connector.
- A separate Confluent Elasticsearch Sink test at approximately 749.53 RPS for
  300 seconds passed; this is not a measurement of this connector.
- The custom connector's evidence-transport-correction run at 749.453 RPS for
  300 seconds passed with 224,836 exact actual-population records, zero
  producer failures, exact Kafka/Elasticsearch identity, and final lag zero.
- The earlier custom 1000-RPS run is retained as a data-integrity failure after
  one unexpected identity; it is not a capacity result.

No complete controlled A/B dataset comparing this connector in direct, Docker,
and Kubernetes environments is present in this repository. Throughput, MB/sec,
CPU, memory, latency, and recovery values for those three environments are
therefore **not available**, not estimated.

## Practical interpretation

- Direct execution is useful for isolating connector and client behavior when
  Kafka Connect and Elasticsearch versions remain fixed.
- Docker is useful for reproducible packaging and dependency isolation; it does
  not by itself prove a performance difference.
- Kubernetes is justified when the operator needs source-backed rollout,
  health management, scheduling, and observability; it requires a separately
  controlled resource and network baseline.

No benchmark in this document authorizes production traffic, crawler
activation, tuning, or a release-performance claim.
