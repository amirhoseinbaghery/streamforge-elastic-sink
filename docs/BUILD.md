# Build

The Gradle Wrapper pins Gradle 8.10.2 and the Java toolchain is pinned to Java
17. The Elasticsearch Java API Client and REST client are both pinned to 9.1.10
to avoid the pre-9.1.10 Rest5 memory-bug range while retaining compatibility
with the 9.1.4 server. The selected transport is the official
`RestClientTransport` over pooled Apache HTTP connections; a single client is
reused for each task lifecycle.

Commands:

```text
./gradlew clean test pluginManifest
```

The image build consumes `build/plugin/datapie-elasticsearch-sink` and never
downloads a plugin at Pod startup.
