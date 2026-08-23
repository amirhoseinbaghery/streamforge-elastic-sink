# StreamForge Elasticsearch Sink

StreamForge Elasticsearch Sink is an independent Kafka Connect sink connector
that writes Kafka records to Elasticsearch 9.x. It is intended for teams that
already run Kafka Connect and need deterministic document identity, explicit
topic-to-index routing, bounded bulk writes, and predictable retry and offset
behavior.

The implementation was originally developed by Amirhosein Bagheri and has been
used in the DataPie platform. DataPie is an example deployment only; it is not
required to build, configure, or operate this connector.

## At a glance

- Java 17, Kafka Connect 4.0.x, and Elasticsearch 9.x.
- Deterministic IDs from record keys or configured value fields.
- Explicit `INDEX`, `CREATE`, `UPSERT`, and `DELETE` operations.
- Allow-listed topic-to-index mappings; index names are configuration, not data.
- Synchronous, bounded bulk requests with bounded retries for transient errors.
- TLS with hostname verification and optional PEM CA certificates.
- Tombstone policies and optional Kafka Connect errant-record/DLQ reporting.
- Per-partition contiguous offset tracking.

## Quick start

Build the complete plugin directory, copy it to the worker's `plugin.path`,
restart the worker, and register a connector with at least one
`index.mapping.<topic>` property:

```sh
./gradlew clean test pluginManifest
cp -R build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/
```

Minimal connector properties:

```properties
name=events-to-elasticsearch
connector.class=ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector
tasks.max=1
topics=events
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schemas.enable=false
elasticsearch.url=https://elasticsearch.example:9200
elasticsearch.username.file=/run/secrets/es-user
elasticsearch.password.file=/run/secrets/es-password
index.mapping.events=events-v1
document.id.strategy=record-key
operation.events=UPSERT
```

Send a keyed JSON record to `events`, then verify it in `events-v1` using the
same key as the document ID. The [getting started guide](docs/getting-started.md)
walks through a complete local setup.

## Deployment choices

- [Getting started](docs/getting-started.md): build, install, register, verify.
- [Standalone Kafka Connect](docs/standalone-deployment.md): worker properties.
- [Docker](docs/docker-deployment.md): build a Kafka Connect image.
- [Kubernetes and Strimzi](docs/kubernetes-deployment.md): image, secrets, and
  `KafkaConnector` workflow.

## Requirements and boundaries

The tested build targets Java 17, Kafka Connect 4.0.x, and Elasticsearch 9.x.
The project does not manage Kafka, Elasticsearch mappings, index templates, ILM,
or cluster lifecycle. The current connector creates one task configuration;
`tasks.max` greater than one is not a demonstrated parallelism guarantee.
Validate other version combinations and workload sizes in your environment.

## Learn more

- [Configuration reference](docs/configuration-reference.md)
- [Architecture](docs/architecture.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Production guide](docs/production-guide.md)
- [Security](docs/SECURITY.md)
- [Benchmark evidence and gaps](docs/performance-benchmark.md)
- [Development guide](docs/development-guide.md)

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening an issue or pull
request. Please include the test command and compatibility impact for changes.
The project follows the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
