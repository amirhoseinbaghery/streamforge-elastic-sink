package ir.datapie.connect.elasticsearch;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;

/** Reports only sanitized error metadata through Kafka Connect's DLQ mechanism. */
public final class DlqReporter {
    private final ErrantRecordReporter reporter;

    public DlqReporter(ErrantRecordReporter reporter) { this.reporter = reporter; }

    public void report(SinkRecord record, Throwable error) throws Exception {
        if (reporter == null) throw new ConnectException("DLQ requested but ErrantRecordReporter is unavailable");
        reporter.report(record, new DataException("Permanent Elasticsearch sink failure: " + error.getClass().getSimpleName()));
    }
}
