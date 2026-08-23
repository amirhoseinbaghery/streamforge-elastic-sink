# Container image

`docker/Dockerfile` extends a verified Kafka Connect 4.0-compatible base image
and copies the source-built plugin into `/opt/kafka/plugins`.

Build locally with a registry-independent image name:

```sh
BASE_IMAGE=datapie-kafka-connect-base:4.0.0 \
IMAGE_REF=datapie-elasticsearch-sink:0.1.0 \
./scripts/build-image.sh
```

The image may be published by an external operator to a registry they control,
but this project does not publish a prebuilt image.
