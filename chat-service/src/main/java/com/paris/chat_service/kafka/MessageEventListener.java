package com.paris.chat_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paris.chat_service.dto.MessageStatusEventDTO;
import com.paris.chat_service.service.ChatService;
import com.paris.common.dto.MessageDTO;
import com.paris.chat_service.ws.SessionRegistry;

import com.paris.common.dto.MessageStatus;
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
    private final ChatService chatService;

    @KafkaListener(topics = "message-events", groupId = "chat-service-consumer")
    public void consume(MessageDTO dto) {
        try {
            log.info("Received Kafka message event: {}", dto);

            boolean firstProcessing =
                    chatService.markKafkaEventProcessed(dto.getId());

            if (!firstProcessing) {
                log.info(
                        "Duplicate Kafka event ignored | messageId={}",
                        dto.getId()
                );
                return;
            }

            /*
             * Convert message to JSON.
             */
            String json = mapper.writeValueAsString(dto);

           sessionRegistry.sendToUser(
                    dto.getReceiverId(),
                    json
            );

            log.info(
                    "Message routed to receiver {} | messageId={}",
                    dto.getReceiverId(),
                    dto.getId()
            );

        } catch (Exception ex) {
            log.error(
                    "Failed to handle Kafka event | messageId={}",
                    dto.getId(),
                    ex
            );
        }
    }
}
