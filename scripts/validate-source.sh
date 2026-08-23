#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
grep -q 'DataPieElasticsearchSinkConnector' "$ROOT/src/main/resources/META-INF/services/org.apache.kafka.connect.connector.Connector"
grep -q 'ordering.mode' "$ROOT/docs/CONFIGURATION.md"
grep -q 'PDP-E5' "$ROOT/docs/BENCHMARKS.md"
echo SOURCE_VALIDATION=PASS
