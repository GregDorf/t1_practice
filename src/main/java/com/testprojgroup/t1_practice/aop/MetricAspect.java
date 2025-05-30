package com.testprojgroup.t1_practice.aop;

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
    private final  TimeLimitExceedLogRepository timeLimitExceedLogRepository;
    private final MetricProperties metricProperties;

    @Around("@annotation(com.testprojgroup.t1_practice.aop.annotation.MetricTrack)")
    public Object executionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        if (duration > metricProperties.getTimeLimitMs()) {
            Signature signature = joinPoint.getSignature();
            TimeLimitExceedLog timeLimitExceedLog = new TimeLimitExceedLog();
            timeLimitExceedLog.setClassName(signature.getDeclaringTypeName());
            timeLimitExceedLog.setMethodName(signature.getName());
            timeLimitExceedLog.setExecutionTime(duration);
            timeLimitExceedLog.setTimestamp(LocalDateTime.now());

            timeLimitExceedLogRepository.save(timeLimitExceedLog);
        }

        return result;
    }
}
