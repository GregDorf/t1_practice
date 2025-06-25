package com.testprojgroup.logging.aspects;

import com.testprojgroup.logging.annotations.MetricTrack;
import com.testprojgroup.logging.config.LoggingStarterProperties;
import com.testprojgroup.logging.model.TimeLimitExceedLog;
import com.testprojgroup.logging.kafka.MetricKafkaProducer;
import com.testprojgroup.logging.kafka.KafkaProduceException;
import com.testprojgroup.logging.repository.TimeLimitExceedLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Aspect
public class ExecutionTimeAspect {
    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    private final LoggingStarterProperties properties;
    private final MetricKafkaProducer metricKafkaProducer;
    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;

    public ExecutionTimeAspect(LoggingStarterProperties properties,
                               @Nullable MetricKafkaProducer metricKafkaProducer,
                               @Nullable TimeLimitExceedLogRepository timeLimitExceedLogRepository) {
        this.properties = properties;
        this.metricKafkaProducer = metricKafkaProducer;
        this.timeLimitExceedLogRepository = timeLimitExceedLogRepository;
    }

    @Around("@annotation(com.testprojgroup.logging.annotations.MetricTrack)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            LoggingStarterProperties.ExecutionTimeProps execTimeProps = properties.getExecutionTime();

            if (duration > execTimeProps.getLimitMs()) {
                Signature signature = joinPoint.getSignature();
                String className = signature.getDeclaringTypeName();
                String methodName = signature.getName();

                log.warn("Method {}.{} execution time {}ms exceeded limit {}ms",
                        className, methodName, duration, execTimeProps.getLimitMs());

                boolean kafkaEnabled = execTimeProps.getKafka().isEnabled() && metricKafkaProducer != null;
                boolean dbEnabled = execTimeProps.getDb().isEnabled() && timeLimitExceedLogRepository != null;

                boolean loggedToKafka = false;
                if (kafkaEnabled) {
                    try {
                        metricKafkaProducer.sendMetric(className, methodName, duration, execTimeProps.getLimitMs());
                        loggedToKafka = true;
                    } catch (KafkaProduceException e) {
                        log.error("Failed to send execution time metric to Kafka for {}.{}. Falling back to DB if enabled. Error: {}",
                                className, methodName, e.getMessage());
                    } catch (Exception e) {
                        log.error("Unexpected error sending execution time metric to Kafka for {}.{}. Falling back to DB if enabled. Error: {}",
                                className, methodName, e.getMessage(), e);
                    }
                }

                if (!loggedToKafka && dbEnabled) {
                    try {
                        TimeLimitExceedLog logEntry = new TimeLimitExceedLog();
                        logEntry.setClassName(className);
                        logEntry.setMethodName(methodName);
                        logEntry.setExecutionTime(duration);
                        logEntry.setTimestamp(LocalDateTime.now());

                        timeLimitExceedLogRepository.save(logEntry);
                        log.info("Execution time exceed log saved to DB for {}.{}", className, methodName);
                    } catch (Exception e) {
                        log.error("Failed to save execution time exceed log to DB for {}.{}. Error: {}",
                                className, methodName, e.getMessage(), e);
                    }
                }
            }
        }
        return result;
    }
}