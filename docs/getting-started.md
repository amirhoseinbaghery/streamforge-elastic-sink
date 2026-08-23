# Getting started

This guide builds the connector, installs it into a Kafka Connect worker, and
checks one record end to end. Use a disposable topic and Elasticsearch index.

## Requirements

- Java 17 (the project compiles with the Java 17 toolchain).
- Kafka Connect 4.0.x with access to Kafka.
- Elasticsearch 9.x reachable from the Connect worker.
- A Kafka Connect converter configuration for the record format.

## Build the plugin

From the repository root:

```sh
./gradlew clean test pluginManifest
```

The complete installable directory is
`build/plugin/datapie-elasticsearch-sink`. Copy the directory, including its
jar and runtime dependencies, into a directory listed by the worker's
`plugin.path`:

```sh
cp -R build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/
```

Restart the worker after changing plugin files. Confirm the worker can read the
directory and that its plugin listing contains
`ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector`.

## Register a first connector

The following example assumes the worker has access to the two mounted files.
Each file contains one value and is trimmed when read. For a worker using the
FileConfigProvider instead, replace those two properties with provider
references and configure the provider in the worker.

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
    "elasticsearch.url": "https://elasticsearch.example:9200",
    "elasticsearch.username.file": "/run/secrets/es-user",
    "elasticsearch.password.file": "/run/secrets/es-password",
    "index.mapping.events": "events-v1",
    "document.id.strategy": "record-key",
    "operation.events": "UPSERT"
  }
}
```

Submit it to the Connect REST API:

```sh
curl -X PUT http://localhost:8083/connectors/events-to-elasticsearch/config \
  -H 'Content-Type: application/json' \
  --data @connector.json
curl http://localhost:8083/connectors/events-to-elasticsearch/status
```

The task should reach `RUNNING`. A startup failure usually means the plugin is
not on `plugin.path`, the URL is unreachable, the Elasticsearch major version
does not match, or no `index.mapping.<topic>` exists.

## Verify ingestion

Produce a keyed JSON record with your Kafka producer. For example, with the
console producer configured for string keys and JSON values:

```sh
printf 'order-1\t{"status":"paid","total":42}\n' | \
  bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic events --property parse.key=true --property key.separator=$'\t'
```

Query Elasticsearch from a network location that can reach the cluster:

```sh
curl -u "$ES_USER:$ES_PASSWORD" \
  'https://elasticsearch.example:9200/events-v1/_doc/order-1'
```

Check the connector status, worker logs, Kafka consumer lag, and Elasticsearch
bulk response errors before testing larger batches. Delete only the disposable
connector, topic, and index after retaining any evidence needed for debugging.

## Next steps

Read the [configuration reference](configuration-reference.md), then choose a
[Docker](docker-deployment.md), [Kubernetes/Strimzi](kubernetes-deployment.md),
or [standalone](standalone-deployment.md) deployment path. If the task does not
start, use [troubleshooting](troubleshooting.md).
