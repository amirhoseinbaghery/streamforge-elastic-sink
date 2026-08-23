package ir.datapie.connect.elasticsearch;

import java.util.concurrent.atomic.LongAdder;

/** Lightweight counters; Kafka Connect/JMX remains the primary runtime metrics source. */
public final class ConnectorMetrics {
    public final LongAdder received = new LongAdder();
    public final LongAdder encoded = new LongAdder();
    public final LongAdder submitted = new LongAdder();
    public final LongAdder acked = new LongAdder();
    public final LongAdder failed = new LongAdder();
    public final LongAdder retried = new LongAdder();
    public final LongAdder dlq = new LongAdder();
    public final LongAdder bytesReceived = new LongAdder();
    public final LongAdder bytesWritten = new LongAdder();

    public String summary() {
        return "received=" + received.sum() + ",encoded=" + encoded.sum() + ",submitted=" + submitted.sum()
                + ",acked=" + acked.sum() + ",failed=" + failed.sum() + ",retried=" + retried.sum() + ",dlq=" + dlq.sum();
    }
}
