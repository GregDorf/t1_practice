package com.testprojgroup.t1_practice.aop;

import com.testprojgroup.t1_practice.aop.annotation.Cached;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class CachedAspect {
    private final Cache cache;

    @Around("@annotation(cached)")
    public Object cache(ProceedingJoinPoint joinPoint, Cached cached) throws Throwable {
        String cacheName = cached.cacheName();

        Object[] args = joinPoint.getArgs();
        Object key = Arrays.deepHashCode(args);

        Object cachedValue = cache.get(cacheName, key);
        if (cachedValue != null) {
            return cachedValue;
        }

        Object result = joinPoint.proceed();

        cache.put(cacheName, key, result);
        return result;
    }
}
