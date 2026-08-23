# StreamForge Elasticsearch Sink v1.0.0

## Release description

Version v1.0.0 is the first public release of StreamForge Elasticsearch Sink.

This release publishes the existing connector implementation together with
documentation, deployment examples, configuration references, and operational
guides.

The release does not introduce behavioral changes, dependency upgrades, or
architecture changes compared with the validated implementation.

## Included capabilities

- Kafka Connect sink lifecycle integration.
- Elasticsearch 9.x client and major-version validation.
- TLS and external credential-file support.
- Deterministic document IDs and allow-listed topic/index mappings.
- Bounded synchronous bulk writes and bounded retry behavior.
- Partition-contiguous offset handling.
- Explicit tombstone, permanent-error, and optional DLQ paths.

## Known limitations

- The build currently preserves the `ir.datapie.connect` package and
  `datapie-elasticsearch-sink` artifact directory for production reproducibility.
- The current connector implementation returns one task configuration; a
  higher `tasks.max` value is not a demonstrated parallelism guarantee.
- This repository does not contain a complete direct-vs-Docker-vs-Kubernetes
  performance A/B dataset.
- Integration, outage-recovery, and long-duration production validation remain
  deployment-specific responsibilities.
- Elasticsearch mappings, templates, ILM, and cluster settings are outside the
  connector's scope.

## Compatibility

The preserved build targets Java 17, Kafka Connect 4.0.x, and Elasticsearch 9.x
with Elasticsearch client dependencies pinned at 9.1.10. Validate any other
combination before use.
