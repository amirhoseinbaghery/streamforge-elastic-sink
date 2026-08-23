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
