package com.testprojgroup.t1_practice.aop;

import com.testprojgroup.t1_practice.kafka.metric_producers.DataSourceErrorKafkaProducer;
import com.testprojgroup.t1_practice.model.DataSourceErrorLog;
import com.testprojgroup.t1_practice.repository.DataSourceErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private final DataSourceErrorLogRepository dataSourceErrorLogRepository;
    private final DataSourceErrorKafkaProducer dataSourceErrorKafkaProducer;

    @AfterThrowing(pointcut="@annotation(com.testprojgroup.t1_practice.aop.annotation.LogDataSourceError)", throwing="ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String stackTrace = Arrays.toString(ex.getStackTrace());
        String message = ex.getMessage();
        String methodSignature = joinPoint.getSignature().toLongString();

        try {
            dataSourceErrorKafkaProducer.sendDataSourceError(methodSignature, message);
        } catch (Exception e) {
            DataSourceErrorLog dataSourceErrorLog = new DataSourceErrorLog(stackTrace, methodSignature, message);
            dataSourceErrorLogRepository.save(dataSourceErrorLog);
        }
    }
}
