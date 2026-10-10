package com.paris.chat_service.kafka;

import com.paris.common.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
    private static final String TOPIC = "message-events";

    public CompletableFuture<SendResult<String, MessageDTO>> sendMessageEvent(MessageDTO message) {
        log.info("Publishing message event to Kafka: {}", message);

        return kafkaTemplate.send(TOPIC, message);
    }
}
