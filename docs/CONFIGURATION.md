# Configuration

Required: `elasticsearch.url` and at least one `index.mapping.<topic>` entry.
Credentials are supplied by Kafka Connect ConfigProvider references or by the
optional `elasticsearch.username.file` and `elasticsearch.password.file` paths.
Only file paths belong in this repository; secret values remain external.

Important bounded settings include `bulk.max.operations`, `bulk.max.bytes`,
`pending.max.records`, `pending.max.bytes`, `record.max.bytes`, and the bounded
retry settings. `ordering.mode=partition` is the only supported production mode
in this initial implementation. `tombstone.mode` is explicit (`ignore`,
`delete`, or `dlq`); permanent errors are `fail` or `dlq`.

Topic-to-index mappings and document-ID fields are allow-listed configuration,
not payload-controlled values.
