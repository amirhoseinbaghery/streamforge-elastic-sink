package ir.datapie.connect.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.connect.sink.SinkRecord;

/** One reusable encoder; payload bytes are bounded before submission. */
public final class RecordEncoder {
    private final ObjectMapper mapper;
    private final RecordMapper recordMapper;

    public RecordEncoder(ObjectMapper mapper, RecordMapper recordMapper) {
        this.mapper = mapper;
        this.recordMapper = recordMapper;
    }

    public byte[] encode(SinkRecord record) throws Exception {
        return mapper.writeValueAsBytes(recordMapper.map(record));
    }
}
