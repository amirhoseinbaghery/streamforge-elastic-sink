# Kubernetes deployment

The intended Kubernetes integration is a Strimzi-managed `KafkaConnect` worker
with the plugin present in every eligible worker image or node-local runtime.
Build an immutable operator-controlled image and reference it from the
source-backed `KafkaConnect` resource. Do not rely on ad-hoc live patches.

## Plugin and configuration

- Bake `build/plugin/datapie-elasticsearch-sink` into the image, or use the
  platform's approved plugin assembly mechanism.
- Mount the CA certificate and external credential files read-only.
- Use a ConfigProvider reference for the file contents, not a Secret value in a
  `KafkaConnector` manifest.
- Keep index mappings, topic selection, and operation settings in the reviewed
  source of truth.

## Health and scaling

Validate the rendered `KafkaConnect` and `KafkaConnector` resources, plugin
discovery, task `RUNNING` state, Kafka consumer lag, Elasticsearch health, and
worker memory. The current connector implementation returns one task
configuration; increasing `tasksMax` alone does not establish parallel task
behavior. Scale only after a separate workload and resource review.

## Rollout and rollback

Apply source change before image build and rollout. Confirm live state matches
the rendered source. For rollback, restore the previous image and source,
preserve Kafka topics and committed offsets, and reconcile Elasticsearch
identity before resuming input. Remove only explicitly run-scoped test
resources.
