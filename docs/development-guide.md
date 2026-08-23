# Development guide

## Requirements

Use Java 17 and the repository Gradle Wrapper. The build pins Gradle 8.10.2,
Kafka Connect 4.0.0 test/compile APIs, Elasticsearch clients 9.1.10, and the
existing Jackson and JUnit versions. Do not run dependency update tools during
maintenance of the production release.

## Local checks

```sh
./gradlew clean test pluginManifest
./scripts/validate-source.sh
```

The current tests cover configured index allow-listing and partition-contiguous
offset advancement. Add a focused test only when it protects an existing
behavior or a reproducible defect; do not rewrite production behavior to make a
test pass.

## Packaging

`pluginPackage` creates the jar, runtime dependency directory, `VERSION`, and
`DEPENDENCIES.txt`. Inspect the generated manifest before distribution. The
Docker build consumes that directory and does not download dependencies at
runtime.

## Change discipline

Keep connector source, configuration defaults, dependency versions, and build
coordinates stable for the v1.0.0 release. Review index/ID/operation behavior,
retry semantics, DLQ behavior, and offset safety for any future change. Run the
unit suite and a bounded isolated smoke before proposing a release.
