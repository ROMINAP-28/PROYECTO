package com.travelink.servicio;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final long LOCK_TIME_DURATION_MS = 15 * 60 * 1000; // 15 minutos

    private static class Attempt {
        int count;
        long lastAttemptTime;

        Attempt(int count, long lastAttemptTime) {
            this.count = count;
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    private final ConcurrentHashMap<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        if (key != null) {
            attemptsCache.remove(key.toLowerCase().trim());
        }
    }

    public void loginFailed(String key) {
        if (key == null) return;
        String normalizedKey = key.toLowerCase().trim();
        long now = System.currentTimeMillis();

        attemptsCache.compute(normalizedKey, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt(1, now);
            }
            // Si el bloqueo anterior ya expiró, reiniciar conteo
            if (now - attempt.lastAttemptTime > LOCK_TIME_DURATION_MS) {
                return new Attempt(1, now);
            }
            attempt.count++;
            attempt.lastAttemptTime = now;
            return attempt;
        });
    }

    public boolean isBlocked(String key) {
        if (key == null) return false;
        String normalizedKey = key.toLowerCase().trim();
        Attempt attempt = attemptsCache.get(normalizedKey);
        if (attempt == null) return false;

        long now = System.currentTimeMillis();
        if (now - attempt.lastAttemptTime > LOCK_TIME_DURATION_MS) {
            attemptsCache.remove(normalizedKey);
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }

    public int getRemainingAttempts(String key) {
        if (key == null) return MAX_ATTEMPTS;
        String normalizedKey = key.toLowerCase().trim();
        Attempt attempt = attemptsCache.get(normalizedKey);
        if (attempt == null) return MAX_ATTEMPTS;

        long now = System.currentTimeMillis();
        if (now - attempt.lastAttemptTime > LOCK_TIME_DURATION_MS) {
            attemptsCache.remove(normalizedKey);
            return MAX_ATTEMPTS;
        }

        return Math.max(0, MAX_ATTEMPTS - attempt.count);
    }
}
