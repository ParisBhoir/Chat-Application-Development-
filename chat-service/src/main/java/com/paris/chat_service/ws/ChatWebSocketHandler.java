package com.paris.chat_service.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paris.chat_service.dto.MessageEventDTO;
import com.paris.chat_service.dto.PresenceEventDTO;
import com.paris.chat_service.dto.PresenceStatus;
import com.paris.chat_service.dto.TypingEventDTO;
import com.paris.chat_service.model.Message;
import com.paris.chat_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final SessionRegistry sessionRegistry;
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Gateway must forward X-User-Name header
        String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.warn("WebSocket handshake missing X-User-Id header; closing session");
            return session.close();
        }

        sessionRegistry.register(userId, session);
        PresenceEventDTO event = new PresenceEventDTO(
                "PRESENCE",
                PresenceStatus.ONLINE,
                userId
        );

        try {
            sessionRegistry.broadcast(objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("WebSocket connected: {}", userId);

        Mono<Void> inbound = session.receive()
                .flatMap(msg -> {
                    String payload = msg.getPayloadAsText();
                    try {
                        JsonNode node = objectMapper.readTree(payload);
                        log.info("JsonNode node: {}", node);
                        String type = node.has("type") ? node.get("type").asText() : null;

                        if (type == null) {
                            log.warn("Invalid WS payload: missing type");
                            return Mono.empty();
                        }

                        switch (type) {

                            case "MESSAGE":
                                MessageEventDTO dto = objectMapper.treeToValue(node, MessageEventDTO.class);
                                Message m = Message.builder()
                                        .senderId(userId)
                                        .receiverId(dto.getReceiverId())
                                        .content(dto.getContent())
                                        .timestamp(LocalDateTime.now())
                                        .seen(false)
                                        .build();
                                chatService.sendMessage(m);

                            case "TYPING":
                                TypingEventDTO typingEvent = objectMapper.treeToValue(node, TypingEventDTO.class);
                                typingEvent.setSenderId(userId);
                                sessionRegistry.sendToUser(
                                        typingEvent.getReceiverId(),
                                        objectMapper.writeValueAsString(typingEvent)
                                );
                                break;

                            default:
                                log.warn("Unknown WS event type: {}", type);
                        }
//                        Message m = objectMapper.readValue(payload, Message.class);
//                        // ensure sender is set by gateway header, ignore client's spoofed sender
//                        m.setSenderId(userId);
//                        log.info("WS message received from {} → {}", userId, m.getReceiverId());
//                        chatService.sendMessage(m);
                    } catch (Exception e) {
                        log.error("Failed to parse incoming WS message", e);
                    }
                    return Mono.empty();
                })
                .then();

        // cleanup on termination
        return inbound.doFinally(sig -> {
            sessionRegistry.unregister(userId);
            PresenceEventDTO event1 = new PresenceEventDTO(
                    "PRESENCE",
                    PresenceStatus.OFFLINE,
                    userId
            );

            try {
                sessionRegistry.broadcast(objectMapper.writeValueAsString(event1));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.info("WebSocket disconnected: {}", userId);
        }).then();
    }
}
