package com.testprojgroup.t1_practice.kafka;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@AllArgsConstructor
public class DataSourceErrorKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "t1_demo_metrics";

    public void sendDataSourceError(String methodSignature, String message) throws Exception {
        String kafkaMessage = String.format("Datasource error in method %s: %s",
                methodSignature, message);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, null, kafkaMessage);
        record.headers().add(new RecordHeader("errorType", "DATA_SOURCE".getBytes(StandardCharsets.UTF_8)));

        kafkaTemplate.send(record).get();
    }
}
