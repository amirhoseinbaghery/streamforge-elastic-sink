package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts Kafka Connect values into JSON-friendly values without retaining Connect objects. */
public final class RecordMapper {
    public Object map(SinkRecord record) {
        return mapValue(record.value(), record.valueSchema());
    }

    @SuppressWarnings("unchecked")
    public Object mapValue(Object value, Schema schema) {
        if (value == null) return null;
        if (value instanceof Struct struct) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (org.apache.kafka.connect.data.Field field : struct.schema().fields()) {
                result.put(field.name(), mapValue(struct.get(field), field.schema()));
            }
            return result;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), mapValue(entry.getValue(), null));
            }
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            for (Object item : iterable) result.add(mapValue(item, null));
            return result;
        }
        if (value.getClass().isArray()) {
            List<Object> result = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            for (int i = 0; i < length; i++) result.add(mapValue(java.lang.reflect.Array.get(value, i), null));
            return result;
        }
        return value;
    }
}
