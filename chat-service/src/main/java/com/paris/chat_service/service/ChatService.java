package com.paris.chat_service.service;

import com.paris.chat_service.kafka.MessageProducer;
import com.paris.chat_service.model.Message;
import com.paris.common.dto.MessageDTO;
import com.paris.chat_service.repository.MessageRepository;
import com.paris.common.dto.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final MessageProducer messageProducer;

    public Message sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        Message saved = messageRepository.save(message);

        // Convert entity to DTO before sending to Kafka
        MessageDTO dto = MessageDTO.builder()
                .id(saved.getId())
                .senderId(saved.getSenderId())
                .receiverId(saved.getReceiverId())
                .content(saved.getContent())
                .timestamp(saved.getTimestamp())
                .status(saved.getStatus())
                .build();
        // publish to Kafka
        messageProducer.sendMessageEvent(dto);

        return saved;
    }

    public void updateMessageStatus(Long messageId, MessageStatus status) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow();

        message.setStatus(status);
        messageRepository.save(message);
    }

    public List<Message> getChatHistory(String senderId, String receiverId) {
        return messageRepository.findChatHistory(senderId, receiverId);
    }

    public List<Message> getUndeliveredMessages(String userId) {
        return messageRepository.findByReceiverIdAndStatus(userId, MessageStatus.SENT);
    }
}
