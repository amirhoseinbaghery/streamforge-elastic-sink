package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.Map;

public final class DocumentIdResolver {
    private final DataPieElasticsearchSinkConfig config;

    public DocumentIdResolver(DataPieElasticsearchSinkConfig config) { this.config = config; }

    public String resolve(SinkRecord record) {
        if ("record-key".equals(config.string(DataPieElasticsearchSinkConfig.DOCUMENT_ID_STRATEGY))) {
            if (record.key() == null) throw new IllegalArgumentException("Record key is required for deterministic ID");
            return safe(record.key().toString());
        }
        String field = config.idFieldFor(record.topic());
        if (field.isBlank()) throw new IllegalArgumentException("document.id.field." + record.topic() + " is required");
        Object value = extract(record.value(), field);
        if (value == null) throw new IllegalArgumentException("Document ID field is null: " + field);
        return safe(value.toString());
    }

    private Object extract(Object value, String field) {
        if (value instanceof Struct struct && struct.schema().field(field) != null) return struct.get(field);
        if (value instanceof Map<?, ?> map) return map.get(field);
        return null;
    }

    private String safe(String value) {
        if (value.isBlank() || value.length() > 512 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Document ID is empty or invalid");
        }
        return value;
    }
}
