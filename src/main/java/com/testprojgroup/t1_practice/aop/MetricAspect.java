package com.testprojgroup.t1_practice.aop;

import com.testprojgroup.t1_practice.config.MetricProperties;
import com.testprojgroup.t1_practice.kafka.metric_producers.MetricKafkaProducer;
import com.testprojgroup.t1_practice.model.TimeLimitExceedLog;
import com.testprojgroup.t1_practice.repository.TimeLimitExceedLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {
    private final TimeLimitExceedLogRepository timeLimitExceedLogRepository;
    private final MetricProperties metricProperties;
    private final MetricKafkaProducer metricKafkaProducer;

    @Around("@annotation(com.testprojgroup.t1_practice.aop.annotation.MetricTrack)")
    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        Signature signature = joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();

        if (duration > metricProperties.getTimeLimitMs()) {
            try {
                metricKafkaProducer.sendMetric(className, methodName, duration, metricProperties.getTimeLimitMs());
            } catch (Exception ex) {
                TimeLimitExceedLog timeLimitExceedLog = new TimeLimitExceedLog();
                timeLimitExceedLog.setClassName(className);
                timeLimitExceedLog.setMethodName(methodName);
                timeLimitExceedLog.setExecutionTime(duration);
                timeLimitExceedLog.setTimestamp(LocalDateTime.now());

                timeLimitExceedLogRepository.save(timeLimitExceedLog);
            }
        }

        return result;
    }
}
