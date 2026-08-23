# Container image

For the complete build, environment, verification, and troubleshooting flow,
see [Docker deployment](docker-deployment.md). `docker/Dockerfile` extends a
Kafka Connect 4.0-compatible base image and copies the source-built plugin into
`/opt/kafka/plugins`.

The recommended local workflow is the
[Docker Compose example](docker-deployment.md). For a registry image, build
locally with an operator-selected Kafka Connect 4.0-compatible base:

```sh
BASE_IMAGE=confluentinc/cp-kafka-connect:8.0.6 \
IMAGE_REF=streamforge-elastic-sink:1.0.0 \
./scripts/build-image.sh
```

The image may be published by an external operator to a registry they control,
but this project does not publish a prebuilt image.
