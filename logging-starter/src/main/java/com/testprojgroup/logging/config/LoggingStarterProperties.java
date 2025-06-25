package com.testprojgroup.logging.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "logging.starter")
public class LoggingStarterProperties {

    private String applicationName = "logging.starter";

    private CacheProps cache;
    private ExecutionTimeProps executionTime;
    private DataSourceErrorProps dataSourceError;

    private KafkaCommonProps kafkaCommon;

    @Data
    public static class CacheProps {
        private boolean enabled = true;
        private long ttlMs = 300_000;
    }

    @Data
    public static class ExecutionTimeProps {
        private boolean enabled = true;
        private long limitMs = 1000;
        private KafkaProps kafka = new KafkaProps("execution-time-logs-topic");
        private DatabaseProps db = new DatabaseProps("time_limit_exceed_log");
    }

    @Data
    public static class DataSourceErrorProps {
        private boolean enabled = true;
        private KafkaProps kafka = new KafkaProps("datasource-errors-topic");
        private DatabaseProps db = new DatabaseProps("data_source_error_log");
    }

    @Data
    public static class KafkaCommonProps {
        private String bootstrapServers;
        private Map<String, String> producerOverrides = Collections.emptyMap();
    }

    @Data
    @NoArgsConstructor
    public static class KafkaProps {
        private boolean enabled = false;
        private String topic;

        public KafkaProps(String defaultTopic) {
            this.topic = defaultTopic;
        }
    }

    @Data
    @NoArgsConstructor
    public static class DatabaseProps {
        private boolean enabled = false;
        private String tableName;

        public DatabaseProps(String defaultTableName) {
            this.tableName = defaultTableName;
        }
    }
}