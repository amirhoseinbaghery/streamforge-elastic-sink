# Release preparation audit

Audit target: `streamforge-elastic-sink` public-release preparation
Local source location: `components/datapie-elasticsearch-sink`
Audit purpose: document the existing implementation and define a safe v1.0.0
documentation-only release preparation.

## Repository structure

- Gradle Java project with Gradle Wrapper (`gradle/wrapper`); root project name
  remains `datapie-elasticsearch-sink`.
- Java 17 toolchain and Java 17 compilation target.
- Production connector source is under
  `src/main/java/ir/datapie/connect/elasticsearch` with a Kafka Connect service
  descriptor under `src/main/resources/META-INF/services`.
- Existing tests are JUnit 5 tests for index resolution and contiguous offset
  tracking.
- Existing deployment material includes a Dockerfile, build scripts, and
  source-backed Kubernetes/Strimzi smoke manifests.
- Existing docs cover build, configuration, architecture, Docker, Kubernetes,
  Strimzi, security, and historical benchmarks.
- `build/` contains generated jars, dependency manifests, reports, and test
  output; it is not treated as source documentation.

## Build and compatibility facts

The existing build uses group `ir.datapie.connect`, version default `0.1.0`,
Kafka Connect `4.0.0`, Elasticsearch Java and REST clients `9.1.10`, and Java
17. No coordinates, package names, dependency versions, or build settings are
changed by release preparation. The public release label `v1.0.0` is release
metadata; it does not rewrite the frozen production artifact coordinates.

## Protected production areas

The following are intentionally untouched:

- every file under `src/main/java`;
- the connector service descriptor under `src/main/resources`;
- `build.gradle`, `gradle.properties`, `settings.gradle`, and Gradle wrapper
  metadata;
- Docker build logic and existing Kubernetes/Strimzi deployment manifests;
- configuration defaults and validation;
- bulk writing, retry classification/backoff, offset tracking, ID/index and
  operation resolution, Elasticsearch client/TLS setup, task lifecycle, and
  error/DLQ handling.

## Existing behavior documented, not changed

The connector accepts Kafka Connect sink records, allow-lists topic-to-index
mappings, resolves deterministic IDs, encodes values to JSON, submits bounded
synchronous Elasticsearch bulk requests, retries retryable item/request
failures within configured limits, and advances offsets only through contiguous
terminal acknowledgements. Permanent failures either fail the task or use the
configured Kafka Connect errant-record reporter path. Tombstones support
`ignore`, `delete`, or `dlq` modes. The implementation does not implement a
second Kafka consumer, Schema Registry client, Elasticsearch mapping manager,
or cluster lifecycle controller.

## Existing evidence and limitations

The benchmark document contains historical results and explicitly separates
connector evidence from other sink implementations. Some later capacity and
production-simulation evidence is held outside this component repository; no
numbers are copied into release documentation unless present in the accepted
local benchmark record. Missing direct-execution and Docker-vs-Kubernetes A/B
measurements remain marked as unavailable.

## Release-preparation decision

The safe scope is additive documentation and release metadata only: README,
license/notice metadata where needed, changelog/release notes, and the requested
deployment/configuration/architecture/benchmark/operations/development guides.
No implementation, dependency, version, runtime-default, or deployment behavior
change is required.

## Verification after documentation preparation

- `scripts/validate-source.sh`: passed.
- Existing Gradle test reports show three tests passed with zero failures or
  errors (`IndexResolverTest` and `OffsetTrackerTest`).
- A fresh Gradle invocation was not completed: the default Java 25 runtime is
  incompatible with the cached Gradle/Groovy state, and a Java 21 retry could
  not delete root-owned generated build output. No source or build file was
  changed to bypass these environment limitations.
- The prohibited attribution scan found no references to restricted tool or
  generated-content terms.
