package com.phoenixtask.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

  private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

  public RateLimitDecision evaluate(String key, int capacity, int windowSeconds) {
    long now = Instant.now().getEpochSecond();
    Window window = windows.computeIfAbsent(
        key,
        ignored -> new Window(now + windowSeconds, new AtomicInteger(0))
    );
    synchronized (window) {
      if (now > window.resetAt) {
        window.resetAt = now + windowSeconds;
        window.counter.set(0);
      }
      int current = window.counter.incrementAndGet();
      int remaining = Math.max(0, capacity - current);
      if (current > capacity) {
        int retryAfter = (int) Math.max(1, window.resetAt - now);
        return new RateLimitDecision(false, 0, window.resetAt, retryAfter);
      }
      return new RateLimitDecision(true, remaining, window.resetAt, 0);
    }
  }

  private static class Window {
    private volatile long resetAt;
    private final AtomicInteger counter;

    private Window(long resetAt, AtomicInteger counter) {
      this.resetAt = resetAt;
      this.counter = counter;
    }
  }
}
