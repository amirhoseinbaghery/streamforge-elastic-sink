package ir.datapie.connect.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.JsonData;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Synchronous bounded bulk writer. Synchronous completion makes put/preCommit offset safety explicit. */
public final class BulkWriter {
    public record Entry(SinkRecord record, String index, String id, String operation, Object document, int bytes) { }

    private final ElasticsearchClient client;
    private final RetryPolicy retryPolicy;
    private final ErrorClassifier classifier = new ErrorClassifier();
    private final DlqReporter dlqReporter;
    private final DataPieElasticsearchSinkConfig config;
    private final OffsetTracker offsets;
    private final ConnectorMetrics metrics;

    public BulkWriter(ElasticsearchClient client, RetryPolicy retryPolicy, DlqReporter dlqReporter,
                      DataPieElasticsearchSinkConfig config, OffsetTracker offsets, ConnectorMetrics metrics) {
        this.client = client;
        this.retryPolicy = retryPolicy;
        this.dlqReporter = dlqReporter;
        this.config = config;
        this.offsets = offsets;
        this.metrics = metrics;
    }

    public void write(List<Entry> entries) throws Exception {
        if (entries.isEmpty()) return;
        List<Entry> pending = new ArrayList<>(entries);
        int attempt = 0;
        long started = System.currentTimeMillis();
        while (!pending.isEmpty()) {
            List<Entry> retry = new ArrayList<>();
            try {
                List<Entry> requestEntries = pending;
                BulkResponse response = client.bulk(request -> {
                    for (Entry entry : requestEntries) request.operations(operation(entry));
                    return request;
                });
                List<co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem> items = response.items();
                if (items.size() != pending.size()) {
                    throw new ConnectException("Elasticsearch returned incomplete bulk item response");
                }
                for (int i = 0; i < items.size(); i++) {
                    Entry entry = pending.get(i);
                    var item = items.get(i);
                    Integer status = item.status();
                    if (item.error() == null) {
                        offsets.acknowledged(entry.record());
                        metrics.acked.increment();
                        metrics.bytesWritten.add(entry.bytes());
                        continue;
                    }
                    if (classifier.classify(status, null) == ErrorClassifier.Category.RETRYABLE) {
                        retry.add(entry);
                        metrics.retried.increment();
                    } else {
                        permanent(entry, new ConnectException("Permanent Elasticsearch item failure status=" + status));
                    }
                }
            } catch (Exception requestFailure) {
                if (requestFailure instanceof PermanentBulkException) throw requestFailure;
                if (pending.isEmpty()) throw requestFailure;
                retry.clear();
                retry.addAll(pending);
                if (!retryPolicy.canRetry(attempt, started)) {
                    throw new ConnectException("Elasticsearch bulk request failed after bounded retries", requestFailure);
                }
            }
            if (!retry.isEmpty()) {
                if (!retryPolicy.canRetry(attempt, started)) {
                    throw new ConnectException("Elasticsearch bulk item retries exhausted");
                }
                attempt++;
                retryPolicy.backoff(attempt);
            }
            pending = retry;
        }
    }

    private BulkOperation operation(Entry entry) {
        return switch (entry.operation()) {
            case "INDEX" -> BulkOperation.of(b -> b.index(i -> i.index(entry.index()).id(entry.id()).document(JsonData.of(entry.document()))));
            case "CREATE" -> BulkOperation.of(b -> b.create(i -> i.index(entry.index()).id(entry.id()).document(JsonData.of(entry.document()))));
            case "DELETE" -> BulkOperation.of(b -> b.delete(i -> i.index(entry.index()).id(entry.id())));
            case "UPSERT" -> BulkOperation.of(b -> b.update(i -> i.index(entry.index()).id(entry.id()).action(a -> a.doc(JsonData.of(entry.document())).docAsUpsert(true))));
            default -> throw new IllegalArgumentException("Unsupported operation=" + entry.operation());
        };
    }

    private void permanent(Entry entry, Exception error) throws Exception {
        metrics.failed.increment();
        if ("dlq".equals(config.string(DataPieElasticsearchSinkConfig.ERRORS_MODE))) {
            dlqReporter.report(entry.record(), error);
            offsets.acknowledged(entry.record());
            metrics.dlq.increment();
            return;
        }
        throw new PermanentBulkException(error);
    }

    private static final class PermanentBulkException extends Exception {
        private static final long serialVersionUID = 1L;
        private PermanentBulkException(Exception cause) { super(cause); }
    }
}
