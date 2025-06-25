package com.testprojgroup.logging.cache;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final Logger log = LoggerFactory.getLogger(Cache.class);

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CacheEntry {
        private Object value;
        private long expirationTime;

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    private final Map<String, Map<Object, CacheEntry>> caches = new ConcurrentHashMap<>();
    private final long ttlMs;

    public Cache (long ttlMs) {
        if (ttlMs <= 0) {
            log.warn("Provided TTL ({}ms) is not positive. Cache might not work as expected or use a default.", ttlMs);
            this.ttlMs = 300_000;
            log.warn("Using emergency default TTL: {}ms", this.ttlMs);
        } else {
            this.ttlMs = ttlMs;
        }
        log.info(" initialized with TTL: {}ms", this.ttlMs);
    }

    public Object get(String cacheName, Object key) {
        if (cacheName == null || key == null) {
            log.warn("Attempted to get from cache with null cacheName or key. CacheName: {}, Key: {}", cacheName, key);
            return null;
        }
        Map<Object, CacheEntry> cacheRegion = caches.get(cacheName);
        if (cacheRegion == null) {
            log.trace("Cache region '{}' not found for get operation.", cacheName);
            return null;
        }

        CacheEntry entry = cacheRegion.get(key);
        if (entry == null) {
            log.trace("Cache miss for key '{}' in region '{}'.", key, cacheName);
            return null;
        }

        if (entry.isExpired()) {
            log.debug("Cache entry for key '{}' in region '{}' expired. Removing.", key, cacheName);
            cacheRegion.remove(key, entry);
            if (cacheRegion.isEmpty()) {
                caches.remove(cacheName);
                log.debug("Cache region '{}' became empty after expired entry removal and was removed.", cacheName);
            }
            return null;
        }
        log.debug("Cache hit for key '{}' in region '{}'.", key, cacheName);
        return entry.getValue();
    }

    public void put(String cacheName, Object key, Object value) {
        if (cacheName == null || key == null) {
            log.warn("Attempted to put into cache with null cacheName or key. CacheName: {}, Key: {}, Value: {}", cacheName, key, value);
            return;
        }
        if (value == null) {
            log.debug("Attempted to put null value for key '{}' in region '{}'. Null values are not cached by default.", key, cacheName);
            return;
        }

        long expirationTime = System.currentTimeMillis() + this.ttlMs;
        CacheEntry newEntry = new CacheEntry(value, expirationTime);

        caches.computeIfAbsent(cacheName, k -> {
            log.debug("Creating new cache region: '{}'", k);
            return new ConcurrentHashMap<>();
        }).put(key, newEntry);
        log.debug("Value cached for key '{}' in region '{}'. Expires at: {}", key, cacheName, expirationTime);
    }

    public void evict(String cacheName, Object key) {
        if (cacheName == null || key == null) return;
        Map<Object, CacheEntry> cacheRegion = caches.get(cacheName);
        if (cacheRegion != null) {
            CacheEntry removed = cacheRegion.remove(key);
            if(removed != null) log.debug("Evicted key '{}' from cache region '{}'", key, cacheName);
            if (cacheRegion.isEmpty()) {
                caches.remove(cacheName);
                log.debug("Cache region '{}' became empty after eviction and was removed.", cacheName);
            }
        }
    }

    public void clearRegion(String cacheName) {
        if (cacheName == null) return;
        Map<Object, CacheEntry> removed = caches.remove(cacheName);
        if(removed != null) log.debug("Cache region '{}' cleared.", cacheName);
    }

    public void clearAll() {
        caches.clear();
        log.info("All cache regions cleared.");
    }
}