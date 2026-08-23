package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexResolverTest {
    @Test
    void usesConfiguredAllowListedIndex() {
        Map<String, Object> props = base(); props.put("index.mapping.tweets", "validation-tweets");
        DataPieElasticsearchSinkConfig config = new DataPieElasticsearchSinkConfig(props);
        assertEquals("validation-tweets", new IndexResolver(config).resolve(new SinkRecord("tweets", 0, null, "x", null, null, 1L)));
    }

    @Test
    void rejectsUnmappedTopic() {
        Map<String, Object> props = base(); props.put("index.mapping.tweets", "validation-tweets");
        DataPieElasticsearchSinkConfig config = new DataPieElasticsearchSinkConfig(props);
        assertThrows(IllegalArgumentException.class, () -> new IndexResolver(config).resolve(new SinkRecord("unknown", 0, null, "x", null, null, 1L)));
    }

    private Map<String, Object> base() {
        Map<String, Object> props = new HashMap<>(); props.put("elasticsearch.url", "https://example.invalid:9200"); return props;
    }
}
