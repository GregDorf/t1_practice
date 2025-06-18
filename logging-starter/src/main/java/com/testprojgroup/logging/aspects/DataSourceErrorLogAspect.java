package com.testprojgroup.logging.aspects;

import com.testprojgroup.logging.annotations.LogDataSourceError;
import com.testprojgroup.logging.config.LoggingStarterProperties;
import com.testprojgroup.logging.model.DataSourceErrorLog;
import com.testprojgroup.logging.kafka.DataSourceErrorKafkaProducer;
import com.testprojgroup.logging.kafka.KafkaProduceException;
import com.testprojgroup.logging.repository.DataSourceErrorLogRepository;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

@Aspect
public class DataSourceErrorLogAspect {
    private static final Logger log = LoggerFactory.getLogger(DataSourceErrorLogAspect.class);

    private final LoggingStarterProperties properties;
    private final DataSourceErrorKafkaProducer dataSourceErrorKafkaProducer;
    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;

    public DataSourceErrorLogAspect(LoggingStarterProperties properties,
                                    @Nullable DataSourceErrorKafkaProducer dataSourceErrorKafkaProducer,
                                    @Nullable DataSourceErrorLogRepository dataSourceErrorLogRepository) {
        this.properties = properties;
        this.dataSourceErrorKafkaProducer = dataSourceErrorKafkaProducer;
        this.dataSourceErrorLogRepository = dataSourceErrorLogRepository;
    }

    @AfterThrowing(pointcut = "@annotation(com.testprojgroup.logging.annotations.LogDataSourceError)", throwing = "ex")
    public void logDataSourceError(JoinPoint joinPoint, Throwable ex) {
        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodSignatureString = signature.toLongString();
        String errorMessage = ex.getMessage();

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        log.error("DataSource error captured in {}: {}", methodSignatureString, errorMessage, ex);

        boolean kafkaEnabled = properties.getDataSourceError().getKafka().isEnabled() && dataSourceErrorKafkaProducer != null;
        boolean dbEnabled = properties.getDataSourceError().getDb().isEnabled() && dataSourceErrorLogRepository != null;

        boolean loggedToKafka = false;
        if (kafkaEnabled) {
            try {
                dataSourceErrorKafkaProducer.sendDataSourceError(methodSignatureString, errorMessage);
                loggedToKafka = true;
            } catch (KafkaProduceException e) {
                log.error("Failed to send DataSource error log to Kafka for {}. Falling back to DB if enabled. Error: {}",
                        methodSignatureString, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error sending DataSource error log to Kafka for {}. Falling back to DB if enabled. Error: {}",
                        methodSignatureString, e.getMessage(), e);
            }
        }

        if (!loggedToKafka && dbEnabled) {
            try {
                DataSourceErrorLog logEntry = new DataSourceErrorLog();
                logEntry.setStacktrace(stackTrace);
                logEntry.setMessage(errorMessage);
                logEntry.setMethodSignature(methodSignatureString);

                dataSourceErrorLogRepository.save(logEntry);
                log.info("DataSource error log saved to DB for {}", methodSignatureString);
            } catch (Exception e) {
                log.error("Failed to save DataSource error log to DB for {}. Error: {}",
                        methodSignatureString, e.getMessage(), e);
            }
        }
    }
}