package ir.datapie.connect.elasticsearch;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OffsetTrackerTest {
    @Test
    void advancesOnlyThroughContiguousAcknowledgementsAndCommitsNPlusOne() {
        OffsetTracker tracker = new OffsetTracker();
        SinkRecord one = record(1), two = record(2), three = record(3), four = record(4), five = record(5);
        tracker.received(one); tracker.received(two); tracker.received(three); tracker.received(four); tracker.received(five);
        tracker.acknowledged(one); tracker.acknowledged(two); tracker.acknowledged(four); tracker.acknowledged(five);
        TopicPartition partition = new TopicPartition("tweets", 0);
        assertEquals(Map.of(partition, 3L), tracker.safeOffsets(Map.of(partition, 6L)));
        tracker.acknowledged(three);
        assertEquals(Map.of(partition, 6L), tracker.safeOffsets(Map.of(partition, 6L)));
    }

    private SinkRecord record(long offset) { return new SinkRecord("tweets", 0, null, "id-" + offset, null, offset, offset); }
}
