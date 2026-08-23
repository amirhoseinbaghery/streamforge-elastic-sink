# Security

TLS hostname and certificate verification remain enabled. Credentials are
externalized through Kafka Connect ConfigProvider and are never written to
source, Docker layers, logs, DLQ metadata, or evidence. Index names are
allow-listed and document IDs are deterministic. The connector does not change
Elasticsearch mappings, templates, ILM, or cluster settings.
