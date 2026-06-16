package com.xuanyue.exp.mobile.support;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 首页 feed / 浏览统计进程内短时缓存（无 Redis 依赖）。
 */
@Component
public class MobileHomeCache {

    private static final long FEED_TTL_MS = 60_000L;
    private static final long STATS_TTL_MS = 120_000L;

    private final ConcurrentHashMap<String, CacheEntry<?>> feedEntries = new ConcurrentHashMap<>();
    private volatile CacheEntry<Map<String, Long>> browseStatsEntry;

    public <T> T getFeed(String key, Supplier<T> loader) {
        return getOrLoad(feedEntries, key, FEED_TTL_MS, loader);
    }

    public Map<String, Long> getBrowseStats(Supplier<Map<String, Long>> loader) {
        CacheEntry<Map<String, Long>> current = browseStatsEntry;
        long now = System.currentTimeMillis();
        if (current != null && current.expireAt > now) {
            return current.value;
        }
        Map<String, Long> loaded = loader.get();
        browseStatsEntry = new CacheEntry<>(loaded, now + STATS_TTL_MS);
        return loaded;
    }

    public void invalidateFeed() {
        feedEntries.clear();
    }

    public void invalidateBrowseStats() {
        browseStatsEntry = null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOrLoad(ConcurrentHashMap<String, CacheEntry<?>> store, String key,
                              long ttlMs, Supplier<T> loader) {
        CacheEntry<?> current = store.get(key);
        long now = System.currentTimeMillis();
        if (current != null && current.expireAt > now) {
            return (T) current.value;
        }
        T loaded = loader.get();
        store.put(key, new CacheEntry<>(loaded, now + ttlMs));
        return loaded;
    }

    private static final class CacheEntry<T> {
        private final T value;
        private final long expireAt;

        private CacheEntry(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}
