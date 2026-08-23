package ir.datapie.connect.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.apache.kafka.connect.sink.SinkTaskContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DataPieElasticsearchSinkTask extends SinkTask {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DataPieElasticsearchSinkTask.class.getName());
    private DataPieElasticsearchSinkConfig config;
    private ElasticsearchClientFactory clients;
    private BulkWriter writer;
    private OffsetTracker offsets;
    private RecordEncoder encoder;
    private IndexResolver indexes;
    private DocumentIdResolver ids;
    private OperationResolver operations;
    private ConnectorMetrics metrics;
    private ObjectMapper mapper;
    private long pendingBytes;
    private int pendingRecords;

    @Override public void initialize(SinkTaskContext context) { super.initialize(context); }

    @Override public void start(Map<String, String> props) {
        try {
            config = new DataPieElasticsearchSinkConfig(props);
            clients = ElasticsearchClientFactory.create(config);
            String version = new ElasticsearchVersionValidator().validate(clients.client(), config.integer(DataPieElasticsearchSinkConfig.EXPECTED_MAJOR));
            offsets = new OffsetTracker();
            metrics = new ConnectorMetrics();
            mapper = new ObjectMapper();
            encoder = new RecordEncoder(mapper, new RecordMapper());
            indexes = new IndexResolver(config);
            ids = new DocumentIdResolver(config);
            operations = new OperationResolver(config);
            ErrantRecordReporter reporter = null;
            try { reporter = context.errantRecordReporter(); } catch (UnsupportedOperationException ignored) { }
            writer = new BulkWriter(clients.client(), new RetryPolicy(config), new DlqReporter(reporter), config, offsets, metrics);
            LOGGER.info(() -> "DataPie Elasticsearch sink started; serverVersion=" + version + ", connectorVersion=" + version());
        } catch (Exception e) {
            closeQuietly();
            throw new ConnectException("Unable to start DataPie Elasticsearch sink", e);
        }
    }

    @Override public void put(Collection<SinkRecord> records) {
        if (records == null || records.isEmpty()) return;
        List<BulkWriter.Entry> batch = new ArrayList<>();
        long batchBytes = 0;
        for (SinkRecord record : records) {
            offsets.received(record);
            metrics.received.increment();
            BulkWriter.Entry entry = toEntry(record);
            if (entry == null) continue;
            if (entry.bytes() > config.longValue(DataPieElasticsearchSinkConfig.RECORD_MAX_BYTES)) {
                handleOversize(record, entry.bytes());
                continue;
            }
            if (pendingRecords + batch.size() >= config.integer(DataPieElasticsearchSinkConfig.PENDING_MAX_RECORDS)
                    || pendingBytes + batchBytes + entry.bytes() > config.longValue(DataPieElasticsearchSinkConfig.PENDING_MAX_BYTES)) {
                throw new ConnectException("Connector pending-data bound reached; Kafka Connect will retry this batch");
            }
            if (!batch.isEmpty() && (batch.size() >= config.integer(DataPieElasticsearchSinkConfig.BULK_MAX_OPERATIONS)
                    || batchBytes + entry.bytes() > config.longValue(DataPieElasticsearchSinkConfig.BULK_MAX_BYTES))) {
                writeBatch(batch);
                batch = new ArrayList<>();
                batchBytes = 0;
            }
            batch.add(entry);
            batchBytes += entry.bytes();
        }
        writeBatch(batch);
    }

    private BulkWriter.Entry toEntry(SinkRecord record) {
        if (record.value() == null) {
            return switch (config.string(DataPieElasticsearchSinkConfig.TOMBSTONE_MODE)) {
                case "ignore" -> null;
                case "delete" -> new BulkWriter.Entry(record, indexes.resolve(record), ids.resolve(record), "DELETE", null, 0);
                default -> { handlePermanent(record, new ConnectException("Tombstone record")); yield null; }
            };
        }
        try {
            byte[] encoded = encoder.encode(record);
            metrics.encoded.increment();
            metrics.bytesReceived.add(encoded.length);
            return new BulkWriter.Entry(record, indexes.resolve(record), ids.resolve(record), operations.resolve(record),
                    mapper.readValue(encoded, Object.class), encoded.length);
        } catch (Exception e) {
            handlePermanent(record, e);
            return null;
        }
    }

    private void writeBatch(List<BulkWriter.Entry> batch) {
        if (batch.isEmpty()) return;
        pendingRecords += batch.size();
        for (BulkWriter.Entry entry : batch) pendingBytes += entry.bytes();
        metrics.submitted.add(batch.size());
        try {
            writer.write(batch);
        } catch (Exception e) {
            throw new ConnectException("Elasticsearch bulk write failed; offsets were not advanced optimistically", e);
        } finally {
            pendingRecords -= batch.size();
            for (BulkWriter.Entry entry : batch) pendingBytes -= entry.bytes();
        }
    }

    private void handleOversize(SinkRecord record, long bytes) {
        handlePermanent(record, new ConnectException("Record exceeds configured record.max.bytes: " + bytes));
    }

    private void handlePermanent(SinkRecord record, Exception error) {
        if ("dlq".equals(config.string(DataPieElasticsearchSinkConfig.ERRORS_MODE))) {
            try {
                offsets.acknowledged(record);
                metrics.dlq.increment();
                return;
            } catch (Exception ignored) { }
        }
        throw new ConnectException("Permanent record failure at topic=" + record.topic() + ", partition=" + record.kafkaPartition()
                + ", offset=" + record.kafkaOffset(), error);
    }

    @Override public void flush(Map<TopicPartition, OffsetAndMetadata> currentOffsets) { }

    @Override public Map<TopicPartition, OffsetAndMetadata> preCommit(Map<TopicPartition, OffsetAndMetadata> currentOffsets) {
        Map<TopicPartition, Long> current = new java.util.LinkedHashMap<>();
        currentOffsets.forEach((partition, metadata) -> current.put(partition, metadata.offset()));
        Map<TopicPartition, Long> safe = offsets.safeOffsets(current);
        Map<TopicPartition, OffsetAndMetadata> result = new java.util.LinkedHashMap<>();
        safe.forEach((partition, value) -> result.put(partition, new OffsetAndMetadata(value)));
        return result;
    }

    @Override public void open(Collection<TopicPartition> partitions) { }

    @Override public void close(Collection<TopicPartition> partitions) { offsets.revoke(partitions); }

    @Override public void stop() {
        LOGGER.info(() -> "Stopping DataPie Elasticsearch sink; " + (metrics == null ? "no metrics" : metrics.summary()));
        closeQuietly();
    }

    private void closeQuietly() {
        if (clients != null) {
            try { clients.close(); } catch (Exception ignored) { }
            clients = null;
        }
    }

    @Override public String version() { return DataPieElasticsearchSinkConnector.VERSION; }
}
