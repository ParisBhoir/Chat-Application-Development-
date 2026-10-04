package com.paris.chat_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String CHANNEL = "ws:messages";

    public Mono<Long> publish(String message) {
        return redisTemplate.convertAndSend(CHANNEL, message);
    }
}
