# Historical smoke manifests

These files preserve a deployment-specific smoke test and contain historical
names. They are not the public first-run examples and do not imply a DataPie
dependency. New users should start with
[`examples/kubernetes/KafkaConnector.yaml`](../examples/kubernetes/KafkaConnector.yaml)
and the [Kubernetes deployment guide](../docs/kubernetes-deployment.md).

If these smoke manifests are adapted, replace the Elasticsearch URL, image,
namespace, labels, topic, connector name, consumer group, index, and mounted
Secret paths. Keep credentials out of source. Apply the topic before the
connector and remove only explicitly disposable test resources after retaining
the required evidence.
