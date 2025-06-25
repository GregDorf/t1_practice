package com.testprojgroup.logging.kafka;

public class KafkaProduceException extends RuntimeException {
    public KafkaProduceException(String message, Throwable cause) {
        super(message, cause);
    }
}