ARG BASE_IMAGE
FROM ${BASE_IMAGE}

USER root
COPY build/plugin/datapie-elasticsearch-sink /opt/kafka/plugins/datapie-elasticsearch-sink
RUN find /opt/kafka/plugins/datapie-elasticsearch-sink -type f -exec chmod 0644 {} +
