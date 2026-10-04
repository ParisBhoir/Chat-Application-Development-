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
public class RedisPresenceSubscriber {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisMessageListenerContainer container;

    @PostConstruct
    public void subscribe() {

        container.receive(ChannelTopic.of("ws:presence"))
                .map(message -> message.getMessage())
                .subscribe(msg -> {

                    try {
                        JsonNode node = objectMapper.readTree(msg);

                        String userId = node.get("userId").asText();

                        log.info(
                                "Presence event received | userId={} | event={}",
                                userId,
                                msg
                        );

                        // Deliver directly to users connected
                        // to THIS chat-service instance.
//                        sessionRegistry.sendToLocalUser(userId, msg);

                        sessionRegistry.broadcastExcept(userId, msg);
                    } catch (Exception e) {
                        log.error("Redis presence subscriber error", e);
                    }
                });
    }
}
