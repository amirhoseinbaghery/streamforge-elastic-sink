# Strimzi

`KafkaConnector` resources are enabled by
`strimzi.io/use-connector-resources: "true"`. Connector configuration is
source-backed and uses the existing file ConfigProvider pattern for credentials.
Apply follows source change -> image build -> Strimzi rollout -> live/source
reconciliation. Rollback restores the prior KafkaConnect image and removes only
the run-scoped validation resource.
