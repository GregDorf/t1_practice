package com.testprojgroup.logging.config;

import com.testprojgroup.logging.aspects.CachedAspect;
import com.testprojgroup.logging.aspects.DataSourceErrorLogAspect;
import com.testprojgroup.logging.aspects.ExecutionTimeAspect;
import com.testprojgroup.logging.cache.Cache;
import com.testprojgroup.logging.kafka.DataSourceErrorKafkaProducer;
import com.testprojgroup.logging.kafka.MetricKafkaProducer;
import com.testprojgroup.logging.repository.DataSourceErrorLogRepository;
import com.testprojgroup.logging.repository.TimeLimitExceedLogRepository;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

@EntityScan(basePackages = "com.testprojgroup.logging.model")
@EnableJpaRepositories(basePackages = "com.testprojgroup.logging.repository")
@EnableConfigurationProperties(LoggingStarterProperties.class)
public class LoggingStarterAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoggingStarterAutoConfiguration.class);
    private final LoggingStarterProperties properties;

    public LoggingStarterAutoConfiguration(LoggingStarterProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(name = "logging.starter.cache.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(Cache.class)
    public Cache Cache() {
        long ttlMs = properties.getCache().getTtlMs();
        Cache cacheInstance = new Cache(ttlMs);

        log.info("Cache bean initialized with TTL: {}ms", ttlMs);
        return cacheInstance;
    }

    @Bean
    @ConditionalOnBean(Cache.class)
    @ConditionalOnMissingBean(CachedAspect.class)
    public CachedAspect CachedAspect(Cache Cache) {
        CachedAspect aspect = new CachedAspect(Cache);

        log.info("CachedAspect bean initialized.");

        return aspect;
    }


    @Bean
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(name = "logging.starter.kafka-common.bootstrap-servers")
    @ConditionalOnMissingBean(name = "starterStringKafkaProducerFactory")
    public ProducerFactory<String, String> starterStringKafkaProducerFactory() {
        String bootstrapServers = properties.getKafkaCommon().getBootstrapServers();
        if (!StringUtils.hasText(bootstrapServers)) {
            log.warn("Kafka bootstrap-servers are not configured. Kafka producer beans for String messages will not be created.");
            return null;
        }

        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        if (properties.getKafkaCommon().getProducerOverrides() != null) {
            configProps.putAll(properties.getKafkaCommon().getProducerOverrides());
        }

        log.info("Initializing Kafka ProducerFactory<String, String> for starter with bootstrap_servers: {}", bootstrapServers);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    @ConditionalOnBean(name = "starterStringKafkaProducerFactory")
    @ConditionalOnMissingBean(name = "starterStringKafkaTemplate")
    public KafkaTemplate<String, String> starterStringKafkaTemplate(ProducerFactory<String, String> starterStringKafkaProducerFactory) {
        log.info("Initializing KafkaTemplate<String, String> for starter.");
        return new KafkaTemplate<>(starterStringKafkaProducerFactory);
    }

    @Bean
    @ConditionalOnProperty(name = "logging.starter.execution-time.kafka.enabled", havingValue = "true")
    @ConditionalOnBean(name = "starterStringKafkaTemplate")
    @ConditionalOnMissingBean(MetricKafkaProducer.class)
    public MetricKafkaProducer metricKafkaProducer(KafkaTemplate<String, String> starterStringKafkaTemplate) {
        String topic = properties.getExecutionTime().getKafka().getTopic();
        if (!StringUtils.hasText(topic)) {
            log.warn("Topic for MetricKafkaProducer is not configured ('logging.starter.execution-time.kafka.topic'). Producer will not be properly initialized.");
        }
        log.info("Initializing MetricKafkaProducer for topic: {}", topic);
        return new MetricKafkaProducer(starterStringKafkaTemplate, topic);
    }

    @Bean
    @ConditionalOnProperty(name = "logging.starter.data-source-error.kafka.enabled", havingValue = "true")
    @ConditionalOnBean(name = "starterStringKafkaTemplate")
    @ConditionalOnMissingBean(DataSourceErrorKafkaProducer.class)
    public DataSourceErrorKafkaProducer dataSourceErrorKafkaProducer(KafkaTemplate<String, String> starterStringKafkaTemplate) {
        String topic = properties.getDataSourceError().getKafka().getTopic();
        if (!StringUtils.hasText(topic)) {
            log.warn("Topic for DataSourceErrorKafkaProducer is not configured ('logging.starter.data-source-error.kafka.topic'). Producer will not be properly initialized.");
        }
        log.info("Initializing DataSourceErrorKafkaProducer for topic: {}", topic);
        return new DataSourceErrorKafkaProducer(starterStringKafkaTemplate, topic);
    }

    @Configuration
    @ConditionalOnProperty(name = "logging.starter.execution-time.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "com.testprojgroup.logging.annotations.MetricTrack")
    static class ExecutionTimeAspectConfiguration {

        @Bean
        @ConditionalOnMissingBean(ExecutionTimeAspect.class)
        public ExecutionTimeAspect executionTimeAspect(
                LoggingStarterProperties properties,
                @Autowired(required = false) @Nullable MetricKafkaProducer metricKafkaProducer,
                @Autowired(required = false) @Nullable TimeLimitExceedLogRepository timeLimitExceedLogRepository
        ) {

            boolean kafkaConfigured = properties.getExecutionTime().getKafka().isEnabled() && metricKafkaProducer != null;
            boolean dbDependenciesPresent = timeLimitExceedLogRepository != null;
            boolean dbConfigured = properties.getExecutionTime().getDb().isEnabled() && dbDependenciesPresent;

            if (properties.getExecutionTime().getKafka().isEnabled() && metricKafkaProducer == null) {
                log.warn("ExecutionTimeAspect: Kafka logging is enabled but MetricKafkaProducer bean is not available. Check Kafka setup and topic config.");
            }
            if (properties.getExecutionTime().getDb().isEnabled() && !dbDependenciesPresent) {
                log.warn("ExecutionTimeAspect: DB logging is enabled but TimeLimitExceedLogRepository bean is not available. Ensure Spring Data JPA is configured and scans starter repositories, or provide the bean.");
            }
            if (properties.getExecutionTime().getDb().isEnabled() && dbDependenciesPresent) {
                log.warn("ExecutionTimeAspect: DB logging is enabled and Repository is present, but EntityManagerFactory bean is not available. JPA operations might fail.");
            }


            log.info("Initializing ExecutionTimeAspect. Kafka logging active: {}DB logging active: {}", kafkaConfigured, dbConfigured);
            return new ExecutionTimeAspect(properties,
                    kafkaConfigured ? metricKafkaProducer : null,
                    dbConfigured ? timeLimitExceedLogRepository : null);
        }

        @Bean
        @ConditionalOnProperty(name = "logging.starter.execution-time.db.enabled", havingValue = "true")
        @ConditionalOnBean({DataSource.class, ExecutionTimeAspect.class})
        @ConditionalOnMissingBean(name = "executionTimeTableInitializer")
        public ApplicationRunner executionTimeTableInitializer(DataSource dataSource, LoggingStarterProperties properties) {
            return args -> {
                String tableName = properties.getExecutionTime().getDb().getTableName();
                String ddl = String.format(
                        "CREATE TABLE IF NOT EXISTS %s (" +
                                "id BIGSERIAL PRIMARY KEY," +
                                "class_name VARCHAR(255)," +
                                "method_name VARCHAR(255)," +
                                "execution_time BIGINT," +
                                "created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP" +
                                ")", tableName);
                createTableIfNeeded(dataSource, tableName, ddl, "execution time logs");
            };
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "logging.starter.data-source-error.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "com.testprojgroup.logging.annotations.LogDataSourceError")
    static class DataSourceErrorLogAspectConfiguration {

        @Bean
        @ConditionalOnMissingBean(DataSourceErrorLogAspect.class)
        public DataSourceErrorLogAspect dataSourceErrorLogAspect(
                LoggingStarterProperties properties,
                @Autowired(required = false) @Nullable DataSourceErrorKafkaProducer dataSourceErrorKafkaProducer,
                @Autowired(required = false) @Nullable DataSourceErrorLogRepository dataSourceErrorLogRepository
        ) {

            boolean kafkaConfigured = properties.getDataSourceError().getKafka().isEnabled() && dataSourceErrorKafkaProducer != null;
            boolean dbDependenciesPresent = dataSourceErrorLogRepository != null;
            boolean dbConfigured = properties.getDataSourceError().getDb().isEnabled() && dbDependenciesPresent;

            if (properties.getDataSourceError().getKafka().isEnabled() && dataSourceErrorKafkaProducer == null) {
                log.warn("DataSourceErrorLogAspect: Kafka logging is enabled but DataSourceErrorKafkaProducer bean is not available. Check Kafka setup and topic config.");
            }
            if (properties.getDataSourceError().getDb().isEnabled() && !dbDependenciesPresent) {
                log.warn("DataSourceErrorLogAspect: DB logging is enabled but DataSourceErrorLogRepository bean is not available. Ensure Spring Data JPA is configured and scans starter repositories, or provide the bean.");
            }
            if (properties.getDataSourceError().getDb().isEnabled() && dbDependenciesPresent) {
                log.warn("DataSourceErrorLogAspect: DB logging is enabled and Repository is present, but EntityManagerFactory bean is not available. JPA operations might fail.");
            }

            log.info("Initializing DataSourceErrorLogAspect. Kafka logging active: {}DB logging active: {}", kafkaConfigured, dbConfigured);
            return new DataSourceErrorLogAspect(properties,
                    kafkaConfigured ? dataSourceErrorKafkaProducer : null,
                    dbConfigured ? dataSourceErrorLogRepository : null);
        }

        @Bean
        @ConditionalOnProperty(name = "logging.starter.data-source-error.db.enabled", havingValue = "true")
        @ConditionalOnBean({DataSource.class, DataSourceErrorLogAspect.class})
        @ConditionalOnMissingBean(name = "dataSourceErrorTableInitializer")
        public ApplicationRunner dataSourceErrorTableInitializer(DataSource dataSource, LoggingStarterProperties properties) {
            return args -> {
                String tableName = properties.getDataSourceError().getDb().getTableName();
                String ddl = String.format(
                        "CREATE TABLE IF NOT EXISTS %s (" +
                                "id BIGSERIAL PRIMARY KEY," +
                                "stacktrace TEXT," +
                                "message TEXT NOT NULL," +
                                "method_signature TEXT NOT NULL" +
                                ")", tableName);
                createTableIfNeeded(dataSource, tableName, ddl, "DataSource errors");
            };
        }
    }

    private static void createTableIfNeeded(DataSource dataSource, String tableName, String ddl, String logContext) {
        if (!StringUtils.hasText(tableName)) {
            log.warn("Table name for {} is not configured or empty. Skipping table creation.", logContext);
            return;
        }
        if (dataSource == null) {
            log.warn("DataSource is null for {}. Skipping table creation for table '{}'.", logContext, tableName);
            return;
        }
        try {
            boolean tableExists = checkTableExists(dataSource, tableName);
            if (!tableExists) {
                log.info("Table '{}' for {} not found. Attempting to create using DDL: {}", tableName, logContext, ddl);
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.execute(ddl);
                log.info("Table '{}' for {} created successfully (or already existed if 'IF NOT EXISTS' was used and checkTableExists had issues).", tableName, logContext);
            } else {
                log.debug("Table '{}' for {} already exists.", tableName, logContext);
            }
        } catch (Exception e) {
            log.error("Failed to check or create table '{}' for {}: {}. DDL was: {}", tableName, logContext, e.getMessage(), ddl, e);
        }
    }
    private static boolean checkTableExists(DataSource dataSource, String tableName) {
        if (dataSource == null) {
            log.warn("DataSource is null in checkTableExists for table '{}'. Assuming table does not exist.", tableName);
            return false;
        }
        if (!StringUtils.hasText(tableName)) {
            log.warn("Table name is empty or null in checkTableExists. Assuming table does not exist.");
            return false;
        }

        String[] namesToCheck = {tableName, tableName.toLowerCase(), tableName.toUpperCase()};
        String[] schemasToCheck = {null, "public"};

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String currentDefaultSchema = connection.getSchema();

            for (String schemaPattern : schemasToCheck) {
                String schemaToCheck = (schemaPattern == null) ? currentDefaultSchema : schemaPattern;
                for (String namePattern : namesToCheck) {
                    if (!StringUtils.hasText(namePattern)) continue;

                    try (ResultSet tables = metaData.getTables(catalog, schemaToCheck, namePattern, new String[]{"TABLE"})) {
                        if (tables.next()) {
                            log.debug("Table '{}' (checked as '{}' in schema '{}') found.", tableName, namePattern, schemaToCheck);
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("SQL error while checking if table '{}' exists: {}. Assuming table does not exist.", tableName, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while checking if table '{}' exists: {}. Assuming table does not exist.", tableName, e.getMessage(), e);
            return false;
        }

        log.debug("Table '{}' not found after checking various cases/schemas.", tableName);
        return false;
    }
}