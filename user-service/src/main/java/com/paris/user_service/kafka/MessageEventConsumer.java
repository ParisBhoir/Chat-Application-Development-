package com.paris.user_service.kafka;

import com.paris.common.dto.MessageDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageEventConsumer {

    @KafkaListener(topics = "message-events", groupId = "user-service-group")
    public void consume(MessageDTO event) {
        System.out.println("📥 Received message event: " + event);
        // You can store, notify, or log here
    }
}
