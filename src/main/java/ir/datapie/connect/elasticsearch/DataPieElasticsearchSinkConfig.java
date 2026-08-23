package ir.datapie.connect.elasticsearch;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.types.Password;

import java.util.Map;

/** Typed, bounded configuration for the DataPie sink. */
public final class DataPieElasticsearchSinkConfig {
    public static final String ES_URL = "elasticsearch.url";
    public static final String ES_USERNAME = "elasticsearch.username";
    public static final String ES_PASSWORD = "elasticsearch.password";
    public static final String ES_USERNAME_FILE = "elasticsearch.username.file";
    public static final String ES_PASSWORD_FILE = "elasticsearch.password.file";
    public static final String ES_CA_CERT_PATH = "elasticsearch.ca.cert.path";
    public static final String EXPECTED_MAJOR = "elasticsearch.expected.major";
    public static final String ORDERING_MODE = "ordering.mode";
    public static final String DOCUMENT_ID_STRATEGY = "document.id.strategy";
    public static final String TOMBSTONE_MODE = "tombstone.mode";
    public static final String ERRORS_MODE = "errors.mode";
    public static final String BULK_MAX_OPERATIONS = "bulk.max.operations";
    public static final String BULK_MAX_BYTES = "bulk.max.bytes";
    public static final String BULK_FLUSH_INTERVAL_MS = "bulk.flush.interval.ms";
    public static final String BULK_MAX_CONCURRENT_REQUESTS = "bulk.max.concurrent.requests";
    public static final String PENDING_MAX_RECORDS = "pending.max.records";
    public static final String PENDING_MAX_BYTES = "pending.max.bytes";
    public static final String RECORD_MAX_BYTES = "record.max.bytes";
    public static final String RETRY_MAX_ATTEMPTS = "retry.max.attempts";
    public static final String RETRY_INITIAL_BACKOFF_MS = "retry.initial.backoff.ms";
    public static final String RETRY_MAX_BACKOFF_MS = "retry.max.backoff.ms";
    public static final String RETRY_TOTAL_TIMEOUT_MS = "retry.total.timeout.ms";
    public static final String FLUSH_TIMEOUT_MS = "flush.timeout.ms";
    public static final String SHUTDOWN_TIMEOUT_MS = "shutdown.timeout.ms";
    public static final String ID_FIELD_PREFIX = "document.id.field.";
    public static final String INDEX_PREFIX = "index.mapping.";
    public static final String OPERATION_PREFIX = "operation.";

    private final Map<String, ?> values;

    public DataPieElasticsearchSinkConfig(Map<String, ?> values) {
        Map<String, Object> parsed = new java.util.LinkedHashMap<>();
        values.forEach(parsed::put);
        parsed.putAll(CONFIG_DEF.parse(values));
        this.values = parsed;
        validate(this.values);
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ES_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Elasticsearch HTTPS URL")
            .define(ES_USERNAME, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Externalized Elasticsearch username")
            .define(ES_PASSWORD, ConfigDef.Type.PASSWORD, "", ConfigDef.Importance.MEDIUM, "Externalized Elasticsearch password")
            .define(ES_USERNAME_FILE, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Path to an externalized Elasticsearch username file")
            .define(ES_PASSWORD_FILE, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Path to an externalized Elasticsearch password file")
            .define(ES_CA_CERT_PATH, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Optional PEM CA bundle path")
            .define(EXPECTED_MAJOR, ConfigDef.Type.INT, 9, ConfigDef.Range.atLeast(1), ConfigDef.Importance.HIGH, "Expected Elasticsearch major")
            .define(ORDERING_MODE, ConfigDef.Type.STRING, "partition", ConfigDef.ValidString.in("partition"), ConfigDef.Importance.HIGH, "Partition ordering mode")
            .define(DOCUMENT_ID_STRATEGY, ConfigDef.Type.STRING, "record-key", ConfigDef.ValidString.in("record-key", "record-field"), ConfigDef.Importance.HIGH, "Deterministic ID strategy")
            .define(TOMBSTONE_MODE, ConfigDef.Type.STRING, "ignore", ConfigDef.ValidString.in("ignore", "delete", "dlq"), ConfigDef.Importance.MEDIUM, "Tombstone policy")
            .define(ERRORS_MODE, ConfigDef.Type.STRING, "fail", ConfigDef.ValidString.in("fail", "dlq"), ConfigDef.Importance.HIGH, "Permanent error policy")
            .define(BULK_MAX_OPERATIONS, ConfigDef.Type.INT, 200, ConfigDef.Range.between(1, 5000), ConfigDef.Importance.MEDIUM, "Maximum operations per request")
            .define(BULK_MAX_BYTES, ConfigDef.Type.LONG, 5L * 1024 * 1024, ConfigDef.Range.between(1024L, 100L * 1024 * 1024), ConfigDef.Importance.MEDIUM, "Maximum request bytes")
            .define(BULK_FLUSH_INTERVAL_MS, ConfigDef.Type.LONG, 1000L, ConfigDef.Range.between(1L, 60000L), ConfigDef.Importance.MEDIUM, "Bulk flush interval")
            .define(BULK_MAX_CONCURRENT_REQUESTS, ConfigDef.Type.INT, 1, ConfigDef.Range.between(1, 32), ConfigDef.Importance.MEDIUM, "Maximum in-flight requests")
            .define(PENDING_MAX_RECORDS, ConfigDef.Type.INT, 2000, ConfigDef.Range.between(1, 100000), ConfigDef.Importance.MEDIUM, "Pending record bound")
            .define(PENDING_MAX_BYTES, ConfigDef.Type.LONG, 32L * 1024 * 1024, ConfigDef.Range.between(1024L, 1024L * 1024 * 1024), ConfigDef.Importance.MEDIUM, "Pending byte bound")
            .define(RECORD_MAX_BYTES, ConfigDef.Type.LONG, 5L * 1024 * 1024, ConfigDef.Range.between(256L, 100L * 1024 * 1024), ConfigDef.Importance.MEDIUM, "Maximum record size")
            .define(RETRY_MAX_ATTEMPTS, ConfigDef.Type.INT, 5, ConfigDef.Range.between(0, 20), ConfigDef.Importance.MEDIUM, "Maximum retries")
            .define(RETRY_INITIAL_BACKOFF_MS, ConfigDef.Type.LONG, 250L, ConfigDef.Range.between(1L, 60000L), ConfigDef.Importance.MEDIUM, "Initial retry backoff")
            .define(RETRY_MAX_BACKOFF_MS, ConfigDef.Type.LONG, 10000L, ConfigDef.Range.between(1L, 300000L), ConfigDef.Importance.MEDIUM, "Maximum retry backoff")
            .define(RETRY_TOTAL_TIMEOUT_MS, ConfigDef.Type.LONG, 60000L, ConfigDef.Range.between(1L, 900000L), ConfigDef.Importance.MEDIUM, "Total retry timeout")
            .define(FLUSH_TIMEOUT_MS, ConfigDef.Type.LONG, 60000L, ConfigDef.Range.between(1L, 900000L), ConfigDef.Importance.MEDIUM, "Flush timeout")
            .define(SHUTDOWN_TIMEOUT_MS, ConfigDef.Type.LONG, 60000L, ConfigDef.Range.between(1L, 900000L), ConfigDef.Importance.MEDIUM, "Shutdown timeout");

    private static void validate(Map<String, ?> values) {
        String url = String.valueOf(values.get(ES_URL));
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new IllegalArgumentException(ES_URL + " must use http or https");
        }
        boolean hasIndex = values.keySet().stream().anyMatch(k -> k.startsWith(INDEX_PREFIX));
        if (!hasIndex) {
            throw new IllegalArgumentException("At least one index.mapping.<topic> is required");
        }
        int bulk = intValue(values, BULK_MAX_OPERATIONS);
        if (intValue(values, PENDING_MAX_RECORDS) < bulk) {
            throw new IllegalArgumentException(PENDING_MAX_RECORDS + " must be >= " + BULK_MAX_OPERATIONS);
        }
        if (longValue(values, RETRY_MAX_BACKOFF_MS) < longValue(values, RETRY_INITIAL_BACKOFF_MS)) {
            throw new IllegalArgumentException(RETRY_MAX_BACKOFF_MS + " must be >= " + RETRY_INITIAL_BACKOFF_MS);
        }
    }

    public String string(String key) { return String.valueOf(values.get(key)); }
    public String optional(String key) { return values.get(key) == null ? "" : String.valueOf(values.get(key)); }
    public int integer(String key) { return intValue(values, key); }
    public long longValue(String key) { return longValue(values, key); }
    public Password password(String key) { return (Password) values.get(key); }
    public Map<String, ?> originals() { return values; }

    private static int intValue(Map<String, ?> map, String key) { return ((Number) map.get(key)).intValue(); }
    private static long longValue(Map<String, ?> map, String key) { return ((Number) map.get(key)).longValue(); }

    public String indexFor(String topic) {
        Object explicit = values.get(INDEX_PREFIX + topic);
        return explicit == null ? "" : String.valueOf(explicit);
    }

    public String idFieldFor(String topic) {
        Object explicit = values.get(ID_FIELD_PREFIX + topic);
        return explicit == null ? "" : String.valueOf(explicit);
    }

    public String operationFor(String topic) {
        Object explicit = values.get(OPERATION_PREFIX + topic);
        return explicit == null ? "UPSERT" : String.valueOf(explicit).toUpperCase(java.util.Locale.ROOT);
    }
}
