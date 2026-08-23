# Kubernetes

The public deployment workflow is documented in
[Kubernetes deployment](kubernetes-deployment.md).

Build an immutable image containing the complete plugin directory and reference
it from the Strimzi `KafkaConnect` resource. Use a separate connector name,
consumer group, and temporary index for validation; do not assume an internal
cluster overlay is present in this repository.

For external deployments, build and publish the image to a registry controlled
by the operator, then use the resulting immutable image reference. For a private
development cluster, load the image into every eligible node's container
runtime or use a registry reachable by the cluster. Verify the rendered Pod and
image digest before rollout; do not rely on an ad-hoc live patch.
