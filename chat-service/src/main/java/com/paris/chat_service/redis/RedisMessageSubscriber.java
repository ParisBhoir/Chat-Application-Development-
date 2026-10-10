package com.paris.chat_service.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paris.chat_service.ws.SessionRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(ReactiveRedisMessageListenerContainer.class)
public class RedisMessageSubscriber {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper mapper;
    private final ReactiveRedisMessageListenerContainer container;

    @PostConstruct
    public void subscribe() {

        container.receive(ChannelTopic.of("ws:messages"))
                .map(message -> message.getMessage())
                .subscribe(msg -> {
                    try {
                        JsonNode node = mapper.readTree(msg);
                        String targetUserId;
                        String payload;

                        if (node.has("targetUserId") && node.has("payload")) {
                            // New format: explicit routing destination
                            targetUserId = node.get("targetUserId").asText();
                            payload = node.get("payload").asText();
                        } else {
                            // Legacy format: route using receiverId
                            targetUserId = node.get("receiverId").asText();
                            payload = msg;
                        }

                        log.info("Redis message received for target user {}", targetUserId);

                        sessionRegistry.sendToLocalUser(targetUserId, payload);

                    } catch (Exception e) {
                        log.error("Redis subscriber error", e);
                    }
                });
    }
}
