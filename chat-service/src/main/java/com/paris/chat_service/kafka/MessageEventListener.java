package com.paris.chat_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paris.common.dto.MessageDTO;
import com.paris.chat_service.ws.SessionRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final SessionRegistry sessionRegistry;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "message-events", groupId = "chat-service-consumer")
    public void consume(MessageDTO dto) {
        try {
            log.info("Received Kafka message event: {}", dto);

            WebSocketSession session = sessionRegistry.get(dto.getReceiverId());
            if (session != null && session.isOpen()) {

                String json = mapper.writeValueAsString(dto);

                session.send(Mono.just(session.textMessage(json)))
                        .subscribe(null,
                                err -> log.error("Error sending WebSocket message", err));

            } else {
                log.info("User {} is offline. Not delivering message now.", dto.getReceiverId());
            }

        } catch (Exception ex) {
            log.error("Failed to handle Kafka event", ex);
        }
    }
}
