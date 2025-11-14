package com.medvault.demo.config;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int WINDOW_MINUTES = 15;
    
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        AttemptInfo info = attempts.get(key);
        
        if (info == null) {
            attempts.put(key, new AttemptInfo(1, LocalDateTime.now()));
            return true;
        }

        if (info.getTimestamp().plusMinutes(WINDOW_MINUTES).isBefore(LocalDateTime.now())) {
            // Window expired, reset
            attempts.put(key, new AttemptInfo(1, LocalDateTime.now()));
            return true;
        }

        if (info.getCount() >= MAX_ATTEMPTS) {
            return false;
        }

        info.increment();
        return true;
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private static class AttemptInfo {
        private int count;
        private LocalDateTime timestamp;

        public AttemptInfo(int count, LocalDateTime timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }

        public void increment() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}

