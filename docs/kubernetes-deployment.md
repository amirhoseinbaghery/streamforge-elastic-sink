# Kubernetes deployment with Strimzi

The supported example workflow runs the connector in a Strimzi-managed Kafka
Connect cluster. The connector does not require DataPie or any internal cluster
components.

## Required components

- A Kubernetes cluster and `kubectl` access
- A running Kafka cluster
- The Strimzi operator and `KafkaConnect`/`KafkaConnector` CRDs
- A registry accessible from the cluster
- Elasticsearch 9.x reachable from Kafka Connect Pods
- A Kafka Connect 4.0-compatible base image

The examples use namespace `kafka` and KafkaConnect name
`streamforge-connect`. Replace them with your own source-controlled names.

## 1. Build and push the image

From the repository root, choose an immutable image reference:

```sh
BASE_IMAGE=confluentinc/cp-kafka-connect:8.0.6 \
IMAGE_REF=registry.example/streamforge-elastic-sink:1.0.0 \
./scripts/build-image.sh

docker push registry.example/streamforge-elastic-sink:1.0.0
```

Record the pushed digest. Production manifests should use the reviewed digest
rather than a mutable tag.

## 2. Mount Elasticsearch credentials and CA

Create local files containing only the username, password, and PEM CA, then
create Kubernetes Secrets without placing literal credentials in shell history:

```sh
kubectl -n kafka create secret generic es-credentials \
  --from-file=username=./es-username.txt \
  --from-file=password=./es-password.txt

kubectl -n kafka create secret generic es-ca \
  --from-file=ca.crt=./ca.crt
```

Mount these Secrets into every Kafka Connect Pod. Strimzi versions that expose
`KafkaConnect.spec.externalConfiguration.volumes` can use this source fragment:

```yaml
metadata:
  name: streamforge-connect
  namespace: kafka
  annotations:
    strimzi.io/use-connector-resources: "true"
spec:
  image: registry.example/streamforge-elastic-sink@sha256:REPLACE_ME
  externalConfiguration:
    volumes:
      - name: es-credentials
        secret:
          secretName: es-credentials
      - name: es-ca
        secret:
          secretName: es-ca
```

This produces the paths used by the example:

- `/opt/kafka/external-configuration/es-credentials/username`
- `/opt/kafka/external-configuration/es-credentials/password`
- `/opt/kafka/external-configuration/es-ca/ca.crt`

Check your installed Strimzi CRD if it uses template-based custom volumes
instead. Keep the same container paths or update the connector example to
match. Kubernetes Secrets require appropriate RBAC and encryption-at-rest
controls; base64 encoding alone is not encryption.

## 3. Update KafkaConnect

Update the source-controlled `KafkaConnect` resource with the immutable image,
Secret mounts, and connector-resource annotation, then apply it:

```sh
kubectl apply -f kafka-connect.yaml
kubectl -n kafka wait --for=condition=Ready \
  kafkaconnect/streamforge-connect --timeout=300s
```

Confirm every Pod runs the expected image digest and that plugin discovery
completed before creating the connector:

```sh
kubectl -n kafka get pods -l strimzi.io/cluster=streamforge-connect \
  -o jsonpath='{range .items[*]}{.metadata.name}{"  "}{.status.containerStatuses[0].imageID}{"\n"}{end}'
```

## 4. Apply KafkaConnector

Review [`examples/kubernetes/KafkaConnector.yaml`](../examples/kubernetes/KafkaConnector.yaml).
Replace the Elasticsearch service URL, namespace, KafkaConnect label, topic,
index, and mounted paths, then apply it:

```sh
kubectl apply -f examples/kubernetes/KafkaConnector.yaml
```

The example uses `tasksMax: 1` because the current implementation creates one
task configuration. Raising the value does not provide additional tasks.

## 5. Verify status and ingestion

```sh
kubectl -n kafka get kafkaconnector events-to-elasticsearch -o yaml
kubectl -n kafka describe kafkaconnector events-to-elasticsearch
kubectl -n kafka logs -l strimzi.io/cluster=streamforge-connect \
  -c kafka-connect --tail=200 --prefix=true
```

The connector and task conditions should report `RUNNING`. Produce a keyed test
record to `events`, query `events-v1` in Elasticsearch, and monitor Kafka lag.
Use a disposable topic and index for the first rollout.

## Rollout and rollback

For upgrades, build and test a new immutable image, update the source manifest,
and let Strimzi roll the Connect Pods. Preserve Kafka topics and committed
offsets during rollback. Restore the previous image digest, verify connector
status and document identity, then resume input.

## Troubleshooting

- **KafkaConnector is ignored:** verify the annotation and
  `strimzi.io/cluster` label match the KafkaConnect resource.
- **Plugin class is missing:** inspect the running image digest and ensure
  `/opt/kafka/plugins` is in the worker's `plugin.path`.
- **Credential file is missing:** inspect the Pod's mounted Secret names and
  paths; the connector reads these files at task startup.
- **TLS validation fails:** verify the PEM CA and Elasticsearch hostname. Do
  not disable hostname verification.
- **Task stays failed:** inspect the first nested worker-log error and compare
  the server major version with `elasticsearch.expected.major`.
