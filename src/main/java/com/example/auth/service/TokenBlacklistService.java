package com.example.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklist;

    public TokenBlacklistService(
            @Value("${app.jwt.access-expiration}") long accessExpirationMs) {
        this.blacklist = Caffeine.newBuilder()
                .expireAfterWrite(accessExpirationMs, TimeUnit.MILLISECONDS)
                .maximumSize(10_000)
                .build();
    }

    public void blacklist(String token) {
        blacklist.put(token, Boolean.TRUE);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(blacklist.getIfPresent(token));
    }
}
