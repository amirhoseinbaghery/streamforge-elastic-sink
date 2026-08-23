# StreamForge Elasticsearch Sink

`streamforge-elastic-sink` is a Kafka Connect sink connector that writes Kafka
records to Elasticsearch 9.x using the official Elasticsearch Java API Client.
The repository preserves the existing production-tested implementation; this
release preparation does not change connector behavior, dependencies, or build
coordinates.

## Why this connector exists

Kafka Connect provides the consumer, task lifecycle, partition assignment, and
offset storage. This connector supplies a small, source-backed Elasticsearch
sink with deterministic IDs, allow-listed index mappings, bounded bulk requests,
bounded retries, and contiguous per-partition offset commits.

## Features

- Kafka Connect `SinkConnector` and `SinkTask` implementation.
- Elasticsearch 9 major-version validation at task startup.
- HTTPS/TLS hostname verification and optional PEM CA trust.
- External credential file paths or Kafka Connect ConfigProvider references.
- Deterministic IDs from the Kafka record key or a configured record field.
- Explicit `INDEX`, `CREATE`, `UPSERT`, and `DELETE` operations.
- Tombstone policies: `ignore`, `delete`, or `dlq`.
- Synchronous, bounded bulk writes with item-level acknowledgement.
- Retry classification for transient HTTP and I/O failures.
- Partition-local contiguous offset tracking.

## Architecture

```text
Kafka Connect worker
  -> StreamForge sink task
  -> index / id / operation resolution
  -> JSON encoding
  -> bounded synchronous Elasticsearch bulk request
  -> item acknowledgement or configured DLQ terminal state
  -> contiguous Kafka offset commit
```

See [docs/architecture.md](docs/architecture.md) for lifecycle details.

## Supported environments

The preserved build targets Java 17, Kafka Connect 4.0.x, and Elasticsearch 9.x.
The Dockerfile expects a compatible Kafka Connect 4.0 base image. Kubernetes
examples target Strimzi-managed Kafka Connect. Other versions require separate
validation.

## Installation and quick start

1. Install Java 17 and obtain a Kafka Connect 4.0.x distribution.
2. Run `./gradlew clean test pluginManifest`.
3. Install `build/plugin/datapie-elasticsearch-sink` as one plugin directory.
4. Configure Kafka Connect converters, the Elasticsearch URL, credentials, and
   at least one `index.mapping.<topic>` property.
5. Register the connector and verify task state, Elasticsearch health, and
   Kafka Connect error metrics.

See [docs/installation.md](docs/installation.md) and
[docs/configuration-reference.md](docs/configuration-reference.md).

## Development and testing

```sh
./gradlew clean test pluginManifest
```

The current unit tests cover index allow-listing and contiguous offset
semantics. They do not replace an integration test against an Elasticsearch
cluster. See [docs/development-guide.md](docs/development-guide.md).

## Deployment options

- [Standalone Kafka Connect](docs/standalone-deployment.md)
- [Docker](docs/docker-deployment.md)
- [Kubernetes / Strimzi](docs/kubernetes-deployment.md)

The repository does not publish a prebuilt image. Build and publish an image
only through an operator-controlled registry and release process.

## Benchmark reference

Benchmark methodology and the incomplete direct/Docker/Kubernetes comparison
are documented in [docs/performance-benchmark.md](docs/performance-benchmark.md).
Only results present in this repository are reported; missing measurements are
not estimated.

## Production experience

This connector has been used and benchmarked in a real production-oriented data
platform called DataPie. That experience is operational context, not a claim
that this repository contains the platform or that every deployment is
production-ready. Review the [production guide](docs/production-guide.md) and
the known limitations in [RELEASE_NOTES.md](RELEASE_NOTES.md) before adoption.

## Release

Version `v1.0.0` is the first public release of the production-tested connector
implementation. The preserved Java package and artifact coordinates remain
unchanged for reproducibility.

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
