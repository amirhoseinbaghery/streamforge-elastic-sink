# Standalone Kafka Connect deployment

## Prerequisites

Install Java 17 and Kafka Connect 4.0.x. Choose a plugin directory readable by
the Connect process and a separate directory for external credential files and
CA certificates.

## Install and start

```sh
./gradlew clean test pluginManifest
cp -R build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/
bin/connect-standalone.sh config/connect-standalone.properties \
  /path/to/streamforge-elastic-sink/examples/standalone/connector.json
```

Set `plugin.path` in the worker properties to include `/opt/kafka/plugins`.
Configure Kafka bootstrap servers, converters, and
`offset.storage.file.filename` in the worker properties. Kafka Connect 4.0
accepts the JSON request-body format directly as a standalone connector file;
replace `/path/to/streamforge-elastic-sink` with this repository's absolute
path. For a secured Elasticsearch cluster, copy the example and add mounted
credential files or ConfigProvider references.

## Validation and operations

Check the worker plugin listing, connector validation response, task state,
Elasticsearch health, and Kafka lag. Start with a non-production topic and
bounded batch limits. For rollback, stop new input, preserve topics and offsets,
stop the connector, restore the previous source-built plugin, and reconcile
before resuming.
