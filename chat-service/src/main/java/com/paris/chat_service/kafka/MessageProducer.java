package com.paris.chat_service.kafka;

import com.paris.common.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, MessageDTO> kafkaTemplate;
    private static final String TOPIC = "message-events";

    public void sendMessageEvent(MessageDTO message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("📤 Sent message event to Kafka: " + message.getContent());
    }
}
