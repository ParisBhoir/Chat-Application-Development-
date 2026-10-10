package com.paris.chat_service.service;

import com.paris.chat_service.kafka.MessageProducer;
import com.paris.chat_service.model.Message;
import com.paris.common.dto.MessageDTO;
import com.paris.chat_service.repository.MessageRepository;
import com.paris.common.dto.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        messageProducer.sendMessageEvent(dto)
                .whenComplete((result, ex) -> {

                    if (ex == null) {
                        try {
                            updateMessageStatus(
                                    saved.getId(),
                                    MessageStatus.SENT
                            );

                            System.out.println(
                                    "✅ Message " + saved.getId()
                                            + " successfully published to Kafka"
                            );

                        } catch (Exception e) {
                            System.err.println(
                                    "❌ Failed to update SENT status for message "
                                            + saved.getId()
                            );
                            e.printStackTrace();
                        }

                    } else {
                        System.err.println(
                                "❌ Failed to publish message "
                                        + saved.getId()
                                        + " to Kafka"
                        );
                        ex.printStackTrace();
                    }
                });

        return saved;
    }

    public void updateMessageStatus(Long messageId, MessageStatus status) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow();
        message.setLastAttemptAt(LocalDateTime.now());
        message.setStatus(status);
        messageRepository.save(message);
    }

    public List<Message> getChatHistory(String senderId, String receiverId) {
        return messageRepository.findChatHistory(senderId, receiverId);
    }

    public List<Message> getUndeliveredMessages(String userId) {
        return messageRepository.findByReceiverIdAndStatus(userId, MessageStatus.PENDING);
    }

    public List<Message> findMessagesForRetry(LocalDateTime threshold) {
        return messageRepository
                .findByStatusAndFailedFalseAndRetryCountLessThanAndLastAttemptAtBefore(
                        MessageStatus.SENT,
                        4,
                        threshold
                );
    }

    public void incrementRetry(Long messageId){
        Message message = messageRepository.findById(messageId)
                .orElseThrow();
        message.setLastAttemptAt(LocalDateTime.now());
        message.setRetryCount(message.getRetryCount()+1);
        messageRepository.save(message);
    }

    public void moveToDLQ(Message msg) {
        msg.setFailed(true);
        messageRepository.save(msg);
    }

    @Transactional
    public boolean markKafkaEventProcessed(Long messageId) {
        return messageRepository.markKafkaEventProcessed(messageId) > 0;
    }
}
