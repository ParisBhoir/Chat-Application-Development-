package com.paris.chat_service.model;

import com.paris.common.dto.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
//    private boolean seen = false;
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private int retryCount = 0;
    private LocalDateTime lastAttemptAt;
    private boolean failed;
}
