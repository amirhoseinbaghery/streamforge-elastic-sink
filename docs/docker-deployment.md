# Docker deployment

The recommended Docker first run uses
[`examples/docker/docker-compose.yml`](../examples/docker/docker-compose.yml).
It starts Kafka 4.0, Elasticsearch 9.x, and a Kafka Connect worker containing
the source-built connector. The stack is for local evaluation, not production.

## Requirements

- Docker Engine with the Compose plugin
- Java 17 for the Gradle build
- At least 2 GiB of memory available to the example stack
- Ports 8083 and 9200 available on the host

The example builds on `confluentinc/cp-kafka-connect:8.0.6`; Confluent Platform
8.0 is based on Kafka 4.0, as documented in its
[release notes](https://docs.confluent.io/platform/8.0/release-notes/index.html).
For production, replace it with a reviewed Kafka Connect 4.0-compatible image
from your normal supply chain.

## 1. Build the connector

Run from the repository root:

```sh
./gradlew clean test pluginManifest
```

This creates `build/plugin/datapie-elasticsearch-sink`, including the connector
jar and all runtime dependencies.

## 2. Build the Kafka Connect image

```sh
docker compose -f examples/docker/docker-compose.yml build connect
```

The repository Dockerfile installs the complete plugin directory at
`/opt/kafka/plugins/datapie-elasticsearch-sink`. No plugin download occurs when
the container starts.

## 3. Start the stack

```sh
docker compose -f examples/docker/docker-compose.yml up -d
docker compose -f examples/docker/docker-compose.yml ps
```

Wait for Kafka Connect to answer before continuing (about one minute maximum):

```sh
curl --retry 30 --retry-delay 2 --retry-connrefused \
  -fsS http://localhost:8083/ >/dev/null
```

## 4. Verify plugin discovery

```sh
curl -fsS http://localhost:8083/connector-plugins
```

The response must include
`ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector`. If it
does not, inspect the worker log and confirm that `CONNECT_PLUGIN_PATH` includes
`/opt/kafka/plugins`:

```sh
docker compose -f examples/docker/docker-compose.yml logs connect
```

## 5. Create the topic and connector

```sh
docker compose -f examples/docker/docker-compose.yml exec -T kafka \
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 \
  --create --if-not-exists --topic events \
  --partitions 1 --replication-factor 1
```

Register a connector that uses the Compose service name for Elasticsearch:

```sh
curl -fsS -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  --data '{
    "name": "events-to-elasticsearch",
    "config": {
      "connector.class": "ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector",
      "tasks.max": "1",
      "topics": "events",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "elasticsearch.url": "http://elasticsearch:9200",
      "elasticsearch.expected.major": "9",
      "index.mapping.events": "events-v1",
      "document.id.strategy": "record-key",
      "operation.events": "UPSERT"
    }
  }'
```

## 6. Verify ingestion

```sh
curl -fsS http://localhost:8083/connectors/events-to-elasticsearch/status

printf 'order-1\t{"status":"paid","total":42}\n' | \
  docker compose -f examples/docker/docker-compose.yml exec -T kafka \
  /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server kafka:9092 --topic events \
  --property parse.key=true --property key.separator=$'\t'

curl -fsS http://localhost:9200/events-v1/_doc/order-1
```

The connector and task should be `RUNNING`, and Elasticsearch should return
the document with ID `order-1`.

## Credentials and environment variables

The Compose example disables Elasticsearch authentication only for local
evaluation. In a real deployment, mount username, password, and CA files
read-only and configure `elasticsearch.username.file`,
`elasticsearch.password.file`, and `elasticsearch.ca.cert.path`. Kafka Connect
worker variables depend on the selected base-image distribution; the example's
`CONNECT_*` names follow the pinned image.

## Common errors

- **Connect REST API never becomes ready:** inspect `docker compose ... logs
  connect` and verify Kafka is reachable as `kafka:9092`.
- **Connector class is absent:** rebuild the plugin and image, then verify the
  complete directory exists under `/opt/kafka/plugins` in the container.
- **Elasticsearch connection fails:** use `http://elasticsearch:9200` from the
  Connect container, not `localhost`.
- **Task fails on document ID:** the example requires a non-empty Kafka key.
- **TLS or authentication fails:** verify mounted paths and file permissions
  inside the container; do not disable hostname verification.

To stop the local stack, run:

```sh
docker compose -f examples/docker/docker-compose.yml down
```
