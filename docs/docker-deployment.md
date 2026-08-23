# Docker deployment

The repository provides a Dockerfile but does not publish an image. Build the
plugin first and use a Kafka Connect 4.0-compatible base image selected by the
operator.

```sh
./gradlew clean test pluginManifest
BASE_IMAGE=your-connect-base:4.0 \
IMAGE_REF=streamforge-elastic-sink:1.0.0 \
./scripts/build-image.sh
```

The Dockerfile copies the complete plugin directory into
`/opt/kafka/plugins/datapie-elasticsearch-sink`. It does not download a plugin
at container startup.

## Compose-style environment

```yaml
services:
  connect:
    image: streamforge-elastic-sink:1.0.0
    environment:
      CONNECT_BOOTSTRAP_SERVERS: kafka:9092
      CONNECT_REST_PORT: 8083
      CONNECT_PLUGIN_PATH: /opt/kafka/plugins
      CONNECT_CONFIG_PROVIDERS: file
      CONNECT_CONFIG_PROVIDERS_FILE_CLASS: org.apache.kafka.common.config.provider.FileConfigProvider
    volumes:
      - ./secrets:/run/secrets:ro
      - ./ca:/run/tls:ro
```

Use the actual environment-variable names and worker configuration required by
the chosen Kafka Connect distribution. Do not put passwords in Compose files,
image layers, or shell history. Verify the mounted paths inside the container,
then register a connector using the configuration reference.

## Building your own image

This project does not publish official container images.

Users can build their own image:

```bash
docker build \
  -t streamforge-elastic-sink:1.0.0 .
```

The Dockerfile extends a Kafka Connect runtime; it is not a standalone worker.
The base image must provide Kafka Connect 4.0-compatible startup behavior.

## Execution and verification

Mount CA and credential files read-only, start the image with the base image's
normal Kafka, converter, REST, and `plugin.path` settings, then register the
connector through port 8083:

```sh
curl http://localhost:8083/connector-plugins
curl http://localhost:8083/connectors/events-to-elasticsearch/status
```

The plugin is copied to `/opt/kafka/plugins/datapie-elasticsearch-sink`; the
image does not download dependencies at startup. A missing class means the
complete plugin directory was not included or `/opt/kafka/plugins` is absent
from `plugin.path`. Authentication and TLS failures should be diagnosed by
checking mounted paths and process permissions inside the container. Growing
lag or restart loops require checking Elasticsearch 429/5xx responses before
increasing batch bounds.

---
