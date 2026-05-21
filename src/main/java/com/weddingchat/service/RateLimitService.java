package com.weddingchat.service;

import com.weddingchat.error.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Map<String, Deque<Instant>> eventsByKey = new ConcurrentHashMap<>();

    public void checkOrThrow(String key, int maxEventsPerMinute, String messageWhenExceeded) {
        Instant now = Instant.now();
        Instant threshold = now.minus(Duration.ofMinutes(1));

        Deque<Instant> events = eventsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (events) {
            while (!events.isEmpty() && events.peekFirst().isBefore(threshold)) {
                events.pollFirst();
            }
            if (events.size() >= maxEventsPerMinute) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, messageWhenExceeded);
            }
            events.addLast(now);
        }
    }
}
