package com.testprojgroup.logging.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;

public class MetricKafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(MetricKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public MetricKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendMetric(String className, String methodName, long duration, long timeLimit) {
        String message = String.format("Method %s from %s took %dms (limit %dms)",
                methodName, className, duration, timeLimit);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, message);
        record.headers().add("errorType", "METRICS".getBytes(StandardCharsets.UTF_8));

        try {
            kafkaTemplate.send(record).get();
            log.debug("Metric sent to Kafka topic '{}': {}", topic, message);
        } catch (Exception e) {
            log.error("Failed to send metric to Kafka topic '{}'. Error: {}", topic, e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new KafkaProduceException("Failed to send message to Kafka topic " + topic, e);
        }
    }
}