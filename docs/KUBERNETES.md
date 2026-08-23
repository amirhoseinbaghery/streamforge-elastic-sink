# Kubernetes

The canonical runtime authority is the repository-backed Strimzi
`KafkaConnect` resource in `charts/kafka/kafka-connector/kafka-connect.yml`.
The validation connector uses a separate name and consumer group and writes only
to source-controlled temporary indices. Existing connectors are not removed and
no production cutover is part of PDP-E1–E4.

For external deployments, build and publish the image to a registry controlled
by the operator, then use the resulting immutable image reference. For a private
development cluster, load the locally built image into every eligible node's
container runtime. For this cluster the source uses the qualified local name
`localhost/datapie-elasticsearch-sink:0.1.0`; the `localhost/` prefix prevents
Kubernetes from normalizing the reference to Docker Hub. Strimzi's
`KafkaConnect` CRD does not expose a container
`imagePullPolicy` field; this non-`latest` local tag therefore uses Kubernetes'
default `IfNotPresent` behavior. Verify the generated Pod before rollout. Do
not rely on an ad-hoc live patch.
