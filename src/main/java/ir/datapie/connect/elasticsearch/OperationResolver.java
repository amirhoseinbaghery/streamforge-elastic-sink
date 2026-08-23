package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.sink.SinkRecord;

public final class OperationResolver {
    private final DataPieElasticsearchSinkConfig config;

    public OperationResolver(DataPieElasticsearchSinkConfig config) { this.config = config; }

    public String resolve(SinkRecord record) {
        String op = config.operationFor(record.topic());
        if (!java.util.Set.of("INDEX", "CREATE", "UPSERT", "DELETE").contains(op)) {
            throw new IllegalArgumentException("Unsupported operation=" + op);
        }
        return op;
    }
}
