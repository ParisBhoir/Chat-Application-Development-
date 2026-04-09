package com.paris.chat_service.kafka;

import com.paris.common.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
    private static final String TOPIC = "message-events";

    public void sendMessageEvent(MessageDTO message) {
        log.info("Publishing message event to Kafka: {}", message);
        kafkaTemplate.send(TOPIC, message);
        System.out.println("📤 Sent message event to Kafka: " + message.getContent());
    }
}
