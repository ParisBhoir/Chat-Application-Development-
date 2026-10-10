package com.paris.chat_service.repository;

import com.paris.chat_service.model.Message;
import com.paris.common.dto.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
            "(m.senderId = :senderId AND m.receiverId = :receiverId) OR " +
            "(m.senderId = :receiverId AND m.receiverId = :senderId) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(String senderId, String receiverId);

    List<Message> findByReceiverIdAndStatus(String receiverId, MessageStatus status);

    List<Message> findByStatusAndFailedFalseAndRetryCountLessThanAndLastAttemptAtBefore(
            MessageStatus status,
            int retryCount,
            LocalDateTime time
    );

    @Modifying
    @Query("""
       UPDATE Message m
       SET m.kafkaEventProcessed = true
       WHERE m.id = :messageId
       AND m.kafkaEventProcessed = false
       """)
    int markKafkaEventProcessed(@Param("messageId") Long messageId);
}

