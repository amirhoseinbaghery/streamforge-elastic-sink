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

## Kubernetes examples

The repository provides Kubernetes examples for Kafka Connect deployments.

These examples are designed as templates and should be adapted according to:

- Kafka distribution
- authentication mechanism
- secret management approach
- cluster policies

No dependency on DataPie infrastructure is required.

## Requirements and plugin delivery

You need Kubernetes, a Kafka Connect worker (normally managed by Strimzi),
network access from Connect Pods to Kafka and Elasticsearch, and a registry
reachable by every eligible node. Build an immutable image with the Docker
guide and push it to an operator-controlled registry; every Connect Pod must
run that image. Do not assume an init-container download or a private cluster
overlay not present in this repository.

## Strimzi workflow

1. Build, scan, and push the image; record its digest.
2. Create Secrets for the Elasticsearch credentials and private CA, and mount
   them read-only where the Connect process can read them.
3. Configure the worker FileConfigProvider if using provider references.
4. Enable `strimzi.io/use-connector-resources: "true"` on `KafkaConnect`.
5. Apply a `KafkaConnector` with a unique name, topic mapping, and credentials
   references.
6. Verify the task and a test document before accepting production traffic.

`k8s/kafkaconnector-smoke.yml` is a template. Replace its run-scoped names,
URL, namespace, service, and secret paths for your environment.

```sh
kubectl -n kafka get kafkaconnect,kafkaconnector
kubectl -n kafka describe kafkaconnector events-to-elasticsearch
kubectl -n kafka logs deploy/kafka-connect -c kafka-connect --tail=200
```

If Strimzi does not reconcile the resource, check the feature flag and label.
If the class is missing, verify the running image digest and worker
`plugin.path`. If authentication fails, inspect Secret mounts and file
permissions in the Pod. Keep Secret values out of manifests and support logs.
