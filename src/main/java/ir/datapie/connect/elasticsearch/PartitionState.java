package ir.datapie.connect.elasticsearch;

import org.apache.kafka.common.TopicPartition;

import java.util.NavigableSet;
import java.util.TreeSet;

/** Partition-local terminal state; safe progress only advances contiguously. */
public final class PartitionState {
    private final TopicPartition partition;
    private final NavigableSet<Long> terminal = new TreeSet<>();
    private long firstReceived = -1L;
    private long highestReceived = -1L;
    private long safeOffset = -1L;

    public PartitionState(TopicPartition partition) { this.partition = partition; }

    public synchronized void received(long offset) {
        if (firstReceived < 0) firstReceived = offset;
        highestReceived = Math.max(highestReceived, offset);
    }

    public synchronized void terminal(long offset) {
        received(offset);
        terminal.add(offset);
        advance();
    }

    private void advance() {
        long next = safeOffset >= 0 ? safeOffset + 1 : firstReceived;
        while (terminal.remove(next)) {
            safeOffset = next;
            next++;
        }
    }

    public synchronized long safeCommitOffset() {
        return safeOffset < 0 ? -1 : safeOffset + 1;
    }

    public synchronized long highestReceived() { return highestReceived; }
    public synchronized int pendingCount() { return terminal.size(); }
    public TopicPartition partition() { return partition; }
}
