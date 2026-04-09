package com.paris.chat_service.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionRegistry {
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
}

