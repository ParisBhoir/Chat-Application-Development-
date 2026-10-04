package com.paris.chat_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisSessionRegistry {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String USER_SESSION_KEY = "ws:user:";

    public Mono<Void> register(String userId, String instanceId) {
        return redisTemplate.opsForValue()
                .set(USER_SESSION_KEY + userId, instanceId).then();
    }

    public Mono<String> getInstance(String userId) {
        return redisTemplate.opsForValue()
                .get(USER_SESSION_KEY + userId);
    }

    public Mono<Void> unregister(String userId) {
        return redisTemplate.delete(USER_SESSION_KEY + userId).then();
    }
}
