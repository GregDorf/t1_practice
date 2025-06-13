package com.testprojgroup.t1_practice.aop;

import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Cache {
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CacheEntry {
        private Object value;
        private long expirationTime;

        private boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    private final Map<String, Map<Object, CacheEntry>> caches = new ConcurrentHashMap<>();

    @Value("${cache.ttl}")
    private long ttl;

    public Object get(String cacheName, Object key) {
        Map<Object, CacheEntry> map = caches.get(cacheName);
        if (map == null) return null;

        CacheEntry entry = map.get(key);
        if (entry == null || entry.isExpired()) {
            map.remove(key);
            return null;
        }

        return entry.value;
    }

    public void put(String cacheName, Object key, Object value) {
        caches.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>())
                .put(key, new CacheEntry(value, System.currentTimeMillis() + ttl));
    }
}
