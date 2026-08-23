package ir.datapie.connect.elasticsearch;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Partition-aware offset tracker implementing Kafka N -> N+1 semantics. */
public final class OffsetTracker {
    private final Map<TopicPartition, PartitionState> states = new ConcurrentHashMap<>();

    public void received(SinkRecord record) {
        states.computeIfAbsent(new TopicPartition(record.topic(), record.kafkaPartition()), PartitionState::new)
                .received(record.kafkaOffset());
    }

    public void acknowledged(SinkRecord record) {
        states.computeIfAbsent(new TopicPartition(record.topic(), record.kafkaPartition()), PartitionState::new)
                .terminal(record.kafkaOffset());
    }

    public Map<TopicPartition, Long> safeOffsets(Map<TopicPartition, Long> current) {
        Map<TopicPartition, Long> result = new LinkedHashMap<>();
        for (Map.Entry<TopicPartition, Long> entry : current.entrySet()) {
            PartitionState state = states.get(entry.getKey());
            if (state == null) continue;
            long safe = state.safeCommitOffset();
            if (safe >= 0 && safe <= entry.getValue()) result.put(entry.getKey(), safe);
        }
        return result;
    }

    public void revoke(Iterable<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) states.remove(partition);
    }

    public int pendingPartitions() { return states.size(); }
}
