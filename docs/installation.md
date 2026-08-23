# Installation

## Prerequisites

- Java 17 runtime and compiler.
- Kafka Connect 4.0.x.
- Elasticsearch 9.x reachable from the Connect worker.
- A Kafka Connect converter configuration appropriate for the input topics.
- A TLS CA path and external credential mechanism when Elasticsearch requires
  authentication.

## Build the plugin

From this directory:

```sh
./gradlew clean test pluginManifest
```

Install the complete directory `build/plugin/datapie-elasticsearch-sink` into
the Kafka Connect plugin path. Keep the connector jar and all generated runtime
dependencies together in one directory. Restart or roll the Connect worker
according to its normal plugin-discovery procedure.

## Register a connector

Provide the connector class
`ir.datapie.connect.elasticsearch.DataPieElasticsearchSinkConnector`, Kafka
topic selection, converters, an Elasticsearch URL, credentials, and at least
one `index.mapping.<topic>`. Use a ConfigProvider or external files for secrets;
do not put secret values in source, examples, images, or logs.

## Validate installation

1. Confirm the plugin appears in the worker's connector/plugin listing.
2. Run connector configuration validation.
3. Confirm the task reaches `RUNNING`.
4. Send a bounded test record to a non-production index.
5. Verify the Elasticsearch document ID and index.
6. Check Kafka Connect error, retry, and lag metrics.
7. Remove only the test connector and test data after evidence is retained.
