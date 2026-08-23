package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.sink.SinkRecord;

/** Resolves only allow-listed configured index names. */
public final class IndexResolver {
    private final DataPieElasticsearchSinkConfig config;

    public IndexResolver(DataPieElasticsearchSinkConfig config) { this.config = config; }

    public String resolve(SinkRecord record) {
        String index = config.indexFor(record.topic());
        if (index.isBlank() || !index.matches("[a-z0-9][a-z0-9._-]{0,254}")) {
            throw new IllegalArgumentException("No valid configured index mapping for topic=" + record.topic());
        }
        return index;
    }
}
