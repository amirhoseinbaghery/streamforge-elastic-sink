# StreamForge Elasticsearch Sink

StreamForge Elasticsearch Sink is an independent Kafka Connect sink connector
that writes Kafka records to Elasticsearch 9.x. It is designed for teams that
need deterministic document IDs, explicit topic-to-index routing, bounded bulk
writes, and predictable retry and offset behavior.

The connector was originally developed by Amirhosein Bagheri and has been used
inside the DataPie platform. DataPie is one real-world deployment example, not
a dependency; any compatible Kafka Connect environment can run the connector.

## Features

- Deterministic document IDs from Kafka record keys or value fields.
- Explicit `INDEX`, `CREATE`, `UPSERT`, and `DELETE` operations.
- Allow-listed topic-to-index mappings.
- Synchronous bulk writes with record, request, and pending-data bounds.
- Bounded retries for transient Elasticsearch and network failures.
- TLS hostname verification with optional PEM CA trust.
- Tombstone policies and optional Kafka Connect DLQ reporting.
- Contiguous per-partition offset tracking.

## Supported environment

The validated build targets:

- Java 17
- Kafka Connect 4.0.x
- Elasticsearch 9.x
- Gradle 8.10.2 through the included wrapper

Docker deployment requires a Kafka Connect 4.0-compatible base image.
Kubernetes examples use Strimzi's `KafkaConnect` and `KafkaConnector` resources.
Validate other version combinations before production use.

## Requirements

For a local installation, provide a reachable Kafka broker and Elasticsearch
9.x node, a Kafka Connect 4.0.x distribution, Java 17, and write access to one
directory listed in the worker's `plugin.path`. Docker users need Docker
Compose; Kubernetes users need Strimzi and an image registry.

## Quick start

This example assumes Kafka is available at `localhost:9092`, Elasticsearch 9.x
without authentication is available at `http://localhost:9200`, and a Kafka
Connect distribution is unpacked locally.

### 1. Build and install the plugin

```sh
./gradlew clean test pluginManifest
sudo mkdir -p /opt/kafka/plugins
sudo cp -R build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/
```

Set the worker's `plugin.path` to include `/opt/kafka/plugins`, then start the
worker from the Kafka distribution:

```sh
bin/connect-distributed.sh config/connect-distributed.properties
```

The worker REST API should now respond at `http://localhost:8083`.

### 2. Create the topic and connector

```sh
bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --if-not-exists --topic events \
  --partitions 1 --replication-factor 1

curl -fsS -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  --data @examples/standalone/connector.json
```

The example connector uses this configuration:

```json
{
  "name": "events-to-elasticsearch",
  "config": {
    "connector.class": "ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector",
    "tasks.max": "1",
    "topics": "events",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "elasticsearch.url": "http://localhost:9200",
    "elasticsearch.expected.major": "9",
    "index.mapping.events": "events-v1",
    "document.id.strategy": "record-key",
    "operation.events": "UPSERT"
  }
}
```

### 3. Verify the task and one document

```sh
curl -fsS http://localhost:8083/connectors/events-to-elasticsearch/status

printf 'order-1\t{"status":"paid","total":42}\n' | \
  bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic events --property parse.key=true --property key.separator=$'\t'

curl -fsS http://localhost:9200/events-v1/_doc/order-1
```

The connector and task states should be `RUNNING`, and Elasticsearch should
return the document with ID `order-1`. See [Getting started](docs/getting-started.md)
if your worker uses authentication, TLS, or a different filesystem layout.

## Deployment options

- [Standalone Kafka Connect](docs/standalone-deployment.md)
- [Docker Compose and custom images](docs/docker-deployment.md)
- [Kubernetes and Strimzi](docs/kubernetes-deployment.md)

Copy-ready starting files are under [`examples/`](examples/).

## Documentation

- [Getting started](docs/getting-started.md)
- [Configuration reference](docs/configuration-reference.md)
- [Configuration examples](docs/configuration-examples.md)
- [Architecture and offset behavior](docs/architecture.md)
- [Production operations](docs/production-guide.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Security](docs/SECURITY.md)
- [Benchmark evidence](docs/performance-benchmark.md)
- [Development guide](docs/development-guide.md)

## Real-world usage

The connector has run as part of the DataPie platform, where it provided a
real deployment context for the preserved implementation. StreamForge remains
an independent project: its build, runtime, configuration, and deployment do
not require DataPie.

## Compatibility and limitations

The Java package namespace and plugin directory name are preserved from the
original validated implementation to maintain compatibility with existing
deployments. The public project name does not change these runtime identifiers.

The connector currently creates one task configuration, so raising `tasks.max`
does not add parallel connector tasks. It does not manage Elasticsearch index
mappings, templates, ILM, cluster settings, Kafka, or Elasticsearch lifecycle.
Performance depends on record size, partitioning, network latency, worker
resources, and Elasticsearch capacity; benchmark results are not universal
throughput guarantees.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening an issue or pull request.
Include the affected versions, sanitized configuration, and validation command.
Participation is governed by the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

Apache License 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
