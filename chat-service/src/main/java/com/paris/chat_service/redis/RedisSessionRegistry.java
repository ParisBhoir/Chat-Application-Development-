package com.paris.chat_service.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisSessionRegistry {

    private final ReactiveStringRedisTemplate redisTemplate;

    private static final String USER_SESSION_KEY = "ws:user:";
    private static final String CONNECTION_SESSION_KEY = "ws:connection:";

    private static final RedisScript<Long> REGISTER_SCRIPT =
            new DefaultRedisScript<>(
                    "redis.call('SET', KEYS[1], ARGV[1]); " +
                            "redis.call('SET', KEYS[2], ARGV[2]); " +
                            "return 1",
                    Long.class
            );

    private static final RedisScript<Long> UNREGISTER_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('GET', KEYS[2]) == ARGV[1] then " +
                            "redis.call('DEL', KEYS[1]); " +
                            "redis.call('DEL', KEYS[2]); " +
                            "return 1; " +
                            "end; " +
                            "return 0",
                    Long.class
            );

    public Mono<Void> register(
            String userId,
            String instanceId,
            String connectionId) {

        return redisTemplate.execute(
                REGISTER_SCRIPT,
                List.of(
                        USER_SESSION_KEY + userId,
                        CONNECTION_SESSION_KEY + userId
                ),
                instanceId,
                connectionId
        ).then();
    }

    public Mono<String> getInstance(String userId) {
        return redisTemplate.opsForValue()
                .get(USER_SESSION_KEY + userId);
    }

    public Mono<Boolean> unregister(
            String userId,
            String connectionId) {

        return redisTemplate.execute(
                        UNREGISTER_SCRIPT,
                        List.of(
                                USER_SESSION_KEY + userId,
                                CONNECTION_SESSION_KEY + userId
                        ),
                        connectionId
                ).next()
                .map(result -> result == 1L)
                .defaultIfEmpty(false);
    }
}
