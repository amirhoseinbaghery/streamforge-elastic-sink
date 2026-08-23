package ir.datapie.connect.elasticsearch;

import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DataPieElasticsearchSinkConnector extends SinkConnector {
    public static final String VERSION = "0.1.0";
    private Map<String, String> props = Map.of();

    @Override public void start(Map<String, String> props) {
        new DataPieElasticsearchSinkConfig(props);
        this.props = Map.copyOf(props);
    }

    @Override public Class<? extends Task> taskClass() { return DataPieElasticsearchSinkTask.class; }

    @Override public List<Map<String, String>> taskConfigs(int maxTasks) {
        int tasks = Math.max(1, Math.min(maxTasks, 1));
        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < tasks; i++) configs.add(props);
        return configs;
    }

    @Override public void stop() { props = Map.of(); }
    @Override public Config validate(Map<String, String> configs) { return super.validate(configs); }
    @Override public ConfigDef config() { return DataPieElasticsearchSinkConfig.CONFIG_DEF; }
    @Override public String version() { return VERSION; }
}
