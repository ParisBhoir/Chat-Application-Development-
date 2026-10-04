package com.paris.chat_service.ws;

import com.paris.chat_service.redis.InstanceIdProvider;
import com.paris.chat_service.redis.RedisSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Mono;
import com.paris.chat_service.redis.RedisMessagePublisher;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRegistry {
    private final RedisSessionRegistry redisSessionRegistry;
    private final RedisMessagePublisher redisMessagePublisher;
    private final InstanceIdProvider instanceIdProvider;
    // userId -> session
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void register(String userId, WebSocketSession session) {
        if (userId == null) return;
        sessions.put(userId, session);

        onlineUsers.add(userId);
        log.info("User {} is ONLINE", userId);
    }

    public void unregister(String userId) {
        if (userId == null) return;
        sessions.remove(userId);

        onlineUsers.remove(userId);
        log.info("User {} is OFFLINE", userId);
    }

    public WebSocketSession get(String userId) {
        return sessions.get(userId);
    }

    public Collection<WebSocketSession> allSessions() {
        return sessions.values();
    }

    public boolean isOnline(String userId) {
        return onlineUsers.contains(userId);
    }

    public void broadcast(String message) {
        sessions.values().forEach(session -> {
            session.send(Mono.just(session.textMessage(message)))
                    .subscribe();
        });
    }

    /*public void sendToUser(String userId, String message) {

        String currentInstance = instanceIdProvider.getInstanceId();
        String instance = redisSessionRegistry.getInstance(userId).toString();
        if (instance == currentInstance) {
            WebSocketSession session = sessions.get(userId);
            if (session != null && session.isOpen()) {
                session.send(Mono.just(session.textMessage(message)))
                        .subscribe();
            }
        } else {
            redisMessagePublisher.publish(message);
        }
        *//*WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.send(Mono.just(session.textMessage(message)))
                    .subscribe();
        }*//*
    }*/

    public void sendToUser(String userId, String message) {

        String currentInstance = instanceIdProvider.getInstanceId();

        redisSessionRegistry.getInstance(userId)
                .defaultIfEmpty("")
                .subscribe(targetInstance -> {

                    log.info(
                            "Routing message | user={} | currentInstance={} | targetInstance={}",
                            userId,
                            currentInstance,
                            targetInstance
                    );

                    if (currentInstance.equals(targetInstance)) {

                        // User is on THIS instance
                        sendToLocalUser(userId, message);

                    } else {

                        // User is on another instance
                        redisMessagePublisher.publish(message)
                                .subscribe();

                        log.info(
                                "Published message to Redis for user {}",
                                userId
                        );
                    }
                });
    }

    public void sendToLocalUser(String userId, String message) {

        WebSocketSession session = sessions.get(userId);

        if (session != null && session.isOpen()) {
            session.send(
                    Mono.just(session.textMessage(message))
            ).subscribe();

            log.info("Message delivered locally to user {}", userId);
        } else {
            log.warn("No local WebSocket session for user {}", userId);
        }
    }

    public void broadcastExcept(String excludedUserId, String message) {

        sessions.forEach((userId, session) -> {

            if (!userId.equals(excludedUserId)
                    && session.isOpen()) {

                session.send(
                        Mono.just(session.textMessage(message))
                ).subscribe();

                log.info(
                        "Presence delivered locally | user={} | excludedUser={}",
                        userId,
                        excludedUserId
                );
            }
        });
    }
}

