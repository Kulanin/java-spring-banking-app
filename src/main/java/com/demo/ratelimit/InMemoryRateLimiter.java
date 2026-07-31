package com.demo.ratelimit;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimiter {

    // Key: Client IP or User ID, Value: Request tracking object
    private final Map<String, RequestBucket> cache = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS = 500; // Limit to 5 requests
    private final long TIME_WINDOW = 60_000; // per 1 minute (in milliseconds)

    public boolean allowRequest(String clientId) {
        long currentTime = System.currentTimeMillis();

        cache.putIfAbsent(clientId, new RequestBucket(currentTime, new AtomicInteger(0)));
        RequestBucket bucket = cache.get(clientId);

        synchronized (bucket) {
            // Reset window if expired
            if (currentTime - bucket.windowStart > TIME_WINDOW) {
                bucket.windowStart = currentTime;
                bucket.requestCount.set(0);
            }

            // Check if limit exceeded
            if (bucket.requestCount.incrementAndGet() > MAX_REQUESTS) {
                return false; // Rate limited
            }
        }
        return true; // Allowed
    }

    private static class RequestBucket {
        long windowStart;
        AtomicInteger requestCount;

        public RequestBucket(long windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}