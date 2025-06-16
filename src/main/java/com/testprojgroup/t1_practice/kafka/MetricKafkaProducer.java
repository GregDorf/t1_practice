package com.testprojgroup.t1_practice.kafka;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
public class MetricKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "t1_demo_metrics";

    public void sendMetric(String className, String methodName, long duration, long timeLimit) throws Exception {
        String message = String.format("Method %s from %s took %dms (limit %dms)",
                methodName, className, duration, timeLimit);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, null, message);
        record.headers().add("errorType", "METRICS".getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record).get();
    }
}
