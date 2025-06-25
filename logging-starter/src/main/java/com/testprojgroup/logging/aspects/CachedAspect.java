package com.testprojgroup.logging.aspects;

import com.testprojgroup.logging.annotations.Cached;
import com.testprojgroup.logging.cache.Cache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Aspect
public class CachedAspect {
    private static final Logger log = LoggerFactory.getLogger(CachedAspect.class);
    private final Cache cache;

    public CachedAspect(Cache cache) {
        this.cache = cache;
        log.info("CachedAspect initialized with Cache instance.");
    }

    @Around("@annotation(cachedAnnotation)")
    public Object cache(ProceedingJoinPoint joinPoint, Cached cachedAnnotation) throws Throwable {
        String cacheName = cachedAnnotation.cacheName();
        if (cacheName == null || cacheName.trim().isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            cacheName = signature.getDeclaringTypeName() + "." + signature.getName();
            log.warn("@Cached: cacheName not specified for method {}. Using generated name: '{}'", signature.toShortString(), cacheName);
        }


        Object[] args = joinPoint.getArgs();
        Object key = (args == null || args.length == 0) ? "_DEFAULT_CACHE_KEY_" : Arrays.deepHashCode(args);

        Object cachedValue = cache.get(cacheName, key);
        if (cachedValue != null) {
            log.debug("Cache hit for method {}, cacheName: '{}', key: '{}'", joinPoint.getSignature().toShortString(), cacheName, key);
            return cachedValue;
        }

        log.debug("Cache miss for method {}, cacheName: '{}', key: '{}'. Proceeding.", joinPoint.getSignature().toShortString(), cacheName, key);
        Object result = joinPoint.proceed();

        cache.put(cacheName, key, result);
        if (result != null) {
            log.debug("Result cached for method {}, cacheName: '{}', key: '{}'", joinPoint.getSignature().toShortString(), cacheName, key);
        } else {
            log.debug("Result for method {} was null. Not cached by CustomCache.put(). cacheName: '{}', key: '{}'", joinPoint.getSignature().toShortString(), cacheName, key);
        }


        return result;
    }
}