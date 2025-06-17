package com.testprojgroup.logging.kafka;

import com.testprojgroup.logging.kafka.KafkaProduceException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;

public class DataSourceErrorKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(DataSourceErrorKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public DataSourceErrorKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendDataSourceError(String methodSignature, String message) {
        String kafkaMessage = String.format("Datasource error in method %s: %s",
                methodSignature, message);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, kafkaMessage);
        record.headers().add(new RecordHeader("errorType", "DATA_SOURCE".getBytes(StandardCharsets.UTF_8)));

        try {
            kafkaTemplate.send(record).get();
            log.debug("DataSource error sent to Kafka topic '{}': {}", topic, kafkaMessage);
        } catch (Exception e) {
            log.error("Failed to send DataSource error to Kafka topic '{}'. Error: {}", topic, e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new KafkaProduceException("Failed to send message to Kafka topic " + topic, e);
        }
    }
}