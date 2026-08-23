# Smoke manifests

These manifests are source-backed templates only. The Elasticsearch URL and
image digest must be filled from the reviewed private deployment overlay; no
secret values belong here. Apply the topic first, then the connector, and remove
both only after the smoke evidence is retained. The connector name and group are
run-scoped and never reuse the production group.
