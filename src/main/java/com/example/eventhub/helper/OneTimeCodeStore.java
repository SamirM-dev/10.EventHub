package com.example.eventhub.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OneTimeCodeStore {

    private final static String REDIS_OAUTH_PREFIX="oauth2:code:";

    private final StringRedisTemplate redisTemplate;

    public String generateOAuth(Long userId){
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(REDIS_OAUTH_PREFIX+code,String.valueOf(userId),Duration.ofSeconds(60));
        return code;
    }

    public Long consumeOAuth(String code){
        String userId = redisTemplate.opsForValue().getAndDelete(REDIS_OAUTH_PREFIX+code);
        if (userId==null){
            return null;
        }
        return Long.parseLong(userId);
    }

}
