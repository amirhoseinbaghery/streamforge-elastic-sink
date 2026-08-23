# Standalone Kafka Connect deployment

## Prerequisites

Install Java 17 and Kafka Connect 4.0.x. Choose a plugin directory readable by
the Connect process and a separate directory for external credential files and
CA certificates.

## Install and start

```sh
./gradlew clean test pluginManifest
cp -R build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/
bin/connect-standalone.sh config/connect-standalone.properties connector.properties
```

Set `plugin.path` in the worker properties to include `/opt/kafka/plugins`.
Keep Kafka bootstrap, converter, offset, config, and status settings in the
normal Kafka Connect configuration. Keep connector properties in a separate
file and use a ConfigProvider or file references for secrets.

## Validation and operations

Check the worker plugin listing, connector validation response, task state,
Elasticsearch health, and Kafka lag. Start with a non-production topic and
bounded batch limits. For rollback, stop new input, preserve topics and offsets,
stop the connector, restore the previous source-built plugin, and reconcile
before resuming.
